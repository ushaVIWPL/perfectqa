package com.example.demo.repo;

import com.example.demo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /* =====================================================
       FETCH NOTIFICATIONS
       ===================================================== */

    // All notifications for a user (case + space safe)
    @Query("""
        SELECT n FROM Notification n
        WHERE LOWER(TRIM(n.recipientUserId)) = LOWER(TRIM(:userId))
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(
            @Param("userId") String userId
    );

    // Unread notifications for a user
    @Query("""
        SELECT n FROM Notification n
        WHERE LOWER(TRIM(n.recipientUserId)) = LOWER(TRIM(:userId))
          AND n.isRead = false
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findUnreadByUserId(
            @Param("userId") String userId
    );

    // Count unread notifications
    @Query("""
        SELECT COUNT(n) FROM Notification n
        WHERE LOWER(TRIM(n.recipientUserId)) = LOWER(TRIM(:userId))
          AND n.isRead = false
    """)
    long countUnreadByUserId(
            @Param("userId") String userId
    );

    // Recent notifications (limit handled in service)
    @Query("""
        SELECT n FROM Notification n
        WHERE LOWER(TRIM(n.recipientUserId)) = LOWER(TRIM(:userId))
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findRecentByUserId(
            @Param("userId") String userId
    );

    /* =====================================================
       COMPANY LEVEL NOTIFICATIONS
       ===================================================== */

    List<Notification> findByCompanyCodeOrderByCreatedAtDesc(String companyCode);

    /* =====================================================
       UPDATE NOTIFICATIONS
       ===================================================== */

    // Mark all notifications as read for a user
    @Modifying
    @Transactional
    @Query("""
        UPDATE Notification n
        SET n.isRead = true,
            n.readAt = CURRENT_TIMESTAMP
        WHERE LOWER(TRIM(n.recipientUserId)) = LOWER(TRIM(:userId))
          AND n.isRead = false
    """)
    int markAllAsReadByUserId(
            @Param("userId") String userId
    );

    // Mark a single notification as read
    @Modifying
    @Transactional
    @Query("""
        UPDATE Notification n
        SET n.isRead = true,
            n.readAt = CURRENT_TIMESTAMP
        WHERE n.id = :id
    """)
    int markAsRead(
            @Param("id") Long id
    );

    /* =====================================================
       CLEANUP / MAINTENANCE
       ===================================================== */

    // Delete old read notifications
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM Notification n
        WHERE n.isRead = true
          AND n.createdAt < :cutoffDate
    """)
    int deleteOldReadNotifications(
            @Param("cutoffDate") LocalDateTime cutoffDate
    );

    /* =====================================================
       REFERENCE BASED (TICKETS, PROJECTS, ETC.)
       ===================================================== */

    List<Notification> findByReferenceTypeAndReferenceId(
            String referenceType,
            Long referenceId
    );

	long countByRecipientUserIdAndIsReadFalse(String normalizedUserId);
}










