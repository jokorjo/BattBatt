package com.battbatt.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@Entity
@PlanningEntity
public class Battery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String barcode;

    // 🔥 UUSI (oikea paikka)
    private String classification = "STABLE"; // default

    @ManyToOne
    private BatteryType batteryType;

    // 🔥 OPTIMOITAVA
    @PlanningVariable(valueRangeProviderRefs = "slotRange")
    @ManyToOne
    @JsonIgnoreProperties("storage")
    private StorageSlot storageSlot;

    // ===== GETTERIT & SETTERIT =====

    public Long getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public BatteryType getBatteryType() {
        return batteryType;
    }

    public void setBatteryType(BatteryType batteryType) {
        this.batteryType = batteryType;
    }

    public StorageSlot getStorageSlot() {
        return storageSlot;
    }

    public void setStorageSlot(StorageSlot storageSlot) {
        this.storageSlot = storageSlot;
    }
}
