package ru.bstrdn.report.fireBird.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Report_1 {
    String fullname;
    LocalDate createdate;
    LocalDate workdate;
    String docFullname;
    String phone1;
}
