package com.battbatt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class DeviceProfile {

    @Id
    @GeneratedValue
    private Long id;

    // 🔥 ESTÄÄ JSON loopin (TÄRKEIN FIX)
    @ManyToOne
    @JsonIgnore
    private Device device;

    private int minVoltage;
    private int maxVoltage;

    private double maxAmps;

    public Long getId() { return id; }

    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }

    public int getMinVoltage() { return minVoltage; }
    public void setMinVoltage(int minVoltage) { this.minVoltage = minVoltage; }

    public int getMaxVoltage() { return maxVoltage; }
    public void setMaxVoltage(int maxVoltage) { this.maxVoltage = maxVoltage; }

    public double getMaxAmps() { return maxAmps; }
    public void setMaxAmps(double maxAmps) { this.maxAmps = maxAmps; }
}
