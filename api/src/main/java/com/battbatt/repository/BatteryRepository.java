package com.battbatt.repository;

import com.battbatt.entity.Battery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatteryRepository extends JpaRepository<Battery, Long> {
}
