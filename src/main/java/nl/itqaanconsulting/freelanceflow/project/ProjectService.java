package nl.itqaanconsulting.freelanceflow.project;

import nl.itqaanconsulting.freelanceflow.customer.Customer;
import nl.itqaanconsulting.freelanceflow.customer.CustomerRepository;
import nl.itqaanconsulting.freelanceflow.shared.DuplicateResourceException;
import nl.itqaanconsulting.freelanceflow.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
class ProjectService {

    private final ProjectRepository projectRepository;
    private final CustomerRepository customerRepository;

    ProjectService(ProjectRepository projectRepository, CustomerRepository customerRepository) {
        this.projectRepository = projectRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    List<ProjectResponse> findAll(UUID customerId) {
        List<Project> projects = customerId == null
                ? projectRepository.findAll()
                : projectRepository.findAllByCustomerId(customerId);

        return projects.stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    ProjectResponse findById(UUID id) {
        return ProjectResponse.from(findProject(id));
    }

    @Transactional
    ProjectResponse create(ProjectRequest request) {
        Customer customer = findCustomer(request.customerId());
        assertUniqueProjectName(request.customerId(), request.name(), null);

        Project project = new Project(
                customer,
                request.name(),
                request.description(),
                request.hourlyRate(),
                request.currency(),
                request.status(),
                request.startDate(),
                request.endDate()
        );

        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = findProject(id);
        findCustomer(request.customerId());

        if (!project.getCustomer().getId().equals(request.customerId())) {
            throw new IllegalArgumentException("Moving a project to another customer is not supported");
        }

        assertUniqueProjectName(request.customerId(), request.name(), id);
        project.update(request);
        return ProjectResponse.from(project);
    }

    @Transactional
    void delete(UUID id) {
        Project project = findProject(id);
        projectRepository.delete(project);
    }

    private Customer findCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    private Project findProject(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    private void assertUniqueProjectName(UUID customerId, String name, UUID currentProjectId) {
        projectRepository.findByCustomerIdAndNameIgnoreCase(customerId, name)
                .filter(existing -> !existing.getId().equals(currentProjectId))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Project with name already exists for customer: " + name);
                });
    }
}
