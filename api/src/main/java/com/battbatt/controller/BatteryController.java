package com.battbatt.controller;

import com.battbatt.entity.Battery;
import com.battbatt.repository.BatteryRepository;
import com.battbatt.dto.StorageSummary;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batteries")
@CrossOrigin
public class BatteryController {

    private final BatteryRepository repo;

    public BatteryController(BatteryRepository repo) {
        this.repo = repo;
    }

    // 🔹 Hae kaikki akut (FIXED)
    @GetMapping
    public List<Battery> getAll() {
        return repo.findAllWithType(); // 🔥 TÄRKEÄ
    }

    // 🔹 Lisää yksi akku
    @PostMapping
    public Battery create(@RequestBody Battery battery) {
        return repo.save(battery);
    }

    // 🔹 Lisää monta akkua
    @PostMapping("/bulk")
    public List<Battery> createMany(@RequestBody List<Battery> batteries) {
        return repo.saveAll(batteries);
    }

    // 🔹 Poista kaikki
    @DeleteMapping
    public void deleteAll() {
        repo.deleteAll();
    }

    // 🔥 SUMMARY (FIXED myös tähän)
    @GetMapping("/summary")
    public List<StorageSummary> getSummary() {
        return repo.findAllWithType().stream() // 🔥 TÄRKEÄ
                .filter(b -> b.getStorageSlot() != null && b.getBatteryType() != null)
                .collect(Collectors.groupingBy(
                        b -> b.getStorageSlot().getStorage().getName() + "|" +
                             b.getBatteryType().getChemistry()
                ))
                .entrySet()
                .stream()
                .map(entry -> {

                    String[] parts = entry.getKey().split("\\|");
                    String storage = parts[0];
                    String chemistry = parts[1];

                    double totalWeight = entry.getValue().stream()
                            .mapToDouble(b -> b.getBatteryType().getWeight())
                            .sum();

                    int count = entry.getValue().size();

                    return new StorageSummary(storage, chemistry, totalWeight, count);
                })
                .toList();
    }
}
