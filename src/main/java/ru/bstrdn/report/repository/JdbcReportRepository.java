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
    static final String FIO_PATTERN = "(\\S+\\s)(\\S{1})\\S+\\s(\\S{1})\\S+";

    private static final RowMapper<Report_1> ROW_MAPPER = BeanPropertyRowMapper.newInstance(Report_1.class);
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JdbcReportRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

//    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    //ПЕРВИЧНЫЕ ПАЦИЕНТЫ (ВСЕ) не уникальные пациенты
    public List<Report_1> queryReport_1(String fromDate, String toDate, String radio, Integer department, Integer registrar) {
        log.debug("start report 1");
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("""
                SELECT
                cl.fullname,
                cl.phone1,
                SUBSTRING (s.createdate FROM 1 FOR 10) fixdate,
                SUBSTRING (s.workdate FROM 1 FOR 10) workdate,
                doc.fullname docFullname
                FROM schedule s
                JOIN clients cl ON s.pcode = cl.pcode
                LEFT JOIN doctor reg ON s.uid = reg.dcode
                LEFT JOIN doctor doc ON s.dcode = doc.dcode
                """);

        if (radio.equals("4")) {
            stringBuilder.append("""
                    JOIN (SELECT
                        DISTINCT (cl.fullname) AS temp_name
                        FROM orderdet o
                        JOIN clients cl ON cl.pcode = o.pcode
                        JOIN wschema ws ON o.schcode = ws.schid
                        WHERE ws.speccode NOT IN (10001002, 10001006)) temp ON cl.fullname = temp.temp_name
                    """);
        }

        stringBuilder.append("""
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

        List<Report_1> report_1 = jdbcTemplate.query(stringBuilder.append("ORDER BY cl.fullname DESC;").toString(), ROW_MAPPER, fromDate, toDate);
        report_1.forEach(r -> r.setDocFullname(r.getDocFullname().replaceAll(FIO_PATTERN, "$1$2. $3.")));
        return report_1;
    }

    //регистраторы
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

    //департамент
    public List<Map<String, Object>> getAllDepartmentWithId() {
        List<Map<String, Object>> map;
        map = jdbcTemplate.queryForList("""
                SELECT depnum, depname
                FROM departments
                WHERE depnum NOT IN (10001542, 10001020, 10001024)
                ORDER BY depname
                """);
        return map;
    }

    //список юзеров для входа в систему
    public List<String> getAllUsers(){
        return jdbcTemplate.queryForList("""
                SELECT
                doc.dname                                
                FROM doctor  doc
                WHERE doc.doctcode IS NOT NULL
                ORDER BY doc.dname
                """, String.class);
    }
}
