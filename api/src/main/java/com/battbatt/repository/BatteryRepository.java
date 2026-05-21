package com.battbatt.repository;

import com.battbatt.entity.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BatteryRepository extends JpaRepository<Battery, Long> {

    // 🔥 TÄRKEIN FIX: hakee batteryTypen mukaan
    @Query("SELECT b FROM Battery b JOIN FETCH b.batteryType")
    List<Battery> findAllWithType();
}
