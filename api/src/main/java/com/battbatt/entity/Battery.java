package com.battbatt.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.entity.PlanningPin;

@Entity
@PlanningEntity
public class Battery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String barcode;

    // 🔥 Pakollinen kenttä (ei null DB:ssä)
    @Column(nullable = false)
    private String classification = "STABLE";

    @ManyToOne
    private BatteryType batteryType;

    // 🔒 PINNED → solver EI saa siirtää jos true
    @PlanningPin
    private boolean pinned = false;

    // 🔥 OPTIMOITAVA (solver muuttaa tätä vain jos pinned = false)
    @PlanningVariable(valueRangeProviderRefs = "slotRange")
    @ManyToOne
    @JoinColumn(name = "storage_slot_id")
    @JsonIgnoreProperties("storage")
    private StorageSlot storageSlot;

    // 🔥 ONKO AKKU KÄSITTELYSSÄ
    private boolean inProcessing = false;

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

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
    
    public boolean isInProcessing() {
    return inProcessing;
    }

    public void setInProcessing(boolean inProcessing) {
    this.inProcessing = inProcessing;
    }
}
