package ru.bstrdn.report.web;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bstrdn.report.model.Report_1;
import ru.bstrdn.report.repository.JdbcReportRepository;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = ReportRestController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportRestController {
    static final String REST_URL = "/rest";


    private JdbcReportRepository rep;

    @GetMapping("/report1")
    public List<Report_1> report_1 (Model model) {
//        System.out.println(rep.queryReport_1(LocalDate.parse("2018-02-26"), LocalDate.now()));
        //"2018-02-26 08:43:48", "2020-02-26 08:43:48");
        return rep.queryReport_1("2018-02-26 08:43:48", "2020-02-26 08:43:48");
    }

}