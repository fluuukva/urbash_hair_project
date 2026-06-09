package by.urbash_hair.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import by.urbash_hair.entity.CourseApplication;

public interface CourseApplicationRepository extends JpaRepository<CourseApplication, Long>, JpaSpecificationExecutor<CourseApplication> {
}
