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

@CrossOrigin(origins = "https://batt-batt-pink.vercel.app")
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
    // 🔹 CONFIRM ALL (FIXED)
    // =========================

    @PostMapping("/confirm-all")
    public ConfirmResponse confirmAll() {

        ProcessingOptimizationService.Result last = service.getLastResult();

        if (last == null || last.selected == null || last.selected.isEmpty()) {
            throw new RuntimeException("No optimization result available. Run /optimize first.");
        }

        List<Battery> toProcess = last.selected;

        StorageSlot processingSlot =
                slotRepo.findByStorageName("Processing Area");

        for (Battery b : toProcess) {
            b.setInProcessing(true);
            b.setStorageSlot(processingSlot);
        }

        batteryRepo.saveAll(toProcess);

        ConfirmResponse response = new ConfirmResponse();
        response.message = "confirmed - optimized batteries moved to processing area";
        response.movedBatteryIds = toProcess.stream().map(Battery::getId).toList();

        return response;
    }
        @PostMapping("/ready/{id}")
            public ConfirmResponse markReady(@PathVariable Long id) {

        Battery b = batteryRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Battery not found"));

        if (!b.isInProcessing()) {
            throw new RuntimeException("Battery not in processing");
        }

        StorageSlot outbound =
            slotRepo.findFirstByStorage_Name("Outbound");

        if (outbound == null) {
            throw new RuntimeException("Outbound storage not found!");
        }
    
        b.setInProcessing(false);
        b.setStorageSlot(outbound);

        batteryRepo.save(b);

        ConfirmResponse response = new ConfirmResponse();
        response.message = "battery moved to outbound";
        response.movedBatteryIds = List.of(b.getId());

        return response;
    }
     @PostMapping("/shipped/{id}")
        public ConfirmResponse markShipped(@PathVariable Long id) {

        Battery b = batteryRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Battery not found"));

        batteryRepo.delete(b);

        ConfirmResponse response = new ConfirmResponse();
        response.message = "battery shipped and removed";
        response.movedBatteryIds = List.of(id);

        return response;
}
    public static class Request {
        public int workers;
        public int workingMinutes;
    }

    // =========================
    // 🔹 WORKER SUMMARY
    // =========================

    @GetMapping("/worker-summary")
    public List<WorkerSummary> workerSummary() {

        ProcessingOptimizationService.Result last = service.getLastResult();

        if (last == null || last.workers == null) return List.of();

        return last.workers.stream().map(ws -> {
            WorkerSummary s = new WorkerSummary();
            s.workerId = ws.workerId;
            s.taskCount = ws.tasks.size();

            s.totalTime = ws.tasks.stream()
                    .mapToDouble(t -> t.end - t.start)
                    .sum();

            s.tasks = ws.tasks;
            return s;
        }).toList();
    }

    // =========================
    // 🔹 DEVICE SUMMARY
    // =========================

    @GetMapping("/device-summary")
    public List<DeviceSummary> deviceSummary() {

        ProcessingOptimizationService.Result last = service.getLastResult();

        if (last == null || last.devices == null) return List.of();

        return last.devices.stream().map(ds -> {
            DeviceSummary s = new DeviceSummary();
            s.deviceName = ds.deviceName;
            s.taskCount = ds.tasks.size();

            s.totalTime = ds.tasks.stream()
                    .mapToDouble(t -> t.end - t.start)
                    .sum();

            s.tasks = ds.tasks;
            return s;
        }).toList();
    }

    // =========================
    // 🔥 RESPONSE DTO
    // =========================

    public static class ConfirmResponse {
        public String message;
        public List<Long> movedBatteryIds;
    }

    // =========================
    // 🔹 WORKER SUMMARY DTO
    // =========================

    public static class WorkerSummary {
        public int workerId;
        public int taskCount;
        public double totalTime;
        public List<ProcessingOptimizationService.Task> tasks;
    }

    // =========================
    // 🔹 DEVICE SUMMARY DTO
    // =========================

    public static class DeviceSummary {
        public String deviceName;
        public int taskCount;
        public double totalTime;
        public List<ProcessingOptimizationService.Task> tasks;
    }
}
