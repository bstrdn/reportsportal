package ru.bstrdn.report.fireBird.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.bstrdn.report.fireBird.model.Report_1;
import ru.bstrdn.report.fireBird.model.Report_buh_1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@Transactional(readOnly = true)
public class JdbcReportRepository {
    static final String FIO_PATTERN = "(\\S+\\s)(\\S{1})\\S+\\s(\\S{1})\\S+";

    private static final RowMapper<Report_1> ROW_MAPPER_1 = BeanPropertyRowMapper.newInstance(Report_1.class);
    private static final RowMapper<Report_buh_1> ROW_MAPPER_BUH = BeanPropertyRowMapper.newInstance(Report_buh_1.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //ПЕРВИЧНЫЕ ПАЦИЕНТЫ (ВСЕ) не уникальные пациенты
    public List<Report_1> queryReport_1(String fromDate, String toDate, String radio, Integer department, Integer registrar) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("""
                SELECT
                cl.fullname,
                cl.phone1,
                SUBSTRING (s.createdate FROM 1 FOR 10) createdate,
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
                AND reg.stdtype = 1
                AND s.createdate BETWEEN ? AND ? 

                """);

        departmentFilter(stringBuilder, department, registrar);

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

        List<Report_1> report_1 = jdbcTemplate.query(stringBuilder.append("ORDER BY cl.fullname DESC;").toString(), ROW_MAPPER_1, fromDate, toDate);
        report_1.forEach(r -> r.setDocFullname(r.getDocFullname().replaceAll(FIO_PATTERN, "$1$2. $3.")));
        return report_1;
    }

