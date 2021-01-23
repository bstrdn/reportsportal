package ru.bstrdn.report.h2.repository;

import org.springframework.data.repository.CrudRepository;
import ru.bstrdn.report.h2.model.User2Test;

import java.util.Optional;

public interface UserH2RepositoryTest extends CrudRepository<User2Test, Long> {
    Optional<User2Test> findByDescription(String description);
}