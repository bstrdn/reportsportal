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
import ru.bstrdn.report.fireBird.model.Report_akt_sverki;
import ru.bstrdn.report.fireBird.model.Report_buh_1;
import ru.bstrdn.report.fireBird.model.Report_buh_4;
import ru.bstrdn.report.fireBird.model.Report_buh_5;
import ru.bstrdn.report.fireBird.repository.JdbcBuhRepository;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping(value = BuhRestController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class BuhRestController {
    static final String REST_URL = "/rest";

    private JdbcBuhRepository buhRepository;


    @GetMapping("/report_buh_3")
    public List<Report_buh_1> report_cert(
            @RequestParam @Nullable String startDate,
            @RequestParam @Nullable String endDate,
            @RequestParam(defaultValue = "report_buh_3") String reportName,
            @RequestParam(defaultValue = "0") Integer sertId,
            Model model) {

        if (startDate == null) {
            startDate = "2020-01-01";
        }
        if (endDate == null) {
            endDate = WebUtil.getEndDate();
        }
        return buhRepository.queryReport_buh_3(sertId, startDate + " 00:00:00", endDate + " 00:00:00");
    }

    @GetMapping("/akt_sverki")
    public List<Report_akt_sverki> art_sverki(
            @RequestParam @Nullable String startDate,
            @RequestParam @Nullable String endDate,
            @RequestParam(defaultValue = "art_sverki") String reportName,
            @RequestParam(defaultValue = "0") Integer orgId,
            Model model) {

        if (startDate == null) {
            startDate = "2020-01-01";
        }
        if (endDate == null) {
            endDate = WebUtil.getEndDate();
        }
        return buhRepository.queryReport_akt_sverki(orgId, startDate + " 00:00:00", endDate + " 00:00:00");
    }

    @GetMapping("/report_buh_4")
    public List<Report_buh_4> report_buh_4(
            @RequestParam @Nullable String startDate,
            @RequestParam @Nullable String endDate,
            @RequestParam(defaultValue = "report_buh_4") String reportName,
            @RequestParam(defaultValue = "0") Integer dcode,
            Model model) {

        if (startDate == null) {
            startDate = "2020-01-01";
        }
        if (endDate == null) {
            endDate = WebUtil.getEndDate();
        }
        return buhRepository.queryReport_buh_4(dcode, startDate + " 00:00:00", endDate + " 00:00:00");
    }

    @GetMapping("/report_buh_5")
    public List<Report_buh_5> report_buh_5(
            @RequestParam @Nullable String endDate,
            @RequestParam(defaultValue = "report_buh_5") String reportName,
            Model model) {

        if (endDate == null) {
            endDate = WebUtil.getEndDate();
        }
        return buhRepository.queryReport_buh_5( endDate + " 00:00:00");
    }

}