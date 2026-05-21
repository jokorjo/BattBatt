package com.battbatt.solver;

import com.battbatt.entity.Battery;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

public class BatteryConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                mustHaveSlot(factory),
                chemistryConstraint(factory),
                typeConstraint(factory),
                criticalConstraint(factory),
                capacityConstraint(factory),
                preferCorrectChemistry(factory), // 🔥 UUSI
                avoidOpenStorage(factory),
                avoidOverflow(factory)
        };
    }

    // ❗ EI NULL SLOT
    private Constraint mustHaveSlot(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() == null)
                .penalize("Battery must have slot", HardSoftScore.ONE_HARD);
    }

    // 🔥 KEMIA (HARD)
    private Constraint chemistryConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null && b.getBatteryType() != null)
                .filter(b -> {
                    String batteryChem = b.getBatteryType().getChemistry();
                    String storageChem = b.getStorageSlot().getStorage().getChemistry();

                    if ("ANY".equalsIgnoreCase(storageChem)) return false;

                    return !batteryChem.equalsIgnoreCase(storageChem);
                })
                .penalize("Wrong chemistry", HardSoftScore.ofHard(100)); // 🔥 vahvempi
    }

    // 🧱 TYPE (HARD)
    private Constraint typeConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null && b.getBatteryType() != null)
                .filter(b -> {
                    String type = b.getBatteryType().getType();
                    String storageType = b.getStorageSlot().getStorage().getStorageType();

                    if ("PACK".equalsIgnoreCase(type)) {
                        return !(storageType.equalsIgnoreCase("PACK")
                                || storageType.equalsIgnoreCase("OPEN"));
                    }

                    if ("MODULE".equalsIgnoreCase(type)) {
                        return !(storageType.equalsIgnoreCase("PALLET")
                                || storageType.equalsIgnoreCase("OPEN"));
                    }

                    return false;
                })
                .penalize("Wrong storage type", HardSoftScore.ofHard(100));
    }

    // 🥈 CRITICAL
    private Constraint criticalConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null && b.getBatteryType() != null)
                .filter(b -> {
                    if (!"CRITICAL".equalsIgnoreCase(b.getClassification())) return false;
                    return !"PACK".equalsIgnoreCase(
                            b.getStorageSlot().getStorage().getStorageType());
                })
                .penalize("Critical must go to PACK", HardSoftScore.ofHard(100));
    }

    // 🔥 KAPASITEETTI (SCALED)
    private Constraint capacityConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null && b.getBatteryType() != null)
                .groupBy(
                        Battery::getStorageSlot,
                        ConstraintCollectors.sum(b -> b.getBatteryType().getScaledVolume())
                )
                .filter((slot, used) -> used > slot.getScaledCapacity())
                .penalize("Over capacity",
                        HardSoftScore.ofHard(100),
                        (slot, used) -> used - slot.getScaledCapacity());
    }

    // 🔥 PEHMEÄ OHJAUS OIKEAAN KEMIAAN
    private Constraint preferCorrectChemistry(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null && b.getBatteryType() != null)
                .filter(b -> {
                    String batteryChem = b.getBatteryType().getChemistry();
                    String storageChem = b.getStorageSlot().getStorage().getChemistry();

                    return storageChem.equalsIgnoreCase(batteryChem);
                })
                .reward("Prefer correct chemistry", HardSoftScore.ONE_SOFT);
    }

    // ⚠️ OPEN
    private Constraint avoidOpenStorage(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        "OPEN".equalsIgnoreCase(
                                b.getStorageSlot().getStorage().getStorageType()))
                .penalize("Avoid open storage", HardSoftScore.ofSoft(5));
    }

    // 🚨 OVERFLOW
    private Constraint avoidOverflow(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        "OVERFLOW".equalsIgnoreCase(
                                b.getStorageSlot().getStorage().getStorageType()))
                .penalize("Avoid overflow storage", HardSoftScore.ofSoft(50));
    }
}
