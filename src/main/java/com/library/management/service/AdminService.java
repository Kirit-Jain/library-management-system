package com.library.management.service;

import com.library.management.entity.User;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User lockUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsLocked(true);
        log.warn("User locked: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public User unlockUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsLocked(false);
        user.setFailedAttempts(0);
        log.info("User unlocked: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public User deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsActive(false);
        log.warn("User deactivated: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public User activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsActive(true);
        log.info("User activated: {}", user.getEmail());
        return userRepository.save(user);
    }
}