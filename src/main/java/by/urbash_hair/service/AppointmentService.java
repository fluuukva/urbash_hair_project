package by.urbash_hair.service;

import by.urbash_hair.config.HashUtils;
import by.urbash_hair.dto.AppointmentRequest;
import by.urbash_hair.dto.GenerateSlotsRequest;
import by.urbash_hair.entity.*;
import by.urbash_hair.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final MasterRepository masterRepository;
    private final HashUtils hashUtils;

    @Transactional(readOnly = true)
    public List<Appointment> findAvailableSlots(String date, Long masterId, Long serviceId) {
        // Получаем все слоты мастера на эту дату (включая занятые и свободные)
        List<Appointment> allSlotsForMaster = appointmentRepository.findByDateAndMasterId(date, masterId);
        // Фильтруем только свободные (STATUS_AVAILABLE)
        return allSlotsForMaster.stream()
                .filter(slot -> Appointment.STATUS_AVAILABLE.equals(slot.getStatus()))
                .filter(slot -> serviceId == null || (slot.getService() != null && slot.getService().getId().equals(serviceId)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> findAvailableDatesInRange(LocalDate startDate, LocalDate endDate, Long masterId, Long serviceId) {
        List<Appointment> slots = appointmentRepository.findByDateBetweenAndMasterId(
                startDate.toString(), endDate.toString(), masterId
        );

        return slots.stream()
                .filter(slot -> Appointment.STATUS_AVAILABLE.equals(slot.getStatus()))
                .filter(slot -> serviceId == null || (slot.getService() != null && slot.getService().getId().equals(serviceId)))
                .map(Appointment::getDate)
                .distinct()
                .collect(Collectors.toList());
    }

    // Получить все слоты мастера в диапазоне дат (включая AVAILABLE и занятые статусы)
    @Transactional(readOnly = true)
    public List<Appointment> findAllSlotsInRange(LocalDate startDate, LocalDate endDate, Long masterId, Long serviceId) {
        List<Appointment> slots = appointmentRepository.findByDateBetweenAndMasterId(
                startDate.toString(), endDate.toString(), masterId
        );

        if (serviceId == null) {
            return slots;
        }

        // Если serviceId указан, возвращаем слоты с этой услугой ИЛИ слоты без услуги (для совместимости)
        return slots.stream()
                .filter(slot -> slot.getService() == null || slot.getService().getId().equals(serviceId))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Appointment> generateSlots(GenerateSlotsRequest request) {
        List<Appointment> createdSlots = new ArrayList<>();
        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        int durationMinutes = request.getDurationMinutes();

        Master master = masterRepository.findById(request.getMasterId())
                .orElseThrow(() -> new RuntimeException("Мастер не найден"));
        by.urbash_hair.entity.Service service = null;
        if (request.getServiceId() != null) {
            service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Услуга не найдена"));
        }

        LocalDate current = start;
        while (!current.isAfter(end)) {
            int dayOfWeek = current.getDayOfWeek().getValue();
            if (request.getWeekdays().contains(dayOfWeek)) {
                String dateStr = current.format(DateTimeFormatter.ISO_LOCAL_DATE);
                String timeStr = startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime slotStart = startTime;
                LocalTime slotEnd = slotStart.plusMinutes(durationMinutes);

                // Проверка на пересечение с существующими слотами мастера в этот день
                List<Appointment> existingSlots = appointmentRepository.findByDateAndMasterId(dateStr, master.getId());
                boolean conflict = existingSlots.stream().anyMatch(existing -> {
                    LocalTime existingStart = LocalTime.parse(existing.getTime());
                    LocalTime existingEnd = existingStart.plusMinutes(durationMinutes);
                    return (slotStart.isBefore(existingEnd) && slotEnd.isAfter(existingStart));
                });

                if (!conflict) {
                    Appointment slot = Appointment.builder()
                            .date(dateStr)
                            .time(timeStr)
                            .master(master)
                            .service(service)
                            .status(Appointment.STATUS_AVAILABLE)
                            .build();
                    createdSlots.add(appointmentRepository.save(slot));
                }
            }
            current = current.plusDays(1);
        }
        return createdSlots;
    }

    @Transactional
    public Appointment bookSlot(Long slotId, Long clientId) {
        Appointment slot = appointmentRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Слот не найден"));

        if (!Appointment.STATUS_AVAILABLE.equals(slot.getStatus())) {
            throw new RuntimeException("Этот слот уже занят или недоступен");
        }

        LocalDate slotDate = LocalDate.parse(slot.getDate());
        if (slotDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Нельзя забронировать прошедшую дату");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));

        slot.setStatus(Appointment.STATUS_BOOKED);
        slot.setClient(client);
        return appointmentRepository.save(slot);
    }
}