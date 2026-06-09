package by.urbash_hair.controller;

import by.urbash_hair.dto.ClientProfileResponse;
import by.urbash_hair.dto.GenerateSlotsRequest;
import by.urbash_hair.entity.*;
import by.urbash_hair.repository.*;
import by.urbash_hair.service.AppointmentService;
import by.urbash_hair.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminApiController {

    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CourseApplicationRepository courseApplicationRepository;
    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final CourseRepository courseRepository;
    private final ServiceRepository serviceRepository;
    private final MasterRepository masterRepository;
    private final AuditLogService auditLogService;
    private final AppointmentService appointmentService;   // <-- добавлено

    // ImageKit configuration (из application.yml)
    @Value("${imagekit.private-key}")
    private String imagekitPrivateKey;

    // ==================== МЕТАДАННЫЕ ТАБЛИЦ ====================
    @GetMapping("/tables")
    public ResponseEntity<List<Map<String, String>>> getTables() {
        List<Map<String, String>> tables = new ArrayList<>();
        tables.add(Map.of("key", "clients", "label", "Клиенты"));
        tables.add(Map.of("key", "appointments", "label", "Записи"));
        tables.add(Map.of("key", "job-applications", "label", "Заявки на работу"));
        tables.add(Map.of("key", "course-applications", "label", "Заявки на курсы"));
        tables.add(Map.of("key", "reviews", "label", "Отзывы"));
        tables.add(Map.of("key", "posts", "label", "Посты (Блог)"));
        tables.add(Map.of("key", "courses", "label", "Курсы"));
        tables.add(Map.of("key", "services", "label", "Услуги"));
        tables.add(Map.of("key", "masters", "label", "Мастера"));
        return ResponseEntity.ok(tables);
    }

    // ==================== УНИВЕРСАЛЬНЫЙ CRUD ====================
    @SuppressWarnings("rawtypes")
    @GetMapping("/data/{table}")
    public ResponseEntity<Map<String, Object>> getTableData(
            @PathVariable String table,
            @RequestParam(required = false) String status) {

        List<Map<String, Object>> data;
        List<Map<String, String>> headers = new ArrayList<>();

        switch (table) {
        case "clients":
            data = clientRepository.findAll().stream().map(c -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("fullName", fullName(c));
                map.put("phone", c.getPhone());
                map.put("email", c.getEmail());
                map.put("createdAt", c.getConsentGivenAt());
                return map;
            }).collect(Collectors.toList());
            headers.add(Map.of("key", "id", "title", "ID"));
            headers.add(Map.of("key", "fullName", "title", "ФИО"));
            headers.add(Map.of("key", "phone", "title", "Телефон"));
            headers.add(Map.of("key", "email", "title", "Email"));
            headers.add(Map.of("key", "createdAt", "title", "Дата регистрации"));
            break;

        case "appointments":
            data = appointmentRepository.findAll().stream().map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", a.getId());
                map.put("clientName", fullName(a.getClient()));
                map.put("clientFirstName", a.getClient() != null ? a.getClient().getFirstName() : null);
                map.put("clientLastName", a.getClient() != null ? a.getClient().getLastName() : null);
                map.put("clientPhone", a.getClient() != null ? a.getClient().getPhone() : null);
                map.put("serviceName", a.getService() != null ? a.getService().getName() : "");
                map.put("masterName", fullName(a.getMaster()));
                map.put("masterId", a.getMaster() != null ? a.getMaster().getId() : null);
                map.put("appointmentDate", a.getDate());
                map.put("time", a.getTime());
                map.put("status", a.getStatus());
                map.put("notes", a.getNotes());
                return map;
            }).collect(Collectors.toList());
            headers.add(Map.of("key", "id", "title", "ID"));
            headers.add(Map.of("key", "clientName", "title", "Клиент"));
            headers.add(Map.of("key", "clientFirstName", "title", "Имя клиента"));
            headers.add(Map.of("key", "clientLastName", "title", "Фамилия клиента"));
            headers.add(Map.of("key", "clientPhone", "title", "Телефон клиента"));
            headers.add(Map.of("key", "serviceName", "title", "Услуга"));
            headers.add(Map.of("key", "masterName", "title", "Мастер"));
            headers.add(Map.of("key", "masterId", "title", "ID мастера"));
            headers.add(Map.of("key", "appointmentDate", "title", "Дата"));
            headers.add(Map.of("key", "time", "title", "Время"));
            headers.add(Map.of("key", "status", "title", "Статус"));
            headers.add(Map.of("key", "notes", "title", "Пожелания"));
            break;

        case "job-applications":
            data = jobApplicationRepository.findAll().stream().map(j -> {
                Client client = j.getApplicant() != null ? j.getApplicant().getClient() : null;
                Map<String, Object> map = new HashMap<>();
                map.put("id", j.getId());
                map.put("fullName", fullName(client));
                map.put("phone", maskPhone(client != null ? client.getPhone() : null));
                map.put("position", j.getApplicant() != null ? j.getApplicant().getVacancy() : "");
                map.put("status", j.getStatus());
                map.put("createdAt", j.getDate());
                return map;
            }).collect(Collectors.toList());
            headers.add(Map.of("key", "id", "title", "ID"));
            headers.add(Map.of("key", "fullName", "title", "ФИО"));
            headers.add(Map.of("key", "phone", "title", "Телефон"));
            headers.add(Map.of("key", "position", "title", "Позиция"));
            headers.add(Map.of("key", "status", "title", "Статус"));
            headers.add(Map.of("key", "createdAt", "title", "Дата"));
            break;

        case "course-applications":
            data = courseApplicationRepository.findAll().stream().map(ca -> {
                Client client = ca.getClient();
                Map<String, Object> map = new HashMap<>();
                map.put("id", ca.getId());
                map.put("fullName", fullName(client));
                map.put("phone", maskPhone(client != null ? client.getPhone() : null));
                map.put("courseName", ca.getCourse() != null ? ca.getCourse().getName() : "");
                map.put("status", ca.getStatus());
                map.put("createdAt", ca.getDate());
                return map;
            }).collect(Collectors.toList());
            headers.add(Map.of("key", "id", "title", "ID"));
            headers.add(Map.of("key", "fullName", "title", "ФИО"));
            headers.add(Map.of("key", "phone", "title", "Телефон"));
            headers.add(Map.of("key", "courseName", "title", "Курс"));
            headers.add(Map.of("key", "status", "title", "Статус"));
            headers.add(Map.of("key", "createdAt", "title", "Дата"));
            break;

        case "reviews":
            List<Review> reviews;
            if (status != null && !status.isEmpty()) {
                reviews = reviewRepository.findAll().stream()
                        .filter(r -> status.equals(r.getStatus()))
                        .collect(Collectors.toList());
            } else {
                reviews = reviewRepository.findAll();
            }
            data = reviews.stream().map(r -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", r.getId());
                map.put("clientName", fullName(r.getClient()));
                map.put("rating", r.getRating());
                map.put("comment", r.getComment());
                map.put("status", r.getStatus());
                map.put("createdAt", r.getDate());
                return map;
            }).collect(Collectors.toList());
            headers.add(Map.of("key", "id", "title", "ID"));
            headers.add(Map.of("key", "clientName", "title", "Клиент"));
            headers.add(Map.of("key", "rating", "title", "Рейтинг"));
            headers.add(Map.of("key", "comment", "title", "Комментарий"));
            headers.add(Map.of("key", "status", "title", "Статус"));
            headers.add(Map.of("key", "createdAt", "title", "Дата"));
            break;

        case "posts":
            data = postRepository.findAll().stream().map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", p.getId());
                map.put("title", p.getTitle());
                map.put("date", p.getDate());
                map.put("description", p.getDescription());
                map.put("image", p.getImage());
                return map;
            }).collect(Collectors.toList());
            headers.add(Map.of("key", "id", "title", "ID"));
            headers.add(Map.of("key", "title", "title", "Заголовок"));
            headers.add(Map.of("key", "date", "title", "Дата"));
            headers.add(Map.of("key", "description", "title", "Описание"));
            headers.add(Map.of("key", "image", "title", "Изображение"));
            break;

        case "courses":
            data = courseRepository.findAll().stream().map(c -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("name", c.getName());
                map.put("description", c.getDescription());
                map.put("price", c.getPrice());
                map.put("duration", c.getDuration());
                return map;
            }).collect(Collectors.toList());
            headers.add(Map.of("key", "id", "title", "ID"));
            headers.add(Map.of("key", "name", "title", "Название"));
            headers.add(Map.of("key", "description", "title", "Описание"));
            headers.add(Map.of("key", "price", "title", "Цена"));
            headers.add(Map.of("key", "duration", "title", "Длительность"));
            break;

        case "services":
            data = serviceRepository.findAll().stream().map(s -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", s.getId());
                map.put("name", s.getName());
                map.put("price", s.getPrice());
                map.put("duration", s.getDuration());
                return map;
            }).collect(Collectors.toList());
            headers.add(Map.of("key", "id", "title", "ID"));
            headers.add(Map.of("key", "name", "title", "Название"));
            headers.add(Map.of("key", "price", "title", "Цена"));
            headers.add(Map.of("key", "duration", "title", "Длительность"));
            break;

        case "masters":
            data = masterRepository.findAll().stream().map(m -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", m.getId());
                map.put("firstName", m.getFirstName());
                map.put("lastName", m.getLastName());
                map.put("specialization", m.getSpecialization());
                map.put("experience", m.getExperience());
                return map;
            }).collect(Collectors.toList());
            headers.add(Map.of("key", "id", "title", "ID"));
            headers.add(Map.of("key", "firstName", "title", "Имя"));
            headers.add(Map.of("key", "lastName", "title", "Фамилия"));
            headers.add(Map.of("key", "specialization", "title", "Специализация"));
            headers.add(Map.of("key", "experience", "title", "Стаж"));
            break;

        default:
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown table: " + table));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("headers", headers);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    // ==================== СОХРАНЕНИЕ / УДАЛЕНИЕ ====================
    @PostMapping("/save/{table}")
    public ResponseEntity<?> saveTableData(@PathVariable String table, @RequestBody Map<String, Object> payload, Principal principal) {
        try {
            switch (table) {
                case "posts":
                    Post post = new Post();
                    if (payload.get("id") != null) {
                        Long id = Long.valueOf(payload.get("id").toString());
                        Optional<Post> existingPost = postRepository.findById(id);
                        if (existingPost.isPresent()) {
                            post = existingPost.get();
                        }
                    }
                    post.setTitle((String) payload.get("title"));
                    post.setDate((String) payload.get("date"));
                    post.setDescription((String) payload.get("description"));
                    if (payload.get("image") != null) {
                        post.setImage((String) payload.get("image"));
                    }
                    Post savedPost = postRepository.save(post);
                    auditLogService.log(getUserId(principal), "SAVE_POST", "Saved post " + savedPost.getId());
                    return ResponseEntity.ok(savedPost);
                case "courses":
                    Course course = new Course();
                    if (payload.get("id") != null) {
                        Long id = Long.valueOf(payload.get("id").toString());
                        Optional<Course> existingCourse = courseRepository.findById(id);
                        if (existingCourse.isPresent()) {
                            course = existingCourse.get();
                        }
                    }
                    course.setName((String) payload.get("name"));
                    course.setDescription((String) payload.get("description"));
                    if (payload.get("price") != null) {
                        course.setPrice(payload.get("price").toString());
                    }
                    if (payload.get("duration") != null) {
                        course.setDuration(payload.get("duration").toString());
                    }
                    Course savedCourse = courseRepository.save(course);
                    auditLogService.log(getUserId(principal), "SAVE_COURSE", "Saved course " + savedCourse.getId());
                    return ResponseEntity.ok(savedCourse);
                case "services":
                    Service service = new Service();
                    if (payload.get("id") != null) {
                        Long id = Long.valueOf(payload.get("id").toString());
                        Optional<Service> existingService = serviceRepository.findById(id);
                        if (existingService.isPresent()) {
                            service = existingService.get();
                        }
                    }
                    service.setName((String) payload.get("name"));
                    if (payload.get("price") != null) {
                        service.setPrice(payload.get("price").toString());
                    }
                    if (payload.get("duration") != null) {
                        service.setDuration(payload.get("duration").toString());
                    }
                    Service savedService = serviceRepository.save(service);
                    auditLogService.log(getUserId(principal), "SAVE_SERVICE", "Saved service " + savedService.getId());
                    return ResponseEntity.ok(savedService);
                case "masters":
                    Master master = new Master();
                    if (payload.get("id") != null) {
                        Long id = Long.valueOf(payload.get("id").toString());
                        Optional<Master> existingMaster = masterRepository.findById(id);
                        if (existingMaster.isPresent()) {
                            master = existingMaster.get();
                        }
                    }
                    master.setFirstName((String) payload.get("firstName"));
                    master.setLastName((String) payload.get("lastName"));
                    master.setSpecialization((String) payload.get("specialization"));
                    master.setExperience((String) payload.get("experience"));
                    Master savedMaster = masterRepository.save(master);
                    auditLogService.log(getUserId(principal), "SAVE_MASTER", "Saved master " + savedMaster.getId());
                    return ResponseEntity.ok(savedMaster);
                default:
                    return ResponseEntity.badRequest().body("Unknown table: " + table);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{table}")
    public ResponseEntity<?> deleteTableData(@PathVariable String table, @RequestBody List<Long> ids, Principal principal) {
        try {
            switch (table) {
                case "clients":
                    ids.forEach(clientRepository::deleteById);
                    auditLogService.log(getUserId(principal), "DELETE_CLIENTS", "Deleted clients: " + ids);
                    break;
                case "appointments":
                    ids.forEach(appointmentRepository::deleteById);
                    auditLogService.log(getUserId(principal), "DELETE_APPOINTMENTS", "Deleted appointments: " + ids);
                    break;
                case "job-applications":
                    ids.forEach(jobApplicationRepository::deleteById);
                    auditLogService.log(getUserId(principal), "DELETE_JOB_APPS", "Deleted job applications: " + ids);
                    break;
                case "course-applications":
                    ids.forEach(courseApplicationRepository::deleteById);
                    auditLogService.log(getUserId(principal), "DELETE_COURSE_APPS", "Deleted course applications: " + ids);
                    break;
                case "reviews":
                    ids.forEach(reviewRepository::deleteById);
                    auditLogService.log(getUserId(principal), "DELETE_REVIEWS", "Deleted reviews: " + ids);
                    break;
                case "posts":
                    ids.forEach(postRepository::deleteById);
                    auditLogService.log(getUserId(principal), "DELETE_POSTS", "Deleted posts: " + ids);
                    break;
                case "courses":
                    ids.forEach(courseRepository::deleteById);
                    auditLogService.log(getUserId(principal), "DELETE_COURSES", "Deleted courses: " + ids);
                    break;
                case "services":
                    ids.forEach(serviceRepository::deleteById);
                    auditLogService.log(getUserId(principal), "DELETE_SERVICES", "Deleted services: " + ids);
                    break;
                case "masters":
                    ids.forEach(masterRepository::deleteById);
                    auditLogService.log(getUserId(principal), "DELETE_MASTERS", "Deleted masters: " + ids);
                    break;
                default:
                    return ResponseEntity.badRequest().body("Unknown table: " + table);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // ЗАГРУЗКА ИЗОБРАЖЕНИЙ ЧЕРЕЗ IMAGEKIT (без SDK)
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Файл не выбран"));
            }

            String imageKitUploadUrl = "https://upload.imagekit.io/api/v1/files/upload";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            String auth = imagekitPrivateKey + ":";
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + new String(encodedAuth));

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("fileName", file.getOriginalFilename());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.postForEntity(imageKitUploadUrl, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String imageUrl = (String) response.getBody().get("url");
                return ResponseEntity.ok(Map.of("path", imageUrl));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "ImageKit error: " + response.getBody()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // ==================== НОВЫЙ ЭНДПОИНТ ДЛЯ ГЕНЕРАЦИИ СЛОТОВ (АДМИН) ====================
    @PostMapping("/slots/generate")
    public ResponseEntity<?> generateSlots(@RequestBody GenerateSlotsRequest request, Principal principal) {
        try {
            List<Appointment> slots = appointmentService.generateSlots(request);
            auditLogService.log(getUserId(principal), "GENERATE_SLOTS", "Generated " + slots.size() + " slots");
            return ResponseEntity.ok(Map.of(
                "message", "Создано " + slots.size() + " слотов",
                "slots", slots
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка генерации слотов: " + e.getMessage()));
        }
    }

    // ==================== СПЕЦИФИЧНЫЕ ЭНДПОИНТЫ ====================

    @PutMapping("/reviews/{id}/status")
    public ResponseEntity<?> updateReviewStatus(@PathVariable Long id, @RequestBody Map<String, String> body, Principal principal) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        String newStatus = body.get("status");
        if (!List.of("PENDING", "APPROVED", "REJECTED").contains(newStatus)) {
            return ResponseEntity.badRequest().body("Invalid status");
        }
        review.setStatus(newStatus);
        reviewRepository.save(review);
        auditLogService.log(getUserId(principal), "REVIEW_STATUS_CHANGE",
                "Changed review " + id + " status to " + newStatus);
        return ResponseEntity.ok(review);
    }

    // Обновление статуса записи (слота)
    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<Appointment> updateAppointmentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(body.get("status"));
        appointmentRepository.save(appointment);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientProfileResponse>> getAllClients(Principal principal) {
        List<Client> clients = clientRepository.findAll();
        List<ClientProfileResponse> response = clients.stream()
                .map(ClientProfileResponse::fromClient)
                .collect(Collectors.toList());
        auditLogService.log(getUserId(principal), "VIEW_CLIENT_LIST", "Viewed all clients");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<ClientProfileResponse> getClientById(@PathVariable Long id, Principal principal) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        auditLogService.log(getUserId(principal), "VIEW_CLIENT_PII", "Viewed client " + id);
        return ResponseEntity.ok(ClientProfileResponse.fromClient(client));
    }

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id, Principal principal) {
        clientRepository.deleteById(id);
        auditLogService.log(getUserId(principal), "DELETE_CLIENT", "Deleted client " + id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments(Principal principal) {
        List<Appointment> list = appointmentRepository.findAll();
        auditLogService.log(getUserId(principal), "VIEW_ALL_APPOINTMENTS", "Viewed all appointments");
        return ResponseEntity.ok(list);
    }

    @GetMapping("/job-applications")
    public ResponseEntity<List<JobApplication>> getAllJobApplications(Principal principal) {
        List<JobApplication> list = jobApplicationRepository.findAll();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/job-applications/{id}/status")
    public ResponseEntity<JobApplication> updateJobApplicationStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        JobApplication app = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("JobApplication not found"));
        app.setStatus(body.get("status"));
        jobApplicationRepository.save(app);
        return ResponseEntity.ok(app);
    }

    @GetMapping("/course-applications")
    public ResponseEntity<List<CourseApplication>> getAllCourseApplications() {
        return ResponseEntity.ok(courseApplicationRepository.findAll());
    }

    @PutMapping("/course-applications/{id}/status")
    public ResponseEntity<CourseApplication> updateCourseApplicationStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        CourseApplication app = courseApplicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CourseApplication not found"));
        app.setStatus(body.get("status"));
        courseApplicationRepository.save(app);
        return ResponseEntity.ok(app);
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewRepository.findAll());
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, Principal principal) {
        reviewRepository.deleteById(id);
        auditLogService.log(getUserId(principal), "DELETE_REVIEW", "Deleted review " + id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(@RequestBody Post post, Principal principal) {
        Post saved = postRepository.save(post);
        auditLogService.log(getUserId(principal), "CREATE_POST", "Created post " + saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody Post post, Principal principal) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        existing.setTitle(post.getTitle());
        existing.setDescription(post.getDescription());
        if (post.getDate() != null) existing.setDate(post.getDate());
        if (post.getImage() != null) existing.setImage(post.getImage());
        postRepository.save(existing);
        auditLogService.log(getUserId(principal), "UPDATE_POST", "Updated post " + id);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Principal principal) {
        postRepository.deleteById(id);
        auditLogService.log(getUserId(principal), "DELETE_POST", "Deleted post " + id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    private Long getUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return 0L;
        }
        try {
            return Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String fullName(Client client) {
        if (client == null) return "";
        String fn = client.getFirstName() != null ? client.getFirstName() : "";
        String ln = client.getLastName() != null ? client.getLastName() : "";
        return (fn + " " + ln).trim();
    }

    private String fullName(Master master) {
        if (master == null) return "";
        String fn = master.getFirstName() != null ? master.getFirstName() : "";
        String ln = master.getLastName() != null ? master.getLastName() : "";
        return (fn + " " + ln).trim();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 3) return phone;
        return "***" + phone.substring(phone.length() - 3);
    }
}