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
    System.out.println("1. View Room Availability & Schedule");
    System.out.println("2. Search Guest & Process Check-Out");
    System.out.println("3. Assign Walk-in Confirmation Number");
    System.out.println("4. Update Guest Details");
    System.out.println("5. Delete Guest Reservation");
    System.out.println("6. Generate Report");
    System.out.println("0. Quit");
    System.out.println("=========================================");
    return readIntInput("Enter choice: ", 0, 6);
  }

// =========================================================================
  // RECEIPT / STATEMENT DISPLAY
  // =========================================================================

  public void printGuestReceipt(Reservation reservation) {
    LocalDate today = LocalDate.now();
    LocalDate checkIn = today.minusDays(reservation.getStayDurationDays());
    double dailyRate = reservation.getStayDurationDays() > 0 
        ? reservation.getTotalBillAmount() / reservation.getStayDurationDays() 
        : reservation.getTotalBillAmount();

    String currentStatus = reservation.getStatus().toUpperCase();
    if ("CHECKED-OUT".equals(currentStatus)) {
      currentStatus = "PAID & CHECKED-OUT";
    }

    System.out.println("\n===================================================");
    System.out.println("                  TARUMT RESORT                     ");
    System.out.println("                RECEIPT / STATEMENT               ");
    System.out.println("===================================================");
    System.out.printf(" Confirmation #: %-33s \n", reservation.getConfirmationNumber());
    System.out.printf(" Guest Name    : %-33s \n", reservation.getGuestName());
    System.out.printf(" Room Number   : %-33s \n", reservation.getRoomNumber() + " (" + reservation.getRoomCategory() + ")");
    System.out.printf(" Check-In Date : %-33s \n", checkIn);
    System.out.printf(" Check-Out Date: %-33s \n", today);
    System.out.printf(" Duration      : %-33s \n", reservation.getStayDurationDays() + " Night(s)");
    System.out.println("---------------------------------------------------");
    System.out.printf(" Daily Rate    : $%-32.2f \n", dailyRate);
    System.out.printf(" TOTAL AMOUNT  : $%-32.2f \n", reservation.getTotalBillAmount());
    System.out.printf(" Status        : %-33s \n", currentStatus);
    System.out.println("===================================================");

  }
  // =========================================================================
  // ROOM AVAILABILITY DISPLAY
  // =========================================================================

  public void displayRoomAvailabilityTable(String tableOutput, LocalDate start, LocalDate end) {
    System.out.println("\n================================================================================================================================================");
    System.out.println("                                      REAL-TIME ROOM AVAILABILITY & SCHEDULE (" + start + " to " + end + ")                                     ");
    System.out.println("================================================================================================================================================");
    System.out.print(tableOutput);
    System.out.println("================================================================================================================================================");
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
    System.out.println("2. Update Room Category & Room Number");
    System.out.println("3. Update Stay Dates & Duration");
    System.out.println("0. Done Updating");
    System.out.println("-----------------------------------------");
    return readIntInput("Enter choice: ", 0, 3);
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
    String name;
    while (true) {
      System.out.print("Enter guest name: ");
      name = scanner.nextLine().trim();
      // Validation: Not empty and contains only letters and spaces
      if (!name.isEmpty() && name.matches("^[a-zA-Z\\s]+$")) {
        return name;
      }
      System.out.println("  [!] Invalid name. Please use only letters and spaces, and do not leave it blank.");
    }
  }

  // =========================================================================
  // ROOM CATEGORY & NUMBER SELECTION (Smart, linked)
  // =========================================================================

  /** Room category menu with full type names. Returns the selected category string. */
  public String selectRoomCategory() {
    System.out.println("\n  Available Room Categories:");
    System.out.println("  +----+----------------------------+----------------+");
    System.out.println("  | No | Category                   | Rate / Night   |");
    System.out.println("  +----+----------------------------+----------------+");
    System.out.println("  |  1 | Standard Single            | RM   120.00    |");
    System.out.println("  |  2 | Standard Double            | RM   150.00    |");
    System.out.println("  |  3 | Deluxe Suite               | RM   250.00    |");
    System.out.println("  |  4 | Executive Suite            | RM   500.00    |");
    System.out.println("  |  5 | Presidential Suite         | RM 1,200.00    |");
    System.out.println("  +----+----------------------------+----------------+");
    int choice = readIntInput("  Select category (1-5): ", 1, 5);
    return switch (choice) {
      case 1 -> "Standard Single";
      case 2 -> "Standard Double";
      case 3 -> "Deluxe Suite";
      case 4 -> "Executive Suite";
      case 5 -> "Presidential Suite";
      default -> "Standard Single";
    };
  }

  /**
   * Legacy single-step room category for places that only need the category string.
   * Maps old-style (Standard/Deluxe/Suite/Presidential) for backward compatibility.
   */
  public String inputRoomCategory() {
    return selectRoomCategory();
  }

  /**
   * Shows available room numbers for the given category in a formatted column,
   * excluding rooms whose numbers appear in the occupied set.
   * Returns the selected room number, or null if no rooms are available.
   */
  public String selectRoomNumber(String category, java.util.Set<String> occupiedRoomNumbers) {
    // Define rooms per category (matches rooms.txt)
    String[][] categoryRooms = getCategoryRooms(category);
    if (categoryRooms == null || categoryRooms.length == 0) {
      System.out.println("  [!] No rooms defined for category: " + category);
      return null;
    }

    // Filter out occupied rooms
    java.util.List<String[]> available = new java.util.ArrayList<>();
    for (String[] row : categoryRooms) {
      String roomNum = row[0];
      if (!occupiedRoomNumbers.contains(roomNum.toUpperCase())) {
        available.add(row);
      }
    }

    if (available.isEmpty()) {
      System.out.println("  [!] No available rooms for " + category + " on the selected dates.");
      return null;
    }

    System.out.println("\n  Available " + category + " Rooms:");
    System.out.println("  +-----+-------------+-----------+");
    System.out.printf( "  | %-3s | %-11s | %-9s |%n", "No", "Room Number", "Status");
    System.out.println("  +-----+-------------+-----------+");
    for (int i = 0; i < available.size(); i++) {
      System.out.printf("  | %-3d | %-11s | %-9s |%n",
          i + 1, available.get(i)[0], "Available");
    }
    System.out.println("  +-----+-------------+-----------+");

    int choice = readIntInput("  Select room (1-" + available.size() + "): ", 1, available.size());
    return available.get(choice - 1)[0];
  }

  /**
   * Returns the room number list for each category (room number, floor label).
   * Matches the actual rooms.txt layout.
   */
  private String[][] getCategoryRooms(String category) {
    if (category == null) return new String[0][];
    return switch (category.toLowerCase()) {
      case "standard single" -> new String[][]{
          {"A-001"}, {"A-002"}, {"A-003"}, {"A-004"}
      };
      case "standard double" -> new String[][]{
          {"A-005"}, {"A-006"}, {"A-007"}, {"A-008"}
      };
      case "deluxe suite" -> new String[][]{
          {"D-101"}, {"D-102"}, {"D-103"}, {"D-104"}, {"D-105"}, {"D-106"}
      };
      case "executive suite" -> new String[][]{
          {"S-201"}, {"S-202"}, {"S-203"}, {"S-204"}, {"S-205"}
      };
      case "presidential suite" -> new String[][]{
          {"P-501"}, {"P-502"}
      };
      // Legacy mappings
      case "standard" -> new String[][]{{"A-001"}, {"A-002"}, {"A-003"}, {"A-004"}};
      case "deluxe"   -> new String[][]{{"D-101"}, {"D-102"}, {"D-103"}, {"D-104"}, {"D-105"}, {"D-106"}};
      case "suite"    -> new String[][]{{"S-201"}, {"S-202"}, {"S-203"}, {"S-204"}, {"S-205"}};
      case "presidential" -> new String[][]{{"P-501"}, {"P-502"}};
      default -> new String[0][];
    };
  }

  /** Legacy stand-alone room number input — kept for backward compatibility. */
  public String inputRoomNumber() {
    while (true) {
      System.out.print("Enter room number (e.g., A-001, D-101) or type 'Pending': ");
      String input = scanner.nextLine().trim();
      if (!input.isEmpty() && input.matches("^[a-zA-Z0-9-]+$")) {
        return input.toUpperCase();
      }
      System.out.println("  [!] Invalid room number. Please use only letters, numbers, and hyphens.");
    }
  }


  public LocalDate inputCheckInDate() {
    LocalDate today = LocalDate.now();
    while (true) {
      System.out.print("Enter Start/Check-In Date (YYYY-MM-DD) [Press ENTER for Today '" + today + "']: ");
      String input = scanner.nextLine().trim();
      if (input.isEmpty()) {
        return today;
      }
      try {
        LocalDate parsed = LocalDate.parse(input);
        if (parsed.isBefore(today)) {
          System.out.println("  [!] Check-in date cannot be before today (" + today + "). Please enter today or a future date.");
        } else {
          return parsed;
        }
      } catch (DateTimeParseException e) {
        System.out.println("  [!] Invalid date format. Use YYYY-MM-DD.");
      }
    }
  }


  public LocalDate inputCheckOutDate(LocalDate checkInDate) {
    LocalDate defaultOut = checkInDate.plusDays(1);
    while (true) {
      System.out.print("Enter End/Check-Out Date (YYYY-MM-DD) [Press ENTER for '" + defaultOut + "']: ");
      String input = scanner.nextLine().trim();
      if (input.isEmpty()) {
        return defaultOut;
      }
      try {
        LocalDate parsed = LocalDate.parse(input);
        if (parsed.isAfter(checkInDate) || parsed.isEqual(checkInDate)) {
          return parsed;
        }
        System.out.println("  [!] End date must be on or AFTER Start date.");
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

  // =========================================================================
  // PAYMENT PROCESSING
  // =========================================================================

  /**
   * Prompts for payment method selection and processes payment.
   * Returns the selected payment method string, or null if cancelled.
   */
  public String processPayment(double totalAmount) {
    System.out.println("\n==========================================================");
    System.out.println("                    PAYMENT PROCESSING                   ");
    System.out.println("==========================================================");
    System.out.printf( "  Total Amount Due  :  RM %-30.2f%n", totalAmount);
    System.out.println("----------------------------------------------------------");
    System.out.println("  [1] Cash");
    System.out.println("  [2] Credit / Debit Card");
    System.out.println("  [3] QR Payment (Touch 'n Go / DuitNow)");
    System.out.println("  [0] Exit / Cancel Payment");
    System.out.println("==========================================================");

    int choice = readIntInput("  Select Payment Method (0-3): ", 0, 3);

    switch (choice) {
      case 0 -> {
        System.out.println("  [ System ] Payment cancelled. Returning to menu.");
        return null;
      }

      case 1 -> {
        System.out.println("\n--- CASH PAYMENT ---");
        System.out.printf("  Amount Due: RM %.2f%n", totalAmount);
        double tendered = 0;
        while (tendered < totalAmount) {
          System.out.print("  Enter cash tendered (RM): ");
          try {
            tendered = Double.parseDouble(scanner.nextLine().trim());
            if (tendered < totalAmount) {
              System.out.printf("  [!] Insufficient. Minimum required: RM %.2f%n", totalAmount);
            }
          } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid amount. Please enter a number.");
          }
        }
        double change = tendered - totalAmount;
        System.out.println("\n  [✓] Cash received: RM " + String.format("%.2f", tendered));
        System.out.println("  [✓] Change:        RM " + String.format("%.2f", change));
        System.out.println("  [✓] Payment SUCCESSFUL!\n");
        return "Cash";
      }

      case 2 -> {
        System.out.println("\n--- CARD PAYMENT ---");
        System.out.printf("  Amount to Charge: RM %.2f%n", totalAmount);
        System.out.println("  [1] Credit Card");
        System.out.println("  [2] Debit Card");
        int cardType = readIntInput("  Select Card Type (1-2): ", 1, 2);
        String cardLabel = cardType == 1 ? "Credit Card" : "Debit Card";

        // Validate 16-Digit Card Number (accepts spaces/hyphens)
        String cleanCardNum = "";
        while (true) {
          System.out.print("  Enter Card Number (e.g. 1111 2222 3333 4444): ");
          String rawCard = scanner.nextLine().trim();
          cleanCardNum = rawCard.replaceAll("[\\s-]", ""); // Strip spaces and dashes
          if (cleanCardNum.matches("^\\d{16}$")) {
            break;
          }
          System.out.println("  [!] Invalid card number. Please enter a valid 16-digit card number.");
        }

        // Validate 3-digit CVV
        String cvv = "";
        while (true) {
          System.out.print("  Enter 3-digit CVV/CCV: ");
          cvv = scanner.nextLine().trim();
          if (cvv.matches("^\\d{3}$")) {
            break;
          }
          System.out.println("  [!] Invalid CVV. Please enter exactly 3 digits.");
        }

        String last4 = cleanCardNum.substring(12);
        
        System.out.println("\n  Authorizing transaction...");
        System.out.printf("  Charging RM %.2f to %s (**** **** **** %s)...%n", totalAmount, cardLabel, last4);
        System.out.println("  [✓] Card authorized successfully.");
        System.out.println("  [✓] Payment SUCCESSFUL!\n");

        return "Card (" + cardLabel + " **" + last4 + ")";
      }

      case 3 -> {
        displayQRCode(totalAmount);
        System.out.print("\n  Press ENTER after completing QR payment to confirm...");
        scanner.nextLine();
        System.out.println("  [✓] QR Payment recorded.");
        System.out.println("  [✓] Payment SUCCESSFUL!\n");
        return "QR Payment (Touch 'n Go / DuitNow)";
      }

      default -> { return null; }
    }
  }

private void displayQRCode(double totalAmount) {
    System.out.println("\n+-------------------------------------------------------------+");
    System.out.println("|                      TOUCH 'N GO EWALLET                    |");
    System.out.println("|                     MALAYSIA NATIONAL QR                    |");
    System.out.println("+-------------------------------------------------------------+");
    System.out.printf( "|  Payee  : %-48s |\n", "TARUMT RESORT");
    System.out.printf( "|  Amount : RM %-46.2f |\n", totalAmount);
    System.out.println("+-------------------------------------------------------------+");
    System.out.println("|                                                             |");
    System.out.println("|        +-------------------------------------------+        |");
    System.out.println("|        |                                           |        |");
    System.out.println("|        | ##############  ######  ##  ############## |        |");
    System.out.println("|        | ##          ##  ######  ##  ##          ## |        |");
    System.out.println("|        | ##  ######  ##  ######      ##  ######  ## |        |");
    System.out.println("|        | ##  ######  ##    ####  ##  ##  ######  ## |        |");
    System.out.println("|        | ##  ######  ##  ######      ##  ######  ## |        |");
    System.out.println("|        | ##          ##  ##  ######  ##          ## |        |");
    System.out.println("|        | ##############  ##  ##  ##  ############## |        |");
    System.out.println("|        |                 ######  ##                 |        |");
    System.out.println("|        | ######  ######  ##    ######  ####    #### |        |");
    System.out.println("|        | ##    ####  ######  ######  ####  ######   |        |");
    System.out.println("|        | ######  ##  ####    ##    ######  ##  #### |        |");
    System.out.println("|        | ####  ######  ######  ####  ##    ######   |        |");
    System.out.println("|        | ##  ######    ####  ######  ##  ######     |        |");
    System.out.println("|        |                 ####  ##  ####      ###### |        |");
    System.out.println("|        | ##############  ##  ######  ##  ######  ## |        |");
    System.out.println("|        | ##          ##  ######  ####    ##    #### |        |");
    System.out.println("|        | ##  ######  ##    ####  ##  ######  ###### |        |");
    System.out.println("|        | ##  ######  ##  ##  ######  ####  ##       |        |");
    System.out.println("|        | ##  ######  ##  ######  ######    ######   |        |");
    System.out.println("|        | ##          ##    ####  ##  ####  ##  #### |        |");
    System.out.println("|        | ##############  ####  ######  ######    ## |        |");
    System.out.println("|        |                                           |        |");
    System.out.println("|        +-------------------------------------------+        |");
    System.out.println("|                                                             |");
    System.out.println("|         Scan with Touch 'n Go eWallet or DuitNow QR         |");
    System.out.println("+-------------------------------------------------------------+");
  }
}