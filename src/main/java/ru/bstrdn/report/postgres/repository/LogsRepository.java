package ru.bstrdn.report.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bstrdn.report.postgres.model.Log;

import java.util.List;

@Repository
public interface LogsRepository extends JpaRepository<Log, Integer> {
 public List<Log> getAllByFioOrderByDateDesc(String fio);
}
