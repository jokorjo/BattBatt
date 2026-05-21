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
                               BatteryTypeRepository batteryTypeRepo) {
        return args -> {

            // 🔥 ESTÄ DUPLIKAATIT
            if (storageRepo.count() > 0) {
                return;
            }

            // 🔹 NMC
            createPackStorage(storageRepo, slotRepo, "NMC Pack", "NMC");
            createPalletStorage(storageRepo, slotRepo, "NMC Pallet", "NMC");
            createOpenStorage(storageRepo, slotRepo, "Open NMC", "NMC");

            // 🔹 LFP
            createPackStorage(storageRepo, slotRepo, "LFP Pack", "LFP");
            createPalletStorage(storageRepo, slotRepo, "LFP Pallet", "LFP");
            createOpenStorage(storageRepo, slotRepo, "Open LFP", "LFP");

            // 🔹 CRITICAL (PACK only)
            createPackStorage(storageRepo, slotRepo, "Critical Storage", "ANY");

            // 🔹 OTHER (PALLET only)
            createPalletStorage(storageRepo, slotRepo, "Other Pallet", "ANY");

            // 🔥 OVERFLOW STORAGE (tärkein lisä)
            createOverflowStorage(storageRepo, slotRepo, "Overflow NMC", "NMC");
            createOverflowStorage(storageRepo, slotRepo, "Overflow LFP", "LFP");
            createOverflowStorage(storageRepo, slotRepo, "Overflow Other", "ANY");
            
            // =========================
            // 🔋 BATTERY TYPES
            // =========================

            if (batteryTypeRepo.count() == 0) {

                // NMC // "name" "type" chemistry" "length meters" "width meters" "height meters" "weight" "voltage" "kwh" "mechanical time in minutes" "preparation time minutes"
                createBatteryType(batteryTypeRepo, "NMC Battery 1", "PACK", "NMC",
                        2, 1.6, 0.5, 600, 700, 82, 30, 30, "STABLE");

                createBatteryType(batteryTypeRepo, "NMC Battery 2", "PACK", "NMC",
                        1, 0.5, 0.15, 120, 350, 13, 20, 20, "STABLE");

                createBatteryType(batteryTypeRepo, "NMC Battery 3", "MODULE", "NMC",
                        0.4, 0.2, 0.1, 12, 22, 1.3, 2, 2, "STABLE");

                // LFP
                createBatteryType(batteryTypeRepo, "LFP Battery 1", "PACK", "LFP",
                        2, 1.6, 0.5, 600, 400, 60.5, 30, 30, "STABLE");

                createBatteryType(batteryTypeRepo, "LFP Battery 2", "PACK", "LFP",
                        1, 0.5, 0.15, 120, 350, 10, 20, 20, "STABLE");

                createBatteryType(batteryTypeRepo, "LFP Battery 3", "MODULE", "LFP",
                        0.4, 0.2, 0.1, 12, 22, 0.8, 2, 2, "STABLE");
            }
        };
    }

    // 🔹 PACK (A, B, C)
    private void createPackStorage(StorageRepository storageRepo,
                                   StorageSlotRepository slotRepo,
                                   String name,
                                   String chemistry) {

        Storage storage = new Storage();
        storage.setName(name);
        storage.setChemistry(chemistry);
        storage.setStorageType("PACK");
        storage = storageRepo.save(storage);

        for (String s : new String[]{"A","B","C"}) {
            StorageSlot slot = new StorageSlot();
            slot.setName(s);
            slot.setCapacity(10);
            slot.setStorage(storage);
            slotRepo.save(slot);
        }
    }

    // 🔹 PALLET (A1–H2)
    private void createPalletStorage(StorageRepository storageRepo,
                                     StorageSlotRepository slotRepo,
                                     String name,
                                     String chemistry) {

        Storage storage = new Storage();
        storage.setName(name);
        storage.setChemistry(chemistry);
        storage.setStorageType("PALLET");
        storage = storageRepo.save(storage);

        for (String s : new String[]{
                "A1","A2","B1","B2","C1","C2","D1","D2",
                "E1","E2","F1","F2","G1","G2","H1","H2"}) {

            StorageSlot slot = new StorageSlot();
            slot.setName(s);
            slot.setCapacity(1);
            slot.setStorage(storage);
            slotRepo.save(slot);
        }
    }

    // 🔹 OPEN (1 iso slot)
    private void createOpenStorage(StorageRepository storageRepo,
                                   StorageSlotRepository slotRepo,
                                   String name,
                                   String chemistry) {

        Storage storage = new Storage();
        storage.setName(name);
        storage.setChemistry(chemistry);
        storage.setStorageType("OPEN");
        storage = storageRepo.save(storage);

        StorageSlot slot = new StorageSlot();
        slot.setName("A");
        slot.setCapacity(100);
        slot.setStorage(storage);
        slotRepo.save(slot);
    }

    // 🚨 OVERFLOW (fallback – iso kapasiteetti, huonoin vaihtoehto)
    private void createOverflowStorage(StorageRepository storageRepo,
                                       StorageSlotRepository slotRepo,
                                       String name,
                                       String chemistry) {

        Storage storage = new Storage();
        storage.setName(name);
        storage.setChemistry(chemistry);
        storage.setStorageType("OPEN"); // 🔥 tärkeä: sama tyyppi kuin open
        storage = storageRepo.save(storage);

        StorageSlot slot = new StorageSlot();
        slot.setName("O1");
        slot.setCapacity(9999); // käytännössä rajaton
        slot.setStorage(storage);
        slotRepo.save(slot);
    }
// =========================
    // BATTERY TYPE METHODS
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
                                   int mechanical,
                                   int preparation,
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

        bt.setMechanicalTime(mechanical);
        bt.setPreparationTime(preparation);

        bt.setClassification(classification);

        repo.save(bt);
    }
}
