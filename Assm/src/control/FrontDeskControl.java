package control;

import adt.*;
import boundary.FrontDeskUI;
import entity.Booking;
import entity.Reservation;
import entity.Room;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import utility.FileHandler;
import utility.MessageUI;

/**
 * Controller class managing Front Desk operations, BST ADT operations,
 * and integration with Walk-In Bookings.
 */
public class FrontDeskControl {

  private TreeInterface<Reservation> reservationTree = new BinarySearchTreeADT<>();
  private QueueInterface<Booking> pendingWalkInQueue = new LinkedQueue<>();
  private FrontDeskUI frontDeskUI = new FrontDeskUI();
  private BookingControl bookingControl;
  private HouseKeepingControl houseKeepingControl;

  public FrontDeskControl() {
    this(null, null);
  }

  public FrontDeskControl(HouseKeepingControl houseKeepingControl) {
    this(null, houseKeepingControl);
  }

  public FrontDeskControl(BookingControl bookingControl, HouseKeepingControl houseKeepingControl) {
    FileHandler.loadReservations(reservationTree);
    loadPendingWalkInsFromFile();

    if (bookingControl != null) {
      setBookingControl(bookingControl);
    }
    if (houseKeepingControl != null) {
      setHouseKeepingControl(houseKeepingControl);
    }
  }

  public void setHouseKeepingControl(HouseKeepingControl houseKeepingControl) {
    this.houseKeepingControl = houseKeepingControl;
    if (houseKeepingControl != null && houseKeepingControl.getFrontDeskControl() != this) {
      houseKeepingControl.setFrontDeskControl(this);
    }
  }

  public HouseKeepingControl getHouseKeepingControl() {
    return houseKeepingControl;
  }

  public void setBookingControl(BookingControl bookingControl) {
    this.bookingControl = bookingControl;
    if (bookingControl != null) {
      this.pendingWalkInQueue = bookingControl.getPendingWalkInConfirmations();
      if (bookingControl.getFrontDeskControl() != this) {
        bookingControl.setFrontDeskControl(this);
      }
    }
  }

  public BookingControl getBookingControl() {
    return bookingControl;
  }

  public void addPendingWalkInBooking(Booking booking) {
    if (booking != null) {
      boolean exists = false;
      QueueInterface<Booking> temp = new LinkedQueue<>();
      while (!pendingWalkInQueue.isEmpty()) {
        Booking b = pendingWalkInQueue.dequeue();
        if (b.getBookingID().equalsIgnoreCase(booking.getBookingID())) {
          exists = true;
        }
        temp.enqueue(b);
      }
      while (!temp.isEmpty()) {
        pendingWalkInQueue.enqueue(temp.dequeue());
      }
      if (!exists) {
        pendingWalkInQueue.enqueue(booking);
      }
    }
  }

  public QueueInterface<Booking> getPendingWalkInQueue() {
    if (pendingWalkInQueue.isEmpty()) {
      loadPendingWalkInsFromFile();
    }
    return pendingWalkInQueue;
  }

  private void loadPendingWalkInsFromFile() {
    FileHandler.loadBookings(null, null, null, null, null, pendingWalkInQueue, null);
  }

  public double getRoomRate(String category) {
    if (category == null) return 120.00;
    return switch (category.toLowerCase()) {
      case "standard", "standard single", "standard double" -> 120.00;
      case "deluxe", "deluxe suite" -> 250.00;
      case "suite", "executive suite", "executive" -> 500.00;
      case "presidential", "presidential suite" -> 1200.00;
      default -> 120.00;
    };
  }

  public String generate8DigitConfirmationNumber() {
    long maxId = 10008800;
    Iterator<Reservation> it = reservationTree.getInorderIterator();

    while (it.hasNext()) {
      try {
        long currentId = Long.parseLong(it.next().getConfirmationNumber());
        if (currentId > maxId) {
          maxId = currentId;
        }
      } catch (NumberFormatException ignored) {
      }
    }
    return String.valueOf(maxId + 1);
  }

