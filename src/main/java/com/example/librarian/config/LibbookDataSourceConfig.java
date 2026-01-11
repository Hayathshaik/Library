package com.example.librarian.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.librarian.repository.libbook",
    entityManagerFactoryRef = "libbookEntityManagerFactory",
    transactionManagerRef = "libbookTransactionManager"
)
public class LibbookDataSourceConfig {
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.libbook")
    public DataSourceProperties libbookDataSourceProperties() {
        return new DataSourceProperties();
    }

//    @Bean
//    @Primary
//    public DataSource libbookDataSource() {
//        return libbookDataSourceProperties().initializeDataSourceBuilder().build();
//    }

//    @Bean
//    @Primary
//    public DataSource libbookDataSource() {
//        DataSourceProperties props = libbookDataSourceProperties();
//        return props.initializeDataSourceBuilder()
//                .username(props.getUsername()) // Explicitly set username
//                .password(props.getPassword()) // Explicitly set password
//                .build();
//    }


    @Bean
    @Primary // only for libbook
    public DataSource libbookDataSource() {
        DataSourceProperties props = libbookDataSourceProperties();
        return props.initializeDataSourceBuilder()
                .type(com.zaxxer.hikari.HikariDataSource.class) // Force Hikari
                .url(props.getUrl())
                .username("root")
                .password("Mysql")
                .build();
    }
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean libbookEntityManagerFactory(
            @Qualifier("libbookDataSource") DataSource dataSource,
            @Qualifier("libbookDataSourceProperties") DataSourceProperties dsProps) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.librarian.entity.libbook"); // Only scan libbook entities
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setPersistenceProviderClass(HibernatePersistenceProvider.class); // Explicitly set provider
        emf.setPersistenceUnitName("libbookPU");
//        java.util.Map<String, Object> jpaProps = new java.util.HashMap<>();
//        jpaProps.put("hibernate.default_schema", "libbook");
//        jpaProps.put("hibernate.default_catalog", "libbook");
//        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        // Inside libbookEntityManagerFactory method
        java.util.Map<String, Object> jpaProps = new java.util.HashMap<>();
        jpaProps.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProps.put("hibernate.hbm2ddl.auto", "none"); // <--- ADD THIS LINE
        jpaProps.put("hibernate.default_schema", "libbook");
        jpaProps.put("hibernate.default_catalog", "libbook");
        // Ensure Hibernate has the JDBC URL (helps dialect/metadata detection)
        if (dsProps != null && dsProps.getUrl() != null) {
            jpaProps.put("jakarta.persistence.jdbc.url", dsProps.getUrl());
            jpaProps.put("jakarta.persistence.jdbc.user", dsProps.getUsername());
            jpaProps.put("jakarta.persistence.jdbc.password", dsProps.getPassword());
        }
        emf.setJpaPropertyMap(jpaProps);
        return emf;
    }

    @Bean
    @Primary
    public PlatformTransactionManager libbookTransactionManager(
            @Qualifier("libbookEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public org.springframework.boot.CommandLineRunner logLibbookDataSourceUrl(DataSourceProperties props) {
        return args -> System.out.println("[DEBUG] libbook.datasource.url=" + props.getUrl());
    }
}
