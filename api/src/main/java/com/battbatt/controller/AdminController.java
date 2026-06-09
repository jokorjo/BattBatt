package com.battbatt.controller;

import com.battbatt.entity.*;
import com.battbatt.repository.*;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "https://batt-batt-pink.vercel.app")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StorageRepository storageRepo;
    private final BatteryTypeRepository typeRepo;
    private final BatteryRepository batteryRepo;
    private final DeviceRepository deviceRepo;

    public AdminController(StorageRepository storageRepo,
                           BatteryTypeRepository typeRepo,
                           BatteryRepository batteryRepo,
                           DeviceRepository deviceRepo) {
        this.storageRepo = storageRepo;
        this.typeRepo = typeRepo;
        this.batteryRepo = batteryRepo;
        this.deviceRepo = deviceRepo;
    }

    // 🔥 CREATE STORAGE
    @PostMapping("/storage")
    public Storage createStorage(@RequestBody Storage s) {
        return storageRepo.save(s);
    }

    // 🔥 CREATE BATTERY TYPE
    @PostMapping("/battery-type")
    public BatteryType createType(@RequestBody BatteryType t) {
        return typeRepo.save(t);
    }

    // 🔥 CREATE DEVICE
    @PostMapping("/device")
    public Device createDevice(@RequestBody Device d) {
        return deviceRepo.save(d);
    }

    // 🔥 DELETE BATTERY
    @DeleteMapping("/battery/{id}")
    public String deleteBattery(@PathVariable Long id) {
        batteryRepo.deleteById(id);
        return "Battery deleted";
    }
}
