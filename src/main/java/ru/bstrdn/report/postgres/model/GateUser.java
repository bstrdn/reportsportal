package ru.bstrdn.report.postgres.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "gate_users")
@Data
@NoArgsConstructor
public class GateUser {

    @Id
    Integer UserPtr;
    String Number;
    String LastName;
    String FirstName;
    String FatherName;
    String GroupPtr;
    String LastUsed;
    String LastUsedRdrName;
    String LastUsedRdrPtr;
    String LastUsedEvent;
    String Details1;
    String Details2;
    String Details3;
    String Details4;
    String Details5;
    String Details6;
    String Details7;
    String Details8;
}
