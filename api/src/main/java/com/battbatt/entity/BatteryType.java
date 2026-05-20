package com.battbatt.entity;

import jakarta.persistence.*;

@Entity
public class BatteryType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // "NMC Battery 1"

    private String type;        // PACK / MODULE
    private String chemistry;   // NMC / LFP / OTHER

    private String classification; // 🔥 CRITICAL / STABLE

    private double length;
    private double width;
    private double height;

    private double weight;

    private int voltage;
    private double kwh;

    private int mechanicalTime;
    private int preparationTime;

    // ===== GETTERIT & SETTERIT =====

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getChemistry() { return chemistry; }
    public void setChemistry(String chemistry) { this.chemistry = chemistry; }

    // 🔥 UUSI
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public int getVoltage() { return voltage; }
    public void setVoltage(int voltage) { this.voltage = voltage; }

    public double getKwh() { return kwh; }
    public void setKwh(double kwh) { this.kwh = kwh; }

    public double getAh() {
        if (voltage == 0) return 0;
        return (1000 * kwh) / voltage;
    }

    public int getMechanicalTime() { return mechanicalTime; }
    public void setMechanicalTime(int mechanicalTime) { this.mechanicalTime = mechanicalTime; }

    public int getPreparationTime() { return preparationTime; }
    public void setPreparationTime(int preparationTime) { this.preparationTime = preparationTime; }

    // 🔥 BONUS: tilavuus
    public double getVolume() {
        return length * width * height;
    }
}
