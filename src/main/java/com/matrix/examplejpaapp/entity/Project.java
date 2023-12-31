package com.matrix.examplejpaapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString
@Table
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    @JoinColumn(name = "student_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Student student;
}
