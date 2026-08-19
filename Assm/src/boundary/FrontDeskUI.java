package boundary;

import entity.Reservation;
import java.util.Scanner;

/**
 * Boundary class for Front Desk Service UI interactions.
 */
public class FrontDeskUI {

  private Scanner scanner = new Scanner(System.in);

  // =========================================================================
  // MAIN MENU
  // =========================================================================

    public int getMenuChoice() {
        System.out.println("\n=========================================");
        System.out.println("        FRONT DESK SERVICE MENU");
        System.out.println("=========================================");
        System.out.println("1. Search Specific Guest Identification");
        System.out.println("2. Register a New Guest");
        System.out.println("3. Update Guest Details");
        System.out.println("4. Delete Guest Reservation");
        System.out.println("5. Assign Walk-in Confirmation Number");
        System.out.println("6. Generate Report");
        System.out.println("7. View Room Availability");
        System.out.println("0. Quit");
        System.out.println("=========================================");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        return choice;
      }

  // =========================================================================
  // ROOM AVAILABILITY DISPLAY
  // =========================================================================

  public void displayRoomAvailabilityTable(String tableOutput) {
    System.out.println("\n===============================================================================");
    System.out.println("                           ROOM AVAILABILITY STATUS                            ");
    System.out.println("===============================================================================");
    System.out.print(tableOutput);
    System.out.println("===============================================================================");
    System.out.println("Press Enter to return to the Main Menu...");
    scanner.nextLine();
  }

  // =========================================================================
  // SEARCH SUB-MENU
  // =========================================================================

  public int getSearchMenuChoice() {
    System.out.println("\n-----------------------------------------");
    System.out.println("           SEARCH RESERVATIONS");
    System.out.println("-----------------------------------------");
    System.out.println("1. View All Reservations");
    System.out.println("2. Search by Confirmation Number");
    System.out.println("0. Back to Main Menu");
    System.out.println("-----------------------------------------");
    System.out.print("Enter choice: ");
    int choice = scanner.nextInt();
    scanner.nextLine();
    System.out.println();
    return choice;
  }
  
  // =========================================================================
  // UPDATE SUB-MENU
  // =========================================================================

  public int getUpdateMenuChoice() {
    System.out.println("\n-----------------------------------------");
    System.out.println("         UPDATE RESERVATION DETAIL");
    System.out.println("-----------------------------------------");
    System.out.println("1. Update Guest Name");
    System.out.println("2. Update Room Category");
    System.out.println("3. Update Room Number");
    System.out.println("4. Update Stay Duration");
    System.out.println("5. Update Total Bill");
    System.out.println("6. Update Status");
    System.out.println("0. Done Updating");
    System.out.println("-----------------------------------------");
    System.out.print("Enter choice: ");
    int choice = scanner.nextInt();
    scanner.nextLine(); // Consume newline
    System.out.println();
    return choice;
  }

  public void listAllReservations(String outputStr) {
    System.out.println("\nList of All Reservations:\n" + outputStr);
  }

  // =========================================================================
  // REPORT FILTER SUB-MENU
  // =========================================================================

  public int getReportFilterChoice(String categoryFilter, String statusFilter,
          boolean useMinBill, double minBill, boolean useMinDays, int minDays) {
    System.out.println("\n-----------------------------------------");
    System.out.println("        GENERATE REPORT - FILTERS");
    System.out.println("-----------------------------------------");
    System.out.println("Current Filters:");
    System.out.println("  Room Category   : " + (categoryFilter == null || categoryFilter.isEmpty() ? "ALL" : categoryFilter));
    System.out.println("  Status          : " + (statusFilter == null || statusFilter.isEmpty() ? "ALL" : statusFilter));
    System.out.println("  Minimum Bill    : " + (useMinBill ? "$" + String.format("%.2f", minBill) : "None"));
    System.out.println("  Minimum Stay    : " + (useMinDays ? minDays + " days" : "None"));
    System.out.println("-----------------------------------------");
    System.out.println("1. Set Room Category Filter");
    System.out.println("2. Set Status Filter");
    System.out.println("3. Set Minimum Bill Amount Filter");
    System.out.println("4. Set Minimum Stay Duration Filter");
    System.out.println("5. Generate Report Now");
    System.out.println("0. Cancel");
    System.out.println("-----------------------------------------");
    System.out.print("Enter choice: ");
    int choice = scanner.nextInt();
    scanner.nextLine();
    System.out.println();
    return choice;
  }

  // =========================================================================
  // RESERVATION DETAIL DISPLAY
  // =========================================================================

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

  // =========================================================================
  // INPUT METHODS
  // =========================================================================

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
    System.out.print("Enter room number (leave blank if not yet assigned): ");
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
    System.out.print("Enter new status (Checked-In/Reserved/Checked-Out/Cleaning/Maintenance, leave blank to keep current): ");
    return scanner.nextLine().trim();
  }

  public String inputStatusFilter() {
    System.out.print("Enter status to filter by (Checked-In/Reserved/Checked-Out): ");
    return scanner.nextLine().trim();
  }

  public String inputCategoryFilter() {
    System.out.print("Enter room category to filter by (Standard/Deluxe/Suite/Presidential): ");
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
