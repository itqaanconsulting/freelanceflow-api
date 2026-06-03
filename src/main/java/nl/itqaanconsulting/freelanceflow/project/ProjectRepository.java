package nl.itqaanconsulting.freelanceflow.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByCustomerId(UUID customerId);

    Optional<Project> findByCustomerIdAndNameIgnoreCase(UUID customerId, String name);
}
