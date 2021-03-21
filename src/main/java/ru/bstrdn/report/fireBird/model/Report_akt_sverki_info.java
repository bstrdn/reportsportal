package ru.bstrdn.report.fireBird.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Report_akt_sverki_info {
    String org1;
    String org1_full;
    String org1_dir;
    String org2;
    String org2_full;
    Integer debt_before;
    Integer debt_after;
    Integer org1_oborot;
    Integer org2_oborot;
}
