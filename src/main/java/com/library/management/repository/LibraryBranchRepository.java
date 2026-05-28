package com.library.management.repository;

import com.library.management.entity.LibraryBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryBranchRepository extends JpaRepository<LibraryBranch, Long> {
}