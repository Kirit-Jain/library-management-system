package com.library.management.repository;

import com.library.management.entity.FinePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinePaymentRepository extends JpaRepository<FinePayment, Long> {
    List<FinePayment> findAllByFineId(Long fineId);
}