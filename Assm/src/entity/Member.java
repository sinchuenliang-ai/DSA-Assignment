package entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Member implements Serializable {

    private String memberId;
    private String name;
    private String email;
    private String phoneNumber;
    private LocalDate joinDate;
    private LocalDate dateOfBirth;
    
    private String tier;
    private int points;
    private int totalPointsEarned;
    private int lifetimePointsEarned;
    private int totalRedemptions;
    private int pointsRedeemed;
    private LocalDate lastActivityDate;
    
    private String preferredRoomType;
    private String specialRequests;
    private boolean emailNotifications;
    private boolean smsNotifications;
    private String preferredLanguage;

    public Member() {
        this.joinDate = LocalDate.now();
        this.tier = "Silver";
        this.points = 0;
        this.totalPointsEarned = 0;
        this.lifetimePointsEarned = 0;
        this.totalRedemptions = 0;
        this.pointsRedeemed = 0;
        this.lastActivityDate = LocalDate.now();
        this.emailNotifications = true;
        this.smsNotifications = true;
        this.preferredLanguage = "English";
    }

    public Member(String memberId, String name, String email, String phoneNumber) {
        this();
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getMemberId() { 
        return memberId; 
    }
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
    
    public String getName() {
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getEmail() {
        return email; 
    }
    public void setEmail(String email) {
        this.email = email; 
    }
    
    public String getPhoneNumber() {
        return phoneNumber; 
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber; 
    }
    
    public LocalDate getJoinDate() { 
        return joinDate; 
    }
    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate; 
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth; 
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth; 
    }
    
    public String getTier() { 
        return tier; 
    }
    public void setTier(String tier) {
        this.tier = tier; 
    }
    
    public int getPoints() {
        return points; 
    }
    public void setPoints(int points) { 
        this.points = points;
    }
    
    public int getTotalPointsEarned() {
        return totalPointsEarned;
    }
    public void setTotalPointsEarned(int totalPointsEarned) {
        this.totalPointsEarned = totalPointsEarned; 
    }
    
    public int getLifetimePointsEarned() { 
        return lifetimePointsEarned; 
    }
    public void setLifetimePointsEarned(int lifetimePointsEarned) {
        this.lifetimePointsEarned = lifetimePointsEarned; 
    }
    
    public int getTotalRedemptions() {
        return totalRedemptions; 
    }
    public void setTotalRedemptions(int totalRedemptions) { 
        this.totalRedemptions = totalRedemptions;
    }
    
    public int getPointsRedeemed() { 
        return pointsRedeemed;
    }
    public void setPointsRedeemed(int pointsRedeemed) { 
        this.pointsRedeemed = pointsRedeemed; 
    }
    
    public LocalDate getLastActivityDate() {
        return lastActivityDate; 
    }
    public void setLastActivityDate(LocalDate lastActivityDate) { 
        this.lastActivityDate = lastActivityDate; 
    }
    public String getPreferredRoomType() { 
        return preferredRoomType; 
    }
    public void setPreferredRoomType(String preferredRoomType) { 
        this.preferredRoomType = preferredRoomType; 
    }
    
    public String getSpecialRequests() {
        return specialRequests; 
    }
    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests; 
    }
    
    public boolean isEmailNotifications() { 
        return emailNotifications; 
    }
    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications; 
    }
    
    public boolean isSmsNotifications() {
        return smsNotifications;
    }
    
    public void setSmsNotifications(boolean smsNotifications) { 
        this.smsNotifications = smsNotifications;
    }
    
    public String getPreferredLanguage() { 
        return preferredLanguage; 
    }
    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage; 
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Member other = (Member) obj;
        return Objects.equals(this.memberId, other.memberId);
    }

    @Override
    public String toString() {
        return String.format("%-10s %-20s %-10s %8d %10d", 
                memberId, 
                name.length() > 20 ? name.substring(0, 17) + "..." : name, 
                tier, points, totalPointsEarned);
    }

    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("MEMBER DETAILS\n");
        sb.append("========================================\n");
        sb.append("Member ID      : ").append(memberId).append("\n");
        sb.append("Name           : ").append(name).append("\n");
        sb.append("Email          : ").append(email).append("\n");
        sb.append("Phone          : ").append(phoneNumber).append("\n");
        sb.append("Join Date      : ").append(joinDate).append("\n");
        sb.append("Date of Birth  : ").append(dateOfBirth != null ? dateOfBirth : "N/A").append("\n");
        sb.append("Tier           : ").append(tier).append("\n");
        sb.append("Points         : ").append(points).append("\n");
        sb.append("Total Earned   : ").append(totalPointsEarned).append("\n");
        sb.append("Lifetime       : ").append(lifetimePointsEarned).append("\n");
        sb.append("Redeemed       : ").append(pointsRedeemed).append("\n");
        sb.append("Redemptions    : ").append(totalRedemptions).append("\n");
        sb.append("Last Activity  : ").append(lastActivityDate).append("\n");
        sb.append("Preferred Room : ").append(preferredRoomType != null ? preferredRoomType : "Any").append("\n");
        sb.append("Language       : ").append(preferredLanguage).append("\n");
        sb.append("========================================\n");
        return sb.toString();
    }
}