package ru.bstrdn.report.postgres.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@Transactional(readOnly = true)
public class JdbcSkudRepository {

    @Autowired
    @Qualifier(value = "postgresJdbc")
    JdbcTemplate postgresJdbc;


    //Список пользователей СКУД
    public List<Map<String, Object>> getAllSkudUsers(String groupNum, String accessLevel) {

        return postgresJdbc.queryForList("""
                SELECT
                gu.lastname || ' ' || SUBSTRING (gu.firstname FROM 1 FOR 1) || '. ' || SUBSTRING (fathername FROM 1 FOR 1) || '. ' || gu.number FIO,
                gu.userptr id
                FROM gate_users gu
                WHERE gu.groupptr = ?
                AND gu.details8 = ?
                """, groupNum, accessLevel);
    }


}