    public List<Report_1> queryReport_2(String fromDate, String toDate, String radio, Integer department, Integer registrar) {

        List<Report_1> report_2 = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();

        if (radio.equals("1")) {
            stringBuilder.append("""
                    SELECT
                    cl.fullname,
                    cl.phone1,
                    SUBSTRING (s.createdate FROM 1 FOR 10) createdate,
                    SUBSTRING (s.workdate FROM 1 FOR 10) workdate,
                    doc.fullname docFullname
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

            stringBuilder.append(" ORDER BY cl.fullname DESC");
            report_2 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_1, fromDate, toDate);

        }

        if (radio.equals("2")) {
            stringBuilder.append("""
                    SELECT
                    tmp_res_1.tmp_name_1 fullname,
                    SUBSTRING (tmp_res_1.tmp_creat_date FROM 1 FOR 10) createdate,
                    SUBSTRING (tmp_res_1.tmp_work_date FROM 1 FOR 10) workdate,
                    tmp_res_1.tmp_doc_fullname docFullname,
                    tmp_res_1.tmp_phone phone1
                            
                    FROM(SELECT  --Таблица всех НЕ пришедших (хоть один раз за период)
                    cl.fullname AS tmp_name_1,
                    s.createdate AS tmp_creat_date,
                    s.workdate AS tmp_work_date,
                    doc.fullname AS tmp_doc_fullname,
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
                            ORDER BY  tmp_res_1.tmp_name_1
                            """);
            report_2 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_1, fromDate, toDate, fromDate, toDate);
        }

//Таблица, записанных пациентов на будущее
        if (radio.equals("3")) {
            stringBuilder.append("""
                    SELECT 
                    cl.fullname,
                    s.createdate,
                    s.workdate,
                    doc.fullname docFullname,
                    cl.phone1
                                        
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
                                                                  
                            ORDER BY cl.fullname
                                   """);
            report_2 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_1, toDate, fromDate, toDate, fromDate, toDate);
        }

        //НЕ_ПЕРЕЗАПИСАВШИЕСЯ
        if (radio.equals("4")) {
            stringBuilder.append("""
                    SELECT
                    tmp_res_1.tmp_name_1 fullname,
                    tmp_res_1.tmp_creat_date createdate,
                    tmp_res_1.tmp_workdate workdate,
                    tmp_res_1.tmp_doc_fullname docFullname,
                    tmp_res_1.tmp_phone phone1
                                        
                                        
                    FROM(SELECT  --Таблица всех НЕ пришедших (хоть один раз за период)
                    cl.fullname AS tmp_name_1,
                    cl.pcode AS tmp_pcode_1,
                    SUBSTRING (s.createdate FROM 1 FOR 10) tmp_creat_date,
                    SUBSTRING (s.workdate FROM 1 FOR 10) tmp_workdate,
                    doc.fullname AS tmp_doc_fullname,
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
                                                 
                            ORDER BY tmp_res_1.tmp_name_1
                            """);
            report_2 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_1, fromDate, toDate, fromDate, toDate, toDate);
        }

        report_2.forEach(r -> r.setDocFullname(r.getDocFullname().replaceAll(FIO_PATTERN, "$1$2. $3.")));
        return report_2;
    }


    //Отчет по сертификатам для бухгалтеров
    public List<Report_buh_1> queryReport_buh_1(Integer certId, String startDate, String endDate) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("""
                SELECT
                cl.fullname,
                SUM (cav.amountrub) summ,
                COALESCE (tmp_res.r_amountrub, 0) rashod,
                cer.cname name_cert
                                
                FROM clavans cav
                LEFT JOIN clients cl ON  cav.pcode1 = cl.pcode
                LEFT JOIN clavanstype cav_type ON cav.avanstype =  cav_type.avanstype
                LEFT JOIN clcertificateref cer ON  cav_type.avanstype = cer.avanstype
                LEFT JOIN (SELECT --Таблица списание аванса за лечение за счет сертификата, где аванс НЕ является СОЗДАНИЕМ сертификата,
                                  --т.е. выводимисам факт зачисления его на счет пациента
                cl.pcode r_pcode,
                cl.fullname r_fullname,
                SUM (cla.amountrub) r_amountrub
                FROM clavans cla
                LEFT JOIN clients cl ON cla.pcode = cl.pcode
                WHERE cla.typeoper = 2 -- списание аванса
                AND cla.avanstype = 10000002 --тип аванса Сертификат (м.б. будет фильтр)
                AND cla.pcode NOT IN (SELECT pcode FROM clcertificateref) -- все сертификаты, которые созданы и будут создаваться
                    AND cla.paydate BETWEEN ? AND ?
                GROUP BY cl.pcode, cl.fullname) tmp_res ON cl.pcode = tmp_res.r_pcode
                WHERE cav.typeoper IN (2)  --тип операции: списание аванса
                """);

        if (certId != 0) {
            stringBuilder.append("AND cav.pcode = " + certId); //  --10005161 --id сертификата (фильтр по разным сертификатам в будущем)
        } else {
            stringBuilder.append("AND cav.pcode = 10005161");
        }

        stringBuilder.append("""
                AND cav.paydate BETWEEN ? AND ?
                GROUP BY cl.fullname, COALESCE(tmp_res.r_amountrub, 0), cer.cname
                ORDER BY cl.fullname
                """);


        List<Report_buh_1> report_buh_1 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_BUH, startDate, endDate, startDate, endDate);
//        report_1.forEach(r -> r.setDocFullname(r.getDocFullname().replaceAll(FIO_PATTERN, "$1$2. $3.")));
        return report_buh_1;
    }


    //сертификаты
    public List<Map<String, Object>> getAllCertWithId() {
        List<Map<String, Object>> map;
        map = jdbcTemplate.queryForList("""
                SELECT
                pcode id,
                cname name
                FROM clcertificateref
                WHERE pcode NOT IN (10000999)
                                """);
        return map;
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
    public List<String> getAllUsers() {
        return jdbcTemplate.queryForList("""
                SELECT
                doc.dname                                
                FROM doctor  doc
                WHERE doc.doctcode IS NOT NULL
                ORDER BY doc.dname
                """, String.class);
    }


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

