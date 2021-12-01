




package com.asset.migration.datasource;

import com.zaxxer.hikari.HikariDataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(basePackages = "com.asset.migration", entityManagerFactoryRef = "secondEntityManager", transactionManagerRef = "secondTransactionManager")
public class SecondaryDB {

    @Autowired
    Environment env;

    @Bean
    @ConfigurationProperties(prefix = "spring.second.datasource")
    public DataSourceProperties primeDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource secondDataSource() {
        return primeDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean secondEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

        String dataSource = env.getProperty("secondary.datasource");
        if (dataSource.toLowerCase().equals("oracle")) {
            em.setDataSource(oracleDataSource());
        }else {
            em.setDataSource(secondDataSource());
        }

        em.setPackagesToScan("com.asset.migration");
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        properties.put("hibernate.dialect", env.getProperty("hibernate.secondary.dialect"));
        String schema = env.getProperty("spring.second.jpa.properties.hibernate.default.schema");
        if(schema != null && !schema.isEmpty()) properties.put("hibernate.default_schema", schema );
        return em;
    }

    @Bean
    public PlatformTransactionManager secondTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(secondEntityManager().getObject());
        return transactionManager;
    }

    private DataSource oracleDataSource() {
        OracleDataSource dataSource = null;
        try {
            dataSource = new OracleDataSource();
            dataSource.setDriverType("thin");
            dataSource.setServiceName("orcl");
            dataSource.setServerName("172.16.30.39");
            dataSource.setNetworkProtocol("tcp");
            dataSource.setPortNumber(1521);
            dataSource.setUser("ARROW_MFA");
            dataSource.setDatabaseName("ARROW_MFA");
            dataSource.setPassword("arrowDBmfa");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataSource;
    }

}
