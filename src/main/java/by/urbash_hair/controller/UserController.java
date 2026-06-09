package by.urbash_hair.controller;

import by.urbash_hair.config.HashUtils;
import by.urbash_hair.dto.ClientProfileResponse;
import by.urbash_hair.dto.UpdateProfileRequest;
import by.urbash_hair.entity.Client;
import by.urbash_hair.repository.ClientRepository;
import by.urbash_hair.service.AuditLogService;
import by.urbash_hair.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class UserController {

    private final ClientRepository clientRepository;
    private final HashUtils hashUtils;
    private final AuditLogService auditLogService;
    private final ClientService clientService;   // используется для удаления аккаунта

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('DATA_OFFICER')")
    public ResponseEntity<ClientProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long clientId = Long.parseLong(currentUser.getUsername());
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));
        auditLogService.log(clientId, "VIEW_CLIENT_PII", "Client viewed own profile");
        return ResponseEntity.ok(ClientProfileResponse.fromClient(client));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('DATA_OFFICER')")
    public ResponseEntity<ClientProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        Long clientId = Long.parseLong(currentUser.getUsername());
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));

        if (request.getLastName() != null) {
            client.setLastName(request.getLastName());
        }
        if (request.getFirstName() != null) {
            client.setFirstName(request.getFirstName());
        }
        if (request.getMiddleName() != null) {
            client.setMiddleName(request.getMiddleName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            client.setEmail(request.getEmail());
            client.setEmailHash(hashUtils.hashEmail(request.getEmail()));
        } else if (request.getEmail() != null && request.getEmail().isBlank()) {
            // Если прислали пустую строку – очищаем email и хеш
            client.setEmail(null);
            client.setEmailHash(null);
        }

        Client saved = clientRepository.save(client);
        auditLogService.log(clientId, "UPDATE_CLIENT", "Client updated profile");
        return ResponseEntity.ok(ClientProfileResponse.fromClient(saved));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN') or hasRole('DATA_OFFICER')")
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        Long clientId = Long.parseLong(currentUser.getUsername());
        clientService.deleteClientWithDependencies(clientId);
        return ResponseEntity.noContent().build();
    }
}