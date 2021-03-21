package ru.bstrdn.report.postgres.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Date;

/**
 * Модель для запроса по времени работы сотрудников по инфоденту
 * Маппинга данных из инфодента
 */
@Getter
@Setter
@Entity
@Table(name = "tmp_ident")
@Data
public class Skud3 {
    Integer dcode;
    String dname;
    @Id
    Date getdates;
    Timestamp startjob;
    Timestamp endjob;
    String norma;
}

