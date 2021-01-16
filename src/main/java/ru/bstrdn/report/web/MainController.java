package ru.bstrdn.report.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bstrdn.report.repository.JdbcReportRepository;

@Data
@AllArgsConstructor
@Controller
public class MainController {
JdbcReportRepository report;

    @GetMapping("/")
    public String home (Model model) {
        model.addAttribute("allRegistrarWithId", report.getAllRegistrarWithId());
        model.addAttribute("allDepartmentWithId", report.getAllDepartmentWithId());
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

    @GetMapping("/user")
    public String user() {
        return ("<h1>Welcome User</h1>");
    }

    @GetMapping("/admin")
    public String admin() {
        return ("<h1>Welcome Admin</h1>");
    }
}
