package ru.bstrdn.report.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Report_1 {
    String fullname;
    //День обращения /сделать Date
    LocalDate fixdate;
//    //Записан на дату /сделать Date
    LocalDate workdate;
//    //Записан к доктору /сделать отдельный класс
    String docFullname;
//    //Проверка статуса отметки о посещении /not used
//    static boolean clvisit;
}
