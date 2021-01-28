package ru.bstrdn.report.postgres.repository;

import org.springframework.data.repository.CrudRepository;
import ru.bstrdn.report.postgres.model.User2Test;

import java.util.Optional;

public interface UserPostgresRepositoryTest extends CrudRepository<User2Test, Long> {
    Optional<User2Test> findByDescription(String description);
}