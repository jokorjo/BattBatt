package com.battbatt.repository;

import com.battbatt.entity.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BatteryRepository extends JpaRepository<Battery, Long> {

    @Query("SELECT b FROM Battery b JOIN FETCH b.batteryType")
    List<Battery> findAllWithType();

    // 🔥 processing akut
    @Query("SELECT b FROM Battery b WHERE b.inProcessing = true")
    List<Battery> findProcessing();
}
