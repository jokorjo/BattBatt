package com.battbatt.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class StorageSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // A, B, C, A1...

    // 🔥 TILAVUUS (m³)
    private double capacity;

    // 🔥 UUSI: LATTIAPINTA (m²)
    private double floorCapacity;

    // 🔥 UUSI: MAX STACK (kerrokset)
    private int maxStack;

    @ManyToOne
    @JsonIgnoreProperties("slots")
    private Storage storage;

    // 🔥 SCALE FACTOR
    public static final int SCALE = 1000;

    // ===== GETTERIT & SETTERIT =====

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCapacity() { return capacity; }
    public void setCapacity(double capacity) { this.capacity = capacity; }

    public double getFloorCapacity() { return floorCapacity; }
    public void setFloorCapacity(double floorCapacity) { this.floorCapacity = floorCapacity; }

    public int getMaxStack() { return maxStack; }
    public void setMaxStack(int maxStack) { this.maxStack = maxStack; }

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }

    // 🔥 SCALED VOLUME
    public int getScaledCapacity() {
        return (int) Math.round(capacity * SCALE);
    }

    // 🔥 DEBUG
    @Override
    public String toString() {
        return "Slot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", floorCapacity=" + floorCapacity +
                ", maxStack=" + maxStack +
                ", scaled=" + getScaledCapacity() +
                ", storage=" + (storage != null ? storage.getName() : "null") +
                '}';
    }
}
