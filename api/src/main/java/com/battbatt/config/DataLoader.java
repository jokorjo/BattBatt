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

            // 🔥 ESTÄ DUPLIKAATIT (tärkeä)
            if (storageRepo.count() > 0) {
                return;
            }

            // 🔹 NMC PACK STORAGE
            Storage nmcPack = new Storage();
            nmcPack.setName("NMC Pack");
            nmcPack.setChemistry("NMC");
            nmcPack.setStorageType("PACK");

            storageRepo.save(nmcPack);

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

            storageRepo.save(nmcPallet);

            for (String s : new String[]{
                    "A1","A2","B1","B2","C1","C2","D1","D2",
                    "E1","E2","F1","F2","G1","G2","H1","H2"}) {

                StorageSlot slot = new StorageSlot();
                slot.setName(s);
                slot.setCapacity(1);
                slot.setStorage(nmcPallet);
                slotRepo.save(slot);
            }

            // 🔹 OPEN NMC
Storage openNmc = new Storage();
openNmc.setName("Open NMC");
openNmc.setChemistry("NMC");
openNmc.setStorageType("OPEN");
openNmc = storageRepo.save(openNmc);

StorageSlot openNmcSlot = new StorageSlot();
openNmcSlot.setName("A");
openNmcSlot.setCapacity(100);
openNmcSlot.setStorage(openNmc);
slotRepo.save(openNmcSlot);

// 🔹 OPEN LFP
Storage openLfp = new Storage();
openLfp.setName("Open LFP");
openLfp.setChemistry("LFP");
openLfp.setStorageType("OPEN");
openLfp = storageRepo.save(openLfp);

StorageSlot openLfpSlot = new StorageSlot();
openLfpSlot.setName("A");
openLfpSlot.setCapacity(100);
openLfpSlot.setStorage(openLfp);
slotRepo.save(openLfpSlot);
        };
    }
}
