package com.example.repository;


import com.example.model.Notification;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndRead(User user, boolean read);
    long countByUserAndRead(User user, boolean read);
}
