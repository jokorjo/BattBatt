package com.battbatt.service;

import com.battbatt.entity.Battery;
import com.battbatt.entity.StorageSlot;
import com.battbatt.solver.BatteryPlan;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptaPlannerService {

    public BatteryPlan solve(List<Battery> batteries, List<StorageSlot> slots) {

        SolverFactory<BatteryPlan> solverFactory =
                SolverFactory.createFromXmlResource("solverConfig.xml");

        Solver<BatteryPlan> solver = solverFactory.buildSolver();

        BatteryPlan problem = new BatteryPlan();
        problem.setBatteryList(batteries);
        problem.setSlotList(slots);

        return solver.solve(problem);
    }
}
