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
 * 5. Долги и авансы {@link #queryReport_buh_5}
 */

@Slf4j
@Repository
@Transactional(readOnly = true)
public class JdbcBuhRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<Report_buh_1> ROW_MAPPER_BUH_1 = BeanPropertyRowMapper.newInstance(Report_buh_1.class);
    private static final RowMapper<Report_buh_2> ROW_MAPPER_BUH_2 = BeanPropertyRowMapper.newInstance(Report_buh_2.class);
    private static final RowMapper<Report_buh_4> ROW_MAPPER_BUH_4 = BeanPropertyRowMapper.newInstance(Report_buh_4.class);
    private static final RowMapper<Report_buh_5> ROW_MAPPER_BUH_5 = BeanPropertyRowMapper.newInstance(Report_buh_5.class);
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
     * БУХГАЛТЕРСКИЙ ОТЧЕТ **
     * 5. Списание
     */
    public List<Report_buh_5> queryReport_buh_5(String endDate) throws DataAccessException {
        Timestamp end = Timestamp.valueOf(endDate);

        StringBuilder sb = new StringBuilder();
        sb.append("""
                SELECT * --SUM (dolg_res), SUM (avans)
                
                FROM
                (
                SELECT
                r3client_name,
                IIF (SUM (res3.dolg) < 0, LIST ((CASE WHEN res3.dolg < 0 THEN r3doctname  END), '
                '), IIF (SUM (res3.dolg) > 0, 'Аванс на докторе', 'Аванс на клинике')) name_doc,
                IIF (SUM (res3.dolg) = 0, 0,
                (LIST ((CASE WHEN res3.dolg != 0 THEN CAST (res3.dolg AS NUMERIC)  END), '
                '))) dolg,
                IIF (SUM (res3.dolg) = 0, 0,
                (SUM ((CASE WHEN res3.dolg != 0 THEN CAST (res3.dolg AS NUMERIC)  END)))) dolg_res,
                IIF (SUM (res3.dolg) != 0, 0,
                CAST (SUM (res3.r3_vse_Avansi) - SUM (res3.r3_vozvrat_patient) - SUM (res3.r3perenos_na_family) -SUM (res3.r3_vozvrati_patient) - SUM (res3.r3_oplata_za_avans) - SUM (res3.r3_oplata_iz_obshego_avansa) AS NUMERIC)) avans,
                
                IIF (SUM (res3.dolg) = 0, MAX (CAST (res3.r3maxdate_avans AS DATE)), MAX (CASE WHEN res3.dolg < 0 THEN CAST (res3.r3date_dolg AS DATE)  END)) data_voznic
                
                
                FROM
                (
                SELECT
                
                res2.ORderCod r3_ORderCod,
                res2.r2client_name r3client_name,
                res2.r2doctname r3doctname,
                res2.vse_Avansi r3_vse_Avansi,
                res2.vozvrat_patient r3_vozvrat_patient,
                res2.vozvrati_patient r3_vozvrati_patient,
                SUM (perenos_na_family) r3perenos_na_family,
                SUM (oplata_za_avans) r3_oplata_za_avans,
                SUM (oplata_iz_obshego_avansa) r3_oplata_iz_obshego_avansa,
                SUM (IIF (res2.oplata_treat = 0, 0, res2.oplata_treat)) dolg,
                MAX (res2.date_dolg) r3date_dolg,
                MAX (res2.maxdate_avans) r3maxdate_avans
                
                
                
                
                FROM
                (
                SELECT
                res1.ORderCod ORderCod,
                SUM ( IIF (res1.actype = 2 AND res1.dcode < 0 AND (res1.depnum NOT IN (4) OR res1.depnum IS NULL) AND res1.paycode IN (2, 201, 203), res1.sumrub, 0)) vse_Avansi,
                SUM (IIF (res1.actype = 2 AND res1.dcode < 0 AND res1.depnum IN (4) AND res1.paycode IN (201), res1.sumrub, 0)) perenos_na_family,
                SUM ( IIF (res1.actype = 2 AND res1.paycode IN (5) AND res1.dcode < 0, res1.sumrub, 0)) vozvrat_patient,
                SUM ( IIF (res1.actype = 6 AND res1.paycode IN (5) AND res1.dcode < 0, res1.sumrub, 0)) vozvrati_patient,
                SUM ( IIF (res1.actype = 5 AND res1.paycode IN (-2), res1.sumrub, 0)) oplata_za_avans,
                SUM ( IIF (res1.actype = 4 AND res1.paycode IN (101), res1.sumrub, 0)) oplata_iz_obshego_avansa,
                
                COALESCE (res1.doctname, 'Аванс на клинике') r2doctname,
                res1.client_name r2client_name,
                (-SUM (IIF (res1.actype = 1 AND res1.paycode = 0, res1.sumrub, 0)))
                + SUM (IIF (res1.actype = 2 AND res1.paycode IN (3), res1.sumrub, 0))
                - SUM (IIF (res1.actype = 2 AND res1.paycode IN (8), res1.sumrub, 0))
                + SUM ( IIF (res1.actype = 3 AND res1.paycode IN (11, 13, 14, 16), res1.sumrub, 0))
                + SUM (IIF (res1.actype = 5 AND res1.paycode IN (-1), res1.sumrub, 0))
                + SUM (IIF (res1.actype = 5 AND res1.paycode IN (-2), res1.sumrub, 0))
                + SUM (IIF (res1.actype = 5 AND res1.paycode IN (-3), res1.sumrub, 0))
                - SUM (IIF (res1.actype = 6 AND res1.paycode IN (8), res1.sumrub, 0))
                + SUM ( IIF (res1.actype = 2 AND res1.paycode IN (1), res1.sumrub, 0))
                + SUM ( IIF (res1.actype = 4 AND res1.paycode IN (101), res1.sumrub, 0))
                - SUM (IIF (res1.actype = 2 AND res1.paycode IN (5) AND res1.dcode > 0, res1.sumrub, 0))
                - SUM (IIF (res1.actype = 6 AND res1.paycode IN (5) AND res1.dcode > 0, res1.sumrub, 0)) oplata_treat,
                MAX (res1.acdate) date_dolg, --Дата долга --Отсюда брать даты!!!!
                MAX (CASE WHEN res1.actype = 2 AND res1.paycode IN (2, 201, 203) THEN res1.acdate END) maxdate_avans --Дата аванса
                FROM
                
                (
                SELECT I.PayDate AcDate, I.Amount SumPay, I.AmountRub SumRub, 2 AcType, I.PayNum PayNum,  I.DCode DCode,  I.AccessLevel AccessLevel,
                0.0  DIScount, 0.00 DIScountRub, COALESCE(I.OrderNo, T2.ORderno) OrderCod, T.TreatCode TreatCode, 0 Kateg,  I.NumDoc NumDoc,
                0.00 JAmount, 0.00 JAmountRub, I.PayCode PayCode, I.ClINicID, I.TransactID sORt_transact, I.TransactID, CAST(CA.IsFamily AS Integer) IsFamily,
                I.IncRef, I.Filial, I.CAShID CAShID, R.Comment, I.Depnum, CAST (NULL AS TIMESTAMP) Udergdate,
                0  DocType, 0 PayPlan,
                r.fIScaldocnumber fIScaldocnumber, r.checknumi checknumi,
                0 AGRID, 0 LSTID, I.PlanID PlanID,
                CAST(I.comment AS VARCHAR(255)) IncComment, T2.OrderNo DolgNo,
                CAST(D.schnum AS VARCHAR(24)) schnum, R.PaymentNum PaymentNum, I.MONeyCAShID MONeyCAShID, ca.avanstype avanstype,
                pr.shORtname payrefname, LEFT(INcr.dicname, 127) INcrefname,
                CAST(NULL AS VARCHAR(255)) jname, CAST(NULL AS VARCHAR(255)) jname2,
                CAST(NULL AS VARCHAR(255)) agname, CAST(NULL AS VARCHAR(64)) agnum, CAST(NULL AS VARCHAR(255)) lIStname, dt.fullname doctname,
                0 narid, CAST(NULL AS CHAR(1)) naradsymb, avt.avanstypename avanstypename, t.nsp nsp,
                i.avansparentid avansparentid, i.InitialAmountRub InitialAmountRub, t2.treatdate dolgdate, NULL pcode1, r.checkprINt, r.detcomment detcomment,
                CAST(NULL AS VARCHAR(1024)) treat_comment,
                COALESCE(r.createdate, DATEADD(secONd,1, t.createdate), DATEADD(1 DAY TO r.adate),
                i.modifydate) transact_createdate, dr.checkprINtmode, c.cAShname cAShname, clIN.jname clINname, ms.mONeycAShname mONeycAShname, fil.fcolOR, dep.depname,
                0 CORrespFilial, 0 bnalpay, cl.fullname client_name
                FROM Incom I
                LEFT JOIN Treat T ON (I.OrderNo = T.OrderNo AND I.PCode = T.PCode)
                LEFT JOIN Treat T2 ON (T2.TreatCode = I.ExTreatCode)
                LEFT JOIN clAvans CA ON (I.AvansID = CA.ID AND CA.TypeOper = 1)
                LEFT JOIN clavanstype avt ON avt.avanstype = ca.avanstype
                LEFT JOIN dailyplan D ON I.PlanID = D.DID
                LEFT JOIN dailyplanref dr ON d.plantype = dr.plantype
                LEFT JOIN TransactLISt R ON I.TransactID = R.TransactID
                LEFT JOIN mONeycAShref ms ON ms.mONeycAShid = r.mONeycAShid
                LEFT JOIN cAShref C ON I.CAShID = C.CAShID
                LEFT JOIN payref pr ON pr.paycode = i.paycode
                LEFT JOIN dicINfo INcr ON (INcr.refid=23 AND INcr.dicid = i.INcref)
                LEFT JOIN doctOR dt ON dt.dcode = i.dcode
                LEFT JOIN jpersONs clIN ON clIN.jid = i.clINicid
                LEFT JOIN filials fil ON fil.filid = i.filial
                LEFT JOIN departments dep ON i.depnum = dep.depnum LEFT JOIN clients cl ON cl.pcode = i.pcode
                WHERE I.pcode > 0
                AND I.paydate >= '01.01.2019' AND I.paydate <= ? AND I.accesslevel <=1    --ФИЛЬТР ДАТА КОНЕЦ ПЕРИОДА!!!
                AND (ca.id IS NULL OR ca.amountrub > 0)
                
                UNION ALL
                
                SELECT T.TreatDate AcDate, (T.AmountCl_DISc / T.CursCurr) SUMPay, T.AmountCl_DISc SumRub, 1 AcType, t.treatcode PayNum,
                T.DCode, T.AccessLevel, O.DIScount, O.DIScountRub, O.OrderCod, T.TreatCode, T.Kateg,  NULL NumDoc,
                (T.AmountJP_DISc / T.CursCurr) JAmount, T.AmountJP_DISc JAmountRub, CAST(0 AS smallINt) PayCode,
                T.ClINicID, 0, R.TransactID, 0, 0, T.Filial,
                T.cAShID, R.Comment, 0, T.UdergDate, CAST(T.DocType AS INteger), T.PayPlan,
                r.fIScaldocnumber, r.checknumi,
                T.JID, T.LSTID, T.PlanID,
                CAST(NULL AS VARCHAR(255)), 0, CAST(D.schnum AS VARCHAR(24)) schnum,
                R.PaymentNum, R.MONeyCAShID, 0,
                pr.shORtname, CAST(NULL AS VARCHAR(127)),
                jp.jname, jp.jname2, ag.agname, ag.agnum, lst.shORtname, dt.fullname, nr.narid, nr.naradsymb, CAST(NULL AS ttext128) avanstypename,
                t.nsp, 0, NULL, NULL, NULL, r.checkprINt, NULL, t.comment, t.createdate, dr.checkprINtmode, c.cAShname, clIN.jname clINicname, ms.mONeycAShname, fil.fcolOR, CAST(NULL AS VARCHAR(100)), 0, 0, cl.fullname
                FROM Treat T
                LEFT JOIN n_naradtypes nr ON (nr.docopen = t.doctype OR nr.docclose = t.doctype)
                LEFT JOIN Orders O ON (t.ORderno = o.ORdercod AND t.pcode = o.pcode)
                LEFT JOIN jpagreement ag ON T.JID = ag.AgrID
                LEFT JOIN jpersONs JP ON JP.JID = ag.JID
                LEFT JOIN jplISts lst ON lst.lid = t.lstid
                LEFT JOIN TransactLISt R ON T.TransactID = R.TransactID
                LEFT JOIN mONeycAShref ms ON ms.mONeycAShid = r.mONeycAShid
                LEFT JOIN dailyplan D ON T.PlanID = D.DID
                LEFT JOIN dailyplanref dr ON d.plantype = dr.plantype
                LEFT JOIN cAShref C ON T.CAShID = C.CAShID
                LEFT JOIN payref pr ON pr.paycode = 0
                LEFT
                joIN doctOR dt ON dt.dcode = t.dcode
                LEFT JOIN jpersONs clIN ON clIN.jid = t.clINicid
                LEFT JOIN filials fil ON fil.filid = t.filial LEFT JOIN clients cl ON cl.pcode = t.pcode
                WHERE T.pcode > 0
                AND t.Treatdate >= '01.01.2019' AND t.Treatdate <= ? AND t.accesslevel <=1  --ФИЛЬТР ДАТА КОНЕЦ ПЕРИОДА!!!
                
                UNION ALL
                
                SELECT L.LCDate, L.Amount, L.AmountRub, IIF(l.paycode>0, 3,5) AcType, L.LCID, L.DCode, L.AccessLevel, 0.00, 0.00,
                T2.OrderNo, L.TreatCode, 0, L.NumDoc, 0.00, 0.00, L.PayCode, 0, L.TransactID, COALESCE(L.TransactID, 0), 0, 0, L.Filial,
                L.CAShID, IIF(l.paycode > 0, CAST(L.Comment AS VARCHAR(1024)), r.comment), 0, CAST (NULL AS TIMESTAMP), 0, 0,
                r.fIScaldocnumber, r.checknumi,
                l.agrid, 0, ca.planid, CAST(NULL AS VARCHAR(255)), t2.ORderno,
                CAST(D.schnum AS VARCHAR(24)) schnum, R.PaymentNum, L.MONeyCAShID,0,
                pr.shORtname payrefname, CAST(NULL AS VARCHAR(127)),
                jp.jname, jp.jname2,
                CAST(NULL AS VARCHAR(255)) agname, CAST(NULL AS VARCHAR(64)) agnum, CAST(NULL AS VARCHAR(255)) lIStname, dt.fullname,
                0 narid, CAST(NULL AS CHAR(1)) naradsymb, avt.avanstypename, CAST(NULL AS ttext64),
                j.avansparentid, NULL, t2.treatdate, NULL, r.checkprINt, NULL, NULL,
                COALESCE(r.createdate, DATEADD(1 DAY TO r.adate), l.modifydate), dr.checkprINtmode, c.cAShname, CAST(NULL AS VARCHAR(255)), ms.mONeycAShname, fil.fcolOR, CAST(NULL AS VARCHAR(100)), 0, l.bnalpay, cl.fullname
                FROM LoseCredit L
                LEFT JOIN jpersONs jp ON jp.jid = l.jid
                LEFT JOIN jppayments j ON l.lcid = j.treatcode AND l.paycode < 0
                LEFT JOIN CLAvans ca ON ca.PCode = L.PCode AND CA.TransactID = L.TransactID AND CA.ID = L.AvansID AND l.paycode < 0
                LEFT JOIN clavanstype avt ON avt.avanstype = ca.avanstype
                LEFT JOIN cAShref C ON L.CAShID = C.CAShID
                LEFT JOIN dailyplan D ON IIF(J.PlanID > 0, J.Planid, ca.planid) = D.DID
                LEFT JOIN dailyplanref dr ON d.plantype = dr.plantype
                LEFT JOIN Treat T2 ON L.TreatCode = T2.TreatCode
                LEFT JOIN payref pr ON pr.paycode = L.paycode
                LEFT JOIN doctOR dt ON dt.dcode = l.dcode
                LEFT JOIN TransactLISt R ON L.TransactID = R.TransactID
                LEFT JOIN mONeycAShref ms ON ms.mONeycAShid = r.mONeycAShid
                LEFT JOIN filials fil ON fil.filid = l.filial LEFT JOIN clients cl ON cl.pcode = l.pcode
                WHERE L.pcode > 0
                AND l.LCdate >= '01.01.2019' AND l.LCdate <= ? AND l.accesslevel <=1  --ФИЛЬТР ДАТА КОНЕЦ ПЕРИОДА!!!
                
                UNION ALL
                
                SELECT CA.PayDate, CA.AmountUE, CA.AmountRub, IIF(ca.treatcode > 0, 4, 2) AcType, CA.ID, CA.DCode, CA.AccessLevel, 0.00, 0.00,
                TC.OrderNo, CA.TreatCode, 0, ca.numdoc, 0.00, 0.00, IIF(ca.treatcode > 0, CAST(100 + CA.TypeOper AS smallINt),CAST(200 + CA.TypeOper AS smallINt)) paycode, CA.PairID,
                CA.TransactID,CA.TransactID, CAST(CA.IsFamily AS Integer), 0, CA.Filial, CA.CAShID,
                R.Comment, CA.Transf_Type, CAST (NULL AS TIMESTAMP), 0, 0,
                r.fIScaldocnumber, r.checknumi,
                ca.agrid, CA.PCode1, CA.PlanID, CAST(CA.Certific_Num AS VARCHAR(255)), 0,
                CAST(D.schnum AS VARCHAR(24)) schnum, R.PaymentNum, CA.MONeyCAShID, ca.avanstype,
                CAST(NULL AS VARCHAR(24)) payrefname, CAST(NULL AS VARCHAR(127)),
                jp.jname, jp.jname2,
                CAST(NULL AS VARCHAR(255)) agname, CAST(NULL AS VARCHAR(64)) agnum, CAST(NULL AS VARCHAR(255)) lIStname, dt.fullname,
                0 narid, CAST(NULL AS CHAR(1)) naradsymb, avt.avanstypename, CAST(NULL AS ttext64), 0, NULL, NULL,
                ca.pcode1, r.checkprINt, NULL, CAST(cl.fullname AS VARCHAR(1024)), COALESCE(r.createdate, DATEADD(1 DAY TO r.adate), ca.modifydate), dr.checkprINtmode, c.cAShname, CAST(cORrfil.shORtname AS VARCHAR(255)), ms.mONeycAShname, fil.fcolOR, CAST(NULL AS VARCHAR(100)), ca.CORrespFilial, ca.bnalpay, cl0.fullname
                FROM ClAvans CA
                LEFT JOIN Treat TC ON (CA.TreatCode = TC.TreatCode AND CA.PCode = TC.PCode)
                LEFT JOIN TransactLISt R ON CA.TransactID = R.TransactID
                LEFT JOIN mONeycAShref ms ON ms.mONeycAShid = r.mONeycAShid
                LEFT JOIN dailyplan D ON CA.PlanID = D.DID
                LEFT JOIN dailyplanref dr ON d.plantype = dr.plantype
                LEFT JOIN cAShref C ON CA.CAShID = C.CAShID
                LEFT JOIN doctOR dt ON dt.dcode = ca.dcode
                LEFT JOIN clavanstype avt ON avt.avanstype = ca.avanstype
                LEFT JOIN filials fil ON fil.
                filid = ca.filial
                LEFT JOIN INcom i  ON (I.PCode = CA.Pcode AND I.AvansID = CA.ID)
                LEFT JOIN jpersONs jp ON jp.jid = ca.jid
                LEFT JOIN clients cl ON cl.pcode = ca.pcode1
                LEFT JOIN filials cORrfil ON cORrfil.filid = ca.CORrespFilial LEFT JOIN clavans ca_pair ON ca_pair.id = ca.pairid LEFT JOIN clients cl0 ON cl0.pcode = ca.pcode
                WHERE CA.pcode > 0
                AND CA.Paydate >= '01.01.2019' AND CA.PayDate <= ? AND  --ФИЛЬТР ДАТА КОНЕЦ ПЕРИОДА!!!
                CA.accesslevel <=1  AND ca.amountrub > 0
                AND i.paynum IS NULL
                AND ( ca.typeoper = 3 OR (CA.TreatCode IS NOT NULL AND CA.TypeOper IN (1,4)) OR (1 = 1 AND CA.Transfer = 1 AND CA.Transf_Type IN (1,7) AND (CA.IsFamily=0 OR CA.PCode <> CA.PCode1)) OR (CA.TypeOPer IN (101,102) AND CA.TreatCode IS NULL AND CA.PlanID > 0) OR (1 = 1 AND CA.Transfer = 1 AND ((CA.Transf_Type IN (2,3,4,6,7) AND CA.TypeOper = 1) OR (CA.Transf_Type = 5))))
                
                UNION ALL
                
                SELECT j.PMDate, J.Amount, J.AmountRub, 6 AcType, J.PID, J.DCode, J.AccessLevel, 0.00, 0.00,
                tc.ORderno, J.TreatCode, 0, 0, 0.00, 0.00,CAST(j.opertype AS smallINt), 0,J.TransactID, J.TransactID,
                0, J.IncRef, J.Filial, J.CAShID, R.Comment, 0,
                CAST (NULL AS TIMESTAMP), 0, 0,
                r.fIScaldocnumber, r.checknumi,
                J.AgrID, 0, 0, CAST(NULL AS VARCHAR(255)), 0, CAST(d.schnum AS VARCHAR(24)) schnum,
                R.PaymentNum, r.mONeycAShid,J.AvansType, CAST(NULL AS VARCHAR(24)) payrefname,
                LEFT(INcr.dicname, 127) INcrefname,
                jp.jname, jp.jname2, ag.agname, ag.agnum, CAST(NULL AS VARCHAR(255)) lIStname, dt.fullname,
                0 narid, CAST(NULL AS CHAR(1)) naradsymb, CAST(NULL AS ttext128) avanstypename, CAST(NULL AS ttext64),
                j.avansparentid, j.InitialAmountRub, NULL, NULL, r.checkprINt,
                r.detcomment, NULL, COALESCE(r.createdate, DATEADD(1 DAY TO r.adate), j.modifydate),
                dr.checkprINtmode, c.cAShname, CAST(NULL AS VARCHAR(255)), ms.mONeycAShname, fil.fcolOR, CAST(NULL AS VARCHAR(100)), 0, j.bnalpay, cl.fullname
                FROM JPPayments J
                LEFT JOIN dailyplan D ON J.PlanID  = D.DID
                LEFT JOIN dailyplanref dr ON d.plantype = dr.plantype
                LEFT JOIN TransactLISt R ON J.TransactID = R.TransactID
                LEFT JOIN mONeycAShref ms ON ms.mONeycAShid = r.mONeycAShid
                LEFT JOIN cAShref C ON J.CAShID = C.CAShID
                LEFT JOIN jpagreement ag ON J.AgrID = ag.AgrID
                LEFT JOIN jpersONs JP ON JP.JID = ag.JID
                LEFT JOIN doctOR dt ON dt.dcode = j.dcode
                LEFT JOIN filials fil ON fil.filid = j.filial LEFT JOIN dicINfo INcr ON (INcr.refid=23 AND INcr.dicid = j.INcref)
                LEFT JOIN treat tc ON tc.treatcode = j.return_treatcode LEFT JOIN clients cl ON cl.pcode = j.pcode
                WHERE J.pcode > 0
                AND J.PMdate >= '01.01.2019' AND J.PMDate <= ? AND (J.OperType = '5' OR J.OperType = '8') --ФИЛЬТР ДАТА КОНЕЦ ПЕРИОДА!!!
                ORDER BY 1,59,18,16  ) res1
                
                GROUP BY res1.client_name, res1.doctname, res1.ORderCod) res2
                
                GROUP BY r3_ORderCod, res2.r2client_name, res2.r2doctname, vse_Avansi, vozvrat_patient, vozvrati_patient) res3
                
                
                
                GROUP BY r3client_name) res4
                
                WHERE
                (res4.dolg != 0 OR
                res4.avans != 0)
                AND res4.r3client_name IS NOT NULL
                """);

        return jdbcTemplate.query(sb.toString(), ROW_MAPPER_BUH_5, end, end, end, end, end);
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
