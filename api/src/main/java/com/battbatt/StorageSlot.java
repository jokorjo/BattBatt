package com.battbatt.entity;

import jakarta.persistence.*;

@Entity
public class StorageSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;      // A, B, C, A1, B2 jne

    private double capacity;  // 10 (pack), 1 (module), 100 (open)

    @ManyToOne
    private Storage storage;

    // ===== GETTERIT & SETTERIT =====

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCapacity() { return capacity; }
    public void setCapacity(double capacity) { this.capacity = capacity; }

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }
}
