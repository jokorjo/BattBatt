package com.example.battbatt.controller;

import com.example.battbatt.entity.Storage;
import com.example.battbatt.repository.StorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storages")
public class StorageController {

    @Autowired
    private StorageRepository storageRepository;

    @PostMapping("/add")
    public Storage addStorage(@RequestBody Storage storage) {
        return storageRepository.save(storage);
    }

    @GetMapping("/all")
    public List<Storage> getAll() {
        return storageRepository.findAll();
    }
}
