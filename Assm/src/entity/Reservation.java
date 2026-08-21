package entity;

import java.io.Serializable;
import java.util.Objects;


public class Reservation implements Comparable<Reservation>, Serializable {

  private String confirmationNumber;
  private String guestName;
  private String roomCategory;
  private String roomNumber;
  private int stayDurationDays;
  private double totalBillAmount;
  private String status;
  private String checkInDate;
  private String checkOutDate;

  public Reservation() {
  }

  public Reservation(String confirmationNumber) {
    this.confirmationNumber = confirmationNumber;
  }

  public Reservation(String confirmationNumber, String guestName, String roomCategory,
          String roomNumber, int stayDurationDays, double totalBillAmount, String status) {
    this(confirmationNumber, guestName, roomCategory, roomNumber, stayDurationDays, totalBillAmount, status, null, null);
  }

  public Reservation(String confirmationNumber, String guestName, String roomCategory,
          String roomNumber, int stayDurationDays, double totalBillAmount, String status,
          String checkInDate, String checkOutDate) {
    this.confirmationNumber = confirmationNumber;
    this.guestName = guestName;
    this.roomCategory = roomCategory;
    this.roomNumber = roomNumber;
    this.stayDurationDays = stayDurationDays;
    this.totalBillAmount = totalBillAmount;
    this.status = status;
    this.checkInDate = checkInDate;
    this.checkOutDate = checkOutDate;
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

  public String getCheckInDate() {
    return checkInDate;
  }

  public void setCheckInDate(String checkInDate) {
    this.checkInDate = checkInDate;
  }

  public String getCheckOutDate() {
    return checkOutDate;
  }

  public void setCheckOutDate(String checkOutDate) {
    this.checkOutDate = checkOutDate;
  }

  @Override
  public int compareTo(Reservation other) {
    return this.confirmationNumber.compareTo(other.getConfirmationNumber());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.confirmationNumber);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Reservation other = (Reservation) obj;
    return Objects.equals(this.confirmationNumber, other.confirmationNumber);
  }

  @Override
  public String toString() {
    return String.format("%-12s %-18s %-16s %-8s %-11s %-11s %5d   $%10.2f %-12s",
            confirmationNumber, guestName, roomCategory, roomNumber,
            (checkInDate != null && !checkInDate.isEmpty() ? checkInDate : "-"),
            (checkOutDate != null && !checkOutDate.isEmpty() ? checkOutDate : "-"),
            stayDurationDays, totalBillAmount, status);
  }
}
