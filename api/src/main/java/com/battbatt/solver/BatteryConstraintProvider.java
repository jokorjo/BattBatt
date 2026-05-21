package com.battbatt.solver;

import com.battbatt.entity.Battery;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

public class BatteryConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                chemistryConstraint(factory),
                typeConstraint(factory),        // 🔥 LISÄTTY
                criticalConstraint(factory),
                capacityConstraint(factory),
                avoidOpenStorage(factory),
                avoidOverflow(factory)
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

    // 🧱 PACK vs MODULE (🔥 TÄRKEIN LISÄYS)
    private Constraint typeConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        b.getBatteryType() != null)
                .filter(b -> {
                    String type = b.getBatteryType().getType(); // PACK / MODULE
                    String storageType = b.getStorageSlot().getStorage().getStorageType();

                    // PACK → PACK + OPEN + OVERFLOW sallittu
                    if ("PACK".equalsIgnoreCase(type)) {
                        return !(storageType.equalsIgnoreCase("PACK") ||
                                 storageType.equalsIgnoreCase("OPEN") ||
                                 storageType.equalsIgnoreCase("OVERFLOW"));
                    }

                    // MODULE → PALLET + OPEN + OVERFLOW sallittu
                    if ("MODULE".equalsIgnoreCase(type)) {
                        return !(storageType.equalsIgnoreCase("PALLET") ||
                                 storageType.equalsIgnoreCase("OPEN") ||
                                 storageType.equalsIgnoreCase("OVERFLOW"));
                    }

                    return false;
                })
                .penalize("Wrong storage type", HardSoftScore.ONE_HARD);
    }

    // 🥈 CRITICAL → vain PACK
    private Constraint criticalConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        b.getBatteryType() != null)
                .filter(b -> {
                    String classification = b.getBatteryType().getClassification();

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
                        (slot, used) -> used - (int) slot.getCapacity());
    }

    // ⚠️ OPEN → vältetään (soft)
    private Constraint avoidOpenStorage(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        "OPEN".equalsIgnoreCase(
                                b.getStorageSlot().getStorage().getStorageType()))
                .penalize("Avoid open storage", HardSoftScore.ONE_SOFT);
    }

    // 🚨 OVERFLOW → erittäin huono (🔥 KORJATTU)
    private Constraint avoidOverflow(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        "OVERFLOW".equalsIgnoreCase(
                                b.getStorageSlot().getStorage().getStorageType()))
                .penalize("Avoid overflow storage", HardSoftScore.ofSoft(20)); // 🔥 kovempi penalty
    }
}
