package nl.itqaanconsulting.freelanceflow.timeentry;

import nl.itqaanconsulting.freelanceflow.audit.AuditService;
import nl.itqaanconsulting.freelanceflow.project.Project;
import nl.itqaanconsulting.freelanceflow.project.ProjectRepository;
import nl.itqaanconsulting.freelanceflow.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final AuditService auditService;

    TimeEntryService(TimeEntryRepository timeEntryRepository, ProjectRepository projectRepository, AuditService auditService) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    List<TimeEntryResponse> findAll(UUID projectId) {
        List<TimeEntry> timeEntries = projectId == null
                ? timeEntryRepository.findAll()
                : timeEntryRepository.findAllByProjectId(projectId);

        return timeEntries.stream()
                .map(TimeEntryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    TimeEntryResponse findById(UUID id) {
        return TimeEntryResponse.from(findTimeEntry(id));
    }

    @Transactional
    TimeEntryResponse create(TimeEntryRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.projectId()));

        TimeEntry timeEntry = new TimeEntry(project, request.workDate(), request.hours(), request.description());
        TimeEntry savedTimeEntry = timeEntryRepository.save(timeEntry);
        auditService.record(
                "TIME_ENTRY",
                savedTimeEntry.getId(),
                "TIME_ENTRY_CREATED",
                "Time entry created for project %s".formatted(project.getId())
        );
        return TimeEntryResponse.from(savedTimeEntry);
    }

    @Transactional
    TimeEntryResponse update(UUID id, TimeEntryRequest request) {
        TimeEntry timeEntry = findTimeEntry(id);
        if (!timeEntry.getProject().getId().equals(request.projectId())) {
            throw new IllegalArgumentException("Moving a time entry to another project is not supported");
        }

        timeEntry.update(request);
        return TimeEntryResponse.from(timeEntry);
    }

    @Transactional
    TimeEntryResponse submit(UUID id) {
        TimeEntry timeEntry = findTimeEntry(id);
        timeEntry.submit();
        auditService.record("TIME_ENTRY", timeEntry.getId(), "TIME_ENTRY_SUBMITTED", "Time entry submitted for approval");
        return TimeEntryResponse.from(timeEntry);
    }

    @Transactional
    TimeEntryResponse approve(UUID id) {
        TimeEntry timeEntry = findTimeEntry(id);
        timeEntry.approve();
        auditService.record("TIME_ENTRY", timeEntry.getId(), "TIME_ENTRY_APPROVED", "Time entry approved");
        return TimeEntryResponse.from(timeEntry);
    }

    @Transactional
    TimeEntryResponse reject(UUID id, TimeEntryRejectionRequest request) {
        TimeEntry timeEntry = findTimeEntry(id);
        timeEntry.reject(request.reason());
        auditService.record(
                "TIME_ENTRY",
                timeEntry.getId(),
                "TIME_ENTRY_REJECTED",
                "Time entry rejected: %s".formatted(request.reason())
        );
        return TimeEntryResponse.from(timeEntry);
    }

    @Transactional
    void delete(UUID id) {
        TimeEntry timeEntry = findTimeEntry(id);
        if (timeEntry.getStatus() != TimeEntryStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft time entries can be deleted");
        }
        timeEntryRepository.delete(timeEntry);
    }

    private TimeEntry findTimeEntry(UUID id) {
        return timeEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time entry not found: " + id));
    }
}
