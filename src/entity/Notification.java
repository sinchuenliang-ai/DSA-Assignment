package entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Notification implements Serializable {

    public enum NotificationType {
        TIER_UPGRADE, TIER_MAINTENANCE, POINTS_EARNED, 
        POINTS_REDEEMED, POINTS_EXPIRY, REDEMPTION_CONFIRMATION,
        REDEMPTION_REQUEST, PROMOTIONAL_OFFER, BIRTHDAY_BONUS,
        MEMBERSHIP_ANNIVERSARY, REFERRAL_BONUS, SPECIAL_EVENT
    }

    public enum Priority { LOW, MEDIUM, HIGH, URGENT }

    private String notificationId;
    private String memberId;
    private NotificationType type;
    private Priority priority;
    private String title;
    private String message;
    private String detailedMessage;
    private boolean isRead;
    private boolean isDismissed;
    private LocalDateTime timestamp;
    private LocalDateTime expiryDate;
    private String actionLink;
    private String relatedData;

    public Notification() {
        this.isRead = false;
        this.isDismissed = false;
        this.timestamp = LocalDateTime.now();
        this.notificationId = generateNotificationId();
        this.priority = Priority.MEDIUM;
    }

    public Notification(String memberId, NotificationType type, String title, String message) {
        this();
        this.memberId = memberId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.detailedMessage = message;
    }

    public Notification(String memberId, NotificationType type, String title, String message, String detailedMessage) {
        this(memberId, type, title, message);
        this.detailedMessage = detailedMessage;
    }

    private String generateNotificationId() {
        return "NOTIF-" + System.currentTimeMillis() + "-" + memberId;
    }

    // getter setter
    public String getNotificationId() { 
        return notificationId;
    }
    
    public String getMemberId() { 
        return memberId; 
    }
    public void setMemberId(String memberId) { 
        this.memberId = memberId; 
    }
    
    public NotificationType getType() { 
        return type; 
    }
    public void setType(NotificationType type) { 
        this.type = type; 
    }
    
    public Priority getPriority() { 
        return priority; 
    }
    public void setPriority(Priority priority) {
        this.priority = priority; 
    }
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) { 
        this.title = title; 
    }
    
    public String getMessage() { 
        return message;
    }
    public void setMessage(String message) { 
        this.message = message; 
    }
    
    public String getDetailedMessage() {
        return detailedMessage;
    }
    public void setDetailedMessage(String detailedMessage) { 
        this.detailedMessage = detailedMessage;
    }
    
    public boolean isRead() { 
        return isRead; 
    }
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public boolean isDismissed() { 
        return isDismissed; 
    }
    public void setDismissed(boolean dismissed) { 
        isDismissed = dismissed; 
    }
    
    public LocalDateTime getTimestamp() { 
        return timestamp; 
    }
    
    public LocalDateTime getExpiryDate() { 
        return expiryDate; 
    }
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getActionLink() { 
        return actionLink;
    }
    public void setActionLink(String actionLink) {
        this.actionLink = actionLink; 
    }
    public String getRelatedData() { 
        return relatedData; 
    }
    public void setRelatedData(String relatedData) {
        this.relatedData = relatedData; 
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Notification other = (Notification) obj;
        return Objects.equals(this.notificationId, other.notificationId);
    }

    @Override
    public String toString() {
        String status = isRead ? "[X]" : "[ ]";
        String priorityIcon = getPriorityIcon();
        return String.format("%s %-15s %-10s %-12s %-30s %-45s %-5s", 
                priorityIcon, notificationId, memberId, type.toString().replace("_", " "),
                title.length() > 30 ? title.substring(0, 27) + "..." : title,
                message.length() > 45 ? message.substring(0, 42) + "..." : message,
                status);
    }

    private String getPriorityIcon() {
        switch (priority) {
            case URGENT: return "[ URGENT ]";
            case HIGH: return "[ HIGH ]";
            case MEDIUM: return "[ MEDIUM ]";
            case LOW: return "[ LOW ]";
            default: return "[ ]";
        }
    }
}