package by.urbash_hair.repository;

import by.urbash_hair.entity.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
    Optional<Applicant> findByClientId(Long clientId);
}