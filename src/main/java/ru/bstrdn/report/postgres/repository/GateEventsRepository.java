package ru.bstrdn.report.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bstrdn.report.postgres.model.GateEvents;
import ru.bstrdn.report.postgres.model.GateUser;

@Repository
public interface GateEventsRepository extends JpaRepository<GateEvents, Integer> {

}
