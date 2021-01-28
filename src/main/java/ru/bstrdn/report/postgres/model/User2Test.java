package ru.bstrdn.report.postgres.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MEALS")
@Data
public class User2Test {
    @Id
    private int id;
    private String description;
}