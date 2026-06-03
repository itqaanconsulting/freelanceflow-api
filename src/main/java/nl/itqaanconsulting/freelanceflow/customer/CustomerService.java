package nl.itqaanconsulting.freelanceflow.customer;

import nl.itqaanconsulting.freelanceflow.shared.DuplicateResourceException;
import nl.itqaanconsulting.freelanceflow.shared.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
class CustomerService {

    private final CustomerRepository customerRepository;

    CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    List<CustomerResponse> findAll() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    CustomerResponse findById(UUID id) {
        return CustomerResponse.from(findCustomer(id));
    }

    @Transactional
    CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Customer with email already exists: " + request.email());
        }

        Customer customer = new Customer(
                request.companyName(),
                request.contactName(),
                request.email(),
                request.phone(),
                request.vatNumber(),
                new Address(request.street(), request.city(), request.country())
        );

        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = findCustomer(id);
        customerRepository.findByEmailIgnoreCase(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Customer with email already exists: " + request.email());
                });

        customer.update(request);
        return CustomerResponse.from(customer);
    }

    @Transactional
    void delete(UUID id) {
        Customer customer = findCustomer(id);
        customerRepository.delete(customer);
    }

    private Customer findCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }
}
