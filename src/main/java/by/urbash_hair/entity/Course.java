package by.urbash_hair.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "курс")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Курса")
    private Long id;

    @Column(name = "Название")
    private String name;

    @Column(name = "Описание")
    private String description;

    @Column(name = "Стоимость")
    private String price;

    @Column(name = "Длительность")
    private String duration;

    @Column(name = "Дата_начала")
    private String startDate;
}
