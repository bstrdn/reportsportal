package ru.bstrdn.report.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Report_1 {
    String fullname;
    LocalDate fixdate;
    LocalDate workdate;
    String docFullname;
    String phone1;
}
