package by.urbash_hair.service;

import by.urbash_hair.config.HashUtils;
import by.urbash_hair.dto.JobApplicationRequest;
import by.urbash_hair.entity.Applicant;
import by.urbash_hair.entity.Client;
import by.urbash_hair.entity.JobApplication;
import by.urbash_hair.repository.ApplicantRepository;
import by.urbash_hair.repository.ClientRepository;
import by.urbash_hair.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final ClientRepository clientRepository;
    private final ApplicantRepository applicantRepository;
    private final HashUtils hashUtils;

    public void createFromRequest(by.urbash_hair.dto.CourseApplicationRequest request) {
        
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

        String vacancy = request.getVacancy();
        if (vacancy == null && request.getInterest() != null) {
            if (request.getInterest().contains("Мастер")) {
                vacancy = "Мастер";
            } else if (request.getInterest().contains("Администратор")) {
                vacancy = "Администратор";
            }
        }

        Applicant applicant = Applicant.builder()
                .clientId(client != null ? client.getId() : null)
                .client(client)
                .vacancy(vacancy)
                .build();
        applicant = applicantRepository.save(applicant);

        @SuppressWarnings("null")
        JobApplication application = JobApplication.builder()
                .applicant(applicant)
                .date(LocalDate.now().toString())
                .status("PENDING")
                .build();

        repository.save(application);
    }
    
    public void create(JobApplicationRequest request) {
        
        Client client = null;
        
        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElse(null);
        }

        Applicant applicant = Applicant.builder()
                .clientId(client != null ? client.getId() : null)
                .client(client)
                .vacancy(request.getVacancy())
                .build();
        applicant = applicantRepository.save(applicant);

        @SuppressWarnings("null")
        JobApplication application = JobApplication.builder()
                .applicant(applicant)
                .date(LocalDate.now().toString())
                .status("PENDING")
                .build();

        repository.save(application);
    }
}
