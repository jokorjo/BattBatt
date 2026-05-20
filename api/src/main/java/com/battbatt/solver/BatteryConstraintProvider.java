package com.battbatt.solver;

import com.battbatt.entity.Battery;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

public class BatteryConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                chemistryConstraint(factory),
                criticalConstraint(factory),
                capacityConstraint(factory),
                avoidOpenStorage(factory)
        };
    }

    // 🥇 Kemia (NMC / LFP / OTHER)
    private Constraint chemistryConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        b.getBatteryType() != null)
                .filter(b -> {
                    String batteryChem = b.getBatteryType().getChemistry();
                    String storageChem = b.getStorageSlot().getStorage().getChemistry();

                    // ANY = sallittu fallback
                    if ("ANY".equalsIgnoreCase(storageChem)) return false;

                    return !batteryChem.equalsIgnoreCase(storageChem);
                })
                .penalize("Wrong chemistry", HardSoftScore.ONE_HARD);
    }

    // 🥈 CRITICAL → vain PACK
    private Constraint criticalConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        b.getBatteryType() != null)
                .filter(b -> {
                    String classification = b.getBatteryType().getClassification();

                    // vain criticalit
                    if (!"CRITICAL".equalsIgnoreCase(classification)) {
                        return false;
                    }

                    String storageType = b.getStorageSlot().getStorage().getStorageType();

                    return !"PACK".equalsIgnoreCase(storageType);
                })
                .penalize("Critical must go to PACK", HardSoftScore.ONE_HARD);
    }

    // 🧱 Kapasiteetti
    private Constraint capacityConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null && b.getBatteryType() != null)
                .groupBy(
                        Battery::getStorageSlot,
                        ConstraintCollectors.sum(b -> (int) b.getBatteryType().getVolume())
                )
                .filter((slot, used) -> used > slot.getCapacity())
                .penalize("Over capacity",
                        HardSoftScore.ONE_HARD,
                        (slot, used) -> 1);
    }

    // ⚠️ OPEN → vältetään (soft)
    private Constraint avoidOpenStorage(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        "OPEN".equalsIgnoreCase(
                                b.getStorageSlot().getStorage().getStorageType()))
                .penalize("Avoid open storage", HardSoftScore.ONE_SOFT);
    }
}
