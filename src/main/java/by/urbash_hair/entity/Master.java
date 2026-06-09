package by.urbash_hair.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "мастер")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Master {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Мастера")
    private Long id;

    @Column(name = "Имя")
    private String firstName;

    @Column(name = "Фамилия")
    private String lastName;

    @Column(name = "Специализация")
    private String specialization;

    @Column(name = "Стаж")
    private String experience;

    @Column(name = "Фото")
    private String photo;

    @Column(name = "Описание")
    private String description;

    @ManyToOne
    @JoinColumn(name = "id_Услуги")
    private Service service;
}
