package com.battbatt.controller;

import com.battbatt.entity.Battery;
import com.battbatt.repository.BatteryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batteries")
public class BatteryController {

    private final BatteryRepository repo;

    public BatteryController(BatteryRepository repo) {
        this.repo = repo;
    }

    // 🔹 Hae kaikki akut
    @GetMapping
    public List<Battery> getAll() {
        return repo.findAll();
    }
}
