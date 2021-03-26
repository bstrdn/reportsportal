package ru.bstrdn.report.postgres.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "act_acc")
public class ActAcc {

    @Id
    @SequenceGenerator(name = "act_acc_id_seq", sequenceName = "act_acc_id_seq", allocationSize = 1)
    @Column(name = "id_act", unique = true, nullable = false, columnDefinition = "integer default nextval('act_acc_id_seq')")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "act_acc_id_seq")
    Integer id_act;
    @Column(name = "date_act", columnDefinition = "now()")
    LocalDate date_act = LocalDate.now();
    String org;
    String period;
    String file_name;

    public ActAcc(String org, String period, String file_name) {
        this.org = org;
        this.period = period;
        this.file_name = file_name;
    }
}
