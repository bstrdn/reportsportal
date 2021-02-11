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
import ru.bstrdn.report.fireBird.model.Report_buh_1;
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
            //TODO make integer var
            @RequestParam(defaultValue = "1") String radio,
            @RequestParam(defaultValue = "1") Integer filter_combine,
            @RequestParam(defaultValue = "0") Integer department,
            @RequestParam(defaultValue = "0") Integer registrar,
            Model model) {
        if (startDate == null) {
//            startDate = WebUtil.getStartDate();
            startDate = "2018-02-26";
        }
        if (endDate == null) {
//            endDate = WebUtil.getEndDate();
            endDate = "2018-02-26";
        }

        return rep.queryReport_1(startDate + " 00:00:00", endDate + " 23:59:59", radio,
                department, registrar, filter_combine);
    }

    @GetMapping("/report2")
    public List<Report_1> report_2(
            @RequestParam @Nullable String startDate,
            @RequestParam @Nullable String endDate,
            @RequestParam(defaultValue = "report2") String reportName,
            @RequestParam(defaultValue = "1") String radio,
            @RequestParam(defaultValue = "1") Integer filter_combine,
            @RequestParam(defaultValue = "0") Integer department,
            @RequestParam(defaultValue = "0") Integer registrar,
            Model model) {

        if (startDate == null) {
            startDate = WebUtil.getStartDate();
        }
        if (endDate == null) {
            endDate = WebUtil.getEndDate();
        }
        return rep.queryReport_2(startDate + " 00:00:00", endDate + " 23:59:59", radio, department, registrar, filter_combine);
    }

    @GetMapping("/report_buh_1")
    public List<Report_buh_1> report_cert(
            @RequestParam @Nullable String startDate,
            @RequestParam @Nullable String endDate,
            @RequestParam(defaultValue = "report_buh_1") String reportName,
            @RequestParam(defaultValue = "0") Integer sertId,
            Model model) {

        if (startDate == null) {
            startDate = "2020-01-01";
        }
        if (endDate == null) {
            endDate = WebUtil.getEndDate();
        }
        return rep.queryReport_buh_1(sertId, startDate + " 00:00:00", endDate + " 00:00:00");
    }


    @GetMapping("/plug")
    public List<Report_1> plug(Model model) {
        return rep.queryReport_1("2018-02-26 08:43:48", "2018-02-26 08:43:50", "0", 0, 0, 1);
    }
}