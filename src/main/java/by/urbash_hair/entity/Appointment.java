package by.urbash_hair.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "запись")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    // Константы статусов для расписания
    public static final String STATUS_AVAILABLE = "AVAILABLE";   // свободный слот
    public static final String STATUS_BOOKED = "BOOKED";        // забронирован клиентом, ожидает подтверждения
    public static final String STATUS_CONFIRMED = "CONFIRMED";  // подтверждён администратором
    public static final String STATUS_CANCELLED = "CANCELLED";  // отменён
    public static final String STATUS_COMPLETED = "COMPLETED";  // выполнен

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Записи")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_Клиента")
    @JsonIgnoreProperties({"appointments"})
    private Client client;

    @ManyToOne
    @JoinColumn(name = "id_Мастера")
    private Master master;

    @ManyToOne
    @JoinColumn(name = "id_Услуги")
    private Service service;

    @Column(name = "Дата")
    private String date;   // формат: yyyy-MM-dd

    @Column(name = "Время")
    private String time;   // формат: HH:mm

    @Column(name = "Статус")
    private String status;

    @Column(name = "Пожелания")
    private String notes;
}