package com.battbatt.service;

import com.battbatt.model.Battery;
import com.battbatt.repository.BatteryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BatteryService {

    private final BatteryRepository batteryRepository;

    @Autowired
    public BatteryService(BatteryRepository batteryRepository) {
        this.batteryRepository = batteryRepository;
    }

    public List<Battery> getAllBatteries() {
        return batteryRepository.findAll();
    }

    public Optional<Battery> getBatteryById(Long id) {
        return batteryRepository.findById(id);
    }

    public Battery createBattery(Battery battery) {
        return batteryRepository.save(battery);
    }

    public Battery updateBattery(Long id, Battery battery) {
        if (batteryRepository.existsById(id)) {
            battery.setId(id);
            return batteryRepository.save(battery);
        }
        return null; // or throw an exception
    }

    public void deleteBattery(Long id) {
        batteryRepository.deleteById(id);
    }
}
