package ru.bstrdn.report.web;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.bstrdn.report.model.Report_1;
import ru.bstrdn.report.repository.JdbcReportRepository;

import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = ReportRestController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportRestController {
    static final String REST_URL = "/rest";


    private JdbcReportRepository rep;

    @GetMapping("/report1")
    public List<Report_1> report_1 (
            @RequestParam @Nullable String startDate,
            @RequestParam @Nullable String endDate,
//            @RequestParam @Nullable Integer limit,
//            @RequestParam @Nullable Integer page,
//            @RequestParam @Nullable String name,
//            @RequestParam @Nullable String sity,
            Model model) {
        if (startDate == null) {
            startDate = "2020-12-01";
        }
        if (endDate == null) {
            endDate = "2020-12-02";
        }
//        System.out.println(rep.queryReport_1(LocalDate.parse("2018-02-26"), LocalDate.now()));
        //"2018-02-26 08:43:48", "2020-02-26 08:43:48");
        return rep.queryReport_1(startDate + " 00:00:00", endDate + " 23:59:59");
    }

//    @GetMapping("/filter")
//    public List<Report_1> getBetween(
//            @RequestParam @Nullable LocalDate startDate,
//            @RequestParam @Nullable LocalDate endDate) {
//        return rep.getBetween(startDate, endDate);
//    }

}