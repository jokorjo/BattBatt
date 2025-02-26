package com.example.battbatt.repository;

import com.example.battbatt.model.Battery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository  // Tämä annotaatio kertoo Springille, että tämä on repository-luokka
public interface BatteryRepository extends JpaRepository<Battery, Long> {

    // Lisää metodi, joka hakee akkuja niiden tilan perusteella
    @Query("SELECT b FROM Battery b WHERE b.status = ?1")
    List<Battery> findByStatus(String status);

    // Lisää metodi, joka hakee akkuja niiden kapasiteetin perusteella
    @Query("SELECT b FROM Battery b WHERE b.capacity > ?1")
    List<Battery> findByCapacityGreaterThan(int capacity);
    
    // Lisää metodi, joka hakee akkuja niiden sijainnin perusteella
    @Query("SELECT b FROM Battery b WHERE b.location = ?1")
    List<Battery> findByLocation(String location);
}
