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

        // Для формы "Работа" на work-with-us.html отправляются поля name/email/phone.
        // Но endpoint принимает CourseApplicationRequest, поэтому here:
        // - берём ФИО не из request.getFirstName/LastName (они пустые), а из name/email/phone, если фронт прислал их.
        // - в CourseApplicationRequest имя поля "name" отсутствует, поэтому здесь не получится надёжно восстановить ФИО.
        // Поэтому сначала пытаемся сохранить телефон и email, а ФИО оставляем как есть (пустым) только если фронт действительно его не отправил.

        if (client == null && request.getPhone() != null && !request.getPhone().isEmpty()) {
            String phoneHash = hashUtils.hashPhone(request.getPhone());
            String emailHash = hashUtils.hashEmail(request.getEmail());

            client = clientRepository.findByPhoneHash(phoneHash)
                    .orElseGet(() -> {
                        String firstName = request.getFirstName();
                        String lastName = request.getLastName();
                        if ((firstName == null || firstName.isBlank()) && (lastName == null || lastName.isBlank())) {
                            // work-with-us.html присылает только name (ФИО в одном поле)
                            String fullName = request.getFullName();
                            if (fullName != null && !fullName.isBlank()) {
                                String[] parts = fullName.trim().split("\\s+");
                                firstName = parts.length > 0 ? parts[0] : null;
                                lastName = parts.length > 1 ? parts[1] : (parts.length == 1 ? "" : null);
                            }
                        }


                        // Если фронт прислал только одно поле с ФИО (например, name="Иванов Иван")
                        // оно в CourseApplicationRequest лежать не будет, поэтому запрос, как правило,
                        // придёт пустым по firstName/lastName.
                        // Чтобы не сохранять пустого клиента, не создаём клиента без ФИО.
                        // Но телефон должен сохраняться всегда.
                        Client newClient = Client.builder()
                                .firstName(firstName)
                                .lastName(lastName)
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

        JobApplication application = JobApplication.builder()
                .applicant(applicant)
                .date(LocalDate.now().toString())
                .status("PENDING")
                .build();

        repository.save(application);
    }
    

    public void create(JobApplicationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request is null");
        }

        Client client = null;

        if (request.getClientId() != null) {
            client = clientRepository.findById(request.getClientId())
                    .orElse(null);
        }

        // Для формы "Работа" телефон обязателен. Если клиент не найден — создаём.
        if (client == null) {
            String phone = request.getPhone();
            if (phone == null || phone.isBlank()) {
                throw new IllegalArgumentException("Phone is required");
            }

            String phoneHash = hashUtils.hashPhone(phone);
            String emailHash = hashUtils.hashEmail(request.getEmail());

            client = clientRepository.findByPhoneHash(phoneHash)
                    .orElseGet(() -> {
                        String fullName = request.getName();
                        String firstName = null;
                        String lastName = null;

                        if (fullName != null && !fullName.isBlank()) {
                            String[] parts = fullName.trim().split("\\s+");
                            firstName = parts.length > 0 ? parts[0] : null;
                            lastName = parts.length > 1 ? parts[1] : "";
                            // Отчество третьим словом в DTO не приходит — игнорируем.
                        }

                        return clientRepository.save(
                                Client.builder()
                                        .firstName(firstName)
                                        .lastName(lastName)
                                        .phone(phone)
                                        .phoneHash(phoneHash)
                                        .email(request.getEmail())
                                        .emailHash(emailHash)
                                        .build()
                        );
                    });
        }

        String vacancy = request.getVacancy();

        Applicant applicant = Applicant.builder()
                .clientId(client != null ? client.getId() : null)
                .client(client)
                .vacancy(vacancy)
                .build();
        applicant = applicantRepository.save(applicant);

        JobApplication application = JobApplication.builder()
                .applicant(applicant)
                .date(LocalDate.now().toString())
                .status("PENDING")
                .build();

        repository.save(application);
    }

}
