package ru.bstrdn.report.repository;

import org.slf4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.bstrdn.report.model.Report_1;

import java.util.List;
import java.util.Map;

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

//    List<Report_1> getUser() {
//        return null;
//    }

    //выполнение запроса в базу
    public void Test1() {
        log.debug("start test");
//        jdbcTemplate.update("insert into TEST (TESTFILD) values ( 'test1' )");
    }

    //маппинг юзеров
//    public void Test2() {
//        log.debug("start test");
//        List<Report_1> list = jdbcTemplate.query("select * from USER", ROW_MAPPER);
//        System.out.println(list);
//    }

    //ПЕРВИЧНЫЕ ПАЦИЕНТЫ (ВСЕ) не уникальные пациенты
    public List<Report_1> queryReport_1(String fromDate, String toDate, String radio, Integer department, Integer registrar) {
        log.debug("start report 1");
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("""
                SELECT
                cl.fullname,
                SUBSTRING (s.createdate FROM 1 FOR 10) fixdate,
                SUBSTRING (s.workdate FROM 1 FOR 10) workdate,
                doc.fullname docFullname
                FROM schedule s
                JOIN clients cl ON s.pcode = cl.pcode
                LEFT JOIN doctor reg ON s.uid = reg.dcode
                LEFT JOIN doctor doc ON s.dcode = doc.dcode
                WHERE s.status = 1 --Статус назначения "Первичный"
                AND doc.fullname IS NOT NULL
                AND doc.depnum != 10001020
                AND s.createdate BETWEEN ? AND ? 

                """);

        //фильтр по отделению
        if (department > 0) {
            stringBuilder.append(String.format(" AND doc.depnum = %d", department));
        }

        //фильтр по ФИО регистратора
        if (registrar > 0) {
            log.debug("id " + registrar);
            stringBuilder.append(String.format(" AND reg.dcode = %d", registrar));
        }

        //пришли на прием
        if (radio.equals("2")) {
            stringBuilder.append("""
                     AND s.clvisit = 1 
                    """);
        }

        //НЕ пришли на прием
        if (radio.equals("3")) {
            stringBuilder.append("""
                    AND  (s.clvisit IS NULL OR s.clvisit != 1) 
                    """);
        }

        return jdbcTemplate.query(stringBuilder.append("ORDER BY cl.fullname DESC;").toString(), ROW_MAPPER, fromDate, toDate);
    }


    public List<String> getAllRegistrar() {
        return jdbcTemplate.queryForList("""
                SELECT dname
                FROM doctor
                WHERE stdtype = 1
                order by dname
                """, String.class);
    }


    public List<Map<String, Object>> getAllRegistrarWithId() {
        List<Map<String, Object>> map;
        map = jdbcTemplate.queryForList("""
                        SELECT dcode, dname
                        FROM doctor
                        WHERE stdtype = 1
                        order by dname
                """);
        return map;
    }

    public List<String> getAllDepartment() {
        return jdbcTemplate.queryForList("""
                SELECT depname
                FROM departments
                WHERE depnum NOT IN (10001542)
                AND depnum != 10001020
                ORDER BY depname
                """, String.class);
    }

    public List<Map<String, Object>> getAllDepartmentWithId() {
        List<Map<String, Object>> map;
        map = jdbcTemplate.queryForList("""
                        SELECT depnum, depname
                        FROM departments
                        WHERE depnum NOT IN (10001542)
                        AND depnum != 10001020
                        ORDER BY depname
                """);
        return map;
    }
}
