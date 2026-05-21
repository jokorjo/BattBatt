package com.battbatt.controller;

import com.battbatt.entity.Battery;
import com.battbatt.entity.StorageSlot;
import com.battbatt.repository.BatteryRepository;
import com.battbatt.repository.StorageSlotRepository;
import com.battbatt.service.StorageOptimizationService;
import com.battbatt.solver.BatteryPlan;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/optimize") // 🔥 yhtenäinen API
public class OptimizationController {

    private final BatteryRepository batteryRepository;
    private final StorageSlotRepository storageSlotRepository;
    private final StorageOptimizationService optimizationService;

    public OptimizationController(BatteryRepository batteryRepository,
                                  StorageSlotRepository storageSlotRepository,
                                  StorageOptimizationService optimizationService) {
        this.batteryRepository = batteryRepository;
        this.storageSlotRepository = storageSlotRepository;
        this.optimizationService = optimizationService;
    }

    @PostMapping // 🔥 käytä POST (muuttaa dataa)
    public BatteryPlan optimize() {

        // 🔹 hae data tietokannasta
        List<Battery> batteries = batteryRepository.findAll();
        List<StorageSlot> slots = storageSlotRepository.findAll();

        // 🔥 aja solver
        BatteryPlan solution =
                optimizationService.solve(batteries, slots);

        // 🔥 TÄRKEÄ: tallenna tulos DB:hen
        batteryRepository.saveAll(solution.getBatteryList());

        return solution;
    }
}
