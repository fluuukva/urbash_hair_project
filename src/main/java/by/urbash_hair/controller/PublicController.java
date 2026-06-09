package by.urbash_hair.controller;

import by.urbash_hair.entity.Master;
import by.urbash_hair.entity.Service;
import by.urbash_hair.repository.MasterRepository;
import by.urbash_hair.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class PublicController {

    private final MasterRepository masterRepository;
    private final ServiceRepository serviceRepository;

    @GetMapping("/masters")
    public ResponseEntity<List<Master>> getAllMasters() {
        return ResponseEntity.ok(masterRepository.findAll());
    }

    @GetMapping("/services")
    public ResponseEntity<List<Service>> getAllServices() {
        return ResponseEntity.ok(serviceRepository.findAll());
    }
}