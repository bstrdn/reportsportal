package ru.bstrdn.report.postgres.model;

import com.sun.istack.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "logs")
@Data
@NoArgsConstructor
public class Log {

    public Log (String fio, String action) {
        this.fio = fio;
        this.action = action;
    }

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "generator", strategy = "increment")
    @GeneratedValue(generator = "generator")
    private Integer id;

    @Column(name = "fio")
    private String fio;

    @Column(name = "action")
    private String action;

    @Column(name = "date", nullable = false, columnDefinition = "timestamp default now()")
    @NotNull
    private Date date = new Date();
}
