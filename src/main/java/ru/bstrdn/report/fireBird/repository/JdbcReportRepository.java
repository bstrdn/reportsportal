package ru.bstrdn.report.fireBird.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.bstrdn.report.fireBird.model.Report_1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * ОТЧЕТЫ ДЛЯ СТОМАТОЛОГИИ:
 * 1. Первичные пациенты по дате обращения {@link #queryReport_1}
 * 2. Первичные пациенты по дате записи  {@link #queryReport_2}
 */

@Slf4j
@Repository
@Transactional(readOnly = true)
public class JdbcReportRepository {
    static final String FIO_PATTERN = "(\\S+\\s)(\\S{1})\\S+\\s(\\S{1})\\S+";

    private static final RowMapper<Report_1> ROW_MAPPER_1 = BeanPropertyRowMapper.newInstance(Report_1.class);
    private final JdbcTemplate jdbcTemplate;


    public JdbcReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    /** ОТЧЕТ ДЛЯ СТОМАТОЛОГИИ **
     * 1. Первичные пациенты по дате обращения
     */
    //ПЕРВИЧНЫЕ ПАЦИЕНТЫ (ВСЕ) не уникальные пациенты
    public List<Report_1> queryReport_1(String fromDate, String toDate, String radio, Integer department, Integer registrar, Integer filter_combine) {
        StringBuilder stringBuilder = new StringBuilder();

        if (filter_combine == 1) {
            stringBuilder.append("""
                    SELECT
                    cl.fullname,
                    cl.phone1,
                    IIF (EXTRACT (DAY FROM s.createdate) < 10, '0' || EXTRACT (DAY FROM s.createdate) || '.', EXTRACT (DAY FROM s.createdate) || '.') ||
                    IIF (EXTRACT (MONTH FROM s.createdate) < 10, '0' || EXTRACT (MONTH FROM s.createdate) || '.', EXTRACT (MONTH FROM s.createdate) || '.') ||
                    EXTRACT (YEAR FROM s.createdate) createdate,
                    IIF (EXTRACT (DAY FROM s.workdate) < 10, '0' || EXTRACT (DAY FROM s.workdate) || '.', EXTRACT (DAY FROM s.workdate) || '.') ||
                    IIF (EXTRACT (MONTH FROM s.workdate) < 10, '0' || EXTRACT (MONTH FROM s.workdate) || '.', EXTRACT (MONTH FROM s.workdate) || '.') ||
                    EXTRACT (YEAR FROM s.workdate) workdate,
                    doc.ntuser docFullname
                    """);
        }
        if (filter_combine == 2) {
            stringBuilder.append("""
                    SELECT
                    DISTINCT (cl.fullname) fullname,
                    cl.phone1,
                    LIST (IIF (EXTRACT (DAY FROM s.createdate) < 10, '0' || EXTRACT (DAY FROM s.createdate) || '.', EXTRACT (DAY FROM s.createdate) || '.') ||
                    IIF (EXTRACT (MONTH FROM s.createdate) < 10, '0' || EXTRACT (MONTH FROM s.createdate) || '.', EXTRACT (MONTH FROM s.createdate) || '.') ||
                    EXTRACT (YEAR FROM s.createdate), '; ') createdate,
                    LIST (IIF (EXTRACT (DAY FROM s.workdate) < 10, '0' || EXTRACT (DAY FROM s.workdate) || '.', EXTRACT (DAY FROM s.workdate) || '.') ||
                    IIF (EXTRACT (MONTH FROM s.workdate) < 10, '0' || EXTRACT (MONTH FROM s.workdate) || '.', EXTRACT (MONTH FROM s.workdate) || '.') ||
                    EXTRACT (YEAR FROM s.workdate), '; ') workdate,
                    LIST (doc.ntuser, '; ') docFullname
                    """);
        }

        stringBuilder.append("""
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
                AND doc.depnum != 10001020 --Отделение доктора не равно рентгену
                AND reg.stdtype = 1 --Запись в расписание внес "Регистратор" а не доктор
                AND s.createdate BETWEEN ? AND ? 

                """);

        //Фильтр по департаменту и регистратору
        departmentFilter(stringBuilder, department, registrar);

        //Пришли на прием
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

        if (filter_combine == 2) {
            stringBuilder.append("""
                    GROUP BY fullname, cl.phone1
                    """);
        }

        List<Report_1> report_1 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_1, fromDate, toDate);
        return report_1;
    }

    /** ОТЧЕТ ДЛЯ СТОМАТОЛОГИИ **
     * 2. Первичные пациенты по дате записи
     */
    public List<Report_1> queryReport_2(String fromDate, String toDate, String radio, Integer department, Integer registrar, Integer filter_combine) {

        List<Report_1> report_2 = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();

        if (filter_combine == 1 && radio.equals("1")) {
            stringBuilder.append("""
                    SELECT
                    cl.fullname,
                    cl.phone1,
                    IIF (EXTRACT (DAY FROM s.createdate) < 10, '0' || EXTRACT (DAY FROM s.createdate) || '.', EXTRACT (DAY FROM s.createdate) || '.') ||
                    IIF (EXTRACT (MONTH FROM s.createdate) < 10, '0' || EXTRACT (MONTH FROM s.createdate) || '.', EXTRACT (MONTH FROM s.createdate) || '.') ||
                    EXTRACT (YEAR FROM s.createdate) createdate,
                    IIF (EXTRACT (DAY FROM s.workdate) < 10, '0' || EXTRACT (DAY FROM s.workdate) || '.', EXTRACT (DAY FROM s.workdate) || '.') ||
                    IIF (EXTRACT (MONTH FROM s.workdate) < 10, '0' || EXTRACT (MONTH FROM s.workdate) || '.', EXTRACT (MONTH FROM s.workdate) || '.') ||
                    EXTRACT (YEAR FROM s.workdate) workdate,
                    doc.NTUSER docFullname
                    FROM schedule s
                    JOIN clients cl ON s.pcode = cl.pcode
                    LEFT JOIN doctor reg ON s.uid = reg.dcode
                    LEFT JOIN doctor doc ON s.dcode = doc.dcode
                    WHERE s.status = 1 --Статус назначения "Первичный"
                    AND doc.fullname IS NOT NULL
                    AND doc.depnum != 10001020
                    AND s.workdate BETWEEN ? AND ?
                    AND reg.stdtype = 1
                                    """);
            departmentFilter(stringBuilder, department, registrar);
            report_2 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_1, fromDate, toDate);
        }

        if (filter_combine == 2 && radio.equals("1")) {
            stringBuilder.append("""
                    SELECT
                    DISTINCT (cl.fullname) fullname,
                    cl.phone1,
                    LIST (IIF (EXTRACT (DAY FROM s.createdate) < 10, '0' || EXTRACT (DAY FROM s.createdate) || '.', EXTRACT (DAY FROM s.createdate) || '.') ||
                    IIF (EXTRACT (MONTH FROM s.createdate) < 10, '0' || EXTRACT (MONTH FROM s.createdate) || '.', EXTRACT (MONTH FROM s.createdate) || '.') ||
                    EXTRACT (YEAR FROM s.createdate), '; ') createdate,
                    LIST (IIF (EXTRACT (DAY FROM s.workdate) < 10, '0' || EXTRACT (DAY FROM s.workdate) || '.', EXTRACT (DAY FROM s.workdate) || '.') ||
                    IIF (EXTRACT (MONTH FROM s.workdate) < 10, '0' || EXTRACT (MONTH FROM s.workdate) || '.', EXTRACT (MONTH FROM s.workdate) || '.') ||
                    EXTRACT (YEAR FROM s.workdate), '; ') workdate,
                    LIST (doc.NTUSER, '; ') docFullname
                    FROM schedule s
                    JOIN clients cl ON s.pcode = cl.pcode
                    LEFT JOIN doctor reg ON s.uid = reg.dcode
                    LEFT JOIN doctor doc ON s.dcode = doc.dcode
                    WHERE s.status = 1 --Статус назначения "Первичный"
                    AND doc.fullname IS NOT NULL
                    AND doc.depnum != 10001020
                    AND s.workdate BETWEEN ? AND ?
                    AND reg.stdtype = 1
                    GROUP BY fullname, cl.phone1
                                    """);
            departmentFilter(stringBuilder, department, registrar);
            report_2 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_1, fromDate, toDate);
        }


        if (radio.equals("2")) {
            if (filter_combine == 1) {
                stringBuilder.append("""
                        SELECT
                        tmp_res_1.tmp_name_1 fullname,
                        IIF (EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) < 10, '0' || EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) || '.', EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) || '.') ||
                        IIF (EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) < 10, '0' || EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) || '.', EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) || '.') ||
                        EXTRACT (YEAR FROM tmp_res_1.tmp_creat_date) createdate,
                        IIF (EXTRACT (DAY FROM tmp_res_1.tmp_work_date) < 10, '0' || EXTRACT (DAY FROM tmp_res_1.tmp_work_date) || '.', EXTRACT (DAY FROM tmp_res_1.tmp_work_date) || '.') ||
                        IIF (EXTRACT (MONTH FROM tmp_res_1.tmp_work_date) < 10, '0' || EXTRACT (MONTH FROM tmp_res_1.tmp_work_date) || '.', EXTRACT (MONTH FROM tmp_res_1.tmp_work_date) || '.') ||
                        EXTRACT (YEAR FROM tmp_res_1.tmp_work_date) workdate,
                        tmp_res_1.tmp_doc_fullname docFullname,
                        tmp_res_1.tmp_phone phone1
                        """);
            }

            if (filter_combine == 2) {
                stringBuilder.append("""
                        SELECT
                        DISTINCT (tmp_res_1.tmp_name_1) fullname,
                        LIST (IIF (EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) < 10, '0' || EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) || '.', EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) || '.') ||
                        IIF (EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) < 10, '0' || EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) || '.', EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) || '.') ||
                        EXTRACT (YEAR FROM tmp_res_1.tmp_creat_date), '; ') createdate,
                        LIST (IIF (EXTRACT (DAY FROM tmp_res_1.tmp_work_date) < 10, '0' || EXTRACT (DAY FROM tmp_res_1.tmp_work_date) || '.', EXTRACT (DAY FROM tmp_res_1.tmp_work_date) || '.') ||
                        IIF (EXTRACT (MONTH FROM tmp_res_1.tmp_work_date) < 10, '0' || EXTRACT (MONTH FROM tmp_res_1.tmp_work_date) || '.', EXTRACT (MONTH FROM tmp_res_1.tmp_work_date) || '.') ||
                        EXTRACT (YEAR FROM tmp_res_1.tmp_work_date), '; ') workdate,
                        LIST (tmp_res_1.tmp_doc_fullname, '; ') docFullname,
                        tmp_res_1.tmp_phone phone1
                        """);
            }
            stringBuilder.append("""
                    FROM(SELECT  --Таблица всех НЕ пришедших (хоть один раз за период)
                                        cl.fullname AS tmp_name_1,
                                        s.createdate AS tmp_creat_date,
                                        s.workdate AS tmp_work_date,
                                        doc.ntuser AS tmp_doc_fullname,
                                        cl.phone1 AS tmp_phone
                                                
                                        FROM schedule s
                                        JOIN clients cl ON s.pcode = cl.pcode
                                        LEFT JOIN doctor reg ON s.uid = reg.dcode
                                        LEFT JOIN doctor doc ON s.dcode = doc.dcode
                                                
                                        WHERE s.status = 1 --Статус назначения "Первичный" 
                                        AND doc.fullname IS NOT NULL
                                        AND doc.depnum != 10001020
                                        AND s.workdate BETWEEN ? AND ?
                                        AND  (s.clvisit IS NULL OR s.clvisit != 1)
                                        AND reg.stdtype = 1
                    """);
            departmentFilter(stringBuilder, department, registrar);
            stringBuilder.append(
                    """
                            AND cl.pcode NOT IN (SELECT    --Таблица пришедших (хоть один раз за период)
                                                    cl1.pcode
                                    
                                                    FROM schedule s1
                                                    LEFT JOIN clients cl1 ON s1.pcode = cl1.pcode
                                                    LEFT JOIN doctor reg1 ON s1.uid = reg1.dcode
                                                    LEFT JOIN doctor doc1 ON s1.dcode = doc1.dcode
                                                    WHERE s1.status = 1 --Статус назначения "Первичный"
                                                    AND doc1.fullname IS NOT NULL
                                                    AND doc1.depnum != 10001020
                                                    AND s1.workdate BETWEEN ? AND ?
                                                    AND s1.clvisit = 1
                                                    AND reg1.stdtype = 1
                                                    ORDER BY cl1.fullname)) tmp_res_1
                            """);
            if (filter_combine == 2) {
                stringBuilder.append("GROUP BY fullname, phone1");
            }
            report_2 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_1, fromDate, toDate, fromDate, toDate);
        }

        //Таблица, записанных пациентов на будущее
        if (radio.equals("3")) {
            if (filter_combine == 1) {
                stringBuilder.append("""
                        SELECT 
                        cl.fullname,
                        IIF (EXTRACT (DAY FROM s.createdate) < 10, '0' || EXTRACT (DAY FROM s.createdate) || '.', EXTRACT (DAY FROM s.createdate) || '.') ||
                        IIF (EXTRACT (MONTH FROM s.createdate) < 10, '0' || EXTRACT (MONTH FROM s.createdate) || '.', EXTRACT (MONTH FROM s.createdate) || '.') ||
                        EXTRACT (YEAR FROM s.createdate) createdate,
                        IIF (EXTRACT (DAY FROM s.workdate) < 10, '0' || EXTRACT (DAY FROM s.workdate) || '.', EXTRACT (DAY FROM s.workdate) || '.') ||
                        IIF (EXTRACT (MONTH FROM s.workdate) < 10, '0' || EXTRACT (MONTH FROM s.workdate) || '.', EXTRACT (MONTH FROM s.workdate) || '.') ||
                        EXTRACT (YEAR FROM s.workdate) workdate,
                        doc.ntuser docFullname,
                        cl.phone1
                        """);
            }

            if (filter_combine == 2) {
                stringBuilder.append("""
                        SELECT
                        DISTINCT (cl.fullname) fullname,
                        LIST (IIF (EXTRACT (DAY FROM s.createdate) < 10, '0' || EXTRACT (DAY FROM s.createdate) || '.', EXTRACT (DAY FROM s.createdate) || '.') ||
                        IIF (EXTRACT (MONTH FROM s.createdate) < 10, '0' || EXTRACT (MONTH FROM s.createdate) || '.', EXTRACT (MONTH FROM s.createdate) || '.') ||
                        EXTRACT (YEAR FROM s.createdate), '; ') createdate,
                        LIST (IIF (EXTRACT (DAY FROM s.workdate) < 10, '0' || EXTRACT (DAY FROM s.workdate) || '.', EXTRACT (DAY FROM s.workdate) || '.') ||
                        IIF (EXTRACT (MONTH FROM s.workdate) < 10, '0' || EXTRACT (MONTH FROM s.workdate) || '.', EXTRACT (MONTH FROM s.workdate) || '.') ||
                        EXTRACT (YEAR FROM s.workdate), '; ') workdate,
                        LIST (doc.ntuser, '; ') docFullname,
                        cl.phone1
                        """);
            }
            stringBuilder.append("""
                    FROM schedule s
                                        JOIN clients cl ON s.pcode = cl.pcode
                                        LEFT JOIN doctor reg ON s.uid = reg.dcode
                                        LEFT JOIN doctor doc ON s.dcode = doc.dcode
                                                            
                                        WHERE s.status = 1 --Статус назначения "Первичный"
                                        AND doc.fullname IS NOT NULL
                                        AND doc.depnum != 10001020
                                        AND s.workdate > ?
                                        AND reg.stdtype = 1
                    """);
            departmentFilter(stringBuilder, department, registrar);
            stringBuilder.append(
                    """
                            AND cl.pcode IN (SELECT tmp_res_1.tmp_pcode_1 --, s.createdate, s.workdate, doc.fullname
                                                                  
                            FROM(SELECT  --Таблица всех НЕ пришедших (хоть один раз за период)
                            cl.fullname AS tmp_name_1,
                            cl.pcode AS tmp_pcode_1
                                                 
                            FROM schedule s
                            JOIN clients cl ON s.pcode = cl.pcode
                            LEFT JOIN doctor reg ON s.uid = reg.dcode
                            LEFT JOIN doctor doc ON s.dcode = doc.dcode
                                                 
                            WHERE s.status = 1 --Статус назначения "Первичный"
                            AND doc.fullname IS NOT NULL
                            AND doc.depnum != 10001020
                            AND s.workdate BETWEEN ? AND ?
                            AND  (s.clvisit IS NULL OR s.clvisit != 1)
                            AND reg.stdtype = 1
                            AND cl.pcode NOT IN (SELECT    --Таблица пришедших (хоть один раз за период)
                            cl1.pcode
                                                 
                            FROM schedule s1
                            LEFT JOIN clients cl1 ON s1.pcode = cl1.pcode
                            LEFT JOIN doctor reg1 ON s1.uid = reg1.dcode
                            LEFT JOIN doctor doc1 ON s1.dcode = doc1.dcode
                                                 
                            WHERE s1.status = 1 --Статус назначения "Первичный"
                            AND doc1.fullname IS NOT NULL
                            AND doc1.depnum != 10001020
                            AND s1.workdate BETWEEN ? AND ?
                            AND s1.clvisit = 1
                            AND reg1.stdtype = 1
                            ORDER BY cl1.fullname)) tmp_res_1)
                            """);
            if (filter_combine == 2) {
                stringBuilder.append("GROUP BY fullname, phone1");
            }
            report_2 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_1, toDate, fromDate, toDate, fromDate, toDate);
        }

        //НЕ_ПЕРЕЗАПИСАВШИЕСЯ
        if (radio.equals("4")) {
            if (filter_combine == 1) {

                stringBuilder.append("""
                        SELECT
                        tmp_res_1.tmp_name_1 fullname,
                        IIF (EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) < 10, '0' || EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) || '.', EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) || '.') ||
                        IIF (EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) < 10, '0' || EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) || '.', EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) || '.') ||
                        EXTRACT (YEAR FROM tmp_res_1.tmp_creat_date) createdate,
                        IIF (EXTRACT (DAY FROM tmp_res_1.tmp_workdate) < 10, '0' || EXTRACT (DAY FROM tmp_res_1.tmp_workdate) || '.', EXTRACT (DAY FROM tmp_res_1.tmp_workdate) || '.') ||
                        IIF (EXTRACT (MONTH FROM tmp_res_1.tmp_workdate) < 10, '0' || EXTRACT (MONTH FROM tmp_res_1.tmp_workdate) || '.', EXTRACT (MONTH FROM tmp_res_1.tmp_workdate) || '.') ||
                        EXTRACT (YEAR FROM tmp_res_1.tmp_workdate) workdate,
                        tmp_res_1.tmp_doc_fullname docFullname,
                        tmp_res_1.tmp_phone phone1
                        """);
            }
            if (filter_combine == 2) {

                stringBuilder.append("""
                        SELECT
                        DISTINCT (tmp_res_1.tmp_name_1) fullname,
                        LIST (IIF (EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) < 10, '0' || EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) || '.', EXTRACT (DAY FROM tmp_res_1.tmp_creat_date) || '.') ||
                        IIF (EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) < 10, '0' || EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) || '.', EXTRACT (MONTH FROM tmp_res_1.tmp_creat_date) || '.') ||
                        EXTRACT (YEAR FROM tmp_res_1.tmp_creat_date), '; ') createdate,
                        LIST (IIF (EXTRACT (DAY FROM tmp_res_1.tmp_workdate) < 10, '0' || EXTRACT (DAY FROM tmp_res_1.tmp_workdate) || '.', EXTRACT (DAY FROM tmp_res_1.tmp_workdate) || '.') ||
                        IIF (EXTRACT (MONTH FROM tmp_res_1.tmp_workdate) < 10, '0' || EXTRACT (MONTH FROM tmp_res_1.tmp_workdate) || '.', EXTRACT (MONTH FROM tmp_res_1.tmp_workdate) || '.') ||
                        EXTRACT (YEAR FROM tmp_res_1.tmp_workdate), '; ') workdate,
                        LIST (tmp_res_1.tmp_doc_fullname, '; ') docFullname,
                        tmp_res_1.tmp_phone phone1
                        """);
            }

            stringBuilder.append("""
                                        FROM(SELECT  --Таблица всех НЕ пришедших (хоть один раз за период)
                                        cl.fullname AS tmp_name_1,
                                        cl.pcode AS tmp_pcode_1,
                                        s.createdate tmp_creat_date,
                                        s.workdate tmp_workdate,
                                        doc.ntuser AS tmp_doc_fullname,
                                        cl.phone1 AS tmp_phone
                                                            
                                                            
                                        FROM schedule s
                                        JOIN clients cl ON s.pcode = cl.pcode
                                        LEFT JOIN doctor reg ON s.uid = reg.dcode
                                        LEFT JOIN doctor doc ON s.dcode = doc.dcode
                                                            
                                        WHERE s.status = 1 --Статус назначения "Первичный"
                                        AND doc.fullname IS NOT NULL
                                        AND doc.depnum != 10001020
                                        AND s.workdate BETWEEN ? AND ?
                                        AND  (s.clvisit IS NULL OR s.clvisit != 1)
                                        AND reg.stdtype = 1
                    """);
            departmentFilter(stringBuilder, department, registrar);
            stringBuilder.append(
                    """
                            AND cl.pcode NOT IN (SELECT --Таблица пришедших (хоть один раз за период)
                            cl1.pcode
                                                 
                            FROM schedule s1
                            LEFT JOIN clients cl1 ON s1.pcode = cl1.pcode
                            LEFT JOIN doctor reg1 ON s1.uid = reg1.dcode
                            LEFT JOIN doctor doc1 ON s1.dcode = doc1.dcode
                                                 
                            WHERE s1.status = 1 --Статус назначения "Первичный"
                            AND doc1.fullname IS NOT NULL
                            AND doc1.depnum != 10001020
                            AND s1.workdate BETWEEN ? AND ?
                            AND s1.clvisit = 1
                            AND reg1.stdtype = 1
                            ORDER BY cl1.fullname)) tmp_res_1
                            WHERE tmp_res_1.tmp_pcode_1 NOT IN (SELECT --Таблица, записанных пациентов на будущее
                            cl.pcode
                                                 
                            FROM schedule s
                            JOIN clients cl ON s.pcode = cl.pcode
                            LEFT JOIN doctor reg ON s.uid = reg.dcode
                            LEFT JOIN doctor doc ON s.dcode = doc.dcode
                                                 
                            WHERE s.status = 1 --Статус назначения "Первичный"
                            AND doc.fullname IS NOT NULL
                            AND doc.depnum != 10001020
                            AND s.workdate > ?
                            AND reg.stdtype = 1)
                            """);
            if (filter_combine == 2) {
                stringBuilder.append("GROUP BY fullname, phone1");
            }
            report_2 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_1, fromDate, toDate, fromDate, toDate, toDate);
        }

