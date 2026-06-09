package com.battbatt.controller;

import com.battbatt.entity.Battery;
import com.battbatt.entity.BatteryType;
import com.battbatt.repository.BatteryRepository;
import com.battbatt.dto.StorageSummary;

import jakarta.persistence.EntityManager;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import com.battbatt.entity.StorageSlot;

@CrossOrigin(origins = "https://batt-batt-pink.vercel.app")
@RestController
@RequestMapping("/api/batteries")
    
public class BatteryController {

    private final BatteryRepository repo;
    private final EntityManager entityManager;

    public BatteryController(BatteryRepository repo, EntityManager entityManager) {
        this.repo = repo;
        this.entityManager = entityManager;
    }

    // 🔹 TEST endpoint
    @GetMapping("/test")
    public String test() {
        return "OK";
    }

    // 🔹 HAE KAIKKI AKUT (🔥 FIX)
    @GetMapping({"", "/"})
    public List<Battery> getAll() {
        return repo.findAllWithType();
    }

    // 🔹 LISÄÄ YKSI AKKU (🔥 FIX)
    @PostMapping
    public Battery create(@RequestBody Battery battery) {

        if (battery.getBatteryType() != null && battery.getBatteryType().getId() != null) {

            BatteryType realType = entityManager.find(
                    BatteryType.class,
                    battery.getBatteryType().getId()
            );

            battery.setBatteryType(realType);
        }

        return repo.save(battery);
    }

    // 🔹 BULK LISÄYS (🔥 FIX)
    @PostMapping("/bulk")
    public List<Battery> createMany(@RequestBody List<Battery> batteries) {

        return repo.saveAll(
                batteries.stream().map(b -> {

                    if (b.getBatteryType() != null && b.getBatteryType().getId() != null) {

                        BatteryType realType = entityManager.find(
                                BatteryType.class,
                                b.getBatteryType().getId()
                        );

                        b.setBatteryType(realType);
                    }

                    return b;
                }).toList()
        );
    }

    // 🔹 DELETE ALL
    @DeleteMapping
    public void deleteAll() {
        repo.deleteAll();
    }

    // 🔹 SUMMARY
    @GetMapping("/summary")
    public List<StorageSummary> getSummary() {
        return repo.findAllWithType().stream()
                .filter(b -> b.getStorageSlot() != null && b.getBatteryType() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getStorageSlot().getStorage().getName() + "|" +
                         b.getStorageSlot().getName() + "|" +
                         b.getBatteryType().getChemistry()
                ))
                .entrySet()
                .stream()
                .map(entry -> {

                List<Battery> list = entry.getValue();

                String[] parts = entry.getKey().split("\\|");
                String storage = parts[0];
                String slotName = parts[1];
                String chemistry = parts[2];

                double totalWeight = list.stream()
                        .mapToDouble(b -> b.getBatteryType().getWeight())
                        .sum();

                int count = list.size();

               StorageSlot slot = list.isEmpty() ? null : list.get(0).getStorageSlot();

                double usedVolume = list.stream()
                        .mapToDouble(b -> b.getBatteryType().getVolume())
                        .sum();

                double capacity = (slot != null) ? slot.getCapacity() : 0;

                double utilization = 0;
                if (capacity > 0) {
                utilization = (usedVolume / capacity) * 100.0;
                }
                    
                utilization = Math.round(utilization * 10.0) / 10.0;
                    
                return new StorageSummary(storage, slotName, chemistry, totalWeight, count, utilization);
                })
            
                .toList();
    }
    @GetMapping("/types")
    public List<BatteryType> getTypes() {
        return entityManager
            .createQuery("SELECT b FROM BatteryType b", BatteryType.class)
            .getResultList();
    }
}
