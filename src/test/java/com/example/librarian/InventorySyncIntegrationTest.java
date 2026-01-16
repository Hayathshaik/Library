package com.example.librarian;

import com.example.librarian.entity.libbook.Book;
import com.example.librarian.entity.libbook.BookInventory;
import com.example.librarian.repository.libbook.BookInventoryRepository;
import com.example.librarian.repository.libbook.BookRepository;
import com.example.librarian.repository.libbook.InventoryEventLogRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
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
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("multidb")
@Testcontainers
public class InventorySyncIntegrationTest {

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
        System.setProperty("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
    }

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookInventoryRepository inventoryRepository;
    @Autowired
    private InventoryEventLogRepository logRepository;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        String kafkaServers = kafka.getBootstrapServers();
        r.add("spring.kafka.bootstrap-servers", () -> kafkaServers);
        r.add("spring.kafka.producer.bootstrap-servers", () -> kafkaServers);
        r.add("spring.kafka.admin.properties.bootstrap.servers", () -> kafkaServers);
        r.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        r.add("spring.kafka.listener.ack-mode", () -> "manual");

        String jdbcUrl = mysql.getJdbcUrl();
        String baseUri = jdbcUrl.substring(0, jdbcUrl.lastIndexOf("/") + 1);

        r.add("spring.datasource.libbook.url", () -> baseUri + "libbook?createDatabaseIfNotExist=true&allowMultiQueries=true");
        r.add("spring.datasource.libbook.username", mysql::getUsername);
        r.add("spring.datasource.libbook.password", mysql::getPassword);

        r.add("spring.datasource.libdemo.url", () -> baseUri + "libdemo?createDatabaseIfNotExist=true&allowMultiQueries=true");
        r.add("spring.datasource.libdemo.username", mysql::getUsername);
        r.add("spring.datasource.libdemo.password", mysql::getPassword);

        try (HikariDataSource dataSource = new HikariDataSource()) {
            dataSource.setJdbcUrl(jdbcUrl + "?allowMultiQueries=true");
            dataSource.setUsername(mysql.getUsername());
            dataSource.setPassword(mysql.getPassword());

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("init-test-db.sql"));
            populator.execute(dataSource);
        } catch (Exception e) {
            throw new RuntimeException("Test DB Init failed", e);
        }
    }

    @Test
    void kafkaConsume_updatesInventory_andLogs_thenCommitsOffset() {
        // Arrange: seed book and inventory
        Book book = new Book();
        book.setBookId("inv_1");
        book.setAuthor("Test");
        book.setStatus("Active");
        book.setTitle("Inventory Book");
        bookRepository.save(book);

        BookInventory inv = new BookInventory();
        inv.setBookId(book.getBookId());
        inv.setQuantity(5);
        inv.setStatus("ACTIVE");
        inventoryRepository.save(inv);

        // Produce inventory update
        Producer<String, String> producer = new KafkaProducer<>(producerProps());
        producer.send(new ProducerRecord<>("inventory_updates", book.getBookId(), "{\"bookId\":\"" + book.getBookId() + "\",\"quantity\":3}"));
        producer.flush();

        // Assert inventory changed and log inserted
        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            BookInventory updated = inventoryRepository.findByBookId(book.getBookId()).orElseThrow();
            assertThat(updated.getQuantity()).isEqualTo(3);
            assertThat(logRepository.findAll()).isNotEmpty();
        });

        // Verify consumer committed offset by consuming with a new group expecting no records
        Consumer<String, String> consumer = new KafkaConsumer<>(consumerProps());
        consumer.subscribe(Collections.singletonList("inventory_updates"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        assertThat(records.count()).isZero();
        consumer.close();
        producer.close();
    }

    private Properties producerProps() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

    private Properties consumerProps() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-offset-checker");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}

