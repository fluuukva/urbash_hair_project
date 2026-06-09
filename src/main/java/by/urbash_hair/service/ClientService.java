package by.urbash_hair.service;

import by.urbash_hair.entity.Client;
import by.urbash_hair.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final CourseApplicationRepository courseApplicationRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ReviewRepository reviewRepository;
    private final ApplicantRepository applicantRepository;

    @Transactional
    public void deleteClientWithDependencies(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));

        // Удаляем связанные записи
        appointmentRepository.findAll().stream()
                .filter(a -> a.getClient() != null && a.getClient().getId().equals(clientId))
                .forEach(appointmentRepository::delete);

        courseApplicationRepository.findAll().stream()
                .filter(ca -> ca.getClient() != null && ca.getClient().getId().equals(clientId))
                .forEach(courseApplicationRepository::delete);

        jobApplicationRepository.findAll().stream()
                .filter(ja -> ja.getApplicant() != null && ja.getApplicant().getClientId() != null && ja.getApplicant().getClientId().equals(clientId))
                .forEach(jobApplicationRepository::delete);

        reviewRepository.findAll().stream()
                .filter(r -> r.getClient() != null && r.getClient().getId().equals(clientId))
                .forEach(reviewRepository::delete);

        applicantRepository.findAll().stream()
                .filter(a -> a.getClientId() != null && a.getClientId().equals(clientId))
                .forEach(applicantRepository::delete);

        clientRepository.delete(client);
    }
}
