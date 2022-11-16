package com.example.homework.repos;

import com.example.homework.domain.StorageTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StorageTableRepo extends JpaRepository<StorageTable, Long> {

    StorageTable findByKey(String key);

    boolean existsByKey(String key);
}
