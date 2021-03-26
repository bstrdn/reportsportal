package ru.bstrdn.report.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bstrdn.report.postgres.model.ActAcc;
import ru.bstrdn.report.postgres.model.GateEvents;

@Repository
public interface ActAccRepository extends JpaRepository<ActAcc, Integer> {

}
