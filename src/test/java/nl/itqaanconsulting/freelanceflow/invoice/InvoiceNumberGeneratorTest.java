package nl.itqaanconsulting.freelanceflow.invoice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class InvoiceNumberGeneratorTest {

    private static final LocalDate ISSUE_DATE = LocalDate.of(2031, 3, 15);

    @Autowired
    private InvoiceNumberGenerator generator;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void issuesUniqueSequentialNumbersUnderConcurrentLoad() throws Exception {
        String first = transactionTemplate.execute(status -> generator.next(ISSUE_DATE));
        assertThat(first).isEqualTo("INV-2031-0001");

        int parallelRequests = 8;
        ExecutorService executor = Executors.newFixedThreadPool(parallelRequests);
        try {
            List<Callable<String>> tasks = IntStream.range(0, parallelRequests)
                    .<Callable<String>>mapToObj(i -> () -> transactionTemplate.execute(status -> generator.next(ISSUE_DATE)))
                    .toList();

            List<String> numbers = new ArrayList<>();
            for (Future<String> future : executor.invokeAll(tasks)) {
                numbers.add(future.get());
            }

            assertThat(numbers)
                    .doesNotHaveDuplicates()
                    .containsExactlyInAnyOrderElementsOf(
                            IntStream.rangeClosed(2, parallelRequests + 1)
                                    .mapToObj(sequence -> "INV-2031-%04d".formatted(sequence))
                                    .toList()
                    );
        } finally {
            executor.shutdownNow();
        }
    }
}
