package ru.bstrdn.report.fireBird.model;

import lombok.Data;

@Data
public class Report_call_1 {
    String name;
    Integer call_out;
    Integer call_in;
    Integer all_call;
    Integer in_sched;
    Integer out_sched;
    Integer procent;
}
