package com.luxoft.highperformance.bookserver.model;


import lombok.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter @ToString
public class Book {
    @Id
    @GeneratedValue
    private Integer id;
    private String title;

}
