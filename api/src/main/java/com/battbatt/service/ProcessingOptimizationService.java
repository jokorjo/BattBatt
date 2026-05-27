package com.battbatt.service;

import com.battbatt.entity.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProcessingOptimizationService {

    public static class Result {
        public List<Battery> selected;
        public double workerUsed;
        public double deviceUsed;
    }

    public Result optimize(List<Battery> batteries,
                           List<Device> devices,
                           int workers,
                           int workingMinutes) {

        double maxWorkerTime = workers * workingMinutes;
        double maxDeviceTime = devices.size() * workingMinutes;

        // 🔥 candidates
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

        while (true) {

            Battery bestBattery = null;
            double bestScore = Double.MAX_VALUE;

            for (Battery b : candidates) {

                double prep = b.getBatteryType().getPreparationTime();
                double mech = b.getBatteryType().getMechanicalTime();

                double discharge = getBestDischarge(b, devices);

                // 🔥 skip impossible
                if (discharge == Double.MAX_VALUE) continue;

                double newWorker = workerUsed + prep + mech;
                double newDevice = deviceUsed + discharge;

                // =========================
                // 🔥 BUFFER CONTROL (TÄRKEIN)
                // =========================

                double endPrep = workerUsed + prep;
                double startDischarge = deviceUsed;

                double bufferTime = startDischarge - endPrep;

                // 🔥 timelimit between prep and discharge - reducing too many batteries prepped
                if (bufferTime > 40) continue;

                // =========================
                // SCORING
                // =========================

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

                // 🔥 FLOW PRIORITY (mech tärkeä)
                double flowPenalty = (newWorker + newDevice);

                double score = waste
                        + imbalance * 1000
                        + overflowPenalty
                        + flowPenalty;

                if (score < bestScore) {
                    bestScore = score;
                    bestBattery = b;
                }
            }

            if (bestBattery == null) break;

            selected.add(bestBattery);
            candidates.remove(bestBattery);

            workerUsed += bestBattery.getBatteryType().getPreparationTime()
                    + bestBattery.getBatteryType().getMechanicalTime();

            deviceUsed += getBestDischarge(bestBattery, devices);
        }

        Result result = new Result();
        result.selected = selected;
        result.workerUsed = workerUsed;
        result.deviceUsed = deviceUsed;

        return result;
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

        return (b.getBatteryType().getAh() *0.9 / amps) * 60; /// Batteries are discharged 90% of max capacity
    }
}
