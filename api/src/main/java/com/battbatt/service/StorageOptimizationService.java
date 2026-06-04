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

            if (b.isPinned()) continue;
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

                // ❌ estä processing kokonaan
                if ("PROCESSING".equalsIgnoreCase(storageType) ||
                    "Processing Area".equalsIgnoreCase(s.getStorage().getName())) {
                    continue;
                }

                // ❌ capacity
                long count = batteries.stream()
                        .filter(x -> x.getStorageSlot() != null)
                        .filter(x -> x.getStorageSlot().getId().equals(s.getId()))
                        .count();

                if (count >= s.getCapacity()) continue;

                // 🔥 CRITICAL
                boolean isCriticalBattery =
                        "CRITICAL".equalsIgnoreCase(
                                b.getBatteryType().getClassification()
                        );

                boolean isCriticalStorage =
                        "Critical Storage".equalsIgnoreCase(
                                s.getStorage().getName()
                        );

                // estä normaalit menemästä criticaliin
                if (!isCriticalBattery && isCriticalStorage) continue;

                // pakota critical akut critical storageen
                if (isCriticalBattery && isCriticalStorage) {
                    bestPrimary = s;
                    break;
                }

                // 🔥 CHEMISTRY
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

                // 🔥 TYPE
                boolean typeMatch =
                        ("PACK".equalsIgnoreCase(batteryType) &&
                         "PACK".equalsIgnoreCase(storageType))
                     || ("MODULE".equalsIgnoreCase(batteryType) &&
                         "PALLET".equalsIgnoreCase(storageType));

                if (!typeMatch) continue;

                // 🔥 PRIMARY
                if (!"OPEN".equalsIgnoreCase(storageType)
                        && !"OVERFLOW".equalsIgnoreCase(storageType)) {

                    bestPrimary = s;
                    break;
                }

                // 🔥 OPEN fallback
                if ("OPEN".equalsIgnoreCase(storageType)) {
                    if (fallbackOpen == null) fallbackOpen = s;
                }

                // 🔥 OVERFLOW fallback
                if ("OVERFLOW".equalsIgnoreCase(storageType)) {
                    if (fallbackOverflow == null) fallbackOverflow = s;
                }
            }

            // 🔥 FINAL ASSIGNMENT + PIN
            if (bestPrimary != null) {
                b.setStorageSlot(bestPrimary);
                b.setPinned(true);
            } else if (fallbackOpen != null) {
                b.setStorageSlot(fallbackOpen);
                b.setPinned(true);
            } else if (fallbackOverflow != null) {
                b.setStorageSlot(fallbackOverflow);
                b.setPinned(true);
            }
        }

        // 🔥 SOLVER
        BatteryPlan problem = new BatteryPlan(batteries, slots);
        Solver<BatteryPlan> solver = solverFactory.buildSolver();
        BatteryPlan solution = solver.solve(problem);

        return solution;
    }
}
