package com.library.repository;

import com.library.entity.Lending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LendingRepository extends JpaRepository<Lending, Integer> {

    long countByMemberMemberIdAndReturnDateIsNull(Integer memberId);

    List<Lending> findByMemberMemberId(Integer memberId);

    void deleteByMemberMemberId(Integer memberId);

    List<Lending> findByReturnDateIsNull();

    @Query("SELECT l FROM Lending l WHERE l.returnDate IS NULL AND l.dueDate <= :threshold")
    List<Lending> findDueOrOverdue(LocalDateTime threshold);

    @Query("""
    SELECT l
    FROM Lending l
    WHERE l.returnDate IS NULL
    AND l.dueDate < :now
    """)
List<Lending> findOverdueBooks(@Param("now") LocalDateTime now);
}
