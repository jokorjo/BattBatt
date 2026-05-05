package com.battbatt.solver;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.calculator.EasyScoreCalculator;
import com.battbatt.model.Battery;

import java.util.List;

public class BatteryProcessingSolver implements EasyScoreCalculator<Battery, SimpleScore> {
    @Override
    public SimpleScore calculateScore(Battery battery) {
        int score = 0;
        score -= battery.getProcessingTime(); // Lyhyempi käsittelyaika parempi
        return SimpleScore.of(score);
    }
}
