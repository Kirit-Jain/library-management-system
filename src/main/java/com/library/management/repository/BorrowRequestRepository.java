package com.library.management.repository;

import com.library.management.entity.BorrowRequest;
import com.library.management.enums.BorrowRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    Page<BorrowRequest> findAllByStatusOrderByCreatedAtDesc(BorrowRequestStatus status, Pageable pageable);
    List<BorrowRequest> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
    long countByStatus(BorrowRequestStatus status);
}