package by.urbash_hair.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "услуга")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Услуги")
    private Long id;

    @Column(name = "Название")
    private String name;

    @Column(name = "Описание")
    private String description;

    @Column(name = "Стоимость")
    private String price;

    @Column(name = "Длительность")
    private String duration;
}
