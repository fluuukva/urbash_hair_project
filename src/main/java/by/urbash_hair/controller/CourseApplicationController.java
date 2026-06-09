package by.urbash_hair.controller;

import by.urbash_hair.dto.CourseApplicationRequest;
import by.urbash_hair.service.CourseApplicationService;
import by.urbash_hair.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/course-applications")
@RequiredArgsConstructor
@CrossOrigin
public class CourseApplicationController {

    private final CourseApplicationService courseApplicationService;
    private final JobApplicationService jobApplicationService;

    @PostMapping
    public void create(@RequestBody CourseApplicationRequest request) {
        // Route to appropriate service based on interest type
        if (request.isJobApplication()) {
            // Create job application
            jobApplicationService.createFromRequest(request);
        } else {
            // Create course application
            courseApplicationService.create(request);
        }
    }
}
