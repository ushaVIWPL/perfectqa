# Clean Up Broken Notifications

## Problem
All existing notifications have empty `recipientUserId` (''), making them unusable. These were created before the validation fix.

## Solution Options

### Option 1: Using the API Endpoint (Recommended)

1. **Login to the application** (any user can do this now)

2. **Open browser console** (F12)

3. **Run this command**:
```javascript
fetch('/api/notifications/debug/delete-empty-recipients', {
    method: 'POST',
    credentials: 'include'
})
.then(response => response.json())
.then(data => {
    console.log('Result:', data);
    if (data.success) {
        alert('Deleted ' + data.deletedCount + ' broken notifications!');
    } else {
        alert('Error: ' + data.message);
    }
})
.catch(error => console.error('Error:', error));
```

4. **Expected Response**:
```json
{
    "success": true,
    "message": "Deleted 10 notifications with empty recipientUserId",
    "deletedCount": 10,
    "deletedIds": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
    "remainingCount": 0
}
```

### Option 2: Using SQL Directly

1. **Connect to your MySQL database**:
   - Host: 45.130.164.100:3306
   - Database: perfect
   - User: philip

2. **Run the SQL script**:
```sql
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

-- Verify deletion (should return 0)
SELECT COUNT(*) as remaining_broken_notifications
FROM notifications
WHERE recipient_user_id IS NULL 
   OR recipient_user_id = ''
   OR TRIM(recipient_user_id) = '';
```

### Option 3: Using Postman/cURL

```bash
# First, get your session cookie by logging in
# Then use it in the request:

curl -X POST http://localhost:9999/api/notifications/debug/delete-empty-recipients \
  -H "Cookie: JSESSIONID=your_session_id_here" \
  -H "Content-Type: application/json"
```

## After Cleanup

1. **Verify cleanup worked**:
   - Visit: `GET /api/notifications/debug/all`
   - Should show 0 notifications (or only valid ones)

2. **Test creating a new notification**:
   - Assign a ticket to a user
   - Check console logs for notification creation
   - Verify the new notification has correct `recipientUserId`

3. **Test notification retrieval**:
   - Login as the assigned user
   - Should see the notification in the bell icon

## Prevention

The validation code now prevents creating notifications with empty `recipientUserId`. If you see the error:
```
ERROR: Attempting to create notification with empty recipientUserId!
```

This means the `assignedTo` parameter is empty when assigning a ticket. Check:
- The assignment form is sending the correct userId
- The dropdown is selecting a valid user
- No JavaScript errors preventing form submission












