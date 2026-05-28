package com.library.management.repository;

import com.library.management.entity.LibrarySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibrarySettingRepository extends JpaRepository<LibrarySetting, Long> {
    Optional<LibrarySetting> findBySettingKey(String key);
    boolean existsBySettingKey(String key);
}