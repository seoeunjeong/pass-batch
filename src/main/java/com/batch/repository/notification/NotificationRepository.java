package com.batch.repository.notification;

import com.batch.entity.notification.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity,Integer> {
}
