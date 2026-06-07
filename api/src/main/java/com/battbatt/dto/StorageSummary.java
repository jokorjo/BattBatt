package com.battbatt.dto;

public class StorageSummary {

    private String storageName;
    private String slotName;
    private String chemistry;
    private double totalWeight;
    private int batteryCount;
    private double utilization;

    public StorageSummary(String storageName, String slotName, String chemistry, double totalWeight, int batteryCount, double utilization) {
        this.storageName = storageName;
        this.slotName = slotName;
        this.chemistry = chemistry;
        this.totalWeight = totalWeight;
        this.batteryCount = batteryCount;
        this.utilization = utilization;
    }

    public String getStorageName() { return storageName; }
    public String getSlotName() {return slotName; }
    public String getChemistry() { return chemistry; }
    public double getTotalWeight() { return totalWeight; }
    public int getBatteryCount() { return batteryCount; }
    public double getUtilization() {return utilization; }
}
