package by.urbash_hair.controller;

import by.urbash_hair.dto.BookSlotRequest;
import by.urbash_hair.entity.Appointment;
import by.urbash_hair.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin
public class AppointmentController {

    private final AppointmentService service;

    // Получить свободные слоты на дату
    @GetMapping("/available")
    public ResponseEntity<List<Appointment>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long masterId,
            @RequestParam(required = false) Long serviceId) {
        List<Appointment> slots = service.findAvailableSlots(date.toString(), masterId, serviceId);
        return ResponseEntity.ok(slots);
    }

    // Получить доступность (свободные слоты) в диапазоне дат
    // Возвращаем список дат, у которых есть хотя бы один слот со статусом AVAILABLE.
    @GetMapping("/available-range")
    public ResponseEntity<List<String>> getAvailableRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long masterId,
            @RequestParam(required = false) Long serviceId) {
        List<String> availableDates = service.findAvailableDatesInRange(startDate, endDate, masterId, serviceId);
        return ResponseEntity.ok(availableDates);
    }


    // Получить все слоты за месяц (включая AVAILABLE/BOOKED/CONFIRMED/CANCELLED)
    @GetMapping("/all-by-month")
    public ResponseEntity<List<Appointment>> getAllSlotsByMonth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long masterId,
            @RequestParam(required = false) Long serviceId) {
        List<Appointment> slots = service.findAllSlotsInRange(startDate, endDate, masterId, serviceId);
        return ResponseEntity.ok(slots);
    }

    // Забронировать слот (клиент)
    @PostMapping("/{id}/book")
    public ResponseEntity<Appointment> bookSlot(
            @PathVariable Long id,
            @RequestBody BookSlotRequest request,
            Principal principal) {
        Long clientId = Long.parseLong(principal.getName());
        Appointment booked = service.bookSlot(id, clientId);
        return ResponseEntity.ok(booked);
    }
}
