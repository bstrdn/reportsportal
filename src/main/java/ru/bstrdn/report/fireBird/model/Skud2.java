package ru.bstrdn.report.fireBird.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Модель для запроса по времени работы сотрудников по инфоденту
 * Маппинга данных из инфодента
 */
@Getter
@Setter
public class Skud2 {
    String dcode;
    String dname;
    String getdates;
    String startjob;
    String endjob;
    String norma;
}



//public class Skud2 {
//    String date;
//    String vhod;
//    String vihod;
//    String times;
//    Integer sumInfodent;
//}
