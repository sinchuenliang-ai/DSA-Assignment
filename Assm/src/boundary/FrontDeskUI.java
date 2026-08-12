package boundary;

import entity.Reservation;
import java.util.Scanner;

/**
 *
 * @author Front Desk Module
 */
public class FrontDeskUI {

  private Scanner scanner = new Scanner(System.in);

  public int getMenuChoice() {
    System.out.println("\n=========================================");
    System.out.println("        FRONT DESK SERVICE MENU");
    System.out.println("=========================================");
    System.out.println("1. Instant Guest Search (Confirmation No.)");
    System.out.println("2. Register New Walk-In Guest");
    System.out.println("3. Update Room Assignment / Status");
    System.out.println("4. Generate Occupancy & Category Report");
    System.out.println("5. Generate High-Value Guest Billing Report");
    System.out.println("0. Quit");
    System.out.println("=========================================");
    System.out.print("Enter choice: ");
    int choice = scanner.nextInt();
    scanner.nextLine();
    System.out.println();
    return choice;
  }

  public void printReservationDetails(Reservation reservation) {
    System.out.println("\nReservation Details:");
    System.out.println("Confirmation #: " + reservation.getConfirmationNumber());
    System.out.println("Guest Name:     " + reservation.getGuestName());
    System.out.println("Room Category:  " + reservation.getRoomCategory());
    System.out.println("Room Number:    " + reservation.getRoomNumber());
    System.out.println("Stay Duration:  " + reservation.getStayDurationDays() + " days");
    System.out.println("Total Bill:     $" + String.format("%.2f", reservation.getTotalBillAmount()));
    System.out.println("Status:         " + reservation.getStatus());
  }

  public String inputConfirmationNumber() {
    System.out.print("Enter 8-digit confirmation number: ");
    return scanner.nextLine().trim();
  }

  public String inputGuestName() {
    System.out.print("Enter guest name: ");
    return scanner.nextLine().trim();
  }

  public String inputRoomCategory() {
    System.out.print("Enter room category (Standard/Deluxe/Suite/Presidential): ");
    return scanner.nextLine().trim();
  }

  public String inputRoomNumber() {
    System.out.print("Enter room number (leave blank to keep current): ");
    return scanner.nextLine().trim();
  }

  public int inputStayDuration() {
    System.out.print("Enter stay duration (days): ");
    int days = scanner.nextInt();
    scanner.nextLine();
    return days;
  }

  public double inputTotalBill() {
    System.out.print("Enter total bill amount: ");
    double bill = scanner.nextDouble();
    scanner.nextLine();
    return bill;
  }

  public String inputStatus() {
    System.out.print("Enter new status (Checked-In/Reserved/Checked-Out, leave blank to keep current): ");
    return scanner.nextLine().trim();
  }

  public String inputStatusFilter() {
    System.out.print("Filter Status (Checked-In/Reserved/Checked-Out/ALL): ");
    return scanner.nextLine().trim();
  }

  public String inputCategoryFilter() {
    System.out.print("Filter Room Category (Standard/Deluxe/Suite/Presidential/ALL): ");
    return scanner.nextLine().trim();
  }

  public double inputMinBill() {
    System.out.print("Enter minimum bill amount: ");
    double bill = scanner.nextDouble();
    scanner.nextLine();
    return bill;
  }

  public int inputMinDays() {
    System.out.print("Enter minimum stay duration (days): ");
    int days = scanner.nextInt();
    scanner.nextLine();
    return days;
  }

  public Reservation inputReservationDetails() {
    String confirmNo = inputConfirmationNumber();
    String name = inputGuestName();
    String category = inputRoomCategory();
    String room = inputRoomNumber();
    int days = inputStayDuration();
    double bill = inputTotalBill();
    return new Reservation(confirmNo, name, category, room, days, bill, "Checked-In");
  }
}