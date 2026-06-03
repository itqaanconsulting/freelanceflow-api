package nl.itqaanconsulting.freelanceflow.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);
}
