package com.battbatt.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Device {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    // 🔥 EAGER ok tässä (tarvitset solverille)
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DeviceProfile> profiles = new ArrayList<>();

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<DeviceProfile> getProfiles() { return profiles; }
    public void setProfiles(List<DeviceProfile> profiles) { this.profiles = profiles; }
}
