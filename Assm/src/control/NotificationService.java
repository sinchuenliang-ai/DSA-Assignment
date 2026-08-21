package control;

import adt.ListInterface;
import adt.LinkedList;
import entity.Notification;

/**
 * NotificationService - Handles notification-related business logic
 */
public class NotificationService {
    private ListInterface<Notification> notificationList;

    public NotificationService(ListInterface<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    
     //Gets notifications for a specific member
    public String getMemberNotifications(String memberId) {
        ListInterface<Notification> memberNotifs = new LinkedList<>();
        int unread = 0;
        for (int i = 1; i <= notificationList.getNumberOfEntries(); i++) {
            Notification n = notificationList.getEntry(i);
            if (n.getMemberId().equals(memberId) && !n.isDismissed()) {
                memberNotifs.add(n);
                if (!n.isRead()) unread++;
            }
        }
        if (memberNotifs.isEmpty()) return "No notifications.";
        StringBuilder sb = new StringBuilder();
        sb.append("Notifications\n=================================\n");
        sb.append("Unread: ").append(unread).append(" | Total: ").append(memberNotifs.getNumberOfEntries()).append("\n");
        sb.append("------------------------------------------------------------\n");
        for (int i = 1; i <= memberNotifs.getNumberOfEntries(); i++) {
            Notification n = memberNotifs.getEntry(i);
            sb.append(n.toString()).append("\n");
        }
        return sb.toString();
    }

    
     //Gets all notifications
    public String getAllNotifications() {
        if (notificationList.isEmpty()) return "No notifications.";
        StringBuilder sb = new StringBuilder();
        sb.append("All Notifications\n=================================\n");
        sb.append("Total: ").append(notificationList.getNumberOfEntries()).append("\n");
        sb.append("------------------------------------------------------------\n");
        for (int i = 1; i <= notificationList.getNumberOfEntries(); i++) {
            sb.append(notificationList.getEntry(i)).append("\n");
        }
        return sb.toString();
    }

    
      //Marks a notification as read
    public boolean markNotificationRead(String notificationId) {
        for (int i = 1; i <= notificationList.getNumberOfEntries(); i++) {
            Notification n = notificationList.getEntry(i);
            if (n.getNotificationId().equals(notificationId)) {
                n.setRead(true);
                return true;
            }
        }
        return false;
    }

   
     // Marks all notifications for a member as read
    public boolean markMemberNotificationsRead(String memberId) {
        boolean marked = false;
        for (int i = 1; i <= notificationList.getNumberOfEntries(); i++) {
            Notification n = notificationList.getEntry(i);
            if (n.getMemberId().equals(memberId) && !n.isRead()) {
                n.setRead(true);
                marked = true;
            }
        }
        return marked;
    }

    
     //Dismisses a notification
    public boolean dismissNotification(String notificationId) {
        for (int i = 1; i <= notificationList.getNumberOfEntries(); i++) {
            Notification n = notificationList.getEntry(i);
            if (n.getNotificationId().equals(notificationId)) {
                n.setDismissed(true);
                return true;
            }
        }
        return false;
    }

      //Generates promotional notifications
    public int generatePromotionalNotifications() {
        return notificationList.getNumberOfEntries();
    }
}