package com.battbatt.solver;

import com.battbatt.entity.Battery;
import com.battbatt.entity.StorageSlot;
import org.optaplanner.core.api.domain.solution.*;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@PlanningSolution
public class BatteryPlan {

    @PlanningEntityCollectionProperty
    private List<Battery> batteryList;

    @ValueRangeProvider(id = "slotRange")
    @ProblemFactCollectionProperty
    private List<StorageSlot> slotList;

    @PlanningScore
    private HardSoftScore score;

    public List<Battery> getBatteryList() {
        return batteryList;
    }

    public void setBatteryList(List<Battery> batteryList) {
        this.batteryList = batteryList;
    }

    public List<StorageSlot> getSlotList() {
        return slotList;
    }

    public void setSlotList(List<StorageSlot> slotList) {
        this.slotList = slotList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
