package by.urbash_hair.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "заявка_на_курс")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Заявки_курса")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_Клиента")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "id_Курса")
    private Course course;

    @Column(name = "Дата")
    private String date;

    @Column(name = "Статус")
    private String status;
}
