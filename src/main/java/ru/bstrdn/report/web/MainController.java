package ru.bstrdn.report.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.bstrdn.report.fireBird.repository.JdbcReportRepository;

@Data
@AllArgsConstructor
@Controller
public class MainController {
    JdbcReportRepository report;

//    @Autowired
//    private UserH2RepositoryTest userH2RepositoryTest;

    @GetMapping("/report_1")
    public String home(Model model) {
        model.addAttribute("allRegistrarWithId", report.getAllRegistrarWithId());
        model.addAttribute("allDepartmentWithId", report.getAllDepartmentWithId());
        model.addAttribute("reportNameRu", "Первичные пациенты");
        model.addAttribute("reportName", "report1");
        return "report_1";
    }

    @GetMapping("/report_2")
    public String report_2(Model model) {
        model.addAttribute("allRegistrarWithId", report.getAllRegistrarWithId());
        model.addAttribute("allDepartmentWithId", report.getAllDepartmentWithId());
        model.addAttribute("reportNameRu", "Второй отчет");
        model.addAttribute("reportName", "report2");
        return "report_1";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("allUsers", report.getAllUsers());
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/user")
    @ResponseBody
    public String user() {
        return "<h1>Welcome User</h1>";
    }

    @GetMapping("/admin")
    @ResponseBody
    public String admin() {
//        userH2RepositoryTest.findByDescription("Админ ужин");
        return "<h1>Welcome Admin</h1>";
    }
}
