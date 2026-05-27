package com.battbatt.service;

import com.battbatt.entity.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProcessingOptimizationService {

    // =========================
    // RESULT
    // =========================

    public static class Result {
        public List<Battery> selected;
        public double workerUsed;
        public double deviceUsed;

        public List<WorkerSchedule> workers;
        public List<DeviceSchedule> devices;
    }

    public static class WorkerSchedule {
        public int workerId;
        public List<Task> tasks = new ArrayList<>();
    }

    public static class DeviceSchedule {
        public String deviceName;
        public List<Task> tasks = new ArrayList<>();
    }

    public static class Task {
        public Long batteryId;
        public String type; // PREP / DISCHARGE / MECH
        public double start;
        public double end;
    }

    // =========================
    // MAIN
    // =========================

    public Result optimize(List<Battery> batteries,
                           List<Device> devices,
                           int workers,
                           int workingMinutes) {

        double maxWorkerTime = workers * workingMinutes;
        double maxDeviceTime = devices.size() * workingMinutes;

        // Candidates
		List<Battery> candidates = new ArrayList<>(
                batteries.stream()
                        .filter(b -> b.getStorageSlot() != null)
                        .filter(Battery::isPinned)
                        .filter(b -> !b.isInProcessing())
                        .toList()
        );

        List<Battery> selected = new ArrayList<>();

        double workerUsed = 0;
        double deviceUsed = 0;

        // 🔥 aikataulut
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

        // 🔥 yksinkertainen timeline
        double workerTime = 0;
        double deviceTime = 0;

        while (true) {

            Battery bestBattery = null;
            double bestScore = Double.MAX_VALUE;

            for (Battery b : candidates) {

                double prep = b.getBatteryType().getPreparationTime();
                double mech = b.getBatteryType().getMechanicalTime();
                double discharge = getBestDischarge(b, devices);
		
		// skip impossible
                if (discharge == Double.MAX_VALUE) continue;

                double newWorker = workerUsed + prep + mech;
                double newDevice = deviceUsed + discharge;

                // 🔥 BUFFER CONTROL
                double endPrep = workerTime + prep;
                double startDischarge = deviceTime;

                double bufferTime = startDischarge - endPrep;
		
		// timelimit between prep and discharge - reducing too many batteries prepped
                if (bufferTime > 40) continue;

		// Scoring
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

		// Flow priority
                double flowPenalty = (newWorker + newDevice);

                double score = waste + imbalance * 1000 + overflowPenalty + flowPenalty;

                if (score < bestScore) {
                    bestScore = score;
                    bestBattery = b;
                }
            }

            if (bestBattery == null) break;

            selected.add(bestBattery);
            candidates.remove(bestBattery);

            double prep = bestBattery.getBatteryType().getPreparationTime();
            double mech = bestBattery.getBatteryType().getMechanicalTime();
            double discharge = getBestDischarge(bestBattery, devices);

            // 🔥 VALITAAN LAITE
            Device bestDevice = findBestDevice(bestBattery, devices);

            // 🔥 WORKER (yksinkertainen jako)
            WorkerSchedule ws = workerSchedules.get(selected.size() % workers);

            double startPrep = workerTime;
            double endPrep = startPrep + prep;

            double startDischarge = Math.max(endPrep, deviceTime);
            double endDischarge = startDischarge + discharge;

            double startMech = endDischarge;
            double endMech = startMech + mech;

            // 🔥 WORKER TASKS
            Task prepTask = new Task();
            prepTask.batteryId = bestBattery.getId();
            prepTask.type = "PREP";
            prepTask.start = startPrep;
            prepTask.end = endPrep;
            ws.tasks.add(prepTask);

            Task mechTask = new Task();
            mechTask.batteryId = bestBattery.getId();
            mechTask.type = "MECH";
            mechTask.start = startMech;
            mechTask.end = endMech;
            ws.tasks.add(mechTask);

            // 🔥 DEVICE TASK
            DeviceSchedule ds = deviceSchedules.get(bestDevice.getName());

            Task dischargeTask = new Task();
            dischargeTask.batteryId = bestBattery.getId();
            dischargeTask.type = "DISCHARGE";
            dischargeTask.start = startDischarge;
            dischargeTask.end = endDischarge;
            ds.tasks.add(dischargeTask);

            // 🔥 päivitä ajat
            workerTime = endMech;
            deviceTime = endDischarge;

            workerUsed += prep + mech;
            deviceUsed += discharge;
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
    // HELPERS
    // =========================

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

    private double calculateDischarge(Battery b, Device d) {

        double amps = d.getProfiles().stream()
                .filter(p -> b.getBatteryType().getVoltage() >= p.getMinVoltage()
                        && b.getBatteryType().getVoltage() <= p.getMaxVoltage())
                .map(DeviceProfile::getMaxAmps)
                .filter(a -> a > 0)
                .max(Double::compare)
                .orElse(0.0);

        if (amps <= 0) return Double.MAX_VALUE;

        return (b.getBatteryType().getAh() * 0.9 / amps) * 60;
    }
}
