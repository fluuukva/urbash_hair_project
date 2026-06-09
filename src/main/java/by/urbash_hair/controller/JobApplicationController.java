package by.urbash_hair.controller;

import by.urbash_hair.dto.JobApplicationRequest;
import by.urbash_hair.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
@CrossOrigin
public class JobApplicationController {

    private final JobApplicationService service;

    @PostMapping
    public void create(@RequestBody JobApplicationRequest request) {
        service.create(request);
    }
}
