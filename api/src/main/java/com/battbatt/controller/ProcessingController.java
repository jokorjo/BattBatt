package com.battbatt.controller;

import com.battbatt.entity.Battery;
import com.battbatt.entity.Device;
import com.battbatt.entity.StorageSlot;
import com.battbatt.repository.BatteryRepository;
import com.battbatt.repository.DeviceRepository;
import com.battbatt.repository.StorageSlotRepository;
import com.battbatt.service.ProcessingOptimizationService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/processing")
public class ProcessingController {

    private final BatteryRepository batteryRepo;
    private final DeviceRepository deviceRepo;
    private final StorageSlotRepository slotRepo;
    private final ProcessingOptimizationService service;

    public ProcessingController(BatteryRepository batteryRepo,
                                DeviceRepository deviceRepo,
                                StorageSlotRepository slotRepo,
                                ProcessingOptimizationService service) {
        this.batteryRepo = batteryRepo;
        this.deviceRepo = deviceRepo;
        this.slotRepo = slotRepo;
        this.service = service;
    }

    @PostMapping("/optimize")
    public Object optimize(@RequestBody Request req) {

        List<Battery> batteries = batteryRepo.findAll();
        List<Device> devices = deviceRepo.findAll();

        return service.optimize(
                batteries,
                devices,
                req.workers,
                req.workingMinutes
        );
    }

    @PostMapping("/confirm")
    public void confirm(@RequestBody List<Long> ids) {

        List<Battery> batteries = batteryRepo.findAllById(ids);

        // 🔥 hae processing slot
        StorageSlot processingSlot =
                slotRepo.findByStorageName("Processing Area");

        for (Battery b : batteries) {
            b.setInProcessing(true);
            b.setStorageSlot(processingSlot); // ❗ EI enää null
        }

        batteryRepo.saveAll(batteries);
    }

    public static class Request {
        public int workers;
        public int workingMinutes;
    }
}
