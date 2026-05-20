package com.battbatt.entity;

import jakarta.persistence.*;

@Entity
public class Battery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 yksittäisen akun tunniste
    private String barcode;

    // 🔥 linkki akkumalliin (NMC Battery 1 jne)
    @ManyToOne
    private BatteryType batteryType;

    // 🔥 optimoinnin tulos (mihin varastoon menee)
    @ManyToOne
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
