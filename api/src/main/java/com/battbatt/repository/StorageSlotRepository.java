package com.battbatt.repository;

import com.battbatt.entity.StorageSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StorageSlotRepository extends JpaRepository<StorageSlot, Long> {

    // 🔥 HAE PROCESSING SLOT
    @Query("SELECT s FROM StorageSlot s WHERE s.storage.name = :name")
    StorageSlot findByStorageName(@Param("name") String name);
}
