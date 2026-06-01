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
    public ProcessingOptimizationService.Result optimize(@RequestBody Request req) {

        List<Battery> batteries = batteryRepo.findAll();
        List<Device> devices = deviceRepo.findAll();

        ProcessingOptimizationService.Result result = service.optimize(
                batteries,
                devices,
                req.workers,
                req.workingMinutes
        );

        // 🔥 TALLENNA viimeisin suunnitelma
        service.setLastResult(result);

        return result;
    }

    @PostMapping("/confirm")
    public ConfirmResponse confirm(@RequestBody List<Long> ids) {

        List<Battery> batteries = batteryRepo.findAllById(ids);

        StorageSlot processingSlot =
                slotRepo.findByStorageName("Processing Area");

        for (Battery b : batteries) {
            b.setInProcessing(true);
            b.setStorageSlot(processingSlot);
        }

        batteryRepo.saveAll(batteries);

        ConfirmResponse response = new ConfirmResponse();
        response.message = "confirmed - batteries moved to processing area virtual storage";
        response.movedBatteryIds = batteries.stream().map(Battery::getId).toList();

        return response;
    }

    // =========================
    // 🔹 CONFIRM ALL
    // =========================

    @PostMapping("/confirm-all")
    public ConfirmResponse confirmAll() {

        List<Battery> batteries = batteryRepo.findAll();

        List<Battery> toProcess = batteries.stream()
                .filter(b -> b.getStorageSlot() != null)
                .filter(Battery::isPinned)
                .filter(b -> !b.isInProcessing())
                .toList();

        StorageSlot processingSlot =
                slotRepo.findByStorageName("Processing Area");

        for (Battery b : toProcess) {
            b.setInProcessing(true);
            b.setStorageSlot(processingSlot);
        }

        batteryRepo.saveAll(toProcess);

        ConfirmResponse response = new ConfirmResponse();
        response.message = "confirmed - batteries moved to processing area virtual storage";
        response.movedBatteryIds = toProcess.stream().map(Battery::getId).toList();

        return response;
    }

    public static class Request {
        public int workers;
        public int workingMinutes;
    }

    // =========================
    // 🔥 RESPONSE DTO
    // =========================

    public static class ConfirmResponse {
        public String message;
        public List<Long> movedBatteryIds;
    }
}
