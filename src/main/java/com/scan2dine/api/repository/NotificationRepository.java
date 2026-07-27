package com.scan2dine.api.repository;

import com.scan2dine.api.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReadStatusFalseOrderByCreatedAtDesc();
}
