package com.library.repository;

import com.library.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByUserUserId(Integer userId);
    Optional<Member> findByMembershipId(String membershipId);
    boolean existsByMembershipId(String membershipId);
    List<Member> findByNameContainingIgnoreCaseOrMembershipIdContainingIgnoreCase(String name, String membershipId);
}
