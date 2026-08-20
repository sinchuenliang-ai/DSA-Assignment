package boundary;

import entity.Reservation;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
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
    System.out.println("2. Process Guest Check-Out");
    System.out.println("3. Update Guest Details");
    System.out.println("4. Delete Guest Reservation");
    System.out.println("5. Assign Walk-in Confirmation Number");
    System.out.println("6. Generate Report");
    System.out.println("7. View Room Availability");
    System.out.println("0. Quit");
    System.out.println("=========================================");
    return readIntInput("Enter choice: ", 0, 7);
  }

  // =========================================================================
  // ROOM AVAILABILITY DISPLAY
  // =========================================================================

  public void displayRoomAvailabilityTable(String tableOutput) {
    System.out.println("\n=============================================================================================================================");
    System.out.println("                                               ROOM AVAILABILITY STATUS                                                      ");
    System.out.println("=============================================================================================================================");
    System.out.print(tableOutput);
    System.out.println("=============================================================================================================================");
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
    return readIntInput("Enter choice: ", 0, 2);
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
    System.out.println("4. Update Stay Dates & Duration");
    System.out.println("5. Update Status");
    System.out.println("0. Done Updating");
    System.out.println("-----------------------------------------");
    return readIntInput("Enter choice: ", 0, 5);
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
    return readIntInput("Enter choice: ", 0, 5);
  }

  // =========================================================================
  // RESERVATION DETAIL DISPLAY
  // =========================================================================

  public void printReservationDetails(Reservation reservation) {
    LocalDate today = LocalDate.now();
    LocalDate checkOut = today.plusDays(reservation.getStayDurationDays());

    System.out.println("\nReservation Details:");
    System.out.println("Confirmation #: " + reservation.getConfirmationNumber());
    System.out.println("Guest Name:     " + reservation.getGuestName());
    System.out.println("Room Category:  " + reservation.getRoomCategory());
    System.out.println("Room Number:    " + reservation.getRoomNumber());
    System.out.println("Check-In Date:  " + today + " (Estimated)");
    System.out.println("Check-Out Date: " + checkOut + " (Estimated)");
    System.out.println("Stay Duration:  " + reservation.getStayDurationDays() + " day(s)");
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

  public String inputBookingID() {
    System.out.print("Enter Booking ID (e.g. B1001): ");
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

  public LocalDate inputCheckInDate() {
    LocalDate today = LocalDate.now();
    while (true) {
      System.out.print("Enter Check-In Date (YYYY-MM-DD) [Press ENTER for Today '" + today + "']: ");
      String input = scanner.nextLine().trim();
      if (input.isEmpty()) {
        return today;
      }
      try {
        return LocalDate.parse(input);
      } catch (DateTimeParseException e) {
        System.out.println("  [!] Invalid date format. Use YYYY-MM-DD.");
      }
    }
  }

  public LocalDate inputCheckOutDate(LocalDate checkInDate) {
    LocalDate defaultOut = checkInDate.plusDays(1);
    while (true) {
      System.out.print("Enter Check-Out Date (YYYY-MM-DD) [Press ENTER for '" + defaultOut + "']: ");
      String input = scanner.nextLine().trim();
      if (input.isEmpty()) {
        return defaultOut;
      }
      try {
        LocalDate parsed = LocalDate.parse(input);
        if (parsed.isAfter(checkInDate)) {
          return parsed;
        }
        System.out.println("  [!] Check-Out date must be AFTER Check-In date.");
      } catch (DateTimeParseException e) {
        System.out.println("  [!] Invalid date format. Use YYYY-MM-DD.");
      }
    }
  }

  public int calculateStayDuration(LocalDate checkIn, LocalDate checkOut) {
    return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
  }

  public boolean readConfirmationInput(String prompt) {
    while (true) {
      System.out.print(prompt);
      String input = scanner.nextLine().trim().toUpperCase();
      if (input.equals("Y") || input.equals("YES")) {
        return true;
      }
      if (input.equals("N") || input.equals("NO")) {
        return false;
      }
      System.out.println("  [!] Invalid choice. Please enter 'Y' or 'N'.");
    }
  }

  public double inputTotalBill() {
    System.out.print("Enter total bill amount: ");
    double bill = 0.0;
    try {
      bill = Double.parseDouble(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      System.out.println("  [!] Invalid amount. Defaulting to 0.00.");
      bill = 0.0;
    }
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
    double bill = 0.0;
    try {
      bill = Double.parseDouble(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      bill = 0.0;
    }
    return bill;
  }

  public int inputMinDays() {
    System.out.print("Enter minimum stay duration (days): ");
    int days = 0;
    try {
      days = Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      days = 0;
    }
    return days;
  }

  public int readIntInput(String prompt, int min, int max) {
    while (true) {
      System.out.print(prompt);
      try {
        int val = Integer.parseInt(scanner.nextLine().trim());
        if (val >= min && val <= max) {
          return val;
        }
        System.out.println("  [!] Please enter a number between " + min + " and " + max + ".");
      } catch (NumberFormatException e) {
        System.out.println("  [!] Invalid number. Please try again.");
      }
    }
  }
}