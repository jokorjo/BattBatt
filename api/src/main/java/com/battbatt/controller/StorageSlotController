package com.battbatt.controller;

import com.battbatt.entity.StorageSlot;
import com.battbatt.repository.StorageSlotRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class StorageSlotController {

    private final StorageSlotRepository repo;

    public StorageSlotController(StorageSlotRepository repo) {
        this.repo = repo;
    }

    // 🔥 TEST ENDPOINT
    @GetMapping("/test")
    public String test() {
        return "SLOTS WORKING";
    }

    @GetMapping
    public List<StorageSlot> getAll() {
        return repo.findAll();
    }
}
