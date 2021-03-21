package ru.bstrdn.report.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bstrdn.report.postgres.model.Skud3;

import java.util.Date;

@Repository
public interface TmpIdentRepository extends JpaRepository<Skud3, Date> {

}
