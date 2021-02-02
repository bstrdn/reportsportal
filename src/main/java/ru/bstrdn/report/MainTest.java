package ru.bstrdn.report;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class MainTest {

    public static void main(String[] args) {
        LocalDate tenDaysAgo = LocalDate.now().minusDays(1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYMMdd");
//       SimpleDateFormat dateFormat = new SimpleDateFormat("YYMMdd");
        log.info("n" + dateTimeFormatter.format(tenDaysAgo) + ".mdb");
    }
}
