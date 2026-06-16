package by.urbash_hair.repository;

import by.urbash_hair.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    List<Appointment> findByDateAndMasterId(String date, Long masterId);

    List<Appointment> findByDateBetweenAndMasterId(String startDate, String endDate, Long masterId);

    // Новый метод для поиска без мастера
    List<Appointment> findByDateBetween(String startDate, String endDate);
}