package nl.itqaanconsulting.freelanceflow.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findAllByProjectId(UUID projectId);

    long countByIssueDateBetween(LocalDate from, LocalDate to);
}
