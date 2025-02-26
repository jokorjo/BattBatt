package com.battbatt.service;

import com.battbatt.model.Storage;
import com.battbatt.repository.StorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StorageService {

    private final StorageRepository storageRepository;

    @Autowired
    public StorageService(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    public List<Storage> getAllStorages() {
        return storageRepository.findAll();
    }

    public Optional<Storage> getStorageById(Long id) {
        return storageRepository.findById(id);
    }

    public Storage createStorage(Storage storage) {
        return storageRepository.save(storage);
    }

    public Storage updateStorage(Long id, Storage storage) {
        if (storageRepository.existsById(id)) {
            storage.setId(id);
            return storageRepository.save(storage);
        }
        return null; // or throw an exception
    }

    public void deleteStorage(Long id) {
        storageRepository.deleteById(id);
    }
}
