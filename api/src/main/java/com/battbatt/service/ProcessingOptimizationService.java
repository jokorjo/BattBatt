package com.battbatt.service;

import com.battbatt.entity.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProcessingOptimizationService {

    // =========================
    // RESULT STRUCTURES
    // =========================

    // Final result returned by optimizer
    public static class Result {
        public List<Battery> selected;     // Batteries selected for processing
        public double workerUsed;          // Total worker time used
        public double deviceUsed;          // Total device time used

        public List<WorkerSchedule> workers; // Worker timelines
        public List<DeviceSchedule> devices; // Device timelines
    }

    // Worker timeline
    public static class WorkerSchedule {
        public int workerId;
        public List<Task> tasks = new ArrayList<>();
    }

    // Device timeline
    public static class DeviceSchedule {
        public String deviceName;
        public List<Task> tasks = new ArrayList<>();
    }

    // Generic task (prep / discharge / mech)
    public static class Task {
        public Long batteryId;
        public String type; // PREP / DISCHARGE / MECH
        public double start;
        public double end;
    }

    // =========================
    // MAIN OPTIMIZATION METHOD
    // =========================

    public Result optimize(List<Battery> batteries,
                           List<Device> devices,
                           int workers,
                           int workingMinutes) {

        // Total available capacity
        double maxWorkerTime = workers * workingMinutes;
        double maxDeviceTime = devices.size() * workingMinutes;

        // Filter valid candidates
        List<Battery> candidates = new ArrayList<>(
                batteries.stream()
                        .filter(b -> b.getStorageSlot() != null) // must be in storage
                        .filter(Battery::isPinned)               // must be selected
                        .filter(b -> !b.isInProcessing())        // not already processing
                        .toList()
        );

        List<Battery> selected = new ArrayList<>();

        double workerUsed = 0;
        double deviceUsed = 0;

        // =========================
        // GROUP BATTERIES INTO PALLETS
        // =========================

        Map<Long, List<Battery>> palletGroups = new HashMap<>();
        List<Battery> normalBatteries = new ArrayList<>();

        for (Battery b : candidates) {

            boolean isModule = b.getBatteryType().getType().equalsIgnoreCase("MODULE");
            boolean isPallet = b.getStorageSlot().getStorage().getStorageType().equalsIgnoreCase("PALLET");

            // Group modules stored in pallets
            if (isModule && isPallet) {
                Long slotId = b.getStorageSlot().getId();
                palletGroups.computeIfAbsent(slotId, k -> new ArrayList<>()).add(b);
            } else {
                normalBatteries.add(b);
            }
        }

        // =========================
        // APPLY 70% PALLET RULE
        // =========================

        List<List<Battery>> validPallets = new ArrayList<>();

        for (Map.Entry<Long, List<Battery>> entry : palletGroups.entrySet()) {

            List<Battery> group = entry.getValue();
            double capacity = group.get(0).getStorageSlot().getCapacity();

            double fillRate = (double) group.size() / capacity;

            // Only allow sufficiently filled pallets
            if (fillRate >= 0.7) {
                validPallets.add(group);
            }
        }

        // =========================
        // INIT SCHEDULE STRUCTURES
        // =========================

        List<WorkerSchedule> workerSchedules = new ArrayList<>();
        for (int i = 0; i < workers; i++) {
            WorkerSchedule ws = new WorkerSchedule();
            ws.workerId = i;
            workerSchedules.add(ws);
        }

        Map<String, DeviceSchedule> deviceSchedules = new HashMap<>();
        for (Device d : devices) {
            DeviceSchedule ds = new DeviceSchedule();
            ds.deviceName = d.getName();
            deviceSchedules.put(d.getName(), ds);
        }

        // Global timeline (single-threaded simulation)
        double workerTime = 0;
        double deviceTime = 0;

        // =========================
        // MAIN LOOP
        // =========================

        while (true) {

            Battery bestBattery = null;
            List<Battery> bestGroup = null;
            double bestScore = Double.MAX_VALUE;

            // -------------------------
            // Evaluate single batteries
            // -------------------------
            for (Battery b : normalBatteries) {

                double prep = b.getBatteryType().getPreparationTime();
                double mech = b.getBatteryType().getMechanicalTime();
                double discharge = getBestDischarge(b, devices);

                if (discharge == Double.MAX_VALUE) continue;

                double newWorker = workerUsed + prep + mech;
                double newDevice = deviceUsed + discharge;

                // Prevent excessive buffer between prep and discharge
                double endPrep = workerTime + prep;
                double startDischarge = deviceTime;

                double bufferTime = startDischarge - endPrep;
                if (bufferTime > 40) continue;

                // Penalties if exceeding available time
                double overflowPenalty = 0;

                if (newWorker > maxWorkerTime) {
                    overflowPenalty += (newWorker - maxWorkerTime) * 100;
                }

                if (newDevice > maxDeviceTime) {
                    overflowPenalty += (newDevice - maxDeviceTime) * 100;
                }

                // Idle time
                double workerIdle = Math.max(0, maxWorkerTime - newWorker);
                double deviceIdle = Math.max(0, maxDeviceTime - newDevice);

                double waste = workerIdle + deviceIdle;

                // Balance worker vs device usage
                double workerRatio = newWorker / maxWorkerTime;
                double deviceRatio = newDevice / maxDeviceTime;

                double imbalance = Math.abs(workerRatio - deviceRatio);

                // Flow penalty (prefer shorter pipelines)
                double flowPenalty = (newWorker + newDevice);

                double score = waste + imbalance * 1000 + overflowPenalty + flowPenalty;

                if (score < bestScore) {
                    bestScore = score;
                    bestBattery = b;
                    bestGroup = null;
                }
            }

            // -------------------------
            // Evaluate pallet groups
            // -------------------------
            for (List<Battery> group : validPallets) {

                double prep = group.stream().mapToDouble(b -> b.getBatteryType().getPreparationTime()).sum();
                double mech = group.stream().mapToDouble(b -> b.getBatteryType().getMechanicalTime()).sum();
                double discharge = group.stream().mapToDouble(b -> getBestDischarge(b, devices)).sum();

                double newWorker = workerUsed + prep + mech;
                double newDevice = deviceUsed + discharge;

                double overflowPenalty = 0;

                if (newWorker > maxWorkerTime) {
                    overflowPenalty += (newWorker - maxWorkerTime) * 100;
                }

                if (newDevice > maxDeviceTime) {
                    overflowPenalty += (newDevice - maxDeviceTime) * 100;
                }

                double workerIdle = Math.max(0, maxWorkerTime - newWorker);
                double deviceIdle = Math.max(0, maxDeviceTime - newDevice);

                double waste = workerIdle + deviceIdle;

                double workerRatio = newWorker / maxWorkerTime;
                double deviceRatio = newDevice / maxDeviceTime;

                double imbalance = Math.abs(workerRatio - deviceRatio);

                double flowPenalty = (newWorker + newDevice);

                double score = waste + imbalance * 1000 + overflowPenalty + flowPenalty;

                if (score < bestScore) {
                    bestScore = score;
                    bestBattery = null;
                    bestGroup = group;
                }
            }

            if (bestBattery == null && bestGroup == null) break;

            // =========================
            // APPLY SELECTION
            // =========================

            if (bestGroup != null) {

                for (Battery b : bestGroup) {
                    selected.add(b);
                }

                normalBatteries.removeAll(bestGroup);
                validPallets.remove(bestGroup);

                for (Battery b : bestGroup) {

                    scheduleBattery(b, devices, workerSchedules, deviceSchedules);

                    workerTime += b.getBatteryType().getPreparationTime()
                            + b.getBatteryType().getMechanicalTime();

                    deviceTime += getBestDischarge(b, devices);

                    workerUsed += b.getBatteryType().getPreparationTime()
                            + b.getBatteryType().getMechanicalTime();

                    deviceUsed += getBestDischarge(b, devices);
                }

                continue;
            }

            // Single battery
            selected.add(bestBattery);
            normalBatteries.remove(bestBattery);

            scheduleBattery(bestBattery, devices, workerSchedules, deviceSchedules);

            workerTime += bestBattery.getBatteryType().getPreparationTime()
                    + bestBattery.getBatteryType().getMechanicalTime();

            deviceTime += getBestDischarge(bestBattery, devices);

            workerUsed += bestBattery.getBatteryType().getPreparationTime()
                    + bestBattery.getBatteryType().getMechanicalTime();

            deviceUsed += getBestDischarge(bestBattery, devices);
        }

        Result result = new Result();
        result.selected = selected;
        result.workerUsed = workerUsed;
        result.deviceUsed = deviceUsed;

        result.workers = workerSchedules;
        result.devices = new ArrayList<>(deviceSchedules.values());

        return result;
    }

    // =========================
    // SCHEDULING HELPER
    // =========================

    private void scheduleBattery(Battery b,
                                 List<Device> devices,
                                 List<WorkerSchedule> workerSchedules,
                                 Map<String, DeviceSchedule> deviceSchedules) {

        Device bestDevice = findBestDevice(b, devices);
        WorkerSchedule ws = workerSchedules.get(0); // simple assignment

        double prep = b.getBatteryType().getPreparationTime();
        double mech = b.getBatteryType().getMechanicalTime();
        double discharge = getBestDischarge(b, devices);

        Task prepTask = new Task();
        prepTask.batteryId = b.getId();
        prepTask.type = "PREP";
        prepTask.start = 0;
        prepTask.end = prep;
        ws.tasks.add(prepTask);

        Task mechTask = new Task();
        mechTask.batteryId = b.getId();
        mechTask.type = "MECH";
        mechTask.start = prep + discharge;
        mechTask.end = mechTask.start + mech;
        ws.tasks.add(mechTask);

        DeviceSchedule ds = deviceSchedules.get(bestDevice.getName());

        Task dischargeTask = new Task();
        dischargeTask.batteryId = b.getId();
        dischargeTask.type = "DISCHARGE";
        dischargeTask.start = prep;
        dischargeTask.end = prep + discharge;
        ds.tasks.add(dischargeTask);
    }

    private Device findBestDevice(Battery b, List<Device> devices) {
        return devices.stream()
                .min(Comparator.comparing(d -> calculateDischarge(b, d)))
                .orElse(null);
    }

    private double getBestDischarge(Battery b, List<Device> devices) {
        return devices.stream()
                .map(d -> calculateDischarge(b, d))
                .min(Double::compare)
                .orElse(Double.MAX_VALUE);
    }

    // =========================
    // DISCHARGE CALCULATION (SAFE)
    // =========================

    private double calculateDischarge(Battery b, Device d) {

        double ah = b.getBatteryType().getAh();

        double deviceMaxAmps = d.getProfiles().stream()
                .filter(p -> b.getBatteryType().getVoltage() >= p.getMinVoltage()
                        && b.getBatteryType().getVoltage() <= p.getMaxVoltage())
                .map(DeviceProfile::getMaxAmps)
                .filter(a -> a > 0)
                .max(Double::compare)
                .orElse(0.0);

        if (deviceMaxAmps <= 0) return Double.MAX_VALUE;

        // Enforce maximum 1C discharge rate
        double maxAllowedAmps = ah;

        double usedAmps = Math.min(deviceMaxAmps, maxAllowedAmps);

        if (usedAmps <= 0) return Double.MAX_VALUE;

        // Discharge 90% of capacity
        return (ah * 0.9 / usedAmps) * 60;
    }
}
