package nl.itqaanconsulting.freelanceflow.timeentry;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/time-entries")
class TimeEntryController {

    private final TimeEntryService timeEntryService;

    TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @GetMapping
    List<TimeEntryResponse> findAll(@RequestParam(required = false) UUID projectId) {
        return timeEntryService.findAll(projectId);
    }

    @GetMapping("/{id}")
    TimeEntryResponse findById(@PathVariable UUID id) {
        return timeEntryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TimeEntryResponse create(@Valid @RequestBody TimeEntryRequest request) {
        return timeEntryService.create(request);
    }

    @PutMapping("/{id}")
    TimeEntryResponse update(@PathVariable UUID id, @Valid @RequestBody TimeEntryRequest request) {
        return timeEntryService.update(id, request);
    }

    @PostMapping("/{id}/submit")
    TimeEntryResponse submit(@PathVariable UUID id) {
        return timeEntryService.submit(id);
    }

    @PostMapping("/{id}/approve")
    TimeEntryResponse approve(@PathVariable UUID id) {
        return timeEntryService.approve(id);
    }

    @PostMapping("/{id}/reject")
    TimeEntryResponse reject(@PathVariable UUID id, @Valid @RequestBody TimeEntryRejectionRequest request) {
        return timeEntryService.reject(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) {
        timeEntryService.delete(id);
    }
}
