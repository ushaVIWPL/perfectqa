-- Cleanup script to delete notifications with empty recipientUserId
-- These notifications are unusable and should be deleted

-- View broken notifications first
SELECT 
    id,
    recipient_user_id,
    sender_user_id,
    title,
    notification_type,
    is_read,
    created_at
FROM notifications
WHERE recipient_user_id IS NULL 
   OR recipient_user_id = ''
   OR TRIM(recipient_user_id) = '';

-- Delete broken notifications
DELETE FROM notifications
WHERE recipient_user_id IS NULL 
   OR recipient_user_id = ''
   OR TRIM(recipient_user_id) = '';

-- Verify deletion
SELECT COUNT(*) as remaining_broken_notifications
FROM notifications
WHERE recipient_user_id IS NULL 
   OR recipient_user_id = ''
   OR TRIM(recipient_user_id) = '';

-- Should return 0












