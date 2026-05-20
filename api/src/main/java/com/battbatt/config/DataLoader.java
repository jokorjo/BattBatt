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

            // 🔹 NMC PACK STORAGE
            Storage nmcPack = new Storage();
            nmcPack.setName("NMC Pack");
            nmcPack.setChemistry("NMC");
            nmcPack.setStorageType("PACK");

            storageRepo.save(nmcPack); // 🔥 TÄRKEIN FIX

            for (String s : new String[]{"A","B","C"}) {
                StorageSlot slot = new StorageSlot();
                slot.setName(s);
                slot.setCapacity(10);
                slot.setStorage(nmcPack);
                slotRepo.save(slot);
            }

            // 🔹 NMC PALLET STORAGE
            Storage nmcPallet = new Storage();
            nmcPallet.setName("NMC Pallet");
            nmcPallet.setChemistry("NMC");
            nmcPallet.setStorageType("PALLET");

            storageRepo.save(nmcPallet); // 🔥 FIX

            for (String s : new String[]{
                    "A1","A2","B1","B2","C1","C2","D1","D2",
                    "E1","E2","F1","F2","G1","G2","H1","H2"}) {

                StorageSlot slot = new StorageSlot();
                slot.setName(s);
                slot.setCapacity(1);
                slot.setStorage(nmcPallet);
                slotRepo.save(slot);
            }

            // 🔹 OPEN STORAGE
            Storage open = new Storage();
            open.setName("Open Storage");
            open.setChemistry("ANY");
            open.setStorageType("OPEN");

            storageRepo.save(open); // 🔥 FIX

            StorageSlot openSlot = new StorageSlot();
            openSlot.setName("A");
            openSlot.setCapacity(100);
            openSlot.setStorage(open);
            slotRepo.save(openSlot);
        };
    }
}
