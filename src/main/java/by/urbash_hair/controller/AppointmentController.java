package by.urbash_hair.controller;

import by.urbash_hair.dto.AppointmentRequest;
import by.urbash_hair.dto.BookSlotRequest;
import by.urbash_hair.entity.Appointment;
import by.urbash_hair.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<Appointment> create(@RequestBody AppointmentRequest request) {
        Appointment created = service.create(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Appointment>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long masterId,
            @RequestParam(required = false) Long serviceId) {
        List<Appointment> slots = service.findAvailableSlots(date.toString(), masterId, serviceId);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/available-range")
    public ResponseEntity<List<String>> getAvailableRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long masterId,
            @RequestParam(required = false) Long serviceId) {
        List<String> availableDates = service.findAvailableDatesInRange(startDate, endDate, masterId, serviceId);
        return ResponseEntity.ok(availableDates);
    }

    @GetMapping("/all-by-month")
    public ResponseEntity<?> getAllSlotsByMonth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long masterId,
            @RequestParam(required = false) Long serviceId) {
        try {
            List<Appointment> slots = service.findAllSlotsInRange(startDate, endDate, masterId, serviceId);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/book")
    public ResponseEntity<Appointment> bookSlot(
            @PathVariable Long id,
            @RequestBody BookSlotRequest request,
            Principal principal) {
        Long clientId = Long.parseLong(principal.getName());
        Appointment booked = service.bookSlot(id, request, clientId);
        return ResponseEntity.ok(booked);
    }
}