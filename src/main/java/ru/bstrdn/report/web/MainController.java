package ru.bstrdn.report.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.bstrdn.report.repository.JdbcReportRepository;

@Data
@AllArgsConstructor
@Controller
public class MainController {
JdbcReportRepository report;

    @GetMapping("/")
    public String home (Model model) {
        model.addAttribute("allRegistrar", report.getAllRegistrar());
//        model.addAttribute("allDepartment", report.getAllDepartment());
        model.addAttribute("allDepartmentWithId", report.getAllDepartmentWithId());
//        List<Map<String, Object>> maptest = report.departments();
//        model.addAttribute("allDepartment2", report.departments());
//        model.addAttribute("fioRegistratora", List.of("Иванов", "Petrov", "Андреищев"));
        model.addAttribute("reportName", "Первичные пациенты");
        return "report_1";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }


    @GetMapping("/home")
    public String home() {
        return "home";
    }



}
