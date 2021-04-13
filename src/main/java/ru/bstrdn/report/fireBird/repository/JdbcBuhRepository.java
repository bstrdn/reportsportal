package ru.bstrdn.report.fireBird.repository;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.bstrdn.report.fireBird.model.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * БУХГЕЛТЕРСКИЕ ОТЧЕТЫ:
 * 1. Сертификаты по дате выдачи {@link #queryReport_buh_1}
 * 2. Сертификаты по дате оплаты {@link #queryReport_buh_3}
 * 3. Акт сверки {@link #queryReport_akt_sverki}
 * 4. Списание {@link #queryReport_buh_4}
 */

@Slf4j
@Repository
@Transactional(readOnly = true)
public class JdbcBuhRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<Report_buh_1> ROW_MAPPER_BUH_1 = BeanPropertyRowMapper.newInstance(Report_buh_1.class);
    private static final RowMapper<Report_buh_2> ROW_MAPPER_BUH_2 = BeanPropertyRowMapper.newInstance(Report_buh_2.class);
    private static final RowMapper<Report_buh_4> ROW_MAPPER_BUH_4 = BeanPropertyRowMapper.newInstance(Report_buh_4.class);
    private static final RowMapper<Report_akt_sverki> ROW_MAPPER_AKT_SVERKI = BeanPropertyRowMapper.newInstance(Report_akt_sverki.class);
    private static final RowMapper<Report_akt_sverki_info> ROW_MAPPER_AKT_SVERKI_INFO = BeanPropertyRowMapper.newInstance(Report_akt_sverki_info.class);

    public JdbcBuhRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    /**
     * БУХГАЛТЕРСКИЙ ОТЧЕТ **
     * 1. Сертификаты по дате выдачи
     */
    public List<Report_buh_1> queryReport_buh_3(Integer certId, String startDate, String endDate) {
        return jdbcTemplate.query("""
                SELECT
                IIF (cav.certific_num IS NULL OR cav.certific_num = '', 'Б/Н', cav.certific_num)  number_cert,
                LIST (CAST (cav.paydate AS DATE)) given,
                cl.fullname,
                SUM (cav.amountrub) summ,
                COALESCE (tmp_res.r_amountrub, 0) rashod
                --cer.cname name_cert
                            
                FROM clavans cav
                LEFT JOIN clients cl ON  cav.pcode1 = cl.pcode
                LEFT JOIN clavanstype cav_type ON cav.avanstype =  cav_type.avanstype
                --LEFT JOIN clcertificateref cer ON  cav_type.avanstype = cer.avanstype
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
                AND cav.pcode = ?  --10005161 --id сертификата (фильтр по разным сертификатам в будущем)
                AND cav.paydate BETWEEN ? AND ?
                GROUP BY cl.fullname, COALESCE(tmp_res.r_amountrub, 0), number_cert --, cer.cname
                ORDER BY cl.fullname
                """, ROW_MAPPER_BUH_1, startDate, endDate, certId, startDate, endDate);
    }


    /**
     * БУХГАЛТЕРСКИЙ ОТЧЕТ **
     * 2. Сертификаты по дате оплаты
     *
     * @param certId  ID серификата
     * @param endDate цена
     */
    public List<Report_buh_2> queryReport_buh_1(Integer certId, String startDate, String endDate) {
        StringBuilder stringBuilder = new StringBuilder();

        List<String> avanstypeList = jdbcTemplate.queryForList("""
                                                                       SELECT  cer.avanstype
                                                                       FROM clavanstype cav_type
                                                                       LEFT JOIN clcertificateref cer ON  cav_type.avanstype = cer.avanstype
                                                                       WHERE cer.pcode = 
                                                                       """ + certId, String.class);
        int avanstype = Integer.parseInt(avanstypeList.get(0));

        stringBuilder.append("""
                SELECT
                IIF (cav.certific_num IS NULL OR cav.certific_num = '', 'Б/Н', cav.certific_num)  number_cert,
                cl.fullname,
                LIST (IIF (EXTRACT (DAY FROM cav.paydate) < 10, '0' || EXTRACT (DAY FROM cav.paydate) || '.', EXTRACT (DAY FROM cav.paydate) || '.') ||
                IIF (EXTRACT (MONTH FROM cav.paydate) < 10, '0' || EXTRACT (MONTH FROM cav.paydate) || '.', EXTRACT (MONTH FROM cav.paydate) || '.') ||
                EXTRACT (YEAR FROM cav.paydate), '; ')  date_reg,
                sert.sumrub summ,
                (sert.sumrub - COALESCE (s_saldo.s_amountrub, 0)) n_saldo,
                COALESCE (tmp_res.r_pay_date, 0) dates_pay,
                COALESCE (ROUND (tmp_res.r_amountrub, 0), '0') r_amountrub,
                (sert.sumrub - COALESCE (s_saldo.s_amountrub, 0)) - COALESCE (tmp_res.r_amountrub, 0) k_saldo
                --, cer.cname name_cert
                                
                FROM clavans cav
                LEFT JOIN clients cl ON  cav.pcode1 = cl.pcode
                                
                LEFT JOIN   (SELECT cav.pcode1 pcode,
                        SUM (cav.amountrub) sumrub  --Таблица для управление суммой сертификата (при group) и выбором сертификата в SELECTе
                        FROM clavans cav
                        LEFT JOIN clients cl ON  cav.pcode1 = cl.pcode
                        WHERE cav.typeoper IN (2)
                        AND cav.pcode = ? --id сертификата (1!!!ФИЛЬТР по разным сертификатам)
                        GROUP BY cav.pcode1) sert ON cav.pcode1 = sert.pcode
                                
                LEFT JOIN clavanstype cav_type ON cav.avanstype =  cav_type.avanstype
                --LEFT JOIN clcertificateref cer ON  cav_type.avanstype = cer.avanstype
                LEFT JOIN (SELECT --Таблица списание аванса за лече сертификата, где аванс НЕ является СОЗДАНИЕМ сертификата,
                                  --т.е. выводим сам факт зачисления его на счение ЗА СЧЕТт пациента
                            cl2.pcode r_pcode,
                            cl2.fullname,
                            SUM (cla2.amountrub) r_amountrub,
                            LIST (IIF (EXTRACT (DAY FROM cla2.paydate) < 10, '0' || EXTRACT (DAY FROM cla2.paydate) || '.', EXTRACT (DAY FROM cla2.paydate) || '.') ||
                            IIF (EXTRACT (MONTH FROM cla2.paydate) < 10, '0' || EXTRACT (MONTH FROM cla2.paydate) || '.', EXTRACT (MONTH FROM cla2.paydate) || '.') ||
                            EXTRACT (YEAR FROM cla2.paydate) || ' - ' || ROUND (cla2.amountrub, 0),  '; ')  r_pay_date
                            FROM clavans cla2
                            LEFT JOIN clients cl2 ON cla2.pcode = cl2.pcode
                            WHERE cla2.typeoper IN (2) -- списание аванса
                            AND cla2.avanstype = ? --тип аванса "Сертификат и др" (2!!!ФИЛЬТР)
                            AND cla2.pcode NOT IN (SELECT pcode FROM clcertificateref) -- все сертификаты, которые созданы и будут создаваться
                                                                                       --(Убираем факт создания сертификата, т.к. нужны только пациенты)
                            AND cla2.paydate BETWEEN ? AND ?  -- 3!!!ФИЛЬТР по датам оплаты
                            GROUP BY r_pcode, cl2.fullname)
                             tmp_res ON cl.pcode = tmp_res.r_pcode
                                
                LEFT JOIN (SELECT --Таблица для расчета сальдо
                            cl2.pcode s_pcode,
                            cl2.fullname,
                            SUM (cla2.amountrub) s_amountrub,
                            LIST (IIF (EXTRACT (DAY FROM cla2.paydate) < 10, '0' || EXTRACT (DAY FROM cla2.paydate) || '.', EXTRACT (DAY FROM cla2.paydate) || '.') ||
                            IIF (EXTRACT (MONTH FROM cla2.paydate) < 10, '0' || EXTRACT (MONTH FROM cla2.paydate) || '.', EXTRACT (MONTH FROM cla2.paydate) || '.') ||
                            EXTRACT (YEAR FROM cla2.paydate) || ' - ' || ROUND (cla2.amountrub, 0),  '; ')  r_pay_date
                            FROM clavans cla2
                            LEFT JOIN clients cl2 ON cla2.pcode = cl2.pcode
                            WHERE cla2.typeoper IN (2) -- списание аванса
                            AND cla2.avanstype = ? --тип аванса "Сертификат и др" (2!ФИЛЬТР)
                            AND cla2.pcode NOT IN (SELECT pcode FROM clcertificateref) -- все сертификаты, которые созданы и будут создаваться(Нужно точно! выяснить надобность)
                            AND cla2.paydate < ?  -- 4!!!ФИЛЬТР для подсчета суммы оплат ДО выбранного периода
                             GROUP BY s_pcode, cl2.fullname)
                             s_saldo ON cl.pcode = s_saldo.s_pcode
                                
                WHERE cav.typeoper IN (2) --тип операции: списание аванса
                AND cav.pcode = ? --id сертификата (1!!!ФИЛЬТР по разным сертификатам)
                --AND cav.paydate BETWEEN '01.01.2019' AND '31.12.2021'  -- фильтр по дате выдачи сертификата (ПОКА НЕ ИСПОЛЬЗУЕТСЯ)
                AND cl.fullname  IS NOT NULL
                --AND cer.cname IS NOT NULL
                GROUP BY cl.fullname, sert.sumrub, n_saldo, dates_pay, r_amountrub, k_saldo, number_cert --, cer.cname
                HAVING  (sert.sumrub - COALESCE (s_saldo.s_amountrub, 0)) != 0
                AND COALESCE (ROUND (tmp_res.r_amountrub, 0), '0') != 0
                ORDER BY  cl.fullname
                """);


        List<Report_buh_2> report_buh_2 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_BUH_2,
                certId, avanstype, startDate, endDate, avanstype, startDate, certId);

        return report_buh_2;
    }


    /**
     * БУХГАЛТЕРСКИЙ ОТЧЕТ **
     * 3. Акт сверки
     */
    public List<Report_akt_sverki> queryReport_akt_sverki(Integer orgId, String startDate, String endDate) {
        Timestamp start = Timestamp.valueOf(startDate);
        Timestamp end = Timestamp.valueOf(endDate);
        return jdbcTemplate.query("""
                SELECT *
                           FROM (
                                       SELECT
                           
                           CAST (t.treatdate AS DATE)dat,
                           'Оказание услуг. Квитанция № ' || t.orderno doc,
                           CAST ('Исполнитель ' || pers_clinic.jname AS char(254)) org,
                           CAST (ROUND (t.amountjp_disc, 0) AS INTEGER) deb,
                           0 cred
                           
                           FROM treat t
                           JOIN jpagreement dog ON t.jid = dog.agrid
                           JOIN jpersons pers ON dog.jid = pers.jid
                           JOIN jpersons pers_clinic ON t.clinicid = pers_clinic.jid
                           
                           WHERE pers.jid = ? --2 Фильтр по юр лицам
                           AND t.amountjp_disc != 0
                           AND t.treatdate BETWEEN ? AND ?   --1Фильтр дата
                           
                           UNION ALL
                           
                           SELECT
                                       --jp.accid id,
                                       CAST (jp.pdate AS DATE) date_s ,
                           --            CAST (IIF (EXTRACT (DAY FROM jp.pdate) < 10, '0' || EXTRACT (DAY FROM jp.pdate) || '.', EXTRACT (DAY FROM jp.pdate) || '.') ||
                           --            IIF (EXTRACT (MONTH FROM jp.pdate) < 10, '0' || EXTRACT (MONTH FROM jp.pdate) || '.', EXTRACT (MONTH FROM jp.pdate) || '.') ||
                           --            EXTRACT (YEAR FROM jp.pdate) AS DATE)  date_s,
                                       IIF (jp.OperType = 2, 'Платежный документ № ' || COALESCE (jp.numdoc, 'б/н'), IIF (jp.OperType = 4,
                                       'Возврат. Документ № ' || COALESCE (jp.numdoc, 'б/н'), 'Не известная операция')) number_o,
                                       CAST (IIF (jp.OperType = 2, 'Плательщик ' || pers.jname, IIF (jp.OperType = 4, 'Получатель ' || pers.jname, 'Плательщик ' || pers.jname)) AS char(254))   name_o,
                                       0 ff,
                                       CAST (ROUND (IIF (jp.OperType = 2, jp.amountrub, IIF (jp.OperType = 4, -jp.amountrub, jp.amountrub)), 0) AS INTEGER) debit_o
                           
                                       FROM  jaccpay jp
                                       LEFT JOIN jpersons pers ON jp.jid = pers.jid
                                       WHERE jp.pdate >= ? AND jp.pdate <= ?     --1фильтр дата
                                       AND jp.jid = ? ORDER BY 1) result  --2Фильтр по Юр. лицам
                           
                           ORDER BY  1
                                """, ROW_MAPPER_AKT_SVERKI, orgId, start, end, start, end, orgId);
    }


    /**
     * БУХГАЛТЕРСКИЙ ОТЧЕТ **
     * 4. Списание
     */
    public List<Report_buh_4> queryReport_buh_4(Integer dcode, String startDate, String endDate) throws DataAccessException {
        Timestamp start = Timestamp.valueOf(startDate);
        Timestamp end = Timestamp.valueOf(endDate);

        StringBuilder sb = new StringBuilder();
        sb.append("""
                SELECT
                priem.data_spis,
                priem.fio_pat,
                priem.fio_doc,
                priem.data_nachisl,
                priem.sum_nachisl,
                priem.sum_spis,
                priem.data_zn,
                LIST (priem.kod || ' - ' || CAST (priem.count_usl AS INTEGER) || ' шт', '
                ') code
                                 
                FROM (SELECT
                cred.lcdate data_spis,
                cl.fullname fio_pat,
                doc.ntuser fio_doc,
                t.treatdate data_nachisl,
                CAST ((t.amountcl_disc + t.amountjp_disc) AS INTEGER) sum_nachisl,
                CAST (cred.amountrub AS INTEGER) sum_spis,
                t.naradclose data_zn,
                LIST (DISTINCT ws.kodoper, '
                ') kod,
                SUM  (od.schcount) count_usl
                                 
                FROM losecredit cred
                LEFT JOIN doctor doc ON cred.dcode = doc.dcode
                LEFT JOIN clients cl ON cred.pcode = cl.pcode
                LEFT JOIN treat t ON cred.treatcode = t.treatcode
                JOIN orderdet od ON t.orderno = od.orderno
                JOIN wschema ws ON od.schcode = ws.schid
                                 
                WHERE
                cred.paycode IN (11, 13, 14, 16) --Списания
                AND t.doctype IN (1, 3)   --Только приемы, без З/Н
                AND cred.lcdate BETWEEN ? AND ? --Фильтр по дате
                """);
//фильтр по доктору
        if (dcode != 0) {
            sb.append("AND cred.dcode = " + dcode);
        }

        sb.append("""
                 GROUP BY t.orderno, cred.lcdate, cl.fullname, doc.ntuser, t.treatdate, sum_nachisl, sum_spis, t.naradclose, ws.kodoper) priem
                 GROUP BY priem.data_spis, priem.fio_pat, priem.fio_doc, priem.data_nachisl, priem.sum_nachisl, priem.sum_spis, priem.data_zn
                 
                 
                 UNION ALL
                 
                 SELECT
                 zn_res.data_spis,
                 zn_res.fio_pat,
                 zn_res.fio_doc,
                 zn_res.data_nachisl,
                 zn_res.sum_nachisl,
                 zn_res.sum_spis,
                 zn_res.data_zn,
                 LIST (zn_res.kod || ' - ' || CAST (zn_res.count_usl AS INTEGER) || ' шт', '
                 ')
                 
                 FROM (SELECT
                 cred.lcdate data_spis,
                 cl.fullname fio_pat,
                 doc.ntuser fio_doc,
                 t.treatdate data_nachisl,
                 CAST ((t.amountcl_disc + t.amountjp_disc) AS INTEGER) sum_nachisl,
                 CAST (cred.amountrub AS INTEGER) sum_spis,
                 t.naradclose data_zn,
                 LIST (DISTINCT ws.kodoper, '
                 ') kod,
                 SUM  (od.schcount) count_usl
                 
                 FROM losecredit cred
                 LEFT JOIN doctor doc ON cred.dcode = doc.dcode
                 LEFT JOIN clients cl ON cred.pcode = cl.pcode
                 LEFT JOIN treat t ON cred.treatcode = t.treatcode
                 JOIN orderdet od ON t.orderno = od.orderno
                 JOIN wschema ws ON od.schcode = ws.schid
                 
                 WHERE
                 cred.paycode IN (11, 13, 14, 16) --Списания
                 AND t.doctype IN (12)  -- З/Н зуботехнический и закрыт
                 AND t.naradclose BETWEEN ? AND ? --Фильтр по дате
                """);
//фильтр по доктору
        if (dcode != 0) {
            sb.append("AND cred.dcode = " + dcode);
        }

        sb.append("""
                GROUP BY t.orderno, cred.lcdate, cl.fullname, doc.ntuser, t.treatdate, sum_nachisl, sum_spis, t.naradclose, ws.kodoper) zn_res
                GROUP BY zn_res.data_spis, zn_res.fio_pat, zn_res.fio_doc, zn_res.data_nachisl, zn_res.sum_nachisl, zn_res.sum_spis, zn_res.data_zn
                ORDER BY 1
                """);

        return jdbcTemplate.query(sb.toString(), ROW_MAPPER_BUH_4, start, end, start, end);
    }


    /**
     * @return Список всех сертификатов с их id
     */
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

    /**
     * @return Список докторов с их id
     */
    public List<Map<String, Object>> getAllDocWithId() {
        List<Map<String, Object>> map;
        map = jdbcTemplate.queryForList("""
                SELECT
                doc.dcode,
                doc.ntuser
                FROM doctor doc
                WHERE doc.stdtype IN (10000003, 10000006)
                AND doc.locked != 1
                AND doc.profid = 10000011
                ORDER BY doc.ntuser
                                """);
        return map;
    }


    /**
     * @return список юредических лиц с их id
     */
    public List<Map<String, Object>> getLegalEntitiesWithId() {
        return jdbcTemplate.queryForList("""
                SELECT
                pers.jid, pers.jname
                FROM jpersons pers
                WHERE pers.jid > 100
                ORDER BY pers.jname
                """);
    }


    /**
     * @return Таблица с информацией для заполнения АКТА СВЕРКИ в EXCEL
     */
    public Report_akt_sverki_info getInfoForAktSverki(Integer orgId, String startDate, String end) throws Exception {
        Timestamp start = Timestamp.valueOf(startDate);
//        Timestamp end = Timestamp.valueOf(endDate);
        List<Report_akt_sverki_info> akt_sverki_infos = jdbcTemplate.query("""
                --Сальдо на начало и конец периода + обороты--
                                
                SELECT
                org.org1,
                org.org1_full org1_full,
                org.org1_dir org1_dir,
                org.org2 org2,
                org.org2_full org2_full,
                COALESCE (start_per2.debit_o, 0) - COALESCE (start_per1.sum_uslug, 0) debt_before,
                (COALESCE (start_per2.debit_o, 0) + COALESCE (end_per2.debit_o, 0)) -
                (COALESCE (start_per1.sum_uslug, 0) + COALESCE (end_per1.sum_uslug, 0)) debt_after,
                COALESCE (end_per1.sum_uslug, 0) org1_oborot,
                COALESCE (end_per2.debit_o, 0) org2_oborot
                                
                FROM
                --Таблица выбирает все лечения начисленные на организацию ДО даты выбранного периода акта сверки и суммирует оплаты
                (SELECT
                IIF (COUNT (t.amountjp_disc) = 0, 0, SUM (CAST (ROUND (t.amountjp_disc, 0) AS INTEGER))) sum_uslug  --ИСПРАВИЛ
                FROM treat t\s
                JOIN jpagreement dog ON t.jid = dog.agrid
                JOIN jpersons pers ON dog.jid = pers.jid
                WHERE pers.jid = ? --2 Фильтр по юр лицам
                AND t.amountjp_disc != 0
                AND t.treatdate < ? ) start_per1  --Фильтр дата (Подставляем начало периода)
                JOIN
                --Таблица Выбирает оплаты от организиции ДО даты выбранного периода акта сверки и суммирует оплаты
                (SELECT
                IIF (COUNT (jp.amountrub) = 0, 0, SUM (CAST (ROUND (IIF (jp.OperType = 2, jp.amountrub,   --ИСПРАВИЛ
                IIF (jp.OperType = 4, -jp.amountrub, jp.amountrub)), 0) AS INTEGER))) debit_o  --ИСПРАВИЛ
                FROM  jaccpay jp
                WHERE jp.pdate < ?     --Фильтр дата (Подставляем начало периода)
                AND jp.jid = ? ) start_per2  ON 1=1      --2Фильтр по Юр. лицам
                LEFT JOIN
                --Таблица выбирает все лечения начисленные на организацию В ВЫБРАННЫЙ период акта сверки и суммирует оплаты
                (SELECT
                COALESCE (SUM (CAST (ROUND (t.amountjp_disc, 0) AS INTEGER)), 0) sum_uslug
                FROM treat t
                JOIN jpagreement dog ON t.jid = dog.agrid
                JOIN jpersons pers ON dog.jid = pers.jid
                WHERE pers.jid = ? --2 Фильтр по юр лицам
                AND t.amountjp_disc != 0
                AND t.treatdate >= ? AND t.treatdate <= ?) end_per1 ON 1=1 --Фильтр дата (Подставляем выбранный период)
                LEFT JOIN
                --Таблица Выбирает оплаты от организиции В ВЫБРАННЫЙ период акта сверки и суммирует оплаты                              \s
                (SELECT
                COALESCE (SUM (CAST (ROUND (IIF (jp.OperType = 2, jp.amountrub, IIF (jp.OperType = 4, -jp.amountrub, jp.amountrub)), 0) AS INTEGER)), 0) debit_o
                FROM  jaccpay jp
                WHERE jp.pdate >= ? AND jp.pdate <= ?    --Фильтр дата (Подставляем выбранный период)
                AND jp.jid = ? ) end_per2 ON 1=1      --2Фильтр по Юр. лицам
                LEFT JOIN
                (SELECT
                org_res1.org1,
                org_res1.org1_full,
                org_res1.org1_dir,
                org_res2.org2,
                org_res2.org2_full
                                
                FROM (SELECT
                pers.jname org1,
                pers.jname ||', '||'ИНН '||
                pers.jinn ||', '||
                pers.jaddr  org1_full,
                pers.jheader org1_dir
                FROM jpersons pers
                WHERE pers.jid = 1) org_res1
                JOIN
                (SELECT
                pers.jname org2,
                pers.jname ||', '||'ИНН '||
                COALESCE (pers.jinn, 'Укажите ИНН в справочнике Инфодента') ||', '||
                COALESCE (pers.jaddr, 'Укажите АДРЕС в справочнике Инфодента')  org2_full,
                COALESCE (pers.jheader, 'Не указан') org2_dir
                FROM jpersons pers
                WHERE pers.jid = ?) org_res2 ON org_res1.org1 != org_res2.org2)org ON 1=1 --Фильтр по организации  pers.jid = ?
                """, ROW_MAPPER_AKT_SVERKI_INFO, orgId, start, start, orgId, orgId, start, end, start, end, orgId, orgId);
        if (akt_sverki_infos.size() == 0) {
            throw new NotFoundException("возвращяется пустая строка");
        }
        Report_akt_sverki_info akt_sverki_info = akt_sverki_infos.get(0);
        return akt_sverki_info;
    }


//    public Report_akt_sverki_info getInfoForAktSverki2(Integer orgId, String startDate, String end) throws Exception {
//Report_akt_sverki_info
//
//
//    }

}
