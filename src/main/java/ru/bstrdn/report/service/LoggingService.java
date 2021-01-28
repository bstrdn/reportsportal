package ru.bstrdn.report.service;

import org.springframework.stereotype.Service;
import ru.bstrdn.report.postgres.model.Log;
import ru.bstrdn.report.postgres.repository.LogsRepository;

import java.util.List;

@Service
public class LoggingService {

    private final LogsRepository logsRepository;

    public LoggingService(LogsRepository logsRepository) {
        this.logsRepository = logsRepository;
    }

    public void save(Log log) {
        logsRepository.save(log);
    }

    public List<Log> getAll() {
        return logsRepository.findAll();
    }

    public List<Log> getAllByFio(String fio) {
        return logsRepository.getAllByFioOrderByDateDesc(fio);
    }
}
