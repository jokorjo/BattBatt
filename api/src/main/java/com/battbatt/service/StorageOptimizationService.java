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

        // 🔥 1. INITIAL ASSIGNMENT (TÄRKEIN)
        for (Battery b : batteries) {

            if (b.getBatteryType() == null) continue;

            for (StorageSlot s : slots) {

                if (s.getStorage() == null) continue;

                boolean chemistryMatch =
                        s.getStorage().getChemistry().equalsIgnoreCase("ANY") ||
                        s.getStorage().getChemistry().equalsIgnoreCase(
                                b.getBatteryType().getChemistry()
                        );

                boolean typeMatch =
                        (b.getBatteryType().getType().equalsIgnoreCase("PACK") &&
                         s.getStorage().getStorageType().equalsIgnoreCase("PACK"))
                     || (b.getBatteryType().getType().equalsIgnoreCase("MODULE") &&
                         s.getStorage().getStorageType().equalsIgnoreCase("PALLET"));

                if (chemistryMatch && typeMatch) {
                    b.setStorageSlot(s);
                    break;
                }
            }

            // 🔥 fallback (estää nullit)
            if (b.getStorageSlot() == null && !slots.isEmpty()) {
                b.setStorageSlot(slots.get(0));
            }
        }

        // 🔥 2. PROBLEM
        BatteryPlan problem = new BatteryPlan(batteries, slots);

        // 🔥 3. SOLVER
        Solver<BatteryPlan> solver = solverFactory.buildSolver();

        return solver.solve(problem);
    }
}
