package nl.itqaanconsulting.freelanceflow.timeentry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findAllByProjectId(UUID projectId);

    List<TimeEntry> findAllByProjectIdAndStatusAndInvoiceIsNull(UUID projectId, TimeEntryStatus status);
}
