package by.urbash_hair.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "отзыв")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Отзыва")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_Клиента")
    @JsonIgnoreProperties({"appointments"})
    private Client client;

    @Column(name = "Оценка")
    private String rating;

    @Column(name = "Комментарий")
    private String comment;

    @Column(name = "Дата")
    private String date;

    @Column(name = "статус")
    @Builder.Default
    private String status = "PENDING";   // PENDING, APPROVED, REJECTED

}