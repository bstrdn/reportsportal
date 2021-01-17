package ru.bstrdn.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.bstrdn.report.repository.JpaUserRepository;

@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = JpaUserRepository.class)
public class ReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportApplication.class, args);
    }
}
