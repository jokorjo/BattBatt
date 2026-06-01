package com.battbatt.service;

import com.battbatt.entity.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProcessingOptimizationService {

    // =========================
    // 🔥 LAST OPTIMIZATION RESULT (ADDED)
    // =========================
    // Stores the latest optimization result in memory
    private Result lastResult;

    public void setLastResult(Result result) {
        this.lastResult = result;
    }

    public Result getLastResult() {
        return lastResult;
    }

    // =========================
    // RESULT STRUCTURES
    // =========================

    // Final result returned to controller
    public static class Result {
        public List<Battery> selected;      // Batteries selected for processing
        public double workerUsed;           // Total worker time used (minutes)
        public double deviceUsed;           // Total device time used (minutes)

        public List<WorkerSchedule> workers; // Worker timelines
        public List<DeviceSchedule> devices; // Device timelines
    }

    // Timeline per worker
    public static class WorkerSchedule {
        public int workerId;
        public List<Task> tasks = new ArrayList<>();
    }

    // Timeline per device
    public static class DeviceSchedule {
        public String deviceName;
        public List<Task> tasks = new ArrayList<>();
    }

    // Generic task representation
    public static class Task {
        public Long batteryId;
        public String type; // PREP / DISCHARGE / MECH
        public double start;
        public double end;
    }

    // =========================
    // MAIN OPTIMIZATION
    // =========================

    public Result optimize(List<Battery> batteries,
                           List<Device> devices,
                           int workers,
                           int workingMinutes) {

        // Total available capacity
        double maxWorkerTime = workers * workingMinutes;
        double maxDeviceTime = devices.size() * workingMinutes;

        // Filter valid batteries for processing
        List<Battery> candidates = new ArrayList<>(
                batteries.stream()
                        .filter(b -> b.getStorageSlot() != null) // must be stored
                        .filter(Battery::isPinned)               // must be selected
                        .filter(b -> !b.isInProcessing())        // not already processing
                        .toList()
        );

        List<Battery> selected = new ArrayList<>();

        double workerUsed = 0;
        double deviceUsed = 0;

        // =========================
        // PALLET GROUPING
        // =========================

        Map<Long, List<Battery>> palletGroups = new HashMap<>();
        List<Battery> normalBatteries = new ArrayList<>();

        for (Battery b : candidates) {

            boolean isModule = b.getBatteryType().getType().equalsIgnoreCase("MODULE");
            boolean isPallet = b.getStorageSlot().getStorage().getStorageType().equalsIgnoreCase("PALLET");

            // Group modules by pallet
            if (isModule && isPallet) {
                Long slotId = b.getStorageSlot().getId();
                palletGroups.computeIfAbsent(slotId, k -> new ArrayList<>()).add(b);
            } else {
                normalBatteries.add(b);
            }
        }

        // =========================
        // 70% PALLET RULE
        // =========================

        List<List<Battery>> validPallets = new ArrayList<>();

        for (Map.Entry<Long, List<Battery>> entry : palletGroups.entrySet()) {

            List<Battery> group = entry.getValue();
            double capacity = group.get(0).getStorageSlot().getCapacity();

            double fillRate = (double) group.size() / capacity;

            // Only process pallets that are sufficiently filled
            if (fillRate >= 0.7) {
                validPallets.add(group);
            }
        }

        // =========================
        // SCHEDULE STRUCTURES
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

        // =========================
        // PARALLEL TIME TRACKING
        // =========================

        double[] workerTimes = new double[workers];
        Map<String, Double> deviceTimes = new HashMap<>();

        for (int i = 0; i < workers; i++) workerTimes[i] = 0;
        for (Device d : devices) deviceTimes.put(d.getName(), 0.0);

        // These are kept for scoring logic (DO NOT REMOVE)
        double workerTime = 0;
        double deviceTime = 0;

        // =========================
        // MAIN OPTIMIZATION LOOP
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

                double endPrep = workerTime + prep;
                double startDischarge = deviceTime;

                double bufferTime = startDischarge - endPrep;
                if (bufferTime > 40) continue;

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

            List<Battery> batch = (bestGroup != null) ? bestGroup : List.of(bestBattery);

            if (bestGroup != null) {
                validPallets.remove(bestGroup);
                normalBatteries.removeAll(bestGroup);
            } else {
                normalBatteries.remove(bestBattery);
            }

            selected.addAll(batch);

            for (Battery b : batch) {

                int workerIndex = getFreeWorker(workerTimes);
                WorkerSchedule ws = workerSchedules.get(workerIndex);

                Device bestDevice = findBestDevice(b, devices, deviceTimes, workingMinutes);

                if (bestDevice == null) {
                continue; // 🔥 mikään laite ei mahdu tälle akulle
                }

                DeviceSchedule ds = deviceSchedules.get(bestDevice.getName());

                double prep = b.getBatteryType().getPreparationTime();
                double mech = b.getBatteryType().getMechanicalTime();
                double discharge = getBestDischarge(b, devices);
                
                double startPrep = workerTimes[workerIndex];
                double endPrep = startPrep + prep;

                double startDischarge = Math.max(endPrep, deviceTimes.get(bestDevice.getName()));
                double endDischarge = startDischarge + discharge;

                double startMech = endDischarge;
                double endMech = startMech + mech;

                ws.tasks.add(task(b, "PREP", startPrep, endPrep));
                ws.tasks.add(task(b, "MECH", startMech, endMech));
                ds.tasks.add(task(b, "DISCHARGE", startDischarge, endDischarge));

                workerTimes[workerIndex] = endMech;
                deviceTimes.put(bestDevice.getName(), endDischarge);

                workerUsed += prep + mech;
                deviceUsed += discharge;
            }
        }

        Result result = new Result();
        result.selected = selected;
        result.workerUsed = workerUsed;
        result.deviceUsed = deviceUsed;

        result.workers = workerSchedules;
        result.devices = new ArrayList<>(deviceSchedules.values());

        // =========================
        // 🔥 SAVE LAST RESULT (ADDED)
        // =========================
        this.lastResult = result;

        return result;
    }

    // =========================
    // HELPERS
    // =========================

    private int getFreeWorker(double[] workerTimes) {
        int best = 0;
        for (int i = 1; i < workerTimes.length; i++) {
            if (workerTimes[i] < workerTimes[best]) best = i;
        }
        return best;
    }

    private Task task(Battery b, String type, double start, double end) {
        Task t = new Task();
        t.batteryId = b.getId();
        t.type = type;
        t.start = start;
        t.end = end;
        return t;
    }


    private double getBestDischarge(Battery b, List<Device> devices) {
        return devices.stream()
                .map(d -> calculateDischarge(b, d))
                .min(Double::compare)
                .orElse(Double.MAX_VALUE);
    }

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

        double usedAmps = Math.min(deviceMaxAmps, ah);

        return (ah * 0.9 / usedAmps) * 60;
    }
    
    private Device findBestDevice(Battery b,
                             List<Device> devices,
                             Map<String, Double> deviceTimes,
                             int workingMinutes) {

    return devices.stream()
            // 🔥 vain laitteet joilla kapasiteettia jäljellä
            .filter(d -> {
                double discharge = calculateDischarge(b, d);
                double current = deviceTimes.get(d.getName());
                return current + discharge <= workingMinutes;
            })
            // 🔥 valitaan nopein näistä
            .min(Comparator.comparing(d -> calculateDischarge(b, d)))
            .orElse(null);
    }
}
