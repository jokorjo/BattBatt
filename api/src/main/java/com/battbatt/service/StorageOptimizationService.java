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

            // 🔒 SKIP jos jo lukittu
            if (b.isPinned()) continue;

            if (b.getBatteryType() == null) continue;

            StorageSlot fallbackOpen = null;

            for (StorageSlot s : slots) {

                if (s.getStorage() == null) continue;

                String storageChem = s.getStorage().getChemistry();
                String batteryChem = b.getBatteryType().getChemistry();

                String storageType = s.getStorage().getStorageType();
                String batteryType = b.getBatteryType().getType();

                // =========================
                // 🔥 MINIMAL FIX: ESTÄ PROCESSING AREA
                // =========================
                if (storageType.equalsIgnoreCase("PROCESSING")) continue;

                // =========================
                // 🔥 HARD RULES (TÄRKEIN FIX)
                // =========================

                // ❌ ESTÄ väärä chemistry
                boolean chemistryMatch =
                        storageChem.equalsIgnoreCase("ANY") ||
                        storageChem.equalsIgnoreCase(batteryChem);

                if (!chemistryMatch) continue;

                // ❌ ESTÄ väärä type
                boolean typeMatch =
                        (batteryType.equalsIgnoreCase("PACK") &&
                         storageType.equalsIgnoreCase("PACK"))
                     || (batteryType.equalsIgnoreCase("MODULE") &&
                         storageType.equalsIgnoreCase("PALLET"))
                     || storageType.equalsIgnoreCase("OPEN"); // OPEN sallitaan fallbackiksi

                if (!typeMatch) continue;

                // ❌ ESTÄ Critical vääriltä akuilta
                if (s.getStorage().getName().equalsIgnoreCase("Critical Storage")
                        && !b.getClassification().equalsIgnoreCase("CRITICAL")) {
                    continue;
                }

                // ❌ ESTÄ Overflow tässä vaiheessa
                if (storageType.equalsIgnoreCase("OVERFLOW")) {
                    continue;
                }

                // 🔥 ENSISIJAINEN VALINTA
                b.setStorageSlot(s);
                break;
            }

            // =========================
            // 🔥 FALLBACK (OPEN ONLY)
            // =========================

            if (b.getStorageSlot() == null) {

                for (StorageSlot s : slots) {

                    if (s.getStorage() == null) continue;

                    String storageType = s.getStorage().getStorageType();

                    // 🔥 ESTÄ PROCESSING
                    if (storageType.equalsIgnoreCase("PROCESSING")) continue;

                    if (storageType.equalsIgnoreCase("OPEN")) {

                        boolean chemistryMatch =
                                s.getStorage().getChemistry().equalsIgnoreCase("ANY") ||
                                s.getStorage().getChemistry().equalsIgnoreCase(
                                        b.getBatteryType().getChemistry()
                                );

                        if (chemistryMatch) {
                            fallbackOpen = s;
                            break;
                        }
                    }
                }

                if (fallbackOpen != null) {
                    b.setStorageSlot(fallbackOpen);
                }
            }

            // =========================
            // 🔥 VIIMEINEN FALLBACK (OVERFLOW)
            // =========================

            if (b.getStorageSlot() == null) {

                for (StorageSlot s : slots) {

                    if (s.getStorage() == null) continue;

                    String storageType = s.getStorage().getStorageType();

                    // 🔥 ESTÄ PROCESSING
                    if (storageType.equalsIgnoreCase("PROCESSING")) continue;

                    if (storageType.equalsIgnoreCase("OVERFLOW")) {

                        boolean chemistryMatch =
                                s.getStorage().getChemistry().equalsIgnoreCase("ANY") ||
                                s.getStorage().getChemistry().equalsIgnoreCase(
                                        b.getBatteryType().getChemistry()
                                );

                        if (chemistryMatch) {
                            b.setStorageSlot(s);
                            break;
                        }
                    }
                }
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
