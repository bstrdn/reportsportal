package ru.bstrdn.report.postgres.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.bstrdn.report.fireBird.model.Skud2;
import ru.bstrdn.report.postgres.model.Skud3;
import ru.bstrdn.report.postgres.model.SkudResult_1;

import java.security.Provider;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
//@Transactional(readOnly = true)
public class JdbcSkudRepository {

    @Autowired
    @Qualifier(value = "postgresJdbc")
    private JdbcTemplate postgresJdbc;
    @Autowired
    @Qualifier(value = "jdbcTemplate")
    private JdbcTemplate firebirdJdbc;
    @Autowired
    private TmpIdentRepository tmpIdentRepository;

    private static final RowMapper<Skud3> ROW_MAPPER_SKUD_2 = BeanPropertyRowMapper.newInstance(Skud3.class);
    private static final RowMapper<SkudResult_1> ROW_MAPPER_SKUD_3 = BeanPropertyRowMapper.newInstance(SkudResult_1.class);

    /**
     * 1 отчет по скуду
     *
     * @param groupSkud    - группа GATE
     * @param details8Skud - 8 поле в карточках пользователей GATE
     */
    public List<SkudResult_1> querySkud_1(String groupSkud, String details8Skud, Integer skudUserId, String startDate, String endDate) {

        Timestamp startDate2 = Timestamp.valueOf(startDate);
        Timestamp endDate2 = Timestamp.valueOf(endDate);

        /**
         * ЭТАП 1 - создание tmp_gate таблицы с отчетом по SKUD
         */
        postgresJdbc.update("""
                TRUNCATE TABLE tmp_gate;
                TRUNCATE TABLE tmp_ident;
                INSERT INTO tmp_gate
                SELECT
                    CAST (gu.details7 AS integer) dcode,
                    CAST (gu.userptr AS integer) userptr,
                    ge.name nameuser,
                    CAST (CASE WHEN EXTRACT (DAY FROM ge.datetime) < 10 THEN '0' ||
                                                                             EXTRACT (DAY FROM ge.datetime) || '.' ELSE EXTRACT (DAY FROM ge.datetime) || '.' END ||
                          CASE WHEN EXTRACT (MONTH FROM ge.datetime) < 10 THEN '0' ||
                                                                               EXTRACT (MONTH FROM ge.datetime) || '.' ELSE EXTRACT (MONTH FROM ge.datetime) || '.' END ||
                          EXTRACT (YEAR FROM ge.datetime) AS date) getdates,
                    CAST (MIN (CASE WHEN EXTRACT (HOUR FROM ge.datetime) < 10 THEN '0' ||
                                                                                   EXTRACT (HOUR FROM ge.datetime) || ':' ELSE EXTRACT (HOUR FROM ge.datetime) || ':' END ||
                               CASE WHEN EXTRACT (MINUTE FROM ge.datetime) < 10 THEN '0' ||
                                                                                     EXTRACT (MINUTE FROM ge.datetime) || '' ELSE EXTRACT (MINUTE FROM ge.datetime) || '' END) AS time)  getin,
                    CAST (MAX (CASE WHEN EXTRACT (HOUR FROM ge.datetime) < 10 THEN '0' ||
                                                                                   EXTRACT (HOUR FROM ge.datetime) || ':' ELSE EXTRACT (HOUR FROM ge.datetime) || ':' END ||
                               CASE WHEN EXTRACT (MINUTE FROM ge.datetime) < 10 THEN '0' ||
                                                                                     EXTRACT (MINUTE FROM ge.datetime) || '' ELSE EXTRACT (MINUTE FROM ge.datetime) || '' END) AS time) getout,
                    SUBSTRING (CAST (max(ge.datetime) - min(ge.datetime)  AS text) FROM 1 FOR 2)  || 'ч. ' ||
                    SUBSTRING (CAST (max(ge.datetime) - min(ge.datetime)  AS text) FROM 4 FOR 2) || 'м.'  facttime
                FROM gate_events ge
                         LEFT JOIN gate_users gu ON ge.userptr = gu.userptr
                WHERE gu.details8 = ?
                  AND gu.groupptr = ?
                  AND ge.eventcode = 8
                  AND ge.datetime BETWEEN ? AND ? --1!Фильтр по дате
                  AND ge.userptr = ? --2!Фильтр по пользователям
                GROUP BY nameuser, gu.userptr, getdates
                ORDER BY getdates;
                """, details8Skud, groupSkud, startDate2, endDate2, skudUserId);

        /**
         * ЭТАП 1.1 - получаем id юзера (в инфоденте) из предыдущего запроса
         */
        Integer idUserStom = postgresJdbc.queryForObject("""
                SELECT tg.dcode
                FROM tmp_gate tg
                LIMIT 1;
                """, Integer.class);

        /**
         * ЭТАП 2 - создание tmp_ident таблицы с отчетом по времени из инфодента
         */
        String infodentJobTimeQuery = """
                SELECT
                doc.dcode dcode,
                doc.dname,
                CAST (ds.wdate AS DATE)  getdates,
                MIN (CAST ((ds.beghour || ':' || ds.begmin) AS TIME)) startjob,
                MAX (CAST ((ds.endhour || ':' || ds.endmin) AS TIME)) endjob,
                CAST (TRIM (TRAILING '.' FROM TRIM (TRAILING 0 FROM CAST (DATEDIFF (MINUTE,  MIN (CAST ((ds.beghour || ':' || ds.begmin) AS TIME)),
                MAX (CAST ((ds.endhour || ':' || ds.endmin) AS TIME))) AS FLOAT) / 60)) AS VARCHAR(10)) || ' ч.' norma
                FROM doctor doc
                JOIN doctshedule ds ON doc.dcode = ds.dcode
                WHERE doc.assistchk = 1
                AND ds.wdate BETWEEN ? AND ?  --!1 ФИЛЬТР по дате
                AND doc.dcode = ?                            --!2 ФИЛЬТР по сотруднику
                GROUP BY   doc.dcode, doc.dname, ds.wdate
                ORDER BY doc.dname,  ds.wdate               
                """;
        List<Skud3> skud2List = firebirdJdbc.query(infodentJobTimeQuery, ROW_MAPPER_SKUD_2, startDate2, endDate2, idUserStom);
        tmpIdentRepository.saveAll(skud2List);

        /**
         * ЭТАП 3 - создание результирующего запроса
         */
        List<SkudResult_1> skudResult_1 = postgresJdbc.query("""
                SELECT
                CASE WHEN EXTRACT (DAY FROM ti.getdates) < 10 THEN '0' ||
                                    EXTRACT (DAY FROM ti.getdates) || '.' ELSE EXTRACT (DAY FROM ti.getdates) || '.' END ||
                CASE WHEN EXTRACT (MONTH FROM ti.getdates) < 10 THEN '0' ||
                                    EXTRACT (MONTH FROM ti.getdates) || '.' ELSE EXTRACT (MONTH FROM ti.getdates) || '.' END ||
                EXTRACT (YEAR FROM ti.getdates) date,
                COALESCE (SUBSTRING (CAST (tg.getin AS varchar) FROM 1 FOR 5), '---') getin,
                COALESCE (SUBSTRING (CAST (tg.getout AS varchar) FROM 1 FOR 5), '---') getout,
                COALESCE (SUBSTRING (CAST ((tg.getout - tg.getin) AS varchar) FROM 1 FOR 2) || 'ч ' ||
                            SUBSTRING (CAST ((tg.getout - tg.getin) AS varchar) FROM 4 FOR 2) || 'м', '---') facttime,
                SUBSTRING (CAST ((ti.endjob - ti.startjob) AS varchar) FROM 1 FOR 2) || 'ч ' ||
                SUBSTRING (CAST ((ti.endjob - ti.startjob) AS varchar) FROM 4 FOR 2) || 'м' norma,
                COALESCE (CASE WHEN SUBSTRING (CAST ((tg.getout - tg.getin) -
                                            (ti.endjob - ti.startjob) AS varchar) FROM 1 FOR 1) = '-' THEN
                                    SUBSTRING (CAST ((tg.getout - tg.getin) -
                                                    (ti.endjob - ti.startjob) AS varchar) FROM 1 FOR 3) || 'ч ' ||
                                    SUBSTRING (CAST ((tg.getout - tg.getin) -
                                                    (ti.endjob - ti.startjob) AS varchar) FROM 5 FOR 2) || 'м' ELSE
                                    SUBSTRING (CAST ((tg.getout - tg.getin) -
                                                    (ti.endjob - ti.startjob) AS varchar) FROM 1 FOR 2) || 'ч ' ||
                                    SUBSTRING (CAST ((tg.getout - tg.getin) -
                                                    (ti.endjob - ti.startjob) AS varchar) FROM 4 FOR 2) || 'м' END,
                '-' || SUBSTRING (CAST ((ti.endjob - ti.startjob) AS varchar) FROM 1 FOR 2) || 'ч ' ||
                SUBSTRING (CAST ((ti.endjob - ti.startjob) AS varchar) FROM 4 FOR 2) || 'м') resulttime
                FROM tmp_ident ti
                LEFT JOIN tmp_gate tg ON ti.dcode = tg.dcode AND ti.getdates = tg.getdates
                WHERE ti.norma != '0 ч.'
                """, ROW_MAPPER_SKUD_3);

        return skudResult_1;
    }


    /**
     * Достаём юзеров из гейт с id из инфодента
     *
     * @param accessLevel - 2 для Поленок
     * @param groupNum    - 9 - группа СТОМА
     * @return
     */
    public List<Map<String, Object>> getAllSkudUsers(String groupNum, String accessLevel) {

        return postgresJdbc.queryForList("""
                SELECT
                gu.userptr,
                gu.details7,
                gu.lastname || ' ' ||
                SUBSTRING (gu.firstname FROM 1 FOR 1) ||
                '.' || ' ' || SUBSTRING (gu.fathername FROM 1 FOR 1)
                || '.' || ' ' || gu.number fio
                FROM gate_users gu
                WHERE gu.details8 = ?
                AND gu.groupptr = ?
                ORDER BY gu.lastname
                """, accessLevel, groupNum);
    }

}

