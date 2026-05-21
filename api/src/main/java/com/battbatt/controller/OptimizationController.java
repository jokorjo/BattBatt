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
@RequestMapping("/api/optimize")
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

    @PostMapping
    public Object optimize() {

        try {
            // 🔹 hae data tietokannasta
            List<Battery> batteries = batteryRepository.findAll();
            List<StorageSlot> slots = storageSlotRepository.findAll();

            // 🔥 DEBUG LOG
            System.out.println("Batteries: " + batteries.size());
            System.out.println("Slots: " + slots.size());

            // 🔥 ESTÄ CRASH
            if (batteries.isEmpty()) {
                return "❌ No batteries found. Add batteries first via /api/batteries";
            }

            if (slots.isEmpty()) {
                return "❌ No storage slots found.";
            }

            // 🔥 aja solver
            BatteryPlan solution =
                    optimizationService.solve(batteries, slots);

            // 🔥 tallenna tulos
            batteryRepository.saveAll(solution.getBatteryList());

            return solution;

        } catch (Exception e) {
            // 🔥 TÄMÄ PALJASTAA VIRHEEN
            e.printStackTrace();
            return "❌ ERROR: " + e.getMessage();
        }
    }
}
