package by.urbash_hair.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "пост")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Поста")
    private Long id;

    @Column(name = "Заголовок")
    private String title;

    @Column(name = "Дата")
    private String date;

    @Column(name = "Описание", columnDefinition = "TEXT")
    private String description;

    @Column(name = "Фото")
    private String image;
}
