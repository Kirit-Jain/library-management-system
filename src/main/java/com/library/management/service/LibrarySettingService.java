package com.library.management.service;

import com.library.management.entity.LibrarySetting;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.LibrarySettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LibrarySettingService {

    private final LibrarySettingRepository settingRepository;

    @Cacheable(value = "settings", key = "#key")
    public String getStringSetting(String key) {
        return settingRepository.findBySettingKey(key)
            .map(LibrarySetting::getSettingValue)
            .orElseThrow(() -> new ResourceNotFoundException("Setting", "key", key));
    }

    public Integer getIntSetting(String key) {
        return Integer.parseInt(getStringSetting(key));
    }

    public BigDecimal getDecimalSetting(String key) {
        return new BigDecimal(getStringSetting(key));
    }

    public Boolean getBooleanSetting(String key) {
        return Boolean.parseBoolean(getStringSetting(key));
    }

    public List<LibrarySetting> getAllSettings() {
        return settingRepository.findAll();
    }

    @Transactional
    @CacheEvict(value = "settings", key = "#key")
    public LibrarySetting updateSetting(String key, String value, Long userId) {
        LibrarySetting setting = settingRepository.findBySettingKey(key)
            .orElseThrow(() -> new ResourceNotFoundException("Setting", "key", key));

        setting.setSettingValue(value);
        setting.setUpdatedBy(userId);

        log.info("Setting updated: {} = {}", key, value);
        return settingRepository.save(setting);
    }
}