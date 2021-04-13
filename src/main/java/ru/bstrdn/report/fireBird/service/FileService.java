package ru.bstrdn.report.fireBird.service;

import com.github.moneytostr.MoneyToStr;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bstrdn.report.fireBird.model.Report_akt_sverki;
import ru.bstrdn.report.fireBird.model.Report_akt_sverki_info;
import ru.bstrdn.report.fireBird.repository.JdbcBuhRepository;
import ru.bstrdn.report.postgres.model.ActAcc;
import ru.bstrdn.report.postgres.repository.ActAccRepository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class FileService {
    @Autowired
    JdbcBuhRepository buhRepository;
    @Autowired
    ActAccRepository actAccRepository;


    /**
     * Тест акта сверки
     *
     * @throws InterruptedException
     */
    public String generateAktSverki(String startDate1, String endDate1, Integer legalEntitiesWithId, Integer userId) throws Exception {
        final MoneyToStr moneyToStr = new MoneyToStr(MoneyToStr.Currency.RUR, MoneyToStr.Language.RUS, MoneyToStr.Pennies.NUMBER);
        final DateTimeFormatter DTF_TODAY = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        final DateTimeFormatter DTF_START_WITH_DAY = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        final DateTimeFormatter DTF_END_WITH_DAY = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        final DateTimeFormatter DTF_START = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        final DateTimeFormatter DTF_END = DateTimeFormatter.ofPattern("dd.MM.yyyy");
//        final DateTimeFormatter DTF_START = DateTimeFormatter.ofPattern("LLLL YYYY");
//        final DateTimeFormatter DTF_END = DateTimeFormatter.ofPattern("LLLL YYYY");
        final DateTimeFormatter FOR_INFO = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        LocalDate today = LocalDate.now();
        LocalDate sDate = LocalDate.parse(startDate1);
        LocalDate eDate = LocalDate.parse(endDate1);
        String startDate = DTF_START.format(sDate);
        String endDate = DTF_END.format(eDate);
        String startDateInfo = FOR_INFO.format(sDate);
        String endDateInfo = FOR_INFO.format(eDate);

        Report_akt_sverki_info infoForAktSverki = buhRepository.getInfoForAktSverki(legalEntitiesWithId, startDate1 + " 00:00:00", endDate1 + " 00:00:00");
        List<Report_akt_sverki> list = buhRepository.queryReport_akt_sverki(legalEntitiesWithId, startDate1 + " 00:00:00", endDate1 + " 00:00:00");

        String org1 = infoForAktSverki.getOrg1();
        String org1_full = infoForAktSverki.getOrg1_full();
        String org2 = infoForAktSverki.getOrg2();
        String org2_full = infoForAktSverki.getOrg2_full();
        String org1_dir = infoForAktSverki.getOrg1_dir();

        Integer doc_num = 0;
        Double debt_before = Double.parseDouble(infoForAktSverki.getDebt_before().toString());
        Double debt_after = Double.parseDouble(infoForAktSverki.getDebt_after().toString());
        Double org1_oborot = Double.parseDouble(infoForAktSverki.getOrg1_oborot().toString());
        Double org2_oborot = Double.parseDouble(infoForAktSverki.getOrg2_oborot().toString());

        String debt_after_text = moneyToStr.convert(Math.abs(debt_after));
        String todayDate = DTF_TODAY.format(today);
        String startDateWithDay = DTF_START_WITH_DAY.format(sDate);
        String endDateWithDay = DTF_END_WITH_DAY.format(eDate);

        String line2 = "";
        if (startDate.equals(endDate)) {
            line2 = String.format("за %s г.", startDate);
        } else {
            line2 = String.format("за %s г. - %s г.", startDate, endDate);
        }
        String line3 = String.format("между %s и %s", org1, org2);
        String line4 = String.format("""
                   Мы, нижеподписавшиеся, Генеральный директор %s %s, с одной стороны, 
                и  %s, с другой стороны, составили настоящий акт 
                сверки в том, что:
                """, org1, org1_dir, org2);
        String line5 = String.format("   1. В период с %s г. по %s г. " +
                                     "были осуществлены следующие расчеты:", startDateWithDay, endDateWithDay);
        String line6 = String.format("Задолженность по состоянию на %s г.", startDateWithDay);

        String line11 = String.format("Задолженность по состоянию на %s г.", endDateWithDay);
        String line12 = String.format("   2. Таким образом, на %s г.", endDateWithDay);
        String line13 = "";
        if (debt_after > 0) {
            line13 = String.format("долг %s в валюте RUB %s (%s);", org1, debt_after, debt_after_text);
        } else if (debt_after == 0) {
            line13 = "Задолженность отсутствует.";
        } else {
            line13 = String.format("долг %s в валюте RUB %s (%s);", org2, Math.abs(debt_after), debt_after_text);
        }

        String period = startDateInfo + "-" + endDateInfo;
        String org2_txt = org2.replaceAll("[\\\"]", "");
        org2_txt = org2_txt.replaceAll("[\\s]", "_");
        String fileName = org2_txt + "_" + period + ".xlsx";
        ActAcc actAcc = new ActAcc(org2, period, fileName);
        actAccRepository.save(actAcc);
        doc_num = actAcc.getId_act();

        String line1 = String.format("Акт сверки взаимных расчетов № %s от %s г.", doc_num, todayDate);

        fileName = doc_num + "_" + fileName;

        try (XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream("C:/Portal/sv.xlsx"));
             FileOutputStream fileOut = new FileOutputStream("C:/Portal/" + fileName);
        ) {

            XSSFSheet sheet1 = wb.getSheetAt(0);
            sheet1.getRow(0).getCell(0).setCellValue(line1);
            sheet1.getRow(1).getCell(0).setCellValue(line2);
            sheet1.getRow(2).getCell(0).setCellValue(line3);
            sheet1.getRow(4).getCell(0).setCellValue(line4);
            sheet1.getRow(6).getCell(0).setCellValue(line5);
            sheet1.getRow(9).getCell(4).setCellValue(org1);
            sheet1.getRow(9).getCell(6).setCellValue(org2);
            sheet1.getRow(11).getCell(1).setCellValue(line6);
            if (debt_before < 0) {
                sheet1.getRow(11).getCell(4).setCellValue(Math.abs(debt_before));
            } else {
                sheet1.getRow(11).getCell(5).setCellValue(debt_before);
            }
            sheet1.getRow(13).getCell(4).setCellValue(org1_oborot);
            sheet1.getRow(13).getCell(5).setCellValue(org2_oborot);

            if (debt_after >= 0) {
                sheet1.getRow(14).getCell(5).setCellValue(debt_after);
            } else {
                sheet1.getRow(14).getCell(4).setCellValue(Math.abs(debt_after));
            }

            sheet1.getRow(14).getCell(1).setCellValue(line11);
            sheet1.getRow(16).getCell(0).setCellValue(line12);
            sheet1.getRow(17).getCell(0).setCellValue(line13);
            sheet1.getRow(21).getCell(0).setCellValue(org1_full);
            sheet1.getRow(21).getCell(5).setCellValue(org2_full);
            sheet1.getRow(26).getCell(0).setCellValue(org1_dir);

            int deb = 0;
            int cred = 0;
            int startRow = 12;
            int listSize = list.size();
            CellCopyPolicy ccp = new CellCopyPolicy();
            for (Report_akt_sverki report : list) {
                XSSFRow row = sheet1.getRow(startRow);
                LocalDate tempDate = LocalDate.parse(report.getDat());
                row.getCell(0).setCellValue(FOR_INFO.format(tempDate));
                row.getCell(1).setCellValue(report.getDoc());
                row.getCell(3).setCellValue(report.getOrg());

                deb = report.getDeb();
                if (deb != 0) {
                    row.getCell(4).setCellValue(deb);
                } else {
                    row.getCell(4).setCellValue("");
                }
                cred = report.getCred();
                if (cred != 0) {
                    row.getCell(5).setCellValue(cred);
                } else {
                    row.getCell(5).setCellValue("");
                }


                listSize--;
                if (listSize > 0) {
                    sheet1.shiftRows(startRow, sheet1.getLastRowNum(), 1);
                    sheet1.copyRows(startRow + 1, startRow + 1, startRow, ccp);
                }
                startRow++;

            }

            wb.write(fileOut);
            log.info("Written xls file");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }
}
