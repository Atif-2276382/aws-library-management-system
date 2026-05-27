package com.library.repository;

import com.library.entity.Lending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LendingRepository extends JpaRepository<Lending, Long> {
    List<Lending> findByBookId(Long bookId);
    List<Lending> findByMemberId(Long memberId);
    long countByMemberIdAndReturnDateIsNull(
            Long memberId);
}