package ru.bstrdn.report.fireBird.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.bstrdn.report.fireBird.model.Report_akt_sverki;
import ru.bstrdn.report.fireBird.model.Report_akt_sverki_info;
import ru.bstrdn.report.fireBird.model.Report_buh_1;
import ru.bstrdn.report.fireBird.model.Report_buh_3;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * БУХГЕЛТЕРСКИЕ ОТЧЕТЫ:
 * 1. Сертификаты по дате выдачи {@link #queryReport_buh_1}
 * 2. Сертификаты по дате оплаты {@link #queryReport_buh_3}
 * 3. Акт сверки {@link #queryReport_akt_sverki}
 */

@Slf4j
@Repository
@Transactional(readOnly = true)
public class JdbcBuhRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<Report_buh_1> ROW_MAPPER_BUH = BeanPropertyRowMapper.newInstance(Report_buh_1.class);
    private static final RowMapper<Report_akt_sverki> ROW_MAPPER_AKT_SVERKI = BeanPropertyRowMapper.newInstance(Report_akt_sverki.class);
    private static final RowMapper<Report_akt_sverki_info> ROW_MAPPER_AKT_SVERKI_INFO = BeanPropertyRowMapper.newInstance(Report_akt_sverki_info.class);
    private static final RowMapper<Report_buh_3> ROW_MAPPER_BUH_3 = BeanPropertyRowMapper.newInstance(Report_buh_3.class);

    public JdbcBuhRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    /** БУХГАЛТЕРСКИЙ ОТЧЕТ **
     * 1. Сертификаты по дате выдачи
     */
    public List<Report_buh_3> queryReport_buh_3(Integer certId, String startDate, String endDate) {
        return jdbcTemplate.query("""
                SELECT
                IIF (cav.certific_num IS NULL OR cav.certific_num = '', 'Б/Н', cav.certific_num)  number_cert,
                cl.fullname,
                SUM (cav.amountrub) summ,
                COALESCE (tmp_res.r_amountrub, 0) rashod
                --, cer.cname name_cert
                                
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
                AND cav.pcode = ? --ID сертификата
                AND cav.paydate BETWEEN ? AND ?
                GROUP BY cl.fullname, COALESCE(tmp_res.r_amountrub, 0), number_cert --, cer.cname
                ORDER BY cl.fullname
                """, ROW_MAPPER_BUH_3, startDate, endDate, certId, startDate, endDate);
    }


    /** БУХГАЛТЕРСКИЙ ОТЧЕТ **
     * 2. Сертификаты по дате оплаты
     *
     * @param certId  ID серификата
     * @param endDate цена
     */
    public List<Report_buh_1> queryReport_buh_1(Integer certId, String startDate, String endDate) {
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


        List<Report_buh_1> report_buh_1 = jdbcTemplate.query(stringBuilder.toString(), ROW_MAPPER_BUH,
                certId, avanstype, startDate, endDate, avanstype, startDate, certId);

        return report_buh_1;
    }


    /** БУХГАЛТЕРСКИЙ ОТЧЕТ **
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
                '' cred
                
                FROM treat t
                JOIN jpagreement dog ON t.jid = dog.agrid
                JOIN jpersons pers ON dog.jid = pers.jid
                JOIN jpersons pers_clinic ON t.clinicid = pers_clinic.jid
                
                WHERE pers.jid = ? --2 Фильтр по юр лицам
                AND t.amountjp_disc != 0
                AND t.treatdate BETWEEN ? AND ?  --1Фильтр дата

                UNION ALL
                
                SELECT
                            --jp.accid id,
                            CAST (IIF (EXTRACT (DAY FROM jp.pdate) < 10, '0' || EXTRACT (DAY FROM jp.pdate) || '.', EXTRACT (DAY FROM jp.pdate) || '.') ||
                            IIF (EXTRACT (MONTH FROM jp.pdate) < 10, '0' || EXTRACT (MONTH FROM jp.pdate) || '.', EXTRACT (MONTH FROM jp.pdate) || '.') ||
                            EXTRACT (YEAR FROM jp.pdate) AS DATE)  date_s,
                            IIF (jp.OperType = 2, 'Платежный документ № ' || COALESCE (jp.numdoc, 'б/н'), IIF (jp.OperType = 4,
                            'Возврат. Документ № ' || COALESCE (jp.numdoc, 'б/н'), 'Не известная операция')) number_o,
                            CAST (IIF (jp.OperType = 2, 'Плательщик ' || pers.jname, IIF (jp.OperType = 4, 'Получатель ' || pers.jname, 'Плательщик ' || pers.jname)) AS char(254))   name_o,
                            '' ff,
                            CAST (ROUND (IIF (jp.OperType = 2, jp.amountrub, IIF (jp.OperType = 4, -jp.amountrub, jp.amountrub)), 0) AS INTEGER) debit_o
                
                            FROM  jaccpay jp
                            LEFT JOIN jpersons pers ON jp.jid = pers.jid
                            WHERE jp.pdate >= ? AND jp.pdate <= ?     --1фильтр дата
                            AND jp.jid = ? ) result  --2Фильтр по Юр. лицам
                
                ORDER BY  1
                                """, ROW_MAPPER_AKT_SVERKI, orgId, start, end, start, end, orgId);
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
    public Report_akt_sverki_info getInfoForAktSverki(Integer orgId, String startDate, String end) {
        Timestamp start = Timestamp.valueOf(startDate);
//        Timestamp end = Timestamp.valueOf(endDate);
        return jdbcTemplate.query("""
                SELECT
                start_per1.org1,
                start_per1.org1_full,
                start_per1.org1_dir,
                start_per2.org2,
                start_per2.org2_full,
                COALESCE (start_per2.debit_o, 0) - COALESCE (start_per1.sum_uslug, 0) debt_before,
                (COALESCE (start_per2.debit_o, 0) + COALESCE (end_per2.debit_o, 0)) -
                (COALESCE (start_per1.sum_uslug, 0) + COALESCE (end_per1.sum_uslug, 0)) debt_after,
                COALESCE (end_per1.sum_uslug, 0)org1_oborot,
                COALESCE (end_per2.debit_o, 0) org2_oborot
                
                FROM
                --Таблица выбирает все лечения начисленные на организацию ДО даты выбранного периода акта сверки и суммирует оплаты
                (SELECT
                pers_clinic.jname org1,
                pers_clinic.jname ||', '||'ИНН '||pers_clinic.jinn ||', '||pers_clinic.jaddr  org1_full,
                pers_clinic.jheader org1_dir,
                SUM (CAST (ROUND (t.amountjp_disc, 0) AS INTEGER)) sum_uslug
                FROM treat t
                JOIN jpagreement dog ON t.jid = dog.agrid
                JOIN jpersons pers ON dog.jid = pers.jid
                JOIN jpersons pers_clinic ON t.clinicid = pers_clinic.jid
                WHERE pers.jid = ? --2 Фильтр по юр лицам 1
                AND t.amountjp_disc != 0
                AND t.treatdate < ?   --Фильтр дата (Подставляем начало периода) 2
                GROUP BY org1, org1_full, org1_dir) start_per1
                JOIN
                --Таблица Выбирает оплаты от организиции ДО даты выбранного периода акта сверки и суммирует оплаты
                (SELECT
                pers.jname org2,
                pers.jname ||', '||'ИНН '||
                COALESCE (pers.jinn, 'Укажите в справочнике Инфодента') ||', '||
                COALESCE (pers.jaddr, 'Укажите АДРЕС в справочнике Инфодента')  org2_full,
                COALESCE (pers.jheader, 'Не указан') org2_dir,
                SUM (CAST (ROUND (IIF (jp.OperType = 2, jp.amountrub, IIF (jp.OperType = 4, -jp.amountrub, jp.amountrub)), 0) AS INTEGER)) debit_o
                FROM  jaccpay jp
                LEFT JOIN jpersons pers ON jp.jid = pers.jid
                WHERE jp.pdate < ?     --Фильтр дата (Подставляем начало периода) 3
                AND jp.jid = ?       --2Фильтр по Юр. лицам 4
                GROUP BY org2, org2_full, org2_dir) start_per2  ON 1=1
                LEFT JOIN
                --Таблица выбирает все лечения начисленные на организацию В ВЫБРАННЫЙ период акта сверки и суммирует оплаты
                (SELECT
                COALESCE (pers_clinic.jname, '') org1,
                COALESCE (SUM (CAST (ROUND (t.amountjp_disc, 0) AS INTEGER)), 0) sum_uslug
                FROM treat t
                JOIN jpagreement dog ON t.jid = dog.agrid
                JOIN jpersons pers ON dog.jid = pers.jid
                JOIN jpersons pers_clinic ON t.clinicid = pers_clinic.jid
                WHERE pers.jid = ? --2 Фильтр по юр лицам 5
                AND t.amountjp_disc != 0
                AND t.treatdate >= ? AND t.treatdate <= ?  --Фильтр дата (Подставляем выбранный период) 6 7
                GROUP BY org1) end_per1 ON start_per1.org1 =  end_per1.org1
                LEFT JOIN
                --Таблица Выбирает оплаты от организиции В ВЫБРАННЫЙ период акта сверки и суммирует оплаты
                (SELECT
                COALESCE (pers.jname, '') org2,
                COALESCE (SUM (CAST (ROUND (IIF (jp.OperType = 2, jp.amountrub, IIF (jp.OperType = 4, -jp.amountrub, jp.amountrub)), 0) AS INTEGER)), 0) debit_o
                FROM  jaccpay jp
                LEFT JOIN jpersons pers ON jp.jid = pers.jid
                WHERE jp.pdate >= ? AND jp.pdate <= ?    --Фильтр дата (Подставляем выбранный период) 8 9 
                AND jp.jid = ?       --2Фильтр по Юр. лицам 10
                GROUP BY org2)end_per2 ON start_per2.org2 = end_per2.org2
                """, ROW_MAPPER_AKT_SVERKI_INFO, orgId, start, start, orgId, orgId, start, end, start, end, orgId).get(0);
    }
}
