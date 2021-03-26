package ru.bstrdn.report.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.bstrdn.report.fireBird.repository.JdbcBuhRepository;
import ru.bstrdn.report.fireBird.repository.JdbcReportRepository;
import ru.bstrdn.report.postgres.repository.JdbcSkudRepository;
import ru.bstrdn.report.service.LoggingService;

@Data
@AllArgsConstructor
@Controller
public class MainController {
    JdbcReportRepository report;
    JdbcBuhRepository buhRepository;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private JdbcSkudRepository skudRepository;

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

    @GetMapping("/report_call_1")
    public String report_call_1(Model model) {
        model.addAttribute("select1", report.getAllCallRegistrarWithId());
//        model.addAttribute("allDepartmentWithId", report.getAllDepartmentWithId());
        model.addAttribute("reportNameRu", "По первичным звонкам");
        model.addAttribute("reportName", "report_call_1");
        return "report_call_1";
    }

    @GetMapping("/report_cert")
    public String report_sert(Model model) {
        model.addAttribute("allCertificate", buhRepository.getAllCertWithId());
        model.addAttribute("reportNameRu", "Отчет по сертификатам");
        model.addAttribute("reportName", "report_buh_1");
        return "report_buh_2";
    }

    //Акт сверки
    @GetMapping("/akt_sverki")
    public String report_buh_2(Model model) {
        model.addAttribute("legalEntitiesWithId", buhRepository.getLegalEntitiesWithId());
        model.addAttribute("reportNameRu", "Акт сверки");
        model.addAttribute("reportName", "akt_sverki");
        return "report_akt_sverki";
    }

    //Сертификаты 2
    @GetMapping("/report_buh_1")
    public String report_buh_3(Model model) {
        model.addAttribute("allCertificate", buhRepository.getAllCertWithId());
        model.addAttribute("reportNameRu", "Отчет по сертификатам");
        model.addAttribute("reportName", "report_buh_3");
        return "report_buh_1";
    }

    //    @GetMapping("/report_skud/{groupNum}/{accessLevel}")
    //    public String report_skud_1(@PathVariable String groupNum, @PathVariable String accessLevel, Model model) {
    @GetMapping("/report_skud")
    public String report_skud_1(Model model) {
        model.addAttribute("allSkudUsers", skudRepository.getAllSkudUsers("9", "2"));
//        model.addAttribute("allSkudUsers", skudRepository.getAllSkudUsers(groupNum, accessLevel));
//        model.addAttribute("allCertificate", report.getAllCertWithId());
        model.addAttribute("reportNameRu", "Учет рабочего времени");
        model.addAttribute("reportName", "report_skud_1");
        return "report_skud_1";
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

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("logs", loggingService.getAllByFio(SecurityContextHolder
                .getContext().getAuthentication().getName()));
        return "profile";
    }


//    @GetMapping("/user")
//    @ResponseBody
//    public String user() {
//        return "<h1>Welcome User</h1>";
//    }
//
//    @GetMapping("/admin")
//    @ResponseBody
//    public String admin() {
//        return "<h1>Welcome Admin</h1>";
//    }
}
