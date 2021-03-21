package ru.bstrdn.report.gate.configurations;

import com.github.moneytostr.MoneyToStr;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.bstrdn.report.fireBird.model.Report_akt_sverki;
import ru.bstrdn.report.fireBird.repository.JdbcBuhRepository;
import ru.bstrdn.report.gate.GateUtil;
import ru.bstrdn.report.gate.repository.GateRepository;
import ru.bstrdn.report.postgres.repository.GateEventsRepository;
import ru.bstrdn.report.postgres.repository.GateUsersRepository;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class SchedulingTasks {
    @Autowired
    JdbcBuhRepository buhRepository;
    @Autowired
    GateRepository gateRepository;
    @Autowired
    GateUsersRepository gateUsersRepository;
    @Autowired
    GateEventsRepository gateEventsRepository;
    @Autowired
    GateUtil gateUtil;

    @Value("${gate.sourceDir}")
    String sourceDir;
    @Value("${gate.targetDir}")
    String targetDir;

    @Scheduled(cron = "0 0 7 * * ?")
//    @Scheduled(cron = "0 21 14 * * ?")
    public void updateGateUsers() {
        gateUsersRepository.deleteAll();
        gateUsersRepository.saveAll(gateRepository.getGateUsers());
        log.info("GATE user base has been completely updated");
    }

    @Scheduled(cron = "0 0 6 * * ?")
    public void updateGateEvents() {
        try {
            String summary = gateUtil.copyDir(sourceDir, targetDir);
            log.info(summary);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


//достать файл за предыдущий день "n210204.mdb"
//    private final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("YYMMdd");
//       LocalDate tenDaysAgo = LocalDate.now().minusDays(1);
//        log.info("n" + DATETIME_FORMATTER.format(tenDaysAgo) + ".mdb");

//отложенная задача на конкретное время
//    @Scheduled(cron = "0 45 15 * * ?")

//https://spring.io/guides/gs/scheduling-tasks/
//https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm
//@Scheduled(cron = "0 1 1 * * ?")
//Below you can find the example patterns from the spring forum:

//        * "0 0 * * * *" = the top of every hour of every day.
//        * "*/10 * * * * *" = every ten seconds.
//        * "0 0 8-10 * * *" = 8, 9 and 10 o'clock of every day.
//        * "0 0 8,10 * * *" = 8 and 10 o'clock of every day.
//        * "0 0/30 8-10 * * *" = 8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.
//        * "0 0 9-17 * * MON-FRI" = on the hour nine-to-five weekdays
//        * "0 0 0 25 12 ?" = every Christmas Day at midnight
//        Cron expression is represented by six fields:
//
//        second, minute, hour, day of month, month, day(s) of week
//        (*) means match any
//
//        */X means "every X"
//        ? ("no specific value") - useful when you need to specify something in one of the two fields in which the character is allowed, but not the other. For example, if I want my trigger to fire on a particular day of the month (say, the 10th), but I don't care what day of the week that happens to be, I would put "10" in the day-of-month field and "?" in the day-of-week field.
