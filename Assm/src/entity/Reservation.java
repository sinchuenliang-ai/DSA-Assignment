
package entity;


public class Reservation{
    private String confirmationNumber;  //8-digit unique ID
    private String guestName;
    private String roomCategory;
    private String roomNumber;
    private int stayDurationDays;
    private double totalBillAmount;
    private String status;  //"Check-in", "Reserved", "Checked-Out"

    public Reservation(String confirmationNumber, String guestName, String roomCategory, String roomNumber, int stayDurationDays, double totalBillAmount, String status) {
        this.confirmationNumber = confirmationNumber;
        this.guestName = guestName;
        this.roomCategory = roomCategory;
        this.roomNumber = roomNumber;
        this.stayDurationDays = stayDurationDays;
        this.totalBillAmount = totalBillAmount;
        this.status = status;
    }

    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    public void setConfirmationNumber(String confirmationNumber) {
        this.confirmationNumber = confirmationNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getRoomCategory() {
        return roomCategory;
    }

    public void setRoomCategory(String roomCategory) {
        this.roomCategory = roomCategory;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getStayDurationDays() {
        return stayDurationDays;
    }

    public void setStayDurationDays(int stayDurationDays) {
        this.stayDurationDays = stayDurationDays;
    }

    public double getTotalBillAmount() {
        return totalBillAmount;
    }

    public void setTotalBillAmount(double totalBillAmount) {
        this.totalBillAmount = totalBillAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
    
    
    
}
