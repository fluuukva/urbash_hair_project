package by.urbash_hair.service;

import by.urbash_hair.config.HashUtils;
import by.urbash_hair.dto.CourseApplicationRequest;
import by.urbash_hair.entity.Client;
import by.urbash_hair.entity.Course;
import by.urbash_hair.entity.CourseApplication;
import by.urbash_hair.repository.ClientRepository;
import by.urbash_hair.repository.CourseApplicationRepository;
import by.urbash_hair.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CourseApplicationService {

    private final CourseApplicationRepository repository;
    private final ClientRepository clientRepository;
    private final CourseRepository courseRepository;
    private final HashUtils hashUtils;

    public void create(CourseApplicationRequest request) {
        
        Client client = null;
        
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElse(null);
        }
        
        if (client == null && request.getPhone() != null && !request.getPhone().isEmpty()) {
            String phoneHash = hashUtils.hashPhone(request.getPhone());
            String emailHash = hashUtils.hashEmail(request.getEmail());
            client = clientRepository.findByPhoneHash(phoneHash)
                    .orElseGet(() -> {
                        Client newClient = Client.builder()
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .phone(request.getPhone())
                                .phoneHash(phoneHash)
                                .email(request.getEmail())
                                .emailHash(emailHash)
                                .build();
                        return clientRepository.save(newClient);
                    });
        }

        @SuppressWarnings("null")
        Course course = null;
        
        if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElse(null);
        } else if (request.getInterest() != null && !request.getInterest().isEmpty()) {
            course = courseRepository.findByName(request.getInterest()).orElse(null);
        }

        @SuppressWarnings("null")
        CourseApplication application = CourseApplication.builder()
                .client(client)
                .course(course)
                .date(LocalDate.now().toString())
                .status("PENDING")
                .build();

        repository.save(application);
    }
}
