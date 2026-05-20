package com.battbatt.config;

import com.battbatt.entity.*;
import com.battbatt.repository.StorageRepository;
import com.battbatt.repository.StorageSlotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(StorageRepository storageRepo,
                               StorageSlotRepository slotRepo) {
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
}
