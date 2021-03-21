package ru.bstrdn.report.fireBird.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Report_buh_1 {

//    @Getter
//    @Setter
//    @JsonSerialize
//    static int sum = 0;

    String fullname;
    String date_reg;
    Integer summ;
    Integer n_saldo;
    String dates_pay;
    Integer r_amountrub;
    Integer k_saldo;
}