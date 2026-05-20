package com.battbatt.solver;

import com.battbatt.entity.Battery;
import com.battbatt.entity.StorageSlot;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

public class BatteryConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                chemistryConstraint(factory),
                capacityConstraint(factory),
                avoidOpenStorage(factory)
        };
    }

    // 🥇 Kemia ei saa olla väärä
    private Constraint chemistryConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        !b.getBatteryType().getChemistry()
                                .equals(b.getStorageSlot().getStorage().getChemistry()) &&
                        !b.getStorageSlot().getStorage().getChemistry().equals("ANY"))
                .penalize("Wrong chemistry", HardSoftScore.ONE_HARD);
    }

    // 🥈 Kapasiteetti ei saa ylittyä
    private Constraint capacityConstraint(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null) // 🔥 tärkeä null check
                .groupBy(
                        Battery::getStorageSlot,
                        ConstraintCollectors.sum(b -> b.getBatteryType().getVolume())
                )
                .filter((slot, used) -> used > slot.getCapacity())
                .penalize("Over capacity",
                        HardSoftScore.ONE_HARD,
                        (slot, used) -> 1); // 🔥 FIX
    }

    // 🥉 Vältä open storagea
    private Constraint avoidOpenStorage(ConstraintFactory factory) {
        return factory.from(Battery.class)
                .filter(b -> b.getStorageSlot() != null &&
                        b.getStorageSlot().getStorage().getStorageType().equals("OPEN"))
                .penalize("Avoid open storage", HardSoftScore.ONE_SOFT);
    }
}
