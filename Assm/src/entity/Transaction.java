package entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class Transaction implements Serializable {

    public enum TransactionType {
        EARN_POINTS, REDEEM_POINTS, BONUS_POINTS, 
        TIER_UPGRADE_BONUS, ADJUSTMENT, EXPIRY_ADJUSTMENT, REFERRAL_BONUS
    }

    private String transactionId;
    private String memberId;
    private TransactionType type;
    private int points;
    private String description;
    private LocalDateTime timestamp;
    private String bookingId;
    private String referenceNumber;
    private String redemptionType;
    private double amountSpent;
    private String tierBefore;
    private String tierAfter;

    public Transaction() {
        this.timestamp = LocalDateTime.now();
        this.transactionId = generateTransactionId();
    }

    public Transaction(String memberId, TransactionType type, int points, String description) {
        this();
        this.memberId = memberId;
        this.type = type;
        this.points = points;
        this.description = description;
    }

    public Transaction(String memberId, TransactionType type, int points, String description, String bookingId) {
        this(memberId, type, points, description);
        this.bookingId = bookingId;
    }

    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + memberId;
    }

    //getter setter
    public String getTransactionId() { 
        return transactionId; 
    }
    
    public String getMemberId() {
        return memberId; 
    }
    
    public TransactionType getType() { 
        return type;
    }
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public int getPoints() { 
        return points; 
    }
    public void setPoints(int points) { 
        this.points = points; 
    }
    
    public String getDescription() { 
        return description;
    }
    public void setDescription(String description) {
        this.description = description; 
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp; 
    }
    
    public String getBookingId() {
        return bookingId; 
    }
    public void setBookingId(String bookingId) { 
        this.bookingId = bookingId;
    }
    
    public String getReferenceNumber() { 
        return referenceNumber;
    }
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber; 
    }
    
    public String getRedemptionType() { 
        return redemptionType; 
    }
    public void setRedemptionType(String redemptionType) {
        this.redemptionType = redemptionType;
    }
    
    public double getAmountSpent() { 
        return amountSpent; 
    }
    public void setAmountSpent(double amountSpent) {
        this.amountSpent = amountSpent; 
    }
    
    public String getTierBefore() { 
        return tierBefore; 
    }
    public void setTierBefore(String tierBefore) { 
        this.tierBefore = tierBefore;
    }
    
    public String getTierAfter() {
        return tierAfter;
    }
    public void setTierAfter(String tierAfter) {
        this.tierAfter = tierAfter; 
    }

    public String getTypeSymbol() {
        switch (type) {
            case EARN_POINTS:
            case BONUS_POINTS:
            case TIER_UPGRADE_BONUS:
            case REFERRAL_BONUS:
                return "+";
            case REDEEM_POINTS:
            case EXPIRY_ADJUSTMENT:
                return "-";
            default:
                return "+/-";
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction other = (Transaction) obj;
        return Objects.equals(this.transactionId, other.transactionId);
    }

    @Override
    public String toString() {
        return String.format("%-15s %-10s %s%7d %-35s", 
                transactionId, memberId, getTypeSymbol(), points, 
                description.length() > 35 ? description.substring(0, 32) + "..." : description);
    }

    public String toSummaryString() {
        return String.format("%-12s %-12s %s%,10d %-35s",
                timestamp.toLocalDate().toString(),
                type.toString().replace("_", " "),
                getTypeSymbol(),
                points,
                description.length() > 35 ? description.substring(0, 32) + "..." : description);
    }
}