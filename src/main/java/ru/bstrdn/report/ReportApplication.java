package ru.bstrdn.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.bstrdn.report.repository.JdbcReportRepository;
import ru.bstrdn.report.repository.JdbcUserRepository;

import javax.sql.DataSource;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class ReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportApplication.class, args);

    }

//    @Bean
//    @ConfigurationProperties(prefix="datasource.db-users")
//    public DataSource usersDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean
////    @Primary
//    @ConfigurationProperties(prefix="datasource.db-report")
//    public DataSource reportDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean
//    public JdbcTemplate usersJdbcTemplate(){
//        return new JdbcTemplate(usersDataSource());
//    }
//
//    @Bean
////    @Primary
//    public JdbcTemplate reportJdbcTemplate(){
//        return new JdbcTemplate(reportDataSource());
//    }

//    @Bean
//    public JdbcReportRepository jdbcPersonRepository() {
//        return new JdbcReportRepository(reportJdbcTemplate());
//    }
////
//    @Bean
//    public JdbcUserRepository jdbcUserRepository() {
//        return new JdbcUserRepository(usersJdbcTemplate());
//    }
}