//        report_2.forEach(r -> r.setDocFullname(r.getDocFullname().replaceAll(FIO_PATTERN, "$1$2. $3.")));
        return report_2;
    }

    /**
     * @return Список регистраторов с ID
     */
    //регистраторы
    public List<Map<String, Object>> getAllRegistrarWithId() {
        List<Map<String, Object>> map;
        map = jdbcTemplate.queryForList("""
                        SELECT dcode, ntuser dname
                        FROM doctor
                        WHERE stdtype = 1
                        AND doctor.locked != 1
                        order by ntuser
                """);
        return map;
    }

    /**
     * @return Список департаментов с ID
     */
    //департамент
    public List<Map<String, Object>> getAllDepartmentWithId() {
        return jdbcTemplate.queryForList("""
                SELECT depnum, depname
                FROM departments
                WHERE depnum NOT IN (10001542, 10001020, 10001024)
                ORDER BY depname
                """);
    }

    /**
     * @return Список список всех юхеров для входа в систему
     */
    //список юзеров для входа в систему
    public List<String> getAllUsers() {
        return jdbcTemplate.queryForList("""
                SELECT
                doc.dname                                
                FROM doctor  doc
                WHERE doc.doctcode IS NOT NULL
                ORDER BY doc.dname
                """, String.class);
    }


    /**
     * Фильтр по отделению и ФИО регистратора
     */
    private void departmentFilter(StringBuilder stringBuilder, Integer department, Integer registrar) {
        //фильтр по отделению
        if (department > 0) {
            stringBuilder.append(String.format(" AND doc.depnum = %d", department));
        }
        //фильтр по ФИО регистратора
        if (registrar > 0) {
            stringBuilder.append(String.format(" AND reg.dcode = %d", registrar));
        }
    }
}

