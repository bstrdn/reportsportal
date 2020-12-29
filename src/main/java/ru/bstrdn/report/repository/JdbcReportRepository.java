package ru.bstrdn.report.repository;

import org.slf4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.bstrdn.report.model.Report_1;

import java.util.List;

@Repository
//@Transactional(readOnly = true)
public class JdbcReportRepository {

    private static final RowMapper<Report_1> ROW_MAPPER = BeanPropertyRowMapper.newInstance(Report_1.class);
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JdbcReportRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

//    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


//    @Override
//    public Meal get(int id, int userId) {
//        List<Meal> meals = jdbcTemplate.query(
//                "SELECT * FROM meals WHERE id = ? AND user_id = ?", ROW_MAPPER, id, userId);
//        return DataAccessUtils.singleResult(meals);
//    }
//
//    @Override
//    public List<Meal> getAll(int userId) {
//        return jdbcTemplate.query(
//                "SELECT * FROM meals WHERE user_id=? ORDER BY date_time DESC", ROW_MAPPER, userId);
//    }

    List<Report_1> getUser() {
        return null;
    }

    //выполнение запроса в базу
    public void Test1() {
        log.debug("start test");
//        jdbcTemplate.update("insert into TEST (TESTFILD) values ( 'test1' )");
    }

    //маппинг юзеров
    public void Test2() {
        log.debug("start test");
        List<Report_1> list = jdbcTemplate.query("select * from USER", ROW_MAPPER);
        System.out.println(list);
    }

    public List<Report_1> queryReport_1(String fromDate, String toDate) {
        log.debug("start report 1");
        return jdbcTemplate.query("""
                SELECT
                cl.fullname,
                s.fixdate,
                s.workdate,
                doc.fullname docFullname
                --s.clvisit  --Проверка статуса отметки о посещении
                FROM schedule s
                INNER JOIN clients cl ON s.pcode = cl.pcode
                LEFT JOIN doctor doc ON s.dcode = doc.dcode
                WHERE s.status = 1 --Статус назначения "Первичный"
                AND s.fixdate BETWEEN ? AND ?
                --AND s.clvisit = 1 --Отрабатывает с галкой "Пришли на прием"
                AND  (s.clvisit IS NULL OR s.clvisit != 1)   --Отрабатывает с галкой "НЕ пришли на прием"
                ORDER BY s.workdate DESC--cl.fullname;
                """, ROW_MAPPER, fromDate, toDate);
    }


}
