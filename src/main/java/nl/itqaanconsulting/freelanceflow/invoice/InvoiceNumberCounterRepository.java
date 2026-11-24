package nl.itqaanconsulting.freelanceflow.invoice;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface InvoiceNumberCounterRepository extends JpaRepository<InvoiceNumberCounter, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from InvoiceNumberCounter c where c.invoiceYear = :year")
    Optional<InvoiceNumberCounter> findForUpdate(@Param("year") int year);
}
