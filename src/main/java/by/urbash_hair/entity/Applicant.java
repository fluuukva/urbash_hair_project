package by.urbash_hair.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "соискатель")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Соискателя")
    private Long id;

    @Column(name = "id_Клиента")
    private Long clientId;

    @Column(name = "Вакансия")
    private String vacancy;

    @Transient
    private Client client;
}
