package com.example.librarian;

import com.example.librarian.entity.libbook.Book;
import com.example.librarian.outbox.OutboxDispatcher;
import com.example.librarian.repository.libbook.BookRepository;
import com.example.librarian.repository.libbook.OutboxRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("multidb")
@Testcontainers
public class BookServiceIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.34")
            .withDatabaseName("libdemo")
            .withUsername("root")
            .withPassword("Mysql")
            .withEnv("MYSQL_ROOT_PASSWORD", "Mysql");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );

    static {
        mysql.start();
        kafka.start();
        // FORCE the system property early so the Kafka AdminClient finds it immediately
        System.setProperty("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
    }

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private OutboxDispatcher outboxDispatcher;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        // 1. Kafka Properties
        String kafkaServers = kafka.getBootstrapServers();
        r.add("spring.kafka.bootstrap-servers", () -> kafkaServers);
        r.add("spring.kafka.producer.bootstrap-servers", () -> kafkaServers);
        r.add("spring.kafka.admin.properties.bootstrap.servers", () -> kafkaServers);

        // Timeout settings to prevent the test from hanging
        r.add("spring.kafka.producer.properties.max.block.ms", () -> "5000");
        r.add("spring.kafka.admin.properties.request.timeout.ms", () -> "5000");

        // 2. Database Properties
        String jdbcUrl = mysql.getJdbcUrl();
        String baseUri = jdbcUrl.substring(0, jdbcUrl.lastIndexOf("/") + 1);

        r.add("spring.datasource.libbook.url", () -> baseUri + "libbook?createDatabaseIfNotExist=true&allowMultiQueries=true");
        r.add("spring.datasource.libbook.username", mysql::getUsername);
        r.add("spring.datasource.libbook.password", mysql::getPassword);

        r.add("spring.datasource.libdemo.url", () -> baseUri + "libdemo?createDatabaseIfNotExist=true&allowMultiQueries=true");
        r.add("spring.datasource.libdemo.username", mysql::getUsername);
        r.add("spring.datasource.libdemo.password", mysql::getPassword);

        // 3. Manual SQL Initialization
        try (HikariDataSource dataSource = new HikariDataSource()) {
            dataSource.setJdbcUrl(jdbcUrl + "?allowMultiQueries=true");
            dataSource.setUsername(mysql.getUsername());
            dataSource.setPassword(mysql.getPassword());

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("init-test-db.sql"));
            populator.execute(dataSource);
            System.out.println(">>> Test Databases Initialized via script");
        } catch (Exception e) {
            throw new RuntimeException("Test DB Init failed", e);
        }
    }

    @Test
    void checkoutAndReturnFlow() {
        // --- 1. PREPARE DATA ---
        var book = new Book();
        book.setBookId("b_100");
        book.setAuthor("George Orwell");
        book.setStatus("Available");
        book.setTitle("1984");
        bookRepository.save(book);

        // --- 2. EXECUTE CHECKOUT ---
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("userId", 100);
        body.put("bookId", "b_100");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity("/books/checkout", request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // --- 3. DISPATCH & VERIFY ---
        await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    outboxDispatcher.dispatch();
                    var events = outboxRepository.findAll();
                    assertThat(events).isNotEmpty();
                    assertThat(events.get(events.size() - 1).isProcessed()).isTrue();
                });
    }
}