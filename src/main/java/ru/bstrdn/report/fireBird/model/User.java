package ru.bstrdn.report.fireBird.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "doctor")
@Data
public class User {
    @Id
    private int dcode;
    private String dname;
    private String dpasswrd;
    private String doctcode;
}
