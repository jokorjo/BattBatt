package com.battbatt.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class StorageSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // A, B, C, A1...

    // 🔥 TÄRKEÄ: capacity on fyysinen tilavuus (m³ tms.)
    private double capacity;

    @ManyToOne
    @JsonIgnoreProperties("slots")
    private Storage storage;

    // 🔥 SCALE FACTOR (yhtenäinen koko sovelluksessa)
    public static final int SCALE = 1000;

    // ===== GETTERIT & SETTERIT =====

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCapacity() { return capacity; }
    public void setCapacity(double capacity) { this.capacity = capacity; }

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }

    // 🔥 KÄYTÄTÄÄN SOLVERISSA (EI PYÖRISTYSBUGIA)
    public int getScaledCapacity() {
        return (int) Math.round(capacity * SCALE);
    }

    // 🔥 DEBUG HELPPONA (erittäin hyödyllinen)
    @Override
    public String toString() {
        return "Slot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", scaled=" + getScaledCapacity() +
                ", storage=" + (storage != null ? storage.getName() : "null") +
                '}';
    }
}
