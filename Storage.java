package com.example.battbatt.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String qrCode;
    private int maxCapacity;
    private int usedCapacity;

    @OneToMany(mappedBy = "storage")
    private List<Battery> batteries;

    // Getterit ja setterit
}
