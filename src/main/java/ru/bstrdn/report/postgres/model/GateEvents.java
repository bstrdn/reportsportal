package ru.bstrdn.report.postgres.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gate_events")
@Data
@NoArgsConstructor
public class GateEvents {
    public static final int START_SEQ = 100000;

    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 1, initialValue = START_SEQ)
    @Column(name = "id", unique = true, nullable = false, columnDefinition = "integer default nextval('global_seq')")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    Integer id;
    LocalDateTime DateTime;
    Integer EventType;
    Integer EventCode;
    Integer DevPtr;
    Integer RdrPtr;
    Integer UserPtr;
    Integer OperatorID;
    Integer AlarmStatus;
    String Unit;
    String Message;
    String Name;
}
