package ru.bstrdn.report.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    @GetMapping("/")
    public String home (Model model) {
        model.addAttribute("title", "Главная страница");
        model.addAttribute("fioRegistratora", List.of("Иванов", "Petrov", "Андреищев"));
        return "report_1";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
