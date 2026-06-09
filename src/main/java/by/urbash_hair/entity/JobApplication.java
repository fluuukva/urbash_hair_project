package by.urbash_hair.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "заявка_на_работу")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Заявки")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_Соискателя")
    private Applicant applicant;

    @Transient
    private String name;
    
    @Transient
    private String email;
    
    @Transient
    private String phone;
    
    @Transient
    private String message;

    @Column(name = "Дата")
    private String date;

    @Column(name = "Статус")
    private String status;
}
