package ru.bstrdn.report.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.bstrdn.report.fireBird.model.Report_1;
import ru.bstrdn.report.fireBird.repository.JdbcReportRepository;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(value = ReportRestController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportRestController {
    static final String REST_URL = "/rest";

    private JdbcReportRepository rep;

    @GetMapping("/report1")
    public List<Report_1> report_1(
            @RequestParam @Nullable String startDate,
            @RequestParam @Nullable String endDate,
            @RequestParam(defaultValue = "report1") String reportName,
            @RequestParam(defaultValue = "1") String radio,
            @RequestParam(defaultValue = "0") Integer department,
            @RequestParam(defaultValue = "0") Integer registrar,
            Model model) {
        if (startDate == null) {
            startDate = WebUtil.getStartDate();
        }
        if (endDate == null) {
            endDate = WebUtil.getEndDate();
        }

        return rep.queryReport_1(startDate + " 00:00:00", endDate + " 23:59:59", radio, department, registrar);
    }

    @GetMapping("/report2")
    public List<Report_1> report_2(
            @RequestParam @Nullable String startDate,
            @RequestParam @Nullable String endDate,
            @RequestParam(defaultValue = "report2") String reportName,
            @RequestParam(defaultValue = "1") String radio,
            @RequestParam(defaultValue = "0") Integer department,
            @RequestParam(defaultValue = "0") Integer registrar,
            Model model) {

        if (startDate == null) {
            startDate = WebUtil.getStartDate();
        }
        if (endDate == null) {
            endDate = WebUtil.getEndDate();
        }
        return rep.queryReport_2(startDate + " 00:00:00", endDate + " 23:59:59", radio, department, registrar);
    }
}