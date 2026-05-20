package com.battbatt.service;

import com.battbatt.entity.Battery;
import com.battbatt.entity.StorageSlot;
import com.battbatt.solver.BatteryAssignmentSolution;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptaPlannerService {

    public BatteryAssignmentSolution solve(List<Battery> batteries, List<StorageSlot> slots) {

        SolverFactory<BatteryAssignmentSolution> solverFactory =
                SolverFactory.createFromXmlResource("solverConfig.xml");

        Solver<BatteryAssignmentSolution> solver = solverFactory.buildSolver();

        BatteryAssignmentSolution problem = new BatteryAssignmentSolution();
        problem.setBatteryList(batteries);
        problem.setStorageSlotList(slots);

        return solver.solve(problem);
    }
}
