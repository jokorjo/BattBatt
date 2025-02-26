package com.example.battbatt.repository;

import com.example.battbatt.model.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository  // Tämä on tärkeä annotaatio, joka kertoo Springille, että tämä on repository
public interface StorageRepository extends JpaRepository<Storage, Long> {

    // Lisää metodi, joka hakee varastoja niiden tilan perusteella
    @Query("SELECT s FROM Storage s WHERE s.status = ?1")
    List<Storage> findByStatus(String status);

    // Lisää metodi, joka palauttaa varaston tietyn alueen perusteella
    @Query("SELECT s FROM Storage s WHERE s.location = ?1")
    List<Storage> findByLocation(String location);
}
