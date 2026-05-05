package com.battbatt.service;

import org.optaplanner.core.api.solver.Solver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.battbatt.model.Battery;
import com.battbatt.model.Storage;
import com.battbatt.repository.BatteryRepository;
import com.battbatt.repository.StorageRepository;

@Service
public class OptaPlannerService {

    private final Solver<Battery> solver;
    private final BatteryRepository batteryRepository;
    private final StorageRepository storageRepository;

    @Autowired
    public OptaPlannerService(Solver<Battery> solver, BatteryRepository batteryRepository, StorageRepository storageRepository) {
        this.solver = solver;
        this.batteryRepository = batteryRepository;
        this.storageRepository = storageRepository;
    }

    public void solveBatteryStorageOptimization() {
        // Lataa akku- ja varastotiedot tietokannasta ja ratkaise optimointiongelma
        // Tällöin OptaPlanner ratkaisee, miten akkuja käsitellään ja varastoidaan
        List<Battery> batteries = batteryRepository.findAll();
        List<Storage> storages = storageRepository.findAll();

        // Optimoi akut ja varastot OptaPlannerilla
        // Esimerkki optimoimisesta, tarkempi määrittely riippuu Solverin konfiguraatiosta
        solver.solve(new BatteryStorageProblem(batteries, storages));
    }
}
