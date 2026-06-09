package by.urbash_hair.service;

import by.urbash_hair.entity.AuditLog;
import by.urbash_hair.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(Long userId, String action, String details) {
        AuditLog log = new AuditLog(userId, action, details);
        auditLogRepository.save(log);
    }
}
