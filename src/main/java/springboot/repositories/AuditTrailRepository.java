package springboot.repositories;

import mvc.model.AuditTrail;
import mvc.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditTrailRepository extends JpaRepository<mvc.model.AuditTrail, Integer>{
    List<AuditTrail> findAllByPersonId(Person person_id);
}