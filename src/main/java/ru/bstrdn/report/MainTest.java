package ru.bstrdn.report;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

//@Service
@Slf4j
public class MainTest {

    public static void main(String[] args) {
//        LocalDate tenDaysAgo = LocalDate.now().minusDays(1);
//        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYMMdd");
////       SimpleDateFormat dateFormat = new SimpleDateFormat("YYMMdd");
//        log.info("n" + dateTimeFormatter.format(tenDaysAgo) + ".mdb");
    }

//    @Scheduled(cron = "0 41 17 * * ?")
//    public void test() throws IOException {
//
//        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream("C:/GATE/sv.xlsx"));
//        FileOutputStream fileOut = new FileOutputStream("C:/GATE/new.xlsx");
//        //Sheet mySheet = wb.getSheetAt(0);
//        XSSFSheet sheet1 = wb.getSheet("Summary");
//        XSSFRow row = sheet1.getRow(15);
//        XSSFCell cell = row.getCell(3);
//        cell.setCellValue("Bharthan");
//
//        wb.write(fileOut);
//        log.info("Written xls file");
//        fileOut.close();
//
//    }
}
