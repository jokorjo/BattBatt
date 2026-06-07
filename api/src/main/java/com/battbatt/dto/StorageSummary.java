package com.battbatt.dto;

public class StorageSummary {

    private String storageName;
    private String chemistry;
    private double totalWeight;
    private int batteryCount;
    private double utilization;

    public StorageSummary(String storageName, String chemistry, double totalWeight, int batteryCount) {
        this.storageName = storageName;
        this.chemistry = chemistry;
        this.totalWeight = totalWeight;
        this.batteryCount = batteryCount;
        this.utilization = utilization;
    }

    public String getStorageName() { return storageName; }
    public String getChemistry() { return chemistry; }
    public double getTotalWeight() { return totalWeight; }
    public int getBatteryCount() { return batteryCount; }
    public double getUtilization() {return utilization; }
}
