package ru.bstrdn.report.fireBird.model;

import lombok.Data;
import oracle.sql.DATE;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

@Data
public class Report_buh_4 {

    LocalDate data_spis;
    String    fio_pat;
    String    fio_doc;
    LocalDate data_nachisl;
    Double    sum_nachisl;
    Double    sum_spis;
    LocalDate data_zn;
    String    code;

}