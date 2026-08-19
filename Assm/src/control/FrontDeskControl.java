package control;

import adt.*;
import boundary.FrontDeskUI;
import entity.Reservation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import utility.MessageUI;

/**
 * Controller class managing Front Desk operations and BST ADT operations.
 */
public class FrontDeskControl {

  private TreeInterface<Reservation> reservationTree = new BinarySearchTreeADT<>();
  private FrontDeskUI frontDeskUI = new FrontDeskUI();

  // Expanded master list of all physical rooms in the hotel
  private final String[] HOTEL_ROOMS = {
      "A-001", "A-002", "A-003", "A-004", "A-005", "A-006", "A-007", "A-008", // Standard
      "D-101", "D-102", "D-103", "D-104", "D-105", "D-106",                   // Deluxe
      "S-201", "S-202", "S-203", "S-204", "S-205",                            // Suite
      "P-501", "P-502"                                                        // Presidential
  };
  
  private double getRoomRate(String category) {
    return switch (category.toLowerCase()) {
      case "standard" -> 120.00;
      case "deluxe" -> 150.00;
      case "suite" -> 500.00;
      case "presidential" -> 1200.00;
      default -> 100.00;
    };
  }

  public FrontDeskControl() {
    seedInitialData();
  }

  private void seedInitialData() {
    reservationTree.add(new Reservation("10008801", "Tan Ah Kow", "Deluxe", "D-101", 3, 450.00, "Checked-In"));
    reservationTree.add(new Reservation("10008805", "Siti Nurhaliza", "Suite", "S-202", 5, 2500.00, "Reserved"));
    reservationTree.add(new Reservation("10008802", "Alex Muthu", "Standard", "A-005", 1, 120.00, "Checked-Out"));
    reservationTree.add(new Reservation("10008809", "John Doe", "Presidential", "P-501", 7, 8400.00, "Checked-In"));
    reservationTree.add(new Reservation("10008804", "Alice Smith", "Suite", "S-201", 2, 1100.00, "Checked-In"));
    reservationTree.add(new Reservation("10008810", "Michael Chen", "Standard", "A-002", 2, 240.00, "Checked-In"));
    reservationTree.add(new Reservation("10008811", "Sarah Lim", "Standard", "A-003", 4, 480.00, "Reserved"));
    reservationTree.add(new Reservation("10008812", "David Wong", "Deluxe", "D-105", 0, 0.00, "Maintenance")); 
    reservationTree.add(new Reservation("10008813", "Emma Brown", "Suite", "S-205", 0, 0.00, "Cleaning"));     
    reservationTree.add(new Reservation("10008814", "Ahmad Ali", "Deluxe", "D-103", 2, 300.00, "Checked-In"));
  }

  public String generate8DigitConfirmationNumber() {
    long maxId = 10008800; // Base starting number
    Iterator<Reservation> it = reservationTree.getInorderIterator();

    // Find the highest existing confirmation number
    while (it.hasNext()) {
      try {
        long currentId = Long.parseLong(it.next().getConfirmationNumber());
        if (currentId > maxId) {
          maxId = currentId;
        }
      } catch (NumberFormatException e) {
        // Ignore non-numeric confirmation numbers
      }
    }
    return String.valueOf(maxId + 1);
  }

  public Reservation registerGuestAndAssignConfirmation(String guestName, String roomCategory, String roomNumber, int stayDays, double totalBill, String status) {
    String confirmationNumber = generate8DigitConfirmationNumber();
    Reservation newReservation = new Reservation(
        confirmationNumber,
        guestName,
        roomCategory,
        roomNumber,
        stayDays,
        totalBill,
        status
    );
    reservationTree.add(newReservation);
    return newReservation;
  }

  public void runFrontDeskService() {
    int choice;
    do {
      choice = frontDeskUI.getMenuChoice();
      switch (choice) {
        case 0 -> MessageUI.displayExitMessage();
        case 1 -> searchReservationByConfirmationNumber();
        case 2 -> addNewReservation();
        case 3 -> updateReservationStatus();
        case 4 -> deleteReservation();
        case 5 -> assignWalkInConfirmation();
        case 6 -> generateCustomReport();
        case 7 -> viewRoomAvailability();
        default -> MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);
  }

  public void deleteReservation() {
    String confirmNo = frontDeskUI.inputConfirmationNumber();
    Reservation target = new Reservation(confirmNo);
    
    if (reservationTree.search(target) != null) {
      reservationTree.remove(target);
      System.out.println("Reservation " + confirmNo + " has been successfully deleted.");
    } else {
      MessageUI.displayNotFoundMessage();
    }
  }

  public void assignWalkInConfirmation() {
    String newConfirmNo = generate8DigitConfirmationNumber(); 
    System.out.println("\n-----------------------------------------");
    System.out.println("Assigned Next Walk-In Confirmation #: " + newConfirmNo);
    System.out.println("-----------------------------------------");
    System.out.println("Please select Option 2 from the menu to register details for this number.");
  }

  // =========================================================================
  // ROOM AVAILABILITY
  // =========================================================================

public void viewRoomAvailability() {
    Map<String, String> roomStatusMap = new HashMap<>();
    Map<String, String> roomGuestMap = new HashMap<>();

    Iterator<Reservation> it = reservationTree.getInorderIterator();
    while (it.hasNext()) {
      Reservation r = it.next();
      String roomNo = r.getRoomNumber();
      String status = r.getStatus();
      
      if (roomNo != null && !roomNo.trim().isEmpty() && !roomNo.equalsIgnoreCase("Pending") && !status.equalsIgnoreCase("Checked-Out")) {
        roomStatusMap.put(roomNo.toUpperCase(), status);
        roomGuestMap.put(roomNo.toUpperCase(), r.getGuestName());
      }
    }

    StringBuilder table = new StringBuilder();

    // Table Header Border
    table.append("+------------+-----------------+------------------+---------------------------+----------------------+\n");
    table.append(String.format("| %-10s | %-15s | %-16s | %-25s | %-20s |\n", 
        "Room No.", "Category", "Rate / Night ($)", "Guest Name", "Current Status"));
    table.append("+------------+-----------------+------------------+---------------------------+----------------------+\n");

    for (String room : HOTEL_ROOMS) {
      String category = getRoomCategoryPrefix(room);
      double rate = getRoomRate(category);
      String status = roomStatusMap.getOrDefault(room.toUpperCase(), "Ready / Available");
      String guest = roomGuestMap.getOrDefault(room.toUpperCase(), "-");

      table.append(String.format("| %-10s | %-15s | $%-15.2f | %-25s | %-20s |\n", 
          room, category, rate, guest, status));
    }
    
    // Table Footer Border
    table.append("+------------+-----------------+------------------+---------------------------+----------------------+\n");

    frontDeskUI.displayRoomAvailabilityTable(table.toString());
  }

  private String getRoomCategoryPrefix(String roomNo) {
    if (roomNo.startsWith("A")) return "Standard";
    if (roomNo.startsWith("D")) return "Deluxe";
    if (roomNo.startsWith("S")) return "Suite";
    if (roomNo.startsWith("P")) return "Presidential";
    return "Unknown";
  }

  // =========================================================================
  // SEARCH
  // =========================================================================

  public void searchReservationsMenu() {
    int choice;
    do {
      choice = frontDeskUI.getSearchMenuChoice();
      switch (choice) {
        case 0:
          break;
        case 1:
          viewAllReservations();
          break;
        case 2:
          searchReservationByConfirmationNumber();
          break;
        default:
          MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);
  }

  public void viewAllReservations() {
    StringBuilder sb = new StringBuilder();
    Iterator<Reservation> it = reservationTree.getInorderIterator();
    int count = 0;

    while (it.hasNext()) {
      sb.append(it.next().toString()).append("\n");
      count++;
    }
    sb.append(String.format("\nTotal Reservations: %d", count));
    frontDeskUI.listAllReservations(sb.toString());
  }

  public void searchReservationByConfirmationNumber() {
    String confirmNo = frontDeskUI.inputConfirmationNumber();
    Reservation found = reservationTree.search(new Reservation(confirmNo));
    if (found != null) {
      frontDeskUI.printReservationDetails(found);
    } else {
      MessageUI.displayNotFoundMessage();
    }
  }

  // =========================================================================
  // REGISTER / UPDATE
  // =========================================================================

  private double calculateTotalBill(String roomCategory, int days) {
    double dailyRate = 0.0;
    
    switch (roomCategory.toLowerCase()) {
      case "standard" -> dailyRate = 120.00;
      case "deluxe" -> dailyRate = 150.00;
      case "suite" -> dailyRate = 500.00;
      case "presidential" -> dailyRate = 1200.00;
      default -> System.out.println("  [!] Unknown room category. Using base rate of $100.00");
    }
    
    if (dailyRate == 0.0 && !roomCategory.isEmpty()) {
       dailyRate = 100.00;
    }
    
    return dailyRate * days;
  }

  public void addNewReservation() {
    String confirmNo = generate8DigitConfirmationNumber();
    System.out.println("-----------------------------------------");
    System.out.println("Assigned 8-Digit Confirmation #: " + confirmNo);
    System.out.println("-----------------------------------------");

    String name = frontDeskUI.inputGuestName();
    String category = frontDeskUI.inputRoomCategory();
    String room = frontDeskUI.inputRoomNumber();
    int days = frontDeskUI.inputStayDuration();
    
    double calculatedBill = calculateTotalBill(category, days);
    System.out.printf("Calculated Total Bill: $%.2f\n", calculatedBill);

    Reservation newReservation = new Reservation(confirmNo, name, category, room, days, calculatedBill, "Checked-In");
    reservationTree.add(newReservation);
    MessageUI.displayAddedMessage();
  }

  public void updateReservationStatus() {
    String confirmNo = frontDeskUI.inputConfirmationNumber();
    Reservation res = reservationTree.search(new Reservation(confirmNo));
    
    if (res == null) {
      MessageUI.displayNotFoundMessage();
      return;
    }

    int choice;
    do {
      frontDeskUI.printReservationDetails(res);
      choice = frontDeskUI.getUpdateMenuChoice();
      
      switch (choice) {
        case 1:
          res.setGuestName(frontDeskUI.inputGuestName());
          break;
        case 2:
          String newCategory = frontDeskUI.inputRoomCategory();
          res.setRoomCategory(newCategory);
          res.setTotalBillAmount(calculateTotalBill(newCategory, res.getStayDurationDays()));
          System.out.println("Room category updated. New bill auto-calculated.");
          break;
        case 3:
          res.setRoomNumber(frontDeskUI.inputRoomNumber());
          break;
        case 4:
          int newDays = frontDeskUI.inputStayDuration();
          res.setStayDurationDays(newDays);
          res.setTotalBillAmount(calculateTotalBill(res.getRoomCategory(), newDays));
          System.out.println("Stay duration updated. New bill auto-calculated.");
          break;
        case 5:
          res.setStatus(frontDeskUI.inputStatus());
          break;
        case 0:
          System.out.println("Finished updating reservation.");
          break;
        default:
          MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);

    MessageUI.displayUpdatedMessage();
  }

  // =========================================================================
  // CUSTOM REPORT
  // =========================================================================

  public void generateCustomReport() {
    String categoryFilter = "";
    String statusFilter = "";
    boolean useMinBill = false;
    double minBill = 0.0;
    boolean useMinDays = false;
    int minDays = 0;

    int choice;
    do {
      choice = frontDeskUI.getReportFilterChoice(categoryFilter, statusFilter, useMinBill, minBill, useMinDays, minDays);
      switch (choice) {
        case 0:
          return;
        case 1:
          categoryFilter = frontDeskUI.inputCategoryFilter();
          break;
        case 2:
          statusFilter = frontDeskUI.inputStatusFilter();
          break;
        case 3:
          minBill = frontDeskUI.inputMinBill();
          useMinBill = true;
          break;
        case 4:
          minDays = frontDeskUI.inputMinDays();
          useMinDays = true;
          break;
        case 5:
          printReport(categoryFilter, statusFilter, useMinBill, minBill, useMinDays, minDays);
          return;
        default:
          MessageUI.displayInvalidChoiceMessage();
      }
    } while (true);
  }

  private void printReport(String categoryFilter, String statusFilter,
          boolean useMinBill, double minBill, boolean useMinDays, int minDays) {

    StringBuilder header = new StringBuilder("Category: ").append(categoryFilter.isEmpty() ? "ALL" : categoryFilter)
            .append(" | Status: ").append(statusFilter.isEmpty() ? "ALL" : statusFilter)
            .append(" | Min Bill: ").append(useMinBill ? "$" + String.format("%.2f", minBill) : "None")
            .append(" | Min Stay: ").append(useMinDays ? minDays + " days" : "None");

    StringBuilder sb = new StringBuilder();
    sb.append("\n=========================================================================================\n");
    sb.append("          FRONT DESK REPORT\n");
    sb.append("          ").append(header).append("\n");
    sb.append("=========================================================================================\n");

    Iterator<Reservation> it = reservationTree.getInorderIterator();
    int count = 0;
    double totalRevenue = 0;

    while (it.hasNext()) {
      Reservation r = it.next();
      boolean matchCategory = categoryFilter.isEmpty() || r.getRoomCategory().equalsIgnoreCase(categoryFilter);
      boolean matchStatus = statusFilter.isEmpty() || r.getStatus().equalsIgnoreCase(statusFilter);
      boolean matchMinBill = !useMinBill || r.getTotalBillAmount() >= minBill;
      boolean matchMinDays = !useMinDays || r.getStayDurationDays() >= minDays;

      if (matchCategory && matchStatus && matchMinBill && matchMinDays) {
        sb.append(r.toString()).append("\n");
        count++;
        totalRevenue += r.getTotalBillAmount();
      }
    }
    sb.append("-----------------------------------------------------------------------------------------\n");
    sb.append(String.format(" Total Records Displayed: %d | Total Revenue: $%.2f\n", count, totalRevenue));
    sb.append("=========================================================================================\n");
    System.out.println(sb.toString());
  }
}
