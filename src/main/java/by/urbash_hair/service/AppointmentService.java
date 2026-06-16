package by.urbash_hair.service;

import by.urbash_hair.config.HashUtils;
import by.urbash_hair.dto.AppointmentRequest;
import by.urbash_hair.dto.BookSlotRequest;
import by.urbash_hair.dto.GenerateSlotsRequest;
import by.urbash_hair.entity.*;
import by.urbash_hair.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${admin.phone}")
    private String adminPhone;

    // ===== ПРОВЕРКА КОНФЛИКТА СЛОТОВ =====
    public boolean hasSlotConflict(Long masterId, String date, String startTime, int durationMinutes, Long excludeId) {
        if (masterId == null || date == null || startTime == null) {
            return false;
        }

        List<Appointment> slots = appointmentRepository.findByDateAndMasterId(date, masterId);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = start.plusMinutes(durationMinutes);

        for (Appointment existing : slots) {
            if (excludeId != null && existing.getId().equals(excludeId)) continue;
            if (Appointment.STATUS_CANCELLED.equals(existing.getStatus())) continue;

            LocalTime existingStart = LocalTime.parse(existing.getTime());
            LocalTime existingEnd = existingStart.plusMinutes(durationMinutes);

            if (start.isBefore(existingEnd) && end.isAfter(existingStart)) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public Appointment create(AppointmentRequest request) {
        Client client = getOrCreateClient(request);
        by.urbash_hair.entity.Service service = getService(request);
        Master master = getMaster(request);

        // Проверка конфликта (длительность 120 минут)
        if (hasSlotConflict(master.getId(), request.getDate(), request.getTime(), 120, null)) {
            throw new RuntimeException("На это время уже есть слот у данного мастера");
        }

        String priceString = calculatePrice(service, request.getHairLength(), request.getHairDensity());
        String fullNotes = priceString;
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            fullNotes += ". Пожелания: " + request.getNotes();
        }

        Appointment appointment = Appointment.builder()
                .date(request.getDate())
                .time(request.getTime())
                .service(service)
                .master(master)
                .client(client)
                .status(Appointment.STATUS_BOOKED)
                .notes(fullNotes)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        notifyAdminAboutBooking(saved);
        return saved;
    }

    @Transactional
    public Appointment bookSlot(Long slotId, BookSlotRequest request, Long clientId) {
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

        by.urbash_hair.entity.Service service = slot.getService();
        String priceString = calculatePrice(service, request.getHairLength(), request.getHairDensity());

        String fullNotes = priceString;
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            fullNotes += ". Пожелания: " + request.getNotes();
        }

        slot.setStatus(Appointment.STATUS_BOOKED);
        slot.setClient(client);
        slot.setNotes(fullNotes);

        Appointment saved = appointmentRepository.save(slot);
        notifyAdminAboutBooking(saved);
        return saved;
    }

    // ===== УВЕДОМЛЕНИЕ АДМИНИСТРАТОРУ =====
    private void notifyAdminAboutBooking(Appointment appointment) {
        Client client = appointment.getClient();
        by.urbash_hair.entity.Service service = appointment.getService();
        Master master = appointment.getMaster();

        String clientName = (client != null) ? client.getFirstName() + " " + client.getLastName() : "Неизвестный клиент";
        String serviceName = (service != null) ? service.getName() : "Не указана";
        String masterName = (master != null) ? master.getFirstName() + " " + master.getLastName() : "Не указан";

        System.out.println("================================================");
        System.out.println("📩 НОВАЯ ЗАПИСЬ (уведомление администратору)");
        System.out.println("Клиент: " + clientName);
        System.out.println("Телефон: " + (client != null ? client.getPhone() : "не указан"));
        System.out.println("Услуга: " + serviceName);
        System.out.println("Мастер: " + masterName);
        System.out.println("Дата: " + appointment.getDate());
        System.out.println("Время: " + appointment.getTime());
        System.out.println("Стоимость: " + (appointment.getNotes() != null ? appointment.getNotes() : "не рассчитана"));
        System.out.println("Пожелания: " + (appointment.getNotes() != null ? appointment.getNotes() : "нет"));
        System.out.println("================================================");
    }

    // ===== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====
    private Client getOrCreateClient(AppointmentRequest request) {
        String phoneHash = hashUtils.hashPhone(request.getPhone());
        return clientRepository.findByPhoneHash(phoneHash)
                .orElseGet(() -> {
                    Client newClient = Client.builder()
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .phone(request.getPhone())
                            .phoneHash(phoneHash)
                            .email(request.getEmail())
                            .emailHash(hashUtils.hashEmail(request.getEmail()))
                            .build();
                    return clientRepository.save(newClient);
                });
    }

    private by.urbash_hair.entity.Service getService(AppointmentRequest request) {
        if (request.getServiceId() != null) {
            return serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new RuntimeException("Услуга не найдена"));
        }
        if (request.getServiceName() != null && !request.getServiceName().isBlank()) {
            String serviceName = convertServiceName(request.getServiceName());
            return serviceRepository.findByName(serviceName)
                    .orElseThrow(() -> new RuntimeException("Услуга не найдена"));
        }
        throw new RuntimeException("Не указана услуга");
    }

    private Master getMaster(AppointmentRequest request) {
        if (request.getMasterId() != null) {
            return masterRepository.findById(request.getMasterId())
                    .orElseThrow(() -> new RuntimeException("Мастер не найден"));
        }
        throw new RuntimeException("Не указан мастер");
    }

    private String convertServiceName(String frontendValue) {
        return switch (frontendValue) {
            case "keratin" -> "Кератиновое выпрямление";
            case "botox" -> "Ботокс";
            case "recovery" -> "Холодное восстановление";
            case "courses" -> "Обучение/Курсы";
            default -> frontendValue;
        };
    }

    private String calculatePrice(by.urbash_hair.entity.Service service, Integer hairLength, Integer hairDensity) {
        if (service == null) return "Стоимость не определена";

        String serviceName = service.getName();
        int length = hairLength != null ? hairLength : 30;
        int density = hairDensity != null ? hairDensity : 0;

        double basePrice = 0;

        if (serviceName.contains("Кератин") || serviceName.contains("Ботокс")) {
            if (length < 30) length = 30;
            if (length > 85) length = 85;
            int extraCm = (length - 30) / 5;
            basePrice = 90 + extraCm * 10;
        } else if (serviceName.contains("Холодное") || serviceName.contains("восстановление")) {
            if (length < 30) length = 30;
            if (length > 85) length = 85;
            int extraCm = (length - 30) / 5;
            basePrice = 50 + extraCm * 10;
        } else if (serviceName.contains("Обучение") || serviceName.contains("Курсы")) {
            return "Стоимость: 300 BYN";
        } else {
            return "Стоимость не определена для данной услуги";
        }

        double densitySurcharge = 0;
        if (density >= 7 && density <= 8) densitySurcharge = 30;
        else if (density >= 9 && density <= 10) densitySurcharge = 40;
        else if (density >= 10 && density <= 12) densitySurcharge = 60;

        double total = basePrice + densitySurcharge;
        return String.format("Стоимость: %.0f BYN (длина %d см, густота %d см)", total, length, density);
    }

    // ===== СУЩЕСТВУЮЩИЕ МЕТОДЫ =====

    @Transactional(readOnly = true)
    public List<Appointment> findAvailableSlots(String date, Long masterId, Long serviceId) {
        List<Appointment> allSlotsForMaster = appointmentRepository.findByDateAndMasterId(date, masterId);
        return allSlotsForMaster.stream()
                .filter(slot -> Appointment.STATUS_AVAILABLE.equals(slot.getStatus()))
                .filter(slot -> serviceId == null || (slot.getService() != null && slot.getService().getId().equals(serviceId)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> findAvailableDatesInRange(LocalDate startDate, LocalDate endDate, Long masterId, Long serviceId) {
        List<Appointment> slots = appointmentRepository.findByDateBetweenAndMasterId(
                startDate.toString(), endDate.toString(), masterId);
        return slots.stream()
                .filter(slot -> Appointment.STATUS_AVAILABLE.equals(slot.getStatus()))
                .filter(slot -> serviceId == null || (slot.getService() != null && slot.getService().getId().equals(serviceId)))
                .map(Appointment::getDate)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAllSlotsInRange(LocalDate startDate, LocalDate endDate, Long masterId, Long serviceId) {
        List<Appointment> slots;
        if (masterId != null) {
            slots = appointmentRepository.findByDateBetweenAndMasterId(
                    startDate.toString(), endDate.toString(), masterId);
        } else {
            slots = appointmentRepository.findByDateBetween(
                    startDate.toString(), endDate.toString());
        }

        if (serviceId == null) {
            return slots;
        }
        return slots.stream()
                .filter(slot -> slot.getService() != null && slot.getService().getId().equals(serviceId))
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
}