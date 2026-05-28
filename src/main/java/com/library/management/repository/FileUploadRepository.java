package com.library.management.repository;

import com.library.management.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
    List<FileUpload> findAllByEntityTypeAndEntityId(String entityType, Long entityId);
    Optional<FileUpload> findByFileName(String fileName);
}