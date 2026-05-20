package com.battbatt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Storage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // esim "NMC Pack Storage"

    private String chemistry;   // NMC, LFP, ANY

    private String storageType; // PACK, PALLET, OPEN

    // 🔥 estää infinite loop JSONissa
    @OneToMany(mappedBy = "storage")
    @JsonIgnore
    private List<StorageSlot> slots;

    // ===== GETTERIT & SETTERIT =====

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChemistry() { return chemistry; }
    public void setChemistry(String chemistry) { this.chemistry = chemistry; }

    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }
}
