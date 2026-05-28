package com.library.management.repository;

import com.library.management.entity.Member;

import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    Optional<Member> findByMembershipNumber(String membershipNumber);

    Optional<Member> findByUserId(Long userId);

    Boolean existsByMembershipNumber(String membershipNumber);

    long countByIsActive(Boolean isActive);

    @Query("SELECT m FROM Member m WHERE m.membershipExpiry < :date AND m.isActive = true")
    List<Member> findExpiredMemberships(@Param("date") LocalDate date);

    @Query("SELECT m FROM Member m JOIN FETCH m.user WHERE m.membershipNumber = :number")
    Optional<Member> findByMembershipNumberWithUser(String number);

    @Query("SELECT m FROM Member m JOIN FETCH m.user WHERE m.user.id = :userId")
    Optional<Member> findByUserIdWithUser(@Param("userId") Long userId);
}
