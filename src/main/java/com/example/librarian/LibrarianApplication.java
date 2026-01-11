package com.example.librarian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Librarian Service.
 * * Auto-configurations for DataSource, TransactionManager, and JPA are excluded
 * because we provide manual configurations in LibbookDataSourceConfig and
 * LibdemoDataSourceConfig to handle the multi-database setup.
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@EnableScheduling
public class LibrarianApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibrarianApplication.class, args);
    }
}