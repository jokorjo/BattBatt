package com.battbatt.service;

import com.battbatt.entity.Battery;
import com.battbatt.entity.StorageSlot;
import com.battbatt.solver.BatteryPlan;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageOptimizationService {

    private final SolverFactory<BatteryPlan> solverFactory;

    public StorageOptimizationService() {
        this.solverFactory =
                SolverFactory.createFromXmlResource("solverConfig.xml");
    }

    public BatteryPlan solve(List<Battery> batteries, List<StorageSlot> slots) {

                // 🔥 INITIAL ASSIGNMENT
                for (Battery b : batteries) {

                // 🔴 SKIP jos akku on jo varastossa
                if (b.getStorageSlot() != null) continue;

                if (b.getBatteryType() == null) continue;

                StorageSlot bestPrimary = null;
                StorageSlot fallbackOpen = null;
                StorageSlot fallbackOverflow = null;

                for (StorageSlot s : slots) {

                if (s.getStorage() == null) continue;

                String storageChem = s.getStorage().getChemistry();
                String batteryChem = b.getBatteryType().getChemistry();

                String storageType = s.getStorage().getStorageType();
                String batteryType = b.getBatteryType().getType();

                // ❌ estä processing
                if ("PROCESSING".equalsIgnoreCase(storageType) ||
                "Processing Area".equalsIgnoreCase(s.getStorage().getName())) {
                continue;
                }
                    
                // =========================
                // 🔥 FALLBACKS
                // =========================
                if ("OPEN".equalsIgnoreCase(storageType)) {
                    if (fallbackOpen == null) fallbackOpen = s;
                }

                if ("OVERFLOW".equalsIgnoreCase(storageType)) {
                    if (fallbackOverflow == null) fallbackOverflow = s;
                }
                // =========================
                // 🔥 CRITICAL (FIXED)
                // =========================
                boolean isCriticalBattery =
                        "CRITICAL".equalsIgnoreCase(b.getClassification());

                boolean isCriticalStorage =
                        "Critical Storage".equalsIgnoreCase(
                                s.getStorage().getName()
                        );

                if (!isCriticalBattery && isCriticalStorage) continue;
                if (isCriticalBattery && !isCriticalStorage) continue;

                if (isCriticalBattery && isCriticalStorage) {
                    bestPrimary = s;
                    continue;
                }

                // =========================
                // 🔥 REALISTIC CAPACITY
                // =========================
                // =========================
                // 🔥 VOLUME CHECK (KAIKILLE)
                // =========================
                double usedVolume = batteries.stream()
                    .filter(x -> x.getStorageSlot() != null)
                    .filter(x -> x.getStorageSlot().getId().equals(s.getId()))
                    .mapToDouble(x -> x.getBatteryType().getVolume())
                    .sum();

                double newVolume = b.getBatteryType().getVolume();

                if (usedVolume + newVolume > s.getCapacity()) {
                continue;
                }

                // =========================
                // 🔥 PACK ONLY: FOOTPRINT + STACK
                // =========================
                boolean isPack = "PACK".equalsIgnoreCase(storageType);

                if (isPack) {

                    // footprint
                    double newFootprint = b.getBatteryType().getFootprint();

                    // lattian kapasiteetti
                    double floorCapacity = s.getFloorCapacity();

                    // montako mahtuu per kerros
                    int maxPerLayer = (int) Math.floor(floorCapacity / newFootprint);

                    if (maxPerLayer == 0) continue;

                    // nykyinen määrä slotissa
                    long count = batteries.stream()
                    .filter(x -> x.getStorageSlot() != null)
                    .filter(x -> x.getStorageSlot().getId().equals(s.getId()))
                    .count();

                    // max stack
                    int maxStack = s.getMaxStack();

                    // maksimi kokonaismäärä
                    int maxTotal = maxPerLayer * maxStack;

                    if (count >= maxTotal) continue;
                    }

                // =========================
                // 🔥 CHEMISTRY
                // =========================
                boolean isBatteryNMC = "NMC".equalsIgnoreCase(batteryChem);
                boolean isBatteryLFP = "LFP".equalsIgnoreCase(batteryChem);
                boolean isBatteryOther = !isBatteryNMC && !isBatteryLFP;

                boolean isStorageNMC = "NMC".equalsIgnoreCase(storageChem);
                boolean isStorageLFP = "LFP".equalsIgnoreCase(storageChem);
                boolean isStorageOther = "OTHER".equalsIgnoreCase(storageChem);

                boolean chemistryMatch =
                        (isBatteryNMC && isStorageNMC) ||
                        (isBatteryLFP && isStorageLFP) ||
                        (isBatteryOther && isStorageOther);

                if (!chemistryMatch) continue;

                // =========================
                // 🔥 TYPE
                // =========================
                boolean typeMatch =
                 ("PACK".equalsIgnoreCase(batteryType) &&
                  "PACK".equalsIgnoreCase(storageType))
              || ("MODULE".equalsIgnoreCase(batteryType) &&
                  "PALLET".equalsIgnoreCase(storageType));

                if (!typeMatch) continue;

                // =========================
                // 🔥 PRIMARY
                // =========================
                if (!"OPEN".equalsIgnoreCase(storageType)
                && !"OVERFLOW".equalsIgnoreCase(storageType)) {

                if (bestPrimary == null) {
                bestPrimary = s;
                }
        }
} // 🔥 closes for (StorageSlot s : slots)

            // =========================
            // 🔥 FINAL ASSIGNMENT
            // =========================
            if (bestPrimary != null) {
                b.setStorageSlot(bestPrimary);
            } else if (fallbackOpen != null) {
                b.setStorageSlot(fallbackOpen);
            } else if (fallbackOverflow != null) {
                b.setStorageSlot(fallbackOverflow);
            } else {
                throw new RuntimeException(
                        "No storage capacity available for battery: " + b.getBarcode()
                );
            }
        }
    }

        // 🔥 SOLVER
        BatteryPlan problem = new BatteryPlan(batteries, slots);
        Solver<BatteryPlan> solver = solverFactory.buildSolver();
        BatteryPlan solution = solver.solve(problem);

        return solution;
    }
}
