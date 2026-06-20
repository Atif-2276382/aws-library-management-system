package com.library.repository;

import com.library.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberMemberIdOrderBySentAtDesc(Integer memberId);

    void deleteByMemberMemberId(Integer memberId);
}