  public void runFrontDeskService() {
    int choice;
    do {
      choice = frontDeskUI.getMenuChoice();
      switch (choice) {
        case 0 -> MessageUI.displayExitMessage();
        case 1 -> searchReservationByConfirmationNumber();
        case 2 -> processGuestCheckOut();
        case 3 -> updateReservationStatus();
        case 4 -> deleteReservation();
        case 5 -> assignWalkInConfirmation();
        case 6 -> generateCustomReport();
        case 7 -> viewRoomAvailability();
        default -> MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);
  }

  // =========================================================================
  // GUEST CHECK-OUT PROCESS
  // =========================================================================

  public void processGuestCheckOut() {
    System.out.println("\n-----------------------------------------");
    System.out.println("         GUEST CHECK-OUT PROCESS");
    System.out.println("-----------------------------------------");
    String confirmNo = frontDeskUI.inputConfirmationNumber();
    Reservation target = reservationTree.search(new Reservation(confirmNo));

    if (target == null) {
      MessageUI.displayNotFoundMessage();
      return;
    }

    frontDeskUI.printReservationDetails(target);

    if ("Checked-Out".equalsIgnoreCase(target.getStatus())) {
      System.out.println("\n  [ NOTICE ] Reservation " + confirmNo + " has ALREADY been checked out.");
      return;
    }

    boolean confirm = frontDeskUI.readConfirmationInput("\nConfirm check-out for Guest '" + target.getGuestName() + "'? (Y/N): ");

    if (confirm) {
      target.setStatus("Checked-Out");
      String roomNo = target.getRoomNumber();

      if (roomNo != null && !roomNo.trim().isEmpty() && !roomNo.equalsIgnoreCase("Pending")) {
        if (houseKeepingControl != null) {
          houseKeepingControl.createTaskForRoom(roomNo, "Room Cleaning", "S001");
          System.out.println("  [ System ] Housekeeping task auto-created for Room " + roomNo + " (Status set to Dirty).");
        }
      }

      FileHandler.saveReservations(reservationTree);
      System.out.println("\n  [ SUCCESS ] Guest successfully checked out. Final bill: $" + String.format("%.2f", target.getTotalBillAmount()));
    } else {
      System.out.println("\n  [ System ] Check-out operation cancelled.");
    }
  }

  public void deleteReservation() {
    String confirmNo = frontDeskUI.inputConfirmationNumber();
    deleteReservation(confirmNo);
  }

  public boolean deleteReservation(String confirmNo) {
    if (confirmNo == null || confirmNo.trim().isEmpty()) {
      MessageUI.displayNotFoundMessage();
      return false;
    }
    Reservation target = new Reservation(confirmNo.trim());
    if (reservationTree.search(target) != null) {
      reservationTree.remove(target);
      FileHandler.saveReservations(reservationTree);
      System.out.println("Reservation " + confirmNo + " has been successfully deleted.");
      return true;
    } else {
      MessageUI.displayNotFoundMessage();
      return false;
    }
  }

  // =========================================================================
  // ASSIGN WALK-IN CONFIRMATION NUMBER
  // =========================================================================

  public void assignWalkInConfirmation() {
    QueueInterface<Booking> pendingQueue = getPendingWalkInQueue();

    if (pendingQueue.isEmpty()) {
      System.out.println("\n+-----------------------------------------------------------------------+");
      System.out.println("|  [ NOTICE ] No assigned walk-in guests waiting for confirmation #.    |");
      System.out.println("+-----------------------------------------------------------------------+");
      System.out.println("  Guests must first be registered and assigned a room in Module 1.");
      return;
    }

    displayPendingWalkInGuests();

    System.out.println("\nSelect Action:");
    System.out.println("  [1] Assign Confirmation # to Next Guest in Line");
    System.out.println("  [2] Select Guest by Specific Booking ID");
    System.out.println("  [0] Return to Front Desk Main Menu");

    int opt = frontDeskUI.readIntInput("Choice (0-2): ", 0, 2);

    if (opt == 0) {
      System.out.println("\n[ System ] Walk-in confirmation assignment cancelled.");
      return;
    }

    Booking targetBooking = null;

    if (opt == 1) {
      targetBooking = pendingQueue.dequeue();
    } else if (opt == 2) {
      String bId = frontDeskUI.inputBookingID();
      targetBooking = extractBookingFromQueue(pendingQueue, bId);
      if (targetBooking == null) {
        System.out.println("\n  [ ERROR ] Booking ID '" + bId.toUpperCase() + "' was not found in the pending confirmation queue.");
        return;
      }
    }

    if (targetBooking != null) {
      processWalkInConfirmation(targetBooking);
    }
  }

  public void displayPendingWalkInGuests() {
    QueueInterface<Booking> queue = getPendingWalkInQueue();
    if (queue.isEmpty()) {
      System.out.println("  [ NOTICE ] No pending walk-in guests currently waiting.");
      return;
    }

    QueueInterface<Booking> temp = new LinkedQueue<>();
    System.out.println("\n=========================================================================================================");
    System.out.println("                       PENDING WALK-IN GUESTS WAITING FOR CONFIRMATION NUMBER                            ");
    System.out.println("=========================================================================================================");
    System.out.printf("%-10s %-18s %-14s %-16s %-12s %-12s %-10s\n",
            "Booking ID", "Guest Name", "Contact", "Assigned Room", "Check-In", "Check-Out", "Status");
    System.out.println("---------------------------------------------------------------------------------------------------------");

    int count = 0;
    while (!queue.isEmpty()) {
      Booking b = queue.dequeue();
      count++;
      String gName = b.getGuest() != null ? b.getGuest().getGuestName() : "-";
      String phone = b.getGuest() != null ? b.getGuest().getPhoneNumber() : "-";
      String room = b.getRoom() != null ? b.getRoom().getRoomNumber() + " (" + b.getRoom().getRoomType() + ")" : "Pending";
      System.out.printf("%-10s %-18s %-14s %-16s %-12s %-12s %-10s\n",
              b.getBookingID(), gName, phone, room, b.getCheckInDate(), b.getCheckOutDate(), "Pending Conf");
      temp.enqueue(b);
    }
    while (!temp.isEmpty()) {
      queue.enqueue(temp.dequeue());
    }
    System.out.println("---------------------------------------------------------------------------------------------------------");
    System.out.println("Total Pending Walk-In Guest(s): " + count);
  }

  private void processWalkInConfirmation(Booking booking) {
    String confirmNo = generate8DigitConfirmationNumber();
    String guestName = booking.getGuest() != null ? booking.getGuest().getGuestName() : "Walk-In Guest";
    String roomNo = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "Pending";
    String category = booking.getRoom() != null ? booking.getRoom().getCategory() : "Standard";

    int days = 1;
    try {
      LocalDate inDate = LocalDate.parse(booking.getCheckInDate());
      LocalDate outDate = LocalDate.parse(booking.getCheckOutDate());
      long diff = ChronoUnit.DAYS.between(inDate, outDate);
      if (diff > 0) days = (int) diff;
    } catch (Exception ignored) {
    }

    double rate = booking.getRoom() != null && booking.getRoom().getRatePerNight() > 0 
                  ? booking.getRoom().getRatePerNight() 
                  : getRoomRate(category);
    double totalBill = rate * days;

    Reservation newRes = new Reservation(confirmNo, guestName, category, roomNo, days, totalBill, "Checked-In");
    reservationTree.add(newRes);
    FileHandler.saveReservations(reservationTree);

    booking.setBookingStatus("Confirmed (Conf: " + confirmNo + ")");
    if (bookingControl != null) {
      bookingControl.markBookingConfirmed(booking);
    }

    System.out.println("\n+=======================================================================+");
    System.out.println("|             WALK-IN CONFIRMATION ASSIGNED SUCCESSFULLY                |");
    System.out.println("+=======================================================================+");
    System.out.printf("|  %-22s : %-44s |\n", "Confirmation Number", confirmNo);
    System.out.printf("|  %-22s : %-44s |\n", "Booking ID", booking.getBookingID());
    System.out.printf("|  %-22s : %-44s |\n", "Guest Name", guestName);
    System.out.printf("|  %-22s : %-44s |\n", "Contact Phone", (booking.getGuest() != null ? booking.getGuest().getPhoneNumber() : "-"));
    System.out.printf("|  %-22s : %-44s |\n", "Assigned Room", roomNo + " (" + (booking.getRoom() != null ? booking.getRoom().getRoomType() : category) + ")");
    System.out.printf("|  %-22s : %-44s |\n", "Stay Duration", days + " Night(s) (" + booking.getCheckInDate() + " to " + booking.getCheckOutDate() + ")");
    System.out.printf("|  %-22s : $%-43.2f |\n", "Total Bill Amount", totalBill);
    System.out.printf("|  %-22s : %-44s |\n", "Front Desk Status", "Checked-In (Active Reservation)");
    System.out.println("+=======================================================================+");
    System.out.println("  Reservation has been added to Front Desk BST and saved to disk.");
  }

  private Booking extractBookingFromQueue(QueueInterface<Booking> queue, String bookingID) {
    if (queue.isEmpty() || bookingID == null) return null;
    QueueInterface<Booking> temp = new LinkedQueue<>();
    Booking target = null;

    while (!queue.isEmpty()) {
      Booking b = queue.dequeue();
      if (target == null && b.getBookingID().equalsIgnoreCase(bookingID.trim())) {
        target = b;
      } else {
        temp.enqueue(b);
      }
    }
    while (!temp.isEmpty()) {
      queue.enqueue(temp.dequeue());
    }
    return target;
  }

  // =========================================================================
  // ROOM AVAILABILITY
  // =========================================================================

  public void viewRoomAvailability() {
    Map<String, String> roomStatusMap = new HashMap<>();
    Map<String, String> roomGuestMap = new HashMap<>();
    Map<String, String> roomConfMap = new HashMap<>(); // Tracks Confirmation Numbers

    Iterator<Reservation> it = reservationTree.getInorderIterator();
    while (it.hasNext()) {
      Reservation r = it.next();
      String roomNo = r.getRoomNumber();
      String status = r.getStatus();
      
      if (roomNo != null && !roomNo.trim().isEmpty() && !roomNo.equalsIgnoreCase("Pending") && !status.equalsIgnoreCase("Checked-Out")) {
        roomStatusMap.put(roomNo.toUpperCase(), status);
        roomGuestMap.put(roomNo.toUpperCase(), r.getGuestName());
        roomConfMap.put(roomNo.toUpperCase(), r.getConfirmationNumber());
      }
    }

    QueueInterface<Booking> pendingQ = getPendingWalkInQueue();
    QueueInterface<Booking> temp = new LinkedQueue<>();
    while (!pendingQ.isEmpty()) {
      Booking b = pendingQ.dequeue();
      if (b.getRoom() != null && b.getRoom().getRoomNumber() != null) {
        String rNo = b.getRoom().getRoomNumber().toUpperCase();
        String gName = b.getGuest() != null ? b.getGuest().getGuestName() : "Walk-In";
        roomStatusMap.put(rNo, "Assigned (Walk-In)");
        roomGuestMap.put(rNo, gName);
        roomConfMap.put(rNo, "Pending (" + b.getBookingID() + ")");
      }
      temp.enqueue(b);
    }
    while (!temp.isEmpty()) {
      pendingQ.enqueue(temp.dequeue());
    }

    Map<String, entity.HousekeepingTask> hkTaskMap = houseKeepingControl != null
        ? houseKeepingControl.getAllActiveTasksMap()
        : new HashMap<>();

    for (Map.Entry<String, entity.HousekeepingTask> entry : hkTaskMap.entrySet()) {
      String rNo = entry.getKey();
      entity.HousekeepingTask task = entry.getValue();
      if (!roomStatusMap.containsKey(rNo) || "Cleaning".equalsIgnoreCase(roomStatusMap.get(rNo))) {
        roomStatusMap.put(rNo, "Cleaning (" + task.getStatus() + ")");
        roomGuestMap.put(rNo, "Staff: " + task.getAssignedStaffID());
        if (!roomConfMap.containsKey(rNo)) {
          roomConfMap.put(rNo, "-");
        }
      }
    }

    List<Room> allRooms = FileHandler.loadAllHotelRooms();
    StringBuilder table = new StringBuilder();

    table.append("+------------+--------------------+------------------+------------------+---------------------------+---------------------------+\n");
    table.append(String.format("| %-10s | %-18s | %-16s | %-16s | %-25s | %-25s |\n", 
        "Room No.", "Room Type", "Rate / Night ($)", "Confirmation #", "Guest / Staff", "Current Status"));
    table.append("+------------+--------------------+------------------+------------------+---------------------------+---------------------------+\n");

    for (Room r : allRooms) {
      String room = r.getRoomNumber();
      String type = r.getRoomType();
      double rate = r.getRatePerNight();
      String status = roomStatusMap.getOrDefault(room.toUpperCase(), r.getRoomStatus());
      String guest = roomGuestMap.getOrDefault(room.toUpperCase(), "-");
      String confNo = roomConfMap.getOrDefault(room.toUpperCase(), "-");

      table.append(String.format("| %-10s | %-18s | $%-15.2f | %-16s | %-25s | %-25s |\n", 
          room, type, rate, confNo, guest, status));
    }
    
    table.append("+------------+--------------------+------------------+------------------+---------------------------+---------------------------+\n");

    frontDeskUI.displayRoomAvailabilityTable(table.toString());
  }

  // =========================================================================
  // SEARCH
  // =========================================================================

  public void searchReservationsMenu() {
    int choice;
    do {
      choice = frontDeskUI.getSearchMenuChoice();
      switch (choice) {
        case 0 -> {}
        case 1 -> viewAllReservations();
        case 2 -> searchReservationByConfirmationNumber();
        default -> MessageUI.displayInvalidChoiceMessage();
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
  // UPDATE
  // =========================================================================

  private double calculateTotalBill(String roomCategory, int days) {
    double dailyRate = getRoomRate(roomCategory);
    return dailyRate * days;
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
        case 1 -> res.setGuestName(frontDeskUI.inputGuestName());
        case 2 -> {
          String newCategory = frontDeskUI.inputRoomCategory();
          res.setRoomCategory(newCategory);
          res.setTotalBillAmount(calculateTotalBill(newCategory, res.getStayDurationDays()));
          System.out.println("Room category updated. New bill auto-calculated.");
        }
        case 3 -> res.setRoomNumber(frontDeskUI.inputRoomNumber());
        case 4 -> {
          LocalDate checkIn = frontDeskUI.inputCheckInDate();
          LocalDate checkOut = frontDeskUI.inputCheckOutDate(checkIn);
          int newDays = frontDeskUI.calculateStayDuration(checkIn, checkOut);
          res.setStayDurationDays(newDays);
          res.setTotalBillAmount(calculateTotalBill(res.getRoomCategory(), newDays));
          System.out.println("Stay dates & duration updated to " + newDays + " day(s) (" + checkIn + " to " + checkOut + "). New bill auto-calculated.");
        }
        case 5 -> {
          String newStatus = frontDeskUI.inputStatus();
          String oldStatus = res.getStatus();
          res.setStatus(newStatus);

          boolean isCheckOut = newStatus.equalsIgnoreCase("Checked-Out") || newStatus.equalsIgnoreCase("Cleaning");
          boolean wasAlreadyCleaning = oldStatus.equalsIgnoreCase("Cleaning");
          String roomNo = res.getRoomNumber();

          if (isCheckOut && !wasAlreadyCleaning && roomNo != null && !roomNo.trim().isEmpty() && !roomNo.equalsIgnoreCase("Pending")) {
            if (houseKeepingControl != null) {
              houseKeepingControl.createTaskForRoom(roomNo, "Room Cleaning", "S001");
              System.out.println("  [ System ] Housekeeping task auto-created for room " + roomNo + " (Status: Dirty).");
            }
          }
        }
        case 0 -> System.out.println("Finished updating reservation.");
        default -> MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);

    FileHandler.saveReservations(reservationTree);
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
        case 0 -> {
          return;
        }
        case 1 -> categoryFilter = frontDeskUI.inputCategoryFilter();
        case 2 -> statusFilter = frontDeskUI.inputStatusFilter();
        case 3 -> {
          minBill = frontDeskUI.inputMinBill();
          useMinBill = true;
        }
        case 4 -> {
          minDays = frontDeskUI.inputMinDays();
          useMinDays = true;
        }
        case 5 -> {
          printReport(categoryFilter, statusFilter, useMinBill, minBill, useMinDays, minDays);
          return;
        }
        default -> MessageUI.displayInvalidChoiceMessage();
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