package com.battbatt.solver;

import com.battbatt.entity.Battery;
import com.battbatt.entity.StorageSlot;

import org.optaplanner.core.api.domain.solution.*;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@PlanningSolution
public class BatteryAssignmentSolution {

    // 🔹 kaikki mahdolliset slotit
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "slotRange")
    private List<StorageSlot> storageSlotList;

    // 🔥 nämä optimoidaan
    @PlanningEntityCollectionProperty
    private List<Battery> batteryList;

    // 🔹 score (OptaPlanner täyttää)
    private HardSoftScore score;

    // ===== GETTERIT & SETTERIT =====

    public List<StorageSlot> getStorageSlotList() {
        return storageSlotList;
    }

    public void setStorageSlotList(List<StorageSlot> storageSlotList) {
        this.storageSlotList = storageSlotList;
    }

    public List<Battery> getBatteryList() {
        return batteryList;
    }

    public void setBatteryList(List<Battery> batteryList) {
        this.batteryList = batteryList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
