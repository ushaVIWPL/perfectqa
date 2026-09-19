# Notification Testing Guide

## Issue Found
All existing notifications in the database have empty `recipientUserId` (''), making them unusable.

## Steps to Test Notifications

### Step 1: Clean Up Broken Notifications
1. Login as ADMIN
2. Visit: `POST /api/notifications/debug/delete-empty-recipients`
   - This will delete all notifications with empty recipientUserId
   - Or manually delete them from the database

### Step 2: Test Notification Creation
1. **Login as a user who can assign tickets** (e.g., usha varma)
   - Email: (your test user email)
   - Password: (your test user password)

2. **Assign a ticket to John**:
   - Go to any ticket
   - Click "Assign Ticket"
   - Select "john.manager@iwpl.org" from the dropdown
   - Submit

3. **Check Console Logs** - You should see:
   ```
   === CREATING NOTIFICATION ===
   Ticket ID: [ticket_id]
   Recipient (assignedTo): john.manager@iwpl.org
   Sender userId: [sender_email]
   Sender name: [sender_name]
   
   === NotificationService.createTicketAssignmentNotification ===
   Recipient userId (assignedTo): 'john.manager@iwpl.org'
   Sender userId (assignedBy): '[sender_email]'
   ...
   About to save notification with recipientUserId: 'john.manager@iwpl.org'
   Notification saved with ID: [id]
   Saved recipientUserId: 'john.manager@iwpl.org'
   ```

### Step 3: Verify Notification in Database
1. Check `/api/notifications/debug/all` endpoint
2. Verify the new notification has:
   - `recipientUserId`: "john.manager@iwpl.org" (NOT empty)
   - `notificationType`: "TICKET_ASSIGNED"
   - `isRead`: false

### Step 4: Test Notification Retrieval
1. **Login as John**:
   - Email: john.manager@iwpl.org
   - Password: IWPL*733@Sp

2. **Check Console Logs** - You should see:
   ```
   Getting unread count for userId: john.manager@iwpl.org
   Unread count for john.manager@iwpl.org: 1
   
   getUnreadNotificationsForUser: Searching for userId 'john.manager@iwpl.org'
   Found 1 unread notifications for userId 'john.manager@iwpl.org'
   ```

3. **Check the UI**:
   - The notification bell should show a badge with count > 0
   - Click the bell to see the notification
   - The notification should appear in the dropdown

### Step 5: Verify Notification Display
1. The notification should show:
   - Title: "New Ticket Assigned"
   - Message: "You have been assigned to ticket [TICKET_NO]: [description]"
   - Clicking it should navigate to the ticket

## Debugging Endpoints

### View All Notifications
```
GET /api/notifications/debug/all
```
Returns all notifications with their details.

### Delete Broken Notifications
```
POST /api/notifications/debug/delete-empty-recipients
```
(Admin only) Deletes notifications with empty recipientUserId.

## Common Issues

### Issue: Notification not created
**Check:**
- Console logs for "=== CREATING NOTIFICATION ==="
- If missing, the assignment code path might not be executing
- Check if exception is being caught silently

### Issue: Notification created but empty recipientUserId
**Check:**
- Console logs for "Recipient (assignedTo):" - should NOT be empty
- The `assignedTo` parameter from the form
- Validation should now prevent this

### Issue: Notification exists but John doesn't see it
**Check:**
- Console logs for "Searching for userId 'john.manager@iwpl.org'"
- Verify the notification's `recipientUserId` matches exactly
- Check for case sensitivity issues
- Verify John's session has correct `userId`

## Expected Behavior After Fix

1. ✅ New notifications are created with correct `recipientUserId`
2. ✅ Validation prevents creating notifications with empty `recipientUserId`
3. ✅ Users can see notifications assigned to them
4. ✅ Notification count badge shows correct number
5. ✅ Clicking notification navigates to the ticket

## Testing Checklist

- [ ] Clean up broken notifications
- [ ] Assign ticket to john.manager@iwpl.org
- [ ] Verify notification created in database with correct recipientUserId
- [ ] Login as john.manager@iwpl.org
- [ ] Verify notification appears in UI
- [ ] Verify notification count badge shows correct number
- [ ] Click notification and verify it navigates to ticket
- [ ] Mark notification as read and verify it disappears from unread list












