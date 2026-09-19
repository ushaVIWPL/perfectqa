# Notification Testing Checklist

## Prerequisites
- Application running on http://localhost:9999
- Database connected and accessible
- At least 2 users: one to assign tickets, one to receive notifications

## Test User Credentials
- **John (Recipient)**: john.manager@iwpl.org / IWPL*733@Sp
- **Assigner**: (any user with permission to assign tickets)

---

## Step 1: Clean Up Broken Notifications

### Option A: Using Browser (Admin Login Required)
1. Login as ADMIN
2. Open browser console (F12)
3. Run this in console:
```javascript
fetch('/api/notifications/debug/delete-empty-recipients', {method: 'POST'})
  .then(r => r.json())
  .then(data => console.log(data));
```

### Option B: Using Database
```sql
DELETE FROM notifications WHERE recipient_user_id IS NULL OR recipient_user_id = '';
```

### Option C: Using Postman/cURL
```bash
curl -X POST http://localhost:9999/api/notifications/debug/delete-empty-recipients \
  -H "Cookie: JSESSIONID=your_session_id"
```

---

## Step 2: Test Notification Creation

### 2.1 Login as Assigner
- Login with a user who can assign tickets
- Note the userId in session (check browser console or logs)

### 2.2 Assign Ticket to John
1. Navigate to any ticket (or create a new one)
2. Click "Assign Ticket" button
3. Select "john.manager@iwpl.org" from dropdown
4. Click "Assign"

### 2.3 Check Console Logs
You should see:
```
=== CREATING NOTIFICATION ===
Ticket ID: [number]
Recipient (assignedTo): john.manager@iwpl.org
Sender userId: [assigner_email]
Sender name: [assigner_name]

=== NotificationService.createTicketAssignmentNotification ===
Recipient userId (assignedTo): 'john.manager@iwpl.org'
Sender userId (assignedBy): '[assigner_email]'
Sender name: '[assigner_name]'
Ticket No: TKT-XXXXX
Ticket ID: [number]

About to save notification with recipientUserId: 'john.manager@iwpl.org'
=== createNotification called ===
Before save - recipientUserId: 'john.manager@iwpl.org'
After save - recipientUserId: 'john.manager@iwpl.org'
After save - ID: [number]
Notification saved with ID: [number]
Saved recipientUserId: 'john.manager@iwpl.org'
Notification created successfully!
```

### 2.4 Verify in Database
```sql
SELECT id, recipient_user_id, sender_user_id, title, notification_type, is_read, created_at
FROM notifications
WHERE recipient_user_id = 'john.manager@iwpl.org'
ORDER BY created_at DESC
LIMIT 1;
```

**Expected**: Should return 1 row with:
- `recipient_user_id` = 'john.manager@iwpl.org' (NOT empty!)
- `notification_type` = 'TICKET_ASSIGNED'
- `is_read` = 0 (false)

---

## Step 3: Test Notification Retrieval

### 3.1 Login as John
1. Logout from current user
2. Login as: john.manager@iwpl.org / IWPL*733@Sp
3. Navigate to dashboard (/Menu)

### 3.2 Check Console Logs
You should see:
```
Getting unread count for userId: john.manager@iwpl.org
=== DEBUG: All notifications in database (countUnreadNotifications) ===
Total notifications in DB: [number]
  ID: [id], Recipient: 'john.manager@iwpl.org', Searching for: 'john.manager@iwpl.org', Match: true, Type: TICKET_ASSIGNED
Unread count for userId 'john.manager@iwpl.org': 1

getUnreadNotificationsForUser: Searching for userId 'john.manager@iwpl.org'
=== DEBUG: All notifications in database ===
Total notifications in DB: [number]
  ID: [id], Recipient: 'john.manager@iwpl.org', ...
Found 1 unread notifications for userId 'john.manager@iwpl.org'
```

### 3.3 Check UI
1. **Notification Bell Icon** (top right):
   - Should show a red badge with number "1"
   - Badge should be visible

2. **Click the Bell**:
   - Dropdown should open
   - Should see notification:
     - Title: "New Ticket Assigned"
     - Message: "You have been assigned to ticket TKT-XXXXX: [description]"
     - Time: "Just now" or time ago
     - Sender name

3. **Click the Notification**:
   - Should navigate to the ticket view page
   - Notification should be marked as read

### 3.4 Verify Notification Count API
Open browser console and run:
```javascript
fetch('/api/notifications/unread-count')
  .then(r => r.json())
  .then(data => console.log('Unread count:', data.count));
```

**Expected**: `{count: 1}` or higher

### 3.5 Verify Notification List API
```javascript
fetch('/api/notifications/unread')
  .then(r => r.json())
  .then(data => console.log('Unread notifications:', data));
```

**Expected**: Array with at least 1 notification object

---

## Step 4: Test Notification View Page

1. Navigate to: `/api/notifications`
2. Should see list of all notifications for John
3. Should see the new ticket assignment notification
4. Click "Mark as Read" - notification should disappear from unread list

---

## Step 5: Test Notification Marking as Read

1. Click on a notification
2. Should navigate to ticket page
3. Notification should be automatically marked as read
4. Refresh dashboard - notification count should decrease

---

## Debug Endpoints

### View All Notifications
```
GET /api/notifications/debug/all
```
Returns JSON with all notifications and their details.

### Check Specific User's Notifications
```sql
SELECT * FROM notifications 
WHERE recipient_user_id = 'john.manager@iwpl.org'
ORDER BY created_at DESC;
```

---

## Common Issues & Solutions

### Issue: Notification not created
**Symptoms**: No logs showing "=== CREATING NOTIFICATION ==="
**Solution**: 
- Check if assignment form is submitting correctly
- Verify `assignedTo` parameter is being sent
- Check for JavaScript errors in browser console

### Issue: Notification created but empty recipientUserId
**Symptoms**: Logs show notification created but recipientUserId is empty in database
**Solution**: 
- Check logs for "Recipient (assignedTo):" - should NOT be empty
- Verify the form is sending the correct userId value
- Check if there's a trim/validation issue

### Issue: Notification exists but John doesn't see it
**Symptoms**: Notification in database but count shows 0
**Solution**:
- Verify `recipient_user_id` in database matches exactly: 'john.manager@iwpl.org'
- Check for case sensitivity issues
- Verify John's session has correct `userId`
- Check console logs for search queries

### Issue: Notification count shows but list is empty
**Symptoms**: Badge shows number but dropdown is empty
**Solution**:
- Check browser console for JavaScript errors
- Verify `/api/notifications/unread` endpoint returns data
- Check network tab for failed requests

---

## Success Criteria

✅ All broken notifications deleted
✅ New notification created with correct recipientUserId
✅ Notification appears in database with proper values
✅ John can see notification count badge
✅ John can see notification in dropdown
✅ Clicking notification navigates to ticket
✅ Notification marked as read after viewing

---

## Quick Test Script

Run this in browser console after logging in as John:

```javascript
// Test notification APIs
async function testNotifications() {
    console.log('Testing notifications...');
    
    // Test count
    const countRes = await fetch('/api/notifications/unread-count');
    const countData = await countRes.json();
    console.log('Unread count:', countData.count);
    
    // Test list
    const listRes = await fetch('/api/notifications/unread');
    const listData = await listRes.json();
    console.log('Unread notifications:', listData);
    
    // Test all (debug)
    const allRes = await fetch('/api/notifications/debug/all');
    const allData = await allRes.json();
    console.log('All notifications:', allData);
    
    return {count: countData.count, list: listData, all: allData};
}

testNotifications();
```












