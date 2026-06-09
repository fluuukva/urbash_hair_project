package by.urbash_hair.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import by.urbash_hair.entity.Appointment;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    List<Appointment> findByStatusAndDateAndMasterIdAndServiceId(String status, String date, Long masterId, Long serviceId);
    List<Appointment> findByDateBetweenAndMasterId(String startDate, String endDate, Long masterId);
    boolean existsByDateAndTimeAndMasterId(String date, String time, Long masterId);
    // Новый метод: получить все слоты мастера на конкретную дату
    List<Appointment> findByDateAndMasterId(String date, Long masterId);
}