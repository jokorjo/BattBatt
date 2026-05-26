package com.battbatt.controller;

import com.battbatt.entity.Device;
import com.battbatt.repository.DeviceRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin
public class DeviceController {

    private final DeviceRepository repo;

    public DeviceController(DeviceRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Device> getAll() {
        return repo.findAll();
    }
}
