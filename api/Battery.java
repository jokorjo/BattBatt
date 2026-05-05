package com.example.battbatt.entity;

import jakarta.persistence.*;

@Entity
public class Battery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String barcode;
    private String type; // "stabiili" tai "kriittinen"
    private int width;
    private int height;
    private int depth;

    @ManyToOne
    private Storage storage;

    // Getterit ja setterit
}
