package ru.bstrdn.report.gate.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.bstrdn.report.gate.configurations.GateUsersDataSource;
import ru.bstrdn.report.postgres.model.GateUser;

import java.util.List;

@Repository
public class GateRepository {


    private static final RowMapper<GateUser> ROW_MAPPER = BeanPropertyRowMapper.newInstance(GateUser.class);
    private static String connUrl = "jdbc:ucanaccess://C:/GATE/Server/config.mdb";

    public List<GateUser> getGateUsers() {

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            JdbcTemplate jdbcTemplate = new JdbcTemplate(new GateUsersDataSource(connUrl));
            return jdbcTemplate.query("SELECT * FROM Users", ROW_MAPPER);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
