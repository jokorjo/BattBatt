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
                otherConstraint(factory),
                capacityConstraint(factory),
                avoidOpenStorage(factory)
        };
    }

    // 🥇 Kemia (NMC / LFP)
    private Constraint chemistryConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        b.getBatteryType() != null)
                .filter(b -> {
                    String batteryChem = b.getBatteryType().getChemistry();
                    String storageChem = b.getStorageSlot().getStorage().getChemistry();

                    // ANY = sallittu fallback
                    if ("ANY".equals(storageChem)) return false;

                    return !batteryChem.equals(storageChem);
                })
                .penalize("Wrong chemistry", HardSoftScore.ONE_HARD);
    }

    // 🥈 CRITICAL → vain PACK
    private Constraint criticalConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        b.getBatteryType() != null)
                .filter(b -> {
                    // 🔥 oletus: type kertoo critical
                    if (!"CRITICAL".equalsIgnoreCase(b.getBatteryType().getType())) {
                        return false;
                    }

                    return !b.getStorageSlot().getStorage().getStorageType().equals("PACK");
                })
                .penalize("Critical must go to PACK", HardSoftScore.ONE_HARD);
    }

    // 🥉 OTHER → vain PALLET
    private Constraint otherConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        b.getBatteryType() != null)
                .filter(b -> {
                    // 🔥 oletus: type kertoo OTHER
                    if (!"OTHER".equalsIgnoreCase(b.getBatteryType().getType())) {
                        return false;
                    }

                    return !b.getStorageSlot().getStorage().getStorageType().equals("PALLET");
                })
                .penalize("Other must go to PALLET", HardSoftScore.ONE_HARD);
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
                        b.getStorageSlot().getStorage().getStorageType().equals("OPEN"))
                .penalize("Avoid open storage", HardSoftScore.ONE_SOFT);
    }
}
