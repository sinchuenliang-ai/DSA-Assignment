package control;

import adt.*;
import boundary.FrontDeskUI;
import entity.Booking;
import entity.HousekeepingTask;
import entity.Reservation;
import entity.Room;
import entity.Member;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import utility.FileHandler;
import utility.MessageUI;

/**
 *
 * @author Clement Chow Quan Liang
 */
public class FrontDeskControl {

  private TreeInterface<Reservation> reservationTree = new BinarySearchTreeADT<>();
  private QueueInterface<Booking> pendingWalkInQueue = new LinkedQueue<>();
  private FrontDeskUI frontDeskUI = new FrontDeskUI();
  private BookingControl bookingControl;
  private HouseKeepingControl houseKeepingControl;
  private LoyaltyControl loyaltyControl;

  public FrontDeskControl() {
    this(null, null);
  }

  public FrontDeskControl(HouseKeepingControl houseKeepingControl) {
    this(null, houseKeepingControl);
  }

  public FrontDeskControl(BookingControl bookingControl, HouseKeepingControl houseKeepingControl) {
    FileHandler.loadReservations(reservationTree);
    normalizeReservationStatuses();
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

  public void setLoyaltyControl(LoyaltyControl loyaltyControl) {
    this.loyaltyControl = loyaltyControl;
  }

  public LoyaltyControl getLoyaltyControl() {
    return loyaltyControl;
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
      case "standard single", "standard"          -> 120.00;
      case "standard double"                       -> 150.00;
      case "deluxe suite", "deluxe"               -> 250.00;
      case "executive suite", "suite", "executive" -> 500.00;
      case "presidential suite", "presidential"   -> 1200.00;
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

  public Reservation registerGuestAndAssignConfirmation(String guestName, String roomCategory, String roomNumber, int stayDays, double totalBill, String status) {
    LocalDate today = LocalDate.now();
    return registerGuestAndAssignConfirmation(guestName, roomCategory, roomNumber, stayDays, totalBill, status, today.toString(), today.plusDays(stayDays).toString());
  }

  public Reservation registerGuestAndAssignConfirmation(String guestName, String roomCategory, String roomNumber, int stayDays, double totalBill, String status, String checkInDate, String checkOutDate) {
    String confirmationNumber = generate8DigitConfirmationNumber();

    // Auto-determine Reserved vs Checked-In based on check-in date
    if (checkInDate != null && !checkInDate.isEmpty()) {
      try {
        LocalDate parsedIn = LocalDate.parse(checkInDate);
        if (parsedIn.isAfter(LocalDate.now())) {
          status = "Reserved";
        }
      } catch (Exception ignored) {}
    }

    Reservation newReservation = new Reservation(
        confirmationNumber,
        guestName,
        roomCategory,
        roomNumber,
        stayDays,
        totalBill,
        status,
        checkInDate,
        checkOutDate
    );
    reservationTree.add(newReservation);
    FileHandler.saveReservations(reservationTree);
    return newReservation;
  }

  public void runFrontDeskService() {
    int choice;
    do {
      choice = frontDeskUI.getMenuChoice();
      switch (choice) {
        case 0 -> MessageUI.displayExitMessage();
        case 1 -> viewRoomAvailability();
        case 2 -> searchAndCheckOutGuest();
        case 3 -> assignWalkInConfirmation();
        case 4 -> updateReservationStatus();
        case 5 -> deleteReservation();
        case 6 -> generateReportsMenu();
        default -> MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);
  }

  // =========================================================================
  // STATUS FORMATTER WITH ALIGNMENT
  // =========================================================================

  private String formatPaddedStatus(String status, int columnWidth) {
    if ("Checked-In".equalsIgnoreCase(status)) {
      String text = "** CHECKED-IN **";
      return String.format("%" + columnWidth + "s", text);
    }
    if ("Reserved".equalsIgnoreCase(status)) {
      String text = "* RESERVED *";
      return String.format("%" + columnWidth + "s", text);
    }
    
    if (status == null || status.trim().isEmpty()) {
      status = "Available";
    }

    return String.format("%-" + columnWidth + "s", status);
  }

// =========================================================================
  // SEARCH GUEST & PROCESS CHECK-OUT (IMMEDIATE RECEIPT GENERATION)
  // =========================================================================

  public void searchAndCheckOutGuest() {
    System.out.println("\n-----------------------------------------");
    System.out.println("    SEARCH GUEST & CHECK-OUT PROCESS");
    System.out.println("-----------------------------------------");
    String confirmNo = frontDeskUI.inputConfirmationNumber();
    Reservation target = reservationTree.search(new Reservation(confirmNo));

    if (target == null) {
      MessageUI.displayNotFoundMessage();
      return;
    }

    // 1. Generate and display receipt immediately upon finding guest
    frontDeskUI.printGuestReceipt(target);

    // 2. Check if already checked out
    if ("Checked-Out".equalsIgnoreCase(target.getStatus())) {
      System.out.println("  [ NOTICE ] Reservation " + confirmNo + " is ALREADY checked out.");
      return;
    }

    // 3. Prompt to proceed with payment
    boolean confirm = frontDeskUI.readConfirmationInput("Proceed to process payment and check-out guest '" + target.getGuestName() + "'? (Y/N): ");

    if (!confirm) {
      System.out.println("\n  [ System ] Check-out cancelled. Guest remains Checked-In.");
      return;
    }

    // 4. Process payment via chosen method
    String paymentMethod = null;
    while (paymentMethod == null) {
      paymentMethod = frontDeskUI.processPayment(target.getTotalBillAmount());
      if (paymentMethod == null) {
        boolean retry = frontDeskUI.readConfirmationInput("  Payment cancelled. Cancel check-out entirely? (Y/N): ");
        if (retry) {
          System.out.println("\n  [ System ] Check-out cancelled. Guest remains Checked-In.");
          return;
        }
      }
    }

    // 5. Update status to Checked-Out
    target.setStatus("Checked-Out");
    String roomNo = target.getRoomNumber();

    if (roomNo != null && !roomNo.trim().isEmpty() && !roomNo.equalsIgnoreCase("Pending")) {
      if (houseKeepingControl != null) {
        houseKeepingControl.createTaskForRoom(roomNo, "Room Cleaning", "S001");
        System.out.println("  [ System ] Housekeeping task auto-created for Room " + roomNo + " (Status set to Dirty).");
      }
    }

    FileHandler.saveReservations(reservationTree);

    // 6. Award loyalty points for stay (if member found by guest name)
    if (loyaltyControl != null) {
      Member loyaltyMember = loyaltyControl.findMemberByName(target.getGuestName());
      if (loyaltyMember != null) {
        int pts = loyaltyControl.rewardPointsForStay(loyaltyMember, target.getTotalBillAmount(), confirmNo);
        System.out.println("  [ Loyalty ] Points awarded to " + loyaltyMember.getName()
            + ": +" + pts + " pts (Tier: " + loyaltyMember.getTier() + ")");
      } else {
        System.out.println("  [ Loyalty ] Guest is not a loyalty member. No points awarded.");
      }
    }

    System.out.println("\n===================================================");
    System.out.printf(  "  [ SUCCESS ] Payment via %s%n", paymentMethod);
    System.out.println("  Guest status updated to Checked-Out.");
    System.out.println("===================================================\n");
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

    LocalDate today = LocalDate.now();
    LocalDate inDate = null;
    LocalDate outDate = null;
    int days = 1;
    try {
      if (booking.getCheckInDate() != null && !booking.getCheckInDate().isEmpty()) {
        inDate = LocalDate.parse(booking.getCheckInDate());
      }
      if (booking.getCheckOutDate() != null && !booking.getCheckOutDate().isEmpty()) {
        outDate = LocalDate.parse(booking.getCheckOutDate());
      }
      if (inDate != null && outDate != null) {
        long diff = ChronoUnit.DAYS.between(inDate, outDate);
        if (diff > 0) days = (int) diff;
      }
    } catch (Exception ignored) {
    }

    if (inDate == null) inDate = today;
    if (outDate == null) outDate = inDate.plusDays(days);

    String inDateStr = inDate.toString();
    String outDateStr = outDate.toString();

    double rate = booking.getRoom() != null && booking.getRoom().getRatePerNight() > 0 
                  ? booking.getRoom().getRatePerNight() 
                  : getRoomRate(category);
    double totalBill = rate * days;

    // Auto-determine Reserved if check-in date is in the future
    String initialStatus = inDate.isAfter(today) ? "Reserved" : "Checked-In";

    Reservation newRes = new Reservation(confirmNo, guestName, category, roomNo, days, totalBill, initialStatus, inDateStr, outDateStr);
    reservationTree.add(newRes);
    FileHandler.saveReservations(reservationTree);

    booking.setBookingStatus("Confirmed (Conf: " + confirmNo + ")");
    if (bookingControl != null) {
      bookingControl.markBookingConfirmed(booking);
    }

    // Loyalty Linkage - Reward points for stay
    int pointsEarned = 0;
    Member loyaltyMember = null;
    if (loyaltyControl != null && booking.getGuest() != null) {
      String email = booking.getGuest().getEmail();
      String phone = booking.getGuest().getPhoneNumber();
      loyaltyMember = loyaltyControl.findMemberByEmailOrPhone(email, phone);
      if (loyaltyMember != null) {
        pointsEarned = loyaltyControl.rewardPointsForStay(loyaltyMember, totalBill, booking.getBookingID());
      }
    }

    String statusDisplay = "Reserved".equalsIgnoreCase(initialStatus)
        ? "Reserved (Upcoming Check-In on " + inDateStr + ")"
        : "Checked-In (Active Reservation)";

    System.out.println("\n+=======================================================================+");
    System.out.println("|             WALK-IN CONFIRMATION ASSIGNED SUCCESSFULLY                |");
    System.out.println("+=======================================================================+");
    System.out.printf("|  %-22s : %-44s |\n", "Confirmation Number", confirmNo);
    System.out.printf("|  %-22s : %-44s |\n", "Booking ID", booking.getBookingID());
    System.out.printf("|  %-22s : %-44s |\n", "Guest Name", guestName);
    System.out.printf("|  %-22s : %-44s |\n", "Contact Phone", (booking.getGuest() != null ? booking.getGuest().getPhoneNumber() : "-"));
    System.out.printf("|  %-22s : %-44s |\n", "Assigned Room", roomNo + " (" + (booking.getRoom() != null ? booking.getRoom().getRoomType() : category) + ")");
    System.out.printf("|  %-22s : %-44s |\n", "Stay Duration", days + " Night(s) (" + booking.getCheckInDate() + " to " + booking.getCheckOutDate() + ")");
    System.out.printf("|  %-22s : RM %-41.2f |\n", "Total Bill Amount", totalBill);
    if (loyaltyMember != null) {
      System.out.printf("|  %-22s : %-44s |\n", "Loyalty Member", loyaltyMember.getMemberId() + " (" + loyaltyMember.getTier() + " Tier)");
      System.out.printf("|  %-22s : %-44s |\n", "Points Earned", "+" + pointsEarned + " (New Bal: " + loyaltyMember.getPoints() + ")");
    }
    System.out.printf("|  %-22s : %-44s |\n", "Front Desk Status", statusDisplay);
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
  // CONSOLIDATED ROOM SCHEDULE & AVAILABILITY VIEW
  // =========================================================================

  public void viewRoomAvailability() {
    System.out.println("\n-----------------------------------------");
    System.out.println("   ROOM AVAILABILITY & SCHEDULE FILTER");
    System.out.println("-----------------------------------------");
    LocalDate startDate = frontDeskUI.inputCheckInDate();
    LocalDate endDate = frontDeskUI.inputCheckOutDate(startDate);

    List<Room> allRooms = FileHandler.loadAllHotelRooms();
    StringBuilder table = new StringBuilder();

    table.append("+------------+--------------------+------------------+---------------------------+--------------+--------------+----------------------+\n");
    table.append(String.format("| %-10s | %-18s | %-16s | %-25s | %-12s | %-12s | %-20s |\n", 
        "Room No.", "Room Type", "Confirmation #", "Guest Name", "Check-In", "Check-Out", "Status"));
    table.append("+------------+--------------------+------------------+---------------------------+--------------+--------------+----------------------+\n");

    for (Room room : allRooms) {
      String rNo = room.getRoomNumber();
      Reservation matchingRes = null;

      Iterator<Reservation> it = reservationTree.getInorderIterator();
      while (it.hasNext()) {
        Reservation r = it.next();
        if (rNo.equalsIgnoreCase(r.getRoomNumber()) && !"Checked-Out".equalsIgnoreCase(r.getStatus())) {
          LocalDate resIn = null;
          LocalDate resOut = null;
          try {
            if (r.getCheckInDate() != null && !r.getCheckInDate().isEmpty()) {
              resIn = LocalDate.parse(r.getCheckInDate());
            }
            if (r.getCheckOutDate() != null && !r.getCheckOutDate().isEmpty()) {
              resOut = LocalDate.parse(r.getCheckOutDate());
            }
          } catch (Exception ignored) {}

          if (resIn == null) resIn = LocalDate.now();
          if (resOut == null) resOut = resIn.plusDays(r.getStayDurationDays());

          boolean overlaps = !(resOut.isBefore(startDate) || resIn.isAfter(endDate));
          if (overlaps) {
            matchingRes = r;
            break;
          }
        }
      }

      String confNo = "-";
      String guestName = "-";
      String inStr = "-";
      String outStr = "-";
      String rawStatus = "Available";

      if (matchingRes != null) {
        confNo = matchingRes.getConfirmationNumber();
        guestName = matchingRes.getGuestName();
        inStr = matchingRes.getCheckInDate() != null ? matchingRes.getCheckInDate() : "-";
        outStr = matchingRes.getCheckOutDate() != null ? matchingRes.getCheckOutDate() : "-";
        rawStatus = matchingRes.getStatus() != null ? matchingRes.getStatus() : "Checked-In";
      } else {
        Map<String, HousekeepingTask> hkTaskMap = houseKeepingControl != null
            ? houseKeepingControl.getAllActiveTasksMap()
            : new HashMap<>();

        if (hkTaskMap.containsKey(rNo.toUpperCase())) {
          HousekeepingTask task = hkTaskMap.get(rNo.toUpperCase());
          if (task != null && task.getStatus() != null && !task.getStatus().trim().isEmpty()) {
            rawStatus = task.getStatus();
          } else {
            rawStatus = "Dirty";
          }
        }
      }

      String formattedStatus = formatPaddedStatus(rawStatus, 20);

      table.append(String.format("| %-10s | %-18s | %-16s | %-25s | %-12s | %-12s | %s |\n", 
          rNo, room.getRoomType(), confNo, guestName, inStr, outStr, formattedStatus));
    }

    table.append("+------------+--------------------+------------------+---------------------------+--------------+--------------+----------------------+\n");

    frontDeskUI.displayRoomAvailabilityTable(table.toString(), startDate, endDate);
  }

  // =========================================================================
  // SEARCH SUB-MENU (FOR INTERNAL CALLS IF NEEDED)
  // =========================================================================

  public void searchReservationsMenu() {
    int choice;
    do {
      choice = frontDeskUI.getSearchMenuChoice();
      switch (choice) {
        case 0 -> {}
        case 1 -> viewAllReservations();
        case 2 -> searchAndCheckOutGuest();
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

  // =========================================================================
  // UPDATE
  // =========================================================================

  private double calculateTotalBill(String roomCategory, int days) {
    double dailyRate = getRoomRate(roomCategory);
    return dailyRate * days;
  }

public void updateReservationStatus() {
    System.out.println("\n-----------------------------------------");
    System.out.println("         UPDATE GUEST DETAILS");
    System.out.println("-----------------------------------------");
    String confirmNo = frontDeskUI.inputConfirmationNumber();
    Reservation res = reservationTree.search(new Reservation(confirmNo));
    
    if (res == null) {
      MessageUI.displayNotFoundMessage();
      return;
    }

    int choice;
    boolean isUpdated = false;

    do {
      frontDeskUI.printReservationDetails(res);
      choice = frontDeskUI.getUpdateMenuChoice();
      
      switch (choice) {
        case 1 -> {
          res.setGuestName(frontDeskUI.inputGuestName());
          isUpdated = true;
          System.out.println("  [ System ] Guest name updated successfully.");
        }
        case 2 -> {
          // Step 1: Pick category
          String newCategory = frontDeskUI.selectRoomCategory();

          // Step 2: Build set of occupied rooms (skip current reservation's room)
          java.util.Set<String> occupied = new java.util.HashSet<>();
          Iterator<Reservation> it = reservationTree.getInorderIterator();
          while (it.hasNext()) {
            Reservation r = it.next();
            // A room is occupied if Checked-In or Reserved (excluding current reservation)
            String rStatus = r.getStatus();
            boolean isActive = "Checked-In".equalsIgnoreCase(rStatus)
                || "Reserved".equalsIgnoreCase(rStatus);
            boolean isCurrentRes = r.getConfirmationNumber().equals(res.getConfirmationNumber());
            if (isActive && !isCurrentRes && r.getRoomNumber() != null
                && !r.getRoomNumber().equalsIgnoreCase("Pending")
                && !r.getRoomNumber().equalsIgnoreCase("0")) {
              occupied.add(r.getRoomNumber().toUpperCase());
            }
          }

          // Step 3: Pick room number for that category
          String newRoom = frontDeskUI.selectRoomNumber(newCategory, occupied);
          if (newRoom == null) {
            System.out.println("  [ System ] Room update cancelled — no available rooms.");
            break;
          }

          res.setRoomCategory(newCategory);
          res.setRoomNumber(newRoom);
          res.setTotalBillAmount(calculateTotalBill(newCategory, res.getStayDurationDays()));
          isUpdated = true;
          System.out.println("  [ System ] Room updated to " + newRoom + " (" + newCategory
              + "). Bill auto-recalculated: RM "
              + String.format("%.2f", res.getTotalBillAmount()));
        }
        case 3 -> {
          LocalDate checkIn = frontDeskUI.inputCheckInDate();
          LocalDate checkOut = frontDeskUI.inputCheckOutDate(checkIn);
          int newDays = frontDeskUI.calculateStayDuration(checkIn, checkOut);
          
          if (newDays <= 0) {
              System.out.println("  [!] Update failed: Stay duration must be at least 1 day.");
          } else {
              res.setCheckInDate(checkIn.toString());
              res.setCheckOutDate(checkOut.toString());
              res.setStayDurationDays(newDays);
              res.setTotalBillAmount(calculateTotalBill(res.getRoomCategory(), newDays));
              isUpdated = true;
              System.out.println("  [ System ] Dates updated (" + checkIn + " to " + checkOut + "). Duration is now " + newDays + " day(s). Bill auto-recalculated.");
          }
        }
        case 0 -> {
          if (isUpdated) {
             System.out.println("Saving updates...");
             FileHandler.saveReservations(reservationTree);
             MessageUI.displayUpdatedMessage();
          } else {
             System.out.println("No changes were made. Exiting update menu.");
          }
        }
        default -> MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);
  }


  // =========================================================================
  // REPORTS & ANALYTICS MODULE
  // =========================================================================

  public void generateReportsMenu() {
    int choice;
    do {
      choice = frontDeskUI.getReportMainMenuChoice();
      switch (choice) {
        case 0 -> {
          return;
        }
        case 1 -> generateOperationalReport();
        case 2 -> generateBusinessCycleAnalyticsReport();
        default -> MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);
  }

  // =========================================================================
  // REPORT 1: OPERATIONAL RESERVATION & OCCUPANCY REPORT (SEARCH & MULTI-FILTER)
  // =========================================================================

  public void generateOperationalReport() {
    String categoryFilter = "";
    String statusFilter = "";
    boolean useMinBill = false;
    double minBill = 0.0;
    boolean useMinDays = false;
    int minDays = 0;
    String searchKeyword = "";
    int sortChoice = 1; // 1: Conf # Asc

    int choice;
    do {
      String sortLabel = getReport1SortLabel(sortChoice);
      choice = frontDeskUI.getReportFilterChoice(
          categoryFilter, statusFilter, useMinBill, minBill, useMinDays, minDays, searchKeyword, sortLabel);

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
        case 5 -> searchKeyword = frontDeskUI.inputSearchKeyword();
        case 6 -> sortChoice = frontDeskUI.getReport1SortChoice();
        case 7 -> {
          printOperationalReport(categoryFilter, statusFilter, useMinBill, minBill, useMinDays, minDays, searchKeyword, sortChoice);
          return;
        }
        case 8 -> {
          categoryFilter = "";
          statusFilter = "";
          useMinBill = false;
          minBill = 0.0;
          useMinDays = false;
          minDays = 0;
          searchKeyword = "";
          sortChoice = 1;
          System.out.println("  [✔] All filters reset to default (Show All).");
        }
        default -> MessageUI.displayInvalidChoiceMessage();
      }
    } while (true);
  }

  private String getReport1SortLabel(int choice) {
    return switch (choice) {
      case 1 -> "Confirmation Number (Ascending)";
      case 2 -> "Confirmation Number (Descending)";
      case 3 -> "Guest Name (A to Z)";
      case 4 -> "Stay Duration (Longest to Shortest)";
      case 5 -> "Total Bill (Highest to Lowest)";
      case 6 -> "Total Bill (Lowest to Highest)";
      default -> "Confirmation Number (Ascending)";
    };
  }

  private void printOperationalReport(String categoryFilter, String statusFilter,
          boolean useMinBill, double minBill, boolean useMinDays, int minDays, String searchKeyword, int sortChoice) {

    // 1. Search & Filter from BST
    List<Reservation> filteredList = new ArrayList<>();
    Iterator<Reservation> it = reservationTree.getInorderIterator();

    int countCheckedIn = 0;
    int countReserved = 0;
    int countCheckedOut = 0;
    int countOthers = 0;
    double totalRevenue = 0.0;
    int totalStayDays = 0;

    while (it.hasNext()) {
      Reservation r = it.next();

      // Filter: Category
      boolean matchCategory = categoryFilter.isEmpty() 
          || r.getRoomCategory().equalsIgnoreCase(categoryFilter)
          || (categoryFilter.equalsIgnoreCase("Standard") && r.getRoomCategory().toLowerCase().contains("standard"))
          || (categoryFilter.equalsIgnoreCase("Deluxe") && r.getRoomCategory().toLowerCase().contains("deluxe"))
          || (categoryFilter.equalsIgnoreCase("Suite") && r.getRoomCategory().toLowerCase().contains("suite"));

      // Filter: Status
      boolean matchStatus = statusFilter.isEmpty() || r.getStatus().equalsIgnoreCase(statusFilter);

      // Filter: Min Bill & Min Days
      boolean matchMinBill = !useMinBill || r.getTotalBillAmount() >= minBill;
      boolean matchMinDays = !useMinDays || r.getStayDurationDays() >= minDays;

      // Filter: Keyword Search in Guest Name (Linear Search technique)
      boolean matchSearch = searchKeyword.isEmpty() 
          || (r.getGuestName() != null && r.getGuestName().toLowerCase().contains(searchKeyword.toLowerCase()))
          || (r.getConfirmationNumber() != null && r.getConfirmationNumber().contains(searchKeyword));

      if (matchCategory && matchStatus && matchMinBill && matchMinDays && matchSearch) {
        filteredList.add(r);
        totalRevenue += r.getTotalBillAmount();
        totalStayDays += r.getStayDurationDays();

        String s = r.getStatus().toUpperCase();
        if (s.contains("CHECKED-IN")) countCheckedIn++;
        else if (s.contains("RESERVED")) countReserved++;
        else if (s.contains("CHECKED-OUT")) countCheckedOut++;
        else countOthers++;
      }
    }

    // 2. Apply Custom Sorting Algorithm (Insertion Sort)
    sortReservations(filteredList, sortChoice);

    // 3. Render Formatted Output
    StringBuilder sb = new StringBuilder();
    sb.append("\n=========================================================================================================================\n");
    sb.append("                                   TARUMT RESORTS - FRONT DESK OPERATIONAL REPORT                                        \n");
    sb.append("=========================================================================================================================\n");
    sb.append(String.format(" Generated On: %-25s | Category: %-15s | Status: %-15s\n",
        LocalDate.now().toString(),
        categoryFilter.isEmpty() ? "ALL" : categoryFilter,
        statusFilter.isEmpty() ? "ALL" : statusFilter));
    sb.append(String.format(" Search Term : %-25s | Min Bill: %-15s | Min Stay: %-15s\n",
        searchKeyword.isEmpty() ? "None" : "'" + searchKeyword + "'",
        useMinBill ? "RM " + String.format("%.2f", minBill) : "None",
        useMinDays ? minDays + " Day(s)" : "None"));
    sb.append(String.format(" Sort Order  : %s\n", getReport1SortLabel(sortChoice)));
    sb.append("=========================================================================================================================\n");
    sb.append(String.format("%-12s %-18s %-18s %-9s %-12s %-12s %-6s %-14s %-12s\n",
        "Conf. #", "Guest Name", "Room Category", "Room No", "Check-In", "Check-Out", "Nights", "Total Bill", "Status"));
    sb.append("-------------------------------------------------------------------------------------------------------------------------\n");

    if (filteredList.isEmpty()) {
      sb.append("                              [ NOTICE ] No reservation records matched the specified filter criteria.                   \n");
    } else {
      for (Reservation r : filteredList) {
        String inDate = (r.getCheckInDate() != null && !r.getCheckInDate().isEmpty()) ? r.getCheckInDate() : "-";
        String outDate = (r.getCheckOutDate() != null && !r.getCheckOutDate().isEmpty()) ? r.getCheckOutDate() : "-";
        sb.append(String.format("%-12s %-18s %-18s %-9s %-12s %-12s %5d   RM %10.2f  %-12s\n",
            r.getConfirmationNumber(),
            r.getGuestName(),
            r.getRoomCategory(),
            r.getRoomNumber() != null ? r.getRoomNumber() : "-",
            inDate,
            outDate,
            r.getStayDurationDays(),
            r.getTotalBillAmount(),
            r.getStatus()
        ));
      }
    }

    sb.append("=========================================================================================================================\n");
    sb.append("                                            OPERATIONAL METRICS & SUMMARY                                                \n");
    sb.append("-------------------------------------------------------------------------------------------------------------------------\n");
    double avgBill = filteredList.isEmpty() ? 0.0 : totalRevenue / filteredList.size();
    double avgStay = filteredList.isEmpty() ? 0.0 : (double) totalStayDays / filteredList.size();

    sb.append(String.format(" Total Records Displayed : %-10d | Total Filtered Revenue : RM %12.2f\n", filteredList.size(), totalRevenue));
    sb.append(String.format(" Average Bill / Guest    : RM %-7.2f | Average Stay Duration  : %.1f Night(s)\n", avgBill, avgStay));
    sb.append("-------------------------------------------------------------------------------------------------------------------------\n");
    sb.append(String.format(" Status Breakdown        : Checked-In: %d | Reserved: %d | Checked-Out: %d | Maintenance/Other: %d\n",
        countCheckedIn, countReserved, countCheckedOut, countOthers));
    sb.append("=========================================================================================================================\n");

    System.out.println(sb.toString());
  }

  /**
   * Custom Insertion Sort algorithm demonstration on List of Reservations.
   */
  private void sortReservations(List<Reservation> list, int sortChoice) {
    for (int i = 1; i < list.size(); i++) {
      Reservation current = list.get(i);
      int j = i - 1;
      while (j >= 0 && compareReservations(list.get(j), current, sortChoice) > 0) {
        list.set(j + 1, list.get(j));
        j--;
      }
      list.set(j + 1, current);
    }
  }

  private int compareReservations(Reservation a, Reservation b, int sortChoice) {
    return switch (sortChoice) {
      case 1 -> a.getConfirmationNumber().compareToIgnoreCase(b.getConfirmationNumber());
      case 2 -> b.getConfirmationNumber().compareToIgnoreCase(a.getConfirmationNumber());
      case 3 -> a.getGuestName().compareToIgnoreCase(b.getGuestName());
      case 4 -> Integer.compare(b.getStayDurationDays(), a.getStayDurationDays());
      case 5 -> Double.compare(b.getTotalBillAmount(), a.getTotalBillAmount());
      case 6 -> Double.compare(a.getTotalBillAmount(), b.getTotalBillAmount());
      default -> a.getConfirmationNumber().compareToIgnoreCase(b.getConfirmationNumber());
    };
  }

  // =========================================================================
  // REPORT 2: BUSINESS CYCLE REVENUE & ROOM CATEGORY PERFORMANCE ANALYTICS
  // =========================================================================

  private static class CategoryPerformanceMetric {
    String categoryName;
    int reservationsCount;
    int roomNightsSold;
    double totalRevenue;
    double adr; // Average Daily Rate
    double revenueShare;
    double avgStayLength;

    public CategoryPerformanceMetric(String categoryName) {
      this.categoryName = categoryName;
      this.reservationsCount = 0;
      this.roomNightsSold = 0;
      this.totalRevenue = 0.0;
      this.adr = 0.0;
      this.revenueShare = 0.0;
      this.avgStayLength = 0.0;
    }
  }

  public void generateBusinessCycleAnalyticsReport() {
    LocalDate startDate = null;
    LocalDate endDate = null;
    String categoryFilter = "";
    double minRevenue = 0.0;
    int sortMetric = 1; // 1: Total Revenue Descending

    int choice;
    do {
      String metricLabel = getBusinessCycleSortLabel(sortMetric);
      choice = frontDeskUI.getBusinessCycleReportChoice(startDate, endDate, categoryFilter, minRevenue, metricLabel);

      switch (choice) {
        case 0 -> {
          return;
        }
        case 1 -> {
          startDate = frontDeskUI.inputReportDate("  Enter Business Cycle Start Date (YYYY-MM-DD) [or press ENTER for none]: ");
          if (startDate != null) {
            endDate = frontDeskUI.inputReportDate("  Enter Business Cycle End Date (YYYY-MM-DD) [or press ENTER for none]: ");
            if (endDate != null && endDate.isBefore(startDate)) {
              System.out.println("  [!] End date cannot be before start date. Dates reset.");
              startDate = null;
              endDate = null;
            }
          }
        }
        case 2 -> categoryFilter = frontDeskUI.inputCategoryFilter();
        case 3 -> minRevenue = frontDeskUI.inputMinBill();
        case 4 -> sortMetric = frontDeskUI.getBusinessCycleSortChoice();
        case 5 -> {
          printBusinessCycleReport(startDate, endDate, categoryFilter, minRevenue, sortMetric);
          return;
        }
        case 6 -> {
          startDate = null;
          endDate = null;
          categoryFilter = "";
          minRevenue = 0.0;
          sortMetric = 1;
          System.out.println("  [✔] Business cycle analytics parameters reset.");
        }
        default -> MessageUI.displayInvalidChoiceMessage();
      }
    } while (true);
  }

  private String getBusinessCycleSortLabel(int choice) {
    return switch (choice) {
      case 1 -> "Total Revenue (Highest to Lowest)";
      case 2 -> "Room Nights Sold (Highest to Lowest)";
      case 3 -> "Booking Volume (Highest to Lowest)";
      case 4 -> "Average Daily Rate / ADR (Highest to Lowest)";
      default -> "Total Revenue (Highest to Lowest)";
    };
  }

  private void printBusinessCycleReport(LocalDate startDate, LocalDate endDate, String categoryFilter, double minRevenue, int sortMetric) {
    // 1. Initialize category map with standard master categories
    Map<String, CategoryPerformanceMetric> metricMap = new HashMap<>();
    String[] masterCategories = {
        "Standard Single", "Standard Double", "Deluxe Suite", "Executive Suite", "Presidential Suite"
    };
    for (String cat : masterCategories) {
      metricMap.put(cat.toLowerCase(), new CategoryPerformanceMetric(cat));
    }

    // 2. Search & Aggregate data from reservationTree
    Iterator<Reservation> it = reservationTree.getInorderIterator();
    double hotelGrossRevenue = 0.0;
    int hotelTotalNights = 0;
    int hotelTotalBookings = 0;

    while (it.hasNext()) {
      Reservation r = it.next();

      // Check Business Cycle Date Range Overlap
      if (startDate != null && endDate != null) {
        LocalDate rIn = null;
        LocalDate rOut = null;
        try {
          if (r.getCheckInDate() != null && !r.getCheckInDate().isEmpty()) {
            rIn = LocalDate.parse(r.getCheckInDate());
          }
          if (r.getCheckOutDate() != null && !r.getCheckOutDate().isEmpty()) {
            rOut = LocalDate.parse(r.getCheckOutDate());
          }
        } catch (Exception ignored) {}

        if (rIn == null) rIn = LocalDate.now();
        if (rOut == null) rOut = rIn.plusDays(r.getStayDurationDays());

        boolean overlaps = !(rOut.isBefore(startDate) || rIn.isAfter(endDate));
        if (!overlaps) continue;
      }

      // Check Category Filter
      String rawCat = r.getRoomCategory() != null ? r.getRoomCategory() : "Standard Single";
      if (!categoryFilter.isEmpty()) {
        boolean match = rawCat.equalsIgnoreCase(categoryFilter)
            || (categoryFilter.equalsIgnoreCase("Standard") && rawCat.toLowerCase().contains("standard"))
            || (categoryFilter.equalsIgnoreCase("Deluxe") && rawCat.toLowerCase().contains("deluxe"))
            || (categoryFilter.equalsIgnoreCase("Suite") && rawCat.toLowerCase().contains("suite"));
        if (!match) continue;
      }

      // Map to category metric
      String key = rawCat.toLowerCase();
      if (!metricMap.containsKey(key)) {
        // Fallback for legacy categories
        if (key.contains("presidential")) key = "presidential suite";
        else if (key.contains("executive") || key.equals("suite")) key = "executive suite";
        else if (key.contains("deluxe")) key = "deluxe suite";
        else if (key.contains("double")) key = "standard double";
        else key = "standard single";
      }

      CategoryPerformanceMetric metric = metricMap.get(key);
      metric.reservationsCount++;
      metric.roomNightsSold += r.getStayDurationDays();
      metric.totalRevenue += r.getTotalBillAmount();

      hotelGrossRevenue += r.getTotalBillAmount();
      hotelTotalNights += r.getStayDurationDays();
      hotelTotalBookings++;
    }

    // 3. Compute KPI ratios and prepare list
    List<CategoryPerformanceMetric> results = new ArrayList<>();
    for (CategoryPerformanceMetric m : metricMap.values()) {
      if (m.totalRevenue >= minRevenue && (categoryFilter.isEmpty() || m.reservationsCount > 0)) {
        m.adr = m.roomNightsSold > 0 ? m.totalRevenue / m.roomNightsSold : 0.0;
        m.revenueShare = hotelGrossRevenue > 0 ? (m.totalRevenue / hotelGrossRevenue) * 100.0 : 0.0;
        m.avgStayLength = m.reservationsCount > 0 ? (double) m.roomNightsSold / m.reservationsCount : 0.0;
        results.add(m);
      }
    }

    // 4. Sort results using custom sorting algorithm
    sortCategoryMetrics(results, sortMetric);

    // 5. Render Structured Management Report
    StringBuilder sb = new StringBuilder();
    sb.append("\n===================================================================================================================\n");
    sb.append("                           TARUMT RESORTS - BUSINESS CYCLE REVENUE & PERFORMANCE REPORT                            \n");
    sb.append("===================================================================================================================\n");
    String periodStr = (startDate != null && endDate != null) ? startDate + " to " + endDate : "ALL HISTORICAL CYCLES";
    sb.append(String.format(" Business Cycle Period : %-30s | Generated On    : %s\n", periodStr, LocalDate.now().toString()));
    sb.append(String.format(" Category Scope        : %-30s | Sort Metric     : %s\n",
        categoryFilter.isEmpty() ? "ALL ROOM CATEGORIES" : categoryFilter, getBusinessCycleSortLabel(sortMetric)));
    sb.append("===================================================================================================================\n");
    sb.append(String.format("%-24s %-15s %-15s %-18s %-16s %-14s %-12s\n",
        "Room Category", "Reservations", "Nights Sold", "Total Revenue", "ADR (RM/Night)", "Rev. Share", "Avg. Stay"));
    sb.append("-------------------------------------------------------------------------------------------------------------------\n");

    CategoryPerformanceMetric topRevenueCat = null;
    CategoryPerformanceMetric topVolumeCat = null;

    for (CategoryPerformanceMetric m : results) {
      if (topRevenueCat == null || m.totalRevenue > topRevenueCat.totalRevenue) topRevenueCat = m;
      if (topVolumeCat == null || m.reservationsCount > topVolumeCat.reservationsCount) topVolumeCat = m;

      sb.append(String.format("%-24s %-15d %-15d RM %13.2f   RM %11.2f     %6.1f %%     %4.1f Days\n",
          m.categoryName,
          m.reservationsCount,
          m.roomNightsSold,
          m.totalRevenue,
          m.adr,
          m.revenueShare,
          m.avgStayLength
      ));
    }

    double overallHotelADR = hotelTotalNights > 0 ? hotelGrossRevenue / hotelTotalNights : 0.0;
    double overallAvgStay = hotelTotalBookings > 0 ? (double) hotelTotalNights / hotelTotalBookings : 0.0;

    sb.append("===================================================================================================================\n");
    sb.append("                                        EXECUTIVE FINANCIAL & OPERATIONAL KPIS                                     \n");
    sb.append("-------------------------------------------------------------------------------------------------------------------\n");
    sb.append(String.format(" Gross Business Cycle Revenue : RM %-15.2f | Total Room Nights Sold       : %d\n", hotelGrossRevenue, hotelTotalNights));
    sb.append(String.format(" Total Cycle Reservations     : %-18d | Overall Hotel ADR (Daily Rate): RM %.2f\n", hotelTotalBookings, overallHotelADR));
    sb.append(String.format(" Overall Average Stay Length  : %-18.1f | Total Categories Analyzed     : %d\n", overallAvgStay, results.size()));
    sb.append("-------------------------------------------------------------------------------------------------------------------\n");
    if (topRevenueCat != null && topRevenueCat.totalRevenue > 0) {
      sb.append(String.format(" Top Revenue Generating Category: %s (RM %.2f - %.1f%% of Total)\n",
          topRevenueCat.categoryName, topRevenueCat.totalRevenue, topRevenueCat.revenueShare));
    }
    if (topVolumeCat != null && topVolumeCat.reservationsCount > 0) {
      sb.append(String.format(" Most Popular Category by Volume: %s (%d Reservations, %d Nights Sold)\n",
          topVolumeCat.categoryName, topVolumeCat.reservationsCount, topVolumeCat.roomNightsSold));
    }
    sb.append("===================================================================================================================\n");
    sb.append("                                   MANAGEMENT INSIGHTS & STRATEGIC RECOMMENDATIONS                                 \n");
    sb.append("-------------------------------------------------------------------------------------------------------------------\n");
    if (topRevenueCat != null && topRevenueCat.categoryName.contains("Suite")) {
      sb.append(" * Suite categories are driving the majority of resort revenue. Consider offering weekend premium package upgrades.\n");
    } else {
      sb.append(" * Standard room categories represent steady base volume. Explore seasonal promotions to boost suite occupancy.\n");
    }
    sb.append(" * Utilize ADR trends to dynamically adjust weekend vs. weekday pricing for high-demand business cycles.\n");
    sb.append("===================================================================================================================\n");

    System.out.println(sb.toString());
  }

  /**
   * Custom Insertion Sort algorithm demonstration for Category Performance Metrics.
   */
  private void sortCategoryMetrics(List<CategoryPerformanceMetric> list, int sortMetric) {
    for (int i = 1; i < list.size(); i++) {
      CategoryPerformanceMetric current = list.get(i);
      int j = i - 1;
      while (j >= 0 && compareCategoryMetrics(list.get(j), current, sortMetric) > 0) {
        list.set(j + 1, list.get(j));
        j--;
      }
      list.set(j + 1, current);
    }
  }

  private int compareCategoryMetrics(CategoryPerformanceMetric a, CategoryPerformanceMetric b, int sortMetric) {
    return switch (sortMetric) {
      case 1 -> Double.compare(b.totalRevenue, a.totalRevenue); // Descending
      case 2 -> Integer.compare(b.roomNightsSold, a.roomNightsSold); // Descending
      case 3 -> Integer.compare(b.reservationsCount, a.reservationsCount); // Descending
      case 4 -> Double.compare(b.adr, a.adr); // Descending
      default -> Double.compare(b.totalRevenue, a.totalRevenue);
    };
  }
  
  private void normalizeReservationStatuses() {
    LocalDate today = LocalDate.now();
    boolean updated = false;
    Iterator<Reservation> it = reservationTree.getInorderIterator();
    List<Reservation> toUpdate = new ArrayList<>();
    
    while (it.hasNext()) {
        Reservation r = it.next();
        String currentStatus = r.getStatus();
        String inDateStr = r.getCheckInDate();
        String outDateStr = r.getCheckOutDate();
        
        LocalDate inDate = null, outDate = null;
        try {
            if (inDateStr != null && !inDateStr.isEmpty()) inDate = LocalDate.parse(inDateStr);
        } catch (Exception ignored) {}
        try {
            if (outDateStr != null && !outDateStr.isEmpty()) outDate = LocalDate.parse(outDateStr);
        } catch (Exception ignored) {}
        
        // Future check‑in → should be Reserved (unless already Checked‑Out)
        if (inDate != null && inDate.isAfter(today)) {
            if (!"Reserved".equalsIgnoreCase(currentStatus) && !"Checked-Out".equalsIgnoreCase(currentStatus)) {
                r.setStatus("Reserved");
                updated = true;
            }
        }
        // Past check‑out → should be Checked‑Out (unless Maintenance)
        if (outDate != null && outDate.isBefore(today)) {
            if (!"Checked-Out".equalsIgnoreCase(currentStatus) && !"Maintenance".equalsIgnoreCase(currentStatus)) {
                r.setStatus("Checked-Out");
                updated = true;
            }
        }
        // Optional: if today is between check‑in and check‑out and status is Reserved, you could set to Checked‑In,
        // but we leave it as is to avoid auto‑check‑in before guest arrives.
    }
    
    if (updated) {
        FileHandler.saveReservations(reservationTree);
        System.out.println("  [System] Reservation statuses normalized based on current date.");
    }
}
}
