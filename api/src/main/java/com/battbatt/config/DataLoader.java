package com.battbatt.config;

import com.battbatt.entity.*;
import com.battbatt.repository.*;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(StorageRepository storageRepo,
                               StorageSlotRepository slotRepo,
                               BatteryTypeRepository batteryTypeRepo,
                               DeviceRepository deviceRepo) {

        return args -> {

            // =========================
            // 🔥 STORAGE
            // =========================
            if (storageRepo.count() == 0) {

                // NMC
                createPackStorage(storageRepo, slotRepo, "NMC Pack", "NMC");
                createPalletStorage(storageRepo, slotRepo, "NMC Pallet", "NMC");
                createOpenStorage(storageRepo, slotRepo, "Open NMC", "NMC");

                // LFP
                createPackStorage(storageRepo, slotRepo, "LFP Pack", "LFP");
                createPalletStorage(storageRepo, slotRepo, "LFP Pallet", "LFP");
                createOpenStorage(storageRepo, slotRepo, "Open LFP", "LFP");

                // CRITICAL
                createPackStorage(storageRepo, slotRepo, "Critical Storage", "ANY");

                // OTHER
                createPalletStorage(storageRepo, slotRepo, "Other Pallet", "ANY");

                // OVERFLOW
                createOverflowStorage(storageRepo, slotRepo, "Overflow NMC", "NMC");
                createOverflowStorage(storageRepo, slotRepo, "Overflow LFP", "LFP");
                createOverflowStorage(storageRepo, slotRepo, "Overflow Other", "ANY");
            }

            // =========================
            // 🔋 BATTERY TYPES
            // =========================
            if (batteryTypeRepo.count() == 0) {

                createBatteryType(batteryTypeRepo, "NMC Battery 1", "PACK", "NMC",
                        2, 1.6, 0.5, 600, 700, 82, 30, 30, "STABLE");

                createBatteryType(batteryTypeRepo, "NMC Battery 2", "PACK", "NMC",
                        1, 0.5, 0.15, 120, 350, 13, 20, 20, "STABLE");

                createBatteryType(batteryTypeRepo, "NMC Battery 3", "MODULE", "NMC",
                        0.4, 0.2, 0.1, 12, 22, 1.3, 2, 2, "STABLE");

                createBatteryType(batteryTypeRepo, "LFP Battery 1", "PACK", "LFP",
                        2, 1.6, 0.5, 600, 400, 60.5, 30, 30, "STABLE");

                createBatteryType(batteryTypeRepo, "LFP Battery 2", "PACK", "LFP",
                        1, 0.5, 0.15, 120, 350, 10, 20, 20, "STABLE");

                createBatteryType(batteryTypeRepo, "LFP Battery 3", "MODULE", "LFP",
                        0.4, 0.2, 0.1, 12, 22, 0.8, 2, 2, "STABLE");
            }

            // =========================
            // ⚙️ DEVICES
            // =========================
            if (deviceRepo.count() == 0) {

                Device d1 = createDevice(deviceRepo, "BLU 500C");
                Device d2 = createDevice(deviceRepo, "BLU 700C");
                Device d3 = createDevice(deviceRepo, "ELR 11000-30 3U");
                Device d4 = createDevice(deviceRepo, "ELM-5200-12");

                // 🔴 TÄYTÄ NÄMÄ SUN ARVOILLA
                createProfile(d1, 1, 5, 0);
                createProfile(d1, 6, 10, 40);
                createProfile(d1, 11, 20, 80);
                createProfile(d1, 21, 30, 165);
                createProfile(d1, 31, 42, 185);
                createProfile(d1, 43, 60, 210);
                createProfile(d1, 61, 72, 270);
                createProfile(d1, 73, 90, 220);
                createProfile(d1, 91, 100, 200);
                createProfile(d1, 101, 130, 150);
                createProfile(d1, 131, 142, 140);
                createProfile(d1, 143, 200, 100);
                createProfile(d1, 201, 260, 75);
                createProfile(d1, 261, 300, 65);
                createProfile(d1, 301, 400, 50);
                createProfile(d1, 401, 500, 50);
                createProfile(d1, 501, 600, 0);
                createProfile(d1, 601, 700, 0);
                createProfile(d1, 701, 800, 0);

                createProfile(d2, 1, 5, 50);
                createProfile(d2, 6, 10, 50);
                createProfile(d2, 11, 20, 60);
                createProfile(d2, 21, 30, 120);
                createProfile(d2, 31, 42, 120);
                createProfile(d2, 43, 60, 120);
                createProfile(d2, 61, 72, 125);
                createProfile(d2, 73, 90, 150);
                createProfile(d2, 91, 100, 190);
                createProfile(d2, 101, 130, 210);
                createProfile(d2, 131, 142, 260);
                createProfile(d2, 143, 200, 110);
                createProfile(d2, 201, 260, 110);
                createProfile(d2, 261, 300, 110);
                createProfile(d2, 301, 400, 105);
                createProfile(d2, 401, 500, 80);
                createProfile(d2, 501, 600, 70);
                createProfile(d2, 601, 700, 60);
                createProfile(d2, 701, 800, 0);

                createProfile(d3, 1, 5, 30);
                createProfile(d3, 6, 10, 30);
                createProfile(d3, 11, 20, 30);
                createProfile(d3, 21, 30, 30);
                createProfile(d3, 31, 42, 30);
                createProfile(d3, 43, 60, 30);
                createProfile(d3, 61, 72, 30);
                createProfile(d3, 73, 90, 30);
                createProfile(d3, 91, 100, 30);
                createProfile(d3, 101, 130, 30);
                createProfile(d3, 131, 142, 30);
                createProfile(d3, 143, 200, 30);
                createProfile(d3, 201, 260, 30);
                createProfile(d3, 261, 300, 30);
                createProfile(d3, 301, 400, 25);
                createProfile(d3, 401, 500, 20);
                createProfile(d3, 501, 600, 16);
                createProfile(d3, 601, 700, 14);
                createProfile(d3, 701, 800, 12);

                createProfile(d4, 1, 5, 12);
                createProfile(d4, 6, 10, 12);
                createProfile(d4, 11, 20, 12);
                createProfile(d4, 21, 30, 10);
                createProfile(d4, 31, 42, 7);
                createProfile(d4, 43, 60, 5);
                createProfile(d4, 61, 72, 4);
                createProfile(d4, 73, 90, 3);
                createProfile(d4, 91, 100, 3);
                createProfile(d4, 101, 130, 2);
                createProfile(d4, 131, 142, 2);
                createProfile(d4, 143, 200, 1);
                createProfile(d4, 201, 260, 0);
                createProfile(d4, 261, 300, 0);
                createProfile(d4, 301, 400, 0);
                createProfile(d4, 401, 500, 0);
                createProfile(d4, 501, 600, 0);
                createProfile(d4, 601, 700, 0);
                createProfile(d4, 701, 800, 0);
            }
        };
    }

    // =========================
    // STORAGE METHODS
    // =========================

    private void createPackStorage(StorageRepository repo, StorageSlotRepository slotRepo, String name, String chemistry) {
        Storage s = new Storage();
        s.setName(name);
        s.setChemistry(chemistry);
        s.setStorageType("PACK");
        s = repo.save(s);

        for (String n : new String[]{"A","B","C"}) {
            StorageSlot slot = new StorageSlot();
            slot.setName(n);
            slot.setCapacity(10);
            slot.setStorage(s);
            slotRepo.save(slot);
        }
    }

    private void createPalletStorage(StorageRepository repo, StorageSlotRepository slotRepo, String name, String chemistry) {
        Storage s = new Storage();
        s.setName(name);
        s.setChemistry(chemistry);
        s.setStorageType("PALLET");
        s = repo.save(s);

        for (String n : new String[]{
                "A1","A2","B1","B2","C1","C2","D1","D2",
                "E1","E2","F1","F2","G1","G2","H1","H2"}) {

            StorageSlot slot = new StorageSlot();
            slot.setName(n);
            slot.setCapacity(1);
            slot.setStorage(s);
            slotRepo.save(slot);
        }
    }

    private void createOpenStorage(StorageRepository repo, StorageSlotRepository slotRepo, String name, String chemistry) {
        Storage s = new Storage();
        s.setName(name);
        s.setChemistry(chemistry);
        s.setStorageType("OPEN");
        s = repo.save(s);

        StorageSlot slot = new StorageSlot();
        slot.setName("A");
        slot.setCapacity(100);
        slot.setStorage(s);
        slotRepo.save(slot);
    }

    private void createOverflowStorage(StorageRepository repo, StorageSlotRepository slotRepo, String name, String chemistry) {
        Storage s = new Storage();
        s.setName(name);
        s.setChemistry(chemistry);
        s.setStorageType("OVERFLOW"); // oikein
        s = repo.save(s);

        StorageSlot slot = new StorageSlot();
        slot.setName("O1");
        slot.setCapacity(9999);
        slot.setStorage(s);
        slotRepo.save(slot);
    }

    // =========================
    // BATTERY TYPE
    // =========================

    private void createBatteryType(BatteryTypeRepository repo,
                                   String name,
                                   String type,
                                   String chemistry,
                                   double length,
                                   double width,
                                   double height,
                                   double weight,
                                   int voltage,
                                   double kwh,
                                   int mech,
                                   int prep,
                                   String classification) {

        BatteryType bt = new BatteryType();

        bt.setName(name);
        bt.setType(type);
        bt.setChemistry(chemistry);

        bt.setLength(length);
        bt.setWidth(width);
        bt.setHeight(height);

        bt.setWeight(weight);
        bt.setVoltage(voltage);
        bt.setKwh(kwh);

        bt.setMechanicalTime(mech);
        bt.setPreparationTime(prep);

        bt.setClassification(classification);

        repo.save(bt);
    }

    // =========================
    // DEVICE HELPERS
    // =========================

    private Device createDevice(DeviceRepository repo, String name) {
        Device d = new Device();
        d.setName(name);
        return repo.save(d);
    }

    private void createProfile(Device d, int minV, int maxV, double amps) {
        DeviceProfile p = new DeviceProfile();
        p.setDevice(d);
        p.setMinVoltage(minV);
        p.setMaxVoltage(maxV);
        p.setMaxAmps(amps);

        d.getProfiles().add(p);
    }
}
