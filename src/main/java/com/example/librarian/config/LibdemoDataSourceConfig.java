package com.example.librarian.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.librarian.repository.libdemo",
        entityManagerFactoryRef = "libdemoEntityManagerFactory",
        transactionManagerRef = "libdemoTransactionManager"
)
public class LibdemoDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.libdemo")
    public DataSourceProperties libdemoDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource libdemoDataSource() {
        DataSourceProperties props = libdemoDataSourceProperties();
        return props.initializeDataSourceBuilder()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .url(props.getUrl())
                .username("root")
                .password("Mysql")
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean libdemoEntityManagerFactory(
            @Qualifier("libdemoDataSource") DataSource dataSource,
            @Qualifier("libdemoDataSourceProperties") DataSourceProperties dsProps) {

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.librarian.entity.libdemo");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        emf.setPersistenceUnitName("libdemoPU");

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProps.put("hibernate.hbm2ddl.auto", "none");

        // --- CRITICAL ADDITION: Ensure naming matches Libbook exactly ---
        jpaProps.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");

        // Use the connection properties for metadata detection
        if (dsProps != null && dsProps.getUrl() != null) {
            jpaProps.put("jakarta.persistence.jdbc.url", dsProps.getUrl());
            jpaProps.put("jakarta.persistence.jdbc.user", dsProps.getUsername());
            jpaProps.put("jakarta.persistence.jdbc.password", dsProps.getPassword());
        }

        emf.setJpaPropertyMap(jpaProps);
        return emf;
    }

    @Bean
    public PlatformTransactionManager libdemoTransactionManager(
            @Qualifier("libdemoEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}