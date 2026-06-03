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

        // 🔥 1. INITIAL ASSIGNMENT (vain uusille!)
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

                // ❌ estä processing
                if (storageType.equalsIgnoreCase("PROCESSING")) continue;

                // ❌ capacity check (ILMAN getBatteries)
                long count = batteries.stream()
                        .filter(x -> x.getStorageSlot() != null)
                        .filter(x -> x.getStorageSlot().getId().equals(s.getId()))
                        .count();

                if (count >= s.getCapacity()) continue;

                boolean chemistryMatch =
                        storageChem.equalsIgnoreCase("ANY") ||
                        storageChem.equalsIgnoreCase(batteryChem);

                if (!chemistryMatch) continue;

                boolean typeMatch =
                        (batteryType.equalsIgnoreCase("PACK") &&
                         storageType.equalsIgnoreCase("PACK"))
                     || (batteryType.equalsIgnoreCase("MODULE") &&
                         storageType.equalsIgnoreCase("PALLET"));

                if (!typeMatch) continue;

                // ❌ estä väärä critical
                if (s.getStorage().getName().equalsIgnoreCase("Critical Storage")
                        && !b.getClassification().equalsIgnoreCase("CRITICAL")) {
                    continue;
                }

                // =========================
                // 🔥 PRIMARY (EI OPEN, EI OVERFLOW)
                // =========================
                if (!storageType.equalsIgnoreCase("OPEN")
                        && !storageType.equalsIgnoreCase("OVERFLOW")) {

                    bestPrimary = s;
                    break;
                }

                // =========================
                // 🔥 FALLBACK OPEN
                // =========================
                if (storageType.equalsIgnoreCase("OPEN")) {
                    if (fallbackOpen == null) {
                        fallbackOpen = s;
                    }
                }

                // =========================
                // 🔥 FALLBACK OVERFLOW
                // =========================
                if (storageType.equalsIgnoreCase("OVERFLOW")) {
                    if (fallbackOverflow == null) {
                        fallbackOverflow = s;
                    }
                }
            }

            // =========================
            // 🔥 FINAL VALINTA
            // =========================
            if (bestPrimary != null) {
                b.setStorageSlot(bestPrimary);
            } else if (fallbackOpen != null) {
                b.setStorageSlot(fallbackOpen);
            } else if (fallbackOverflow != null) {
                b.setStorageSlot(fallbackOverflow);
            }
        }

        // 🔥 2. PROBLEM
        BatteryPlan problem = new BatteryPlan(batteries, slots);

        // 🔥 3. SOLVER
        Solver<BatteryPlan> solver = solverFactory.buildSolver();

        BatteryPlan solution = solver.solve(problem);

        // 🔥 4. PIN kaikki uudet
        solution.getBatteryList().forEach(b -> {
            if (!b.isPinned()) {
                b.setPinned(true);
            }
        });

        return solution;
    }
}
