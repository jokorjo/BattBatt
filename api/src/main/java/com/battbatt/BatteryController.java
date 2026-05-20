package com.battbatt.controller;

import com.battbatt.model.Battery;
import com.battbatt.service.BatteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batteries")
public class BatteryController {
    @Autowired
    private BatteryService batteryService;

    @GetMapping
    public List<Battery> getAllBatteries() {
        return batteryService.getAllBatteries();
    }

    @PostMapping
    public Battery addBattery(@RequestBody Battery battery) {
        return batteryService.addBattery(battery);
    }
}
