package by.urbash_hair.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
    @Table(name = "журнал_действий")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_Клиента")

    private Long userId;

    @Column(name = "действие", nullable = false, length = 64)

    private String action;

    @Column(name = "детали_действия", length = 512)

    private String details;

    @Builder.Default
    @Column(name = "дата_действия", nullable = false)

    private LocalDateTime timestamp = LocalDateTime.now();

    public AuditLog(Long userId, String action, String details) {
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
