package com.battbatt.controller;

import com.battbatt.entity.Battery;
import com.battbatt.entity.StorageSlot;
import com.battbatt.repository.BatteryRepository;
import com.battbatt.repository.StorageSlotRepository;
import com.battbatt.service.OptaPlannerService;
import com.battbatt.solver.BatteryAssignmentSolution;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/optimize")
public class OptimizationController {

    private final BatteryRepository batteryRepository;
    private final StorageSlotRepository storageSlotRepository;
    private final OptaPlannerService optaPlannerService;

    public OptimizationController(BatteryRepository batteryRepository,
                                  StorageSlotRepository storageSlotRepository,
                                  OptaPlannerService optaPlannerService) {
        this.batteryRepository = batteryRepository;
        this.storageSlotRepository = storageSlotRepository;
        this.optaPlannerService = optaPlannerService;
    }

    @GetMapping
    public BatteryAssignmentSolution optimize() {

        // 🔹 hae data tietokannasta
        List<Battery> batteries = batteryRepository.findAll();
        List<StorageSlot> slots = storageSlotRepository.findAll();

        // 🔥 aja solver
        BatteryAssignmentSolution solution =
                optaPlannerService.solve(batteries, slots);

        return solution;
    }
}
