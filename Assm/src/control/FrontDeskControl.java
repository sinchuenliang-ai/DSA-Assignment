package control;

import adt.*;
import boundary.FrontDeskUI;
import entity.Reservation;
import java.util.Iterator;
import utility.MessageUI;

/**
 *
 * @author Front Desk Module
 */
public class FrontDeskControl {

  private TreeInterface<Reservation> reservationTree = new BinarySearchTreeADT<>();
  private FrontDeskUI frontDeskUI = new FrontDeskUI();

  public FrontDeskControl() {
    seedInitialData();
  }

  private void seedInitialData() {
    reservationTree.add(new Reservation("10008801", "Tan Ah Kow", "Deluxe", "D-101", 3, 450.00, "Checked-In"));
    reservationTree.add(new Reservation("10008805", "Siti Nurhaliza", "Suite", "S-202", 5, 2500.00, "Reserved"));
    reservationTree.add(new Reservation("10008802", "Alex Muthu", "Standard", "A-005", 1, 120.00, "Checked-Out"));
    reservationTree.add(new Reservation("10008809", "John Doe", "Presidential", "P-501", 7, 8400.00, "Checked-In"));
    reservationTree.add(new Reservation("10008804", "Alice Smith", "Suite", "S-201", 2, 1100.00, "Checked-In"));
  }

  public void runFrontDeskService() {
    int choice = 0;
    do {
      choice = frontDeskUI.getMenuChoice();
      switch (choice) {
        case 0:
          MessageUI.displayExitMessage();
          break;
        case 1:
          searchReservation();
          break;
        case 2:
          addNewReservation();
          break;
        case 3:
          updateReservationStatus();
          break;
        case 4:
          generateOccupancyReport();
          break;
        case 5:
          generateHighValueBillingReport();
          break;
        default:
          MessageUI.displayInvalidChoiceMessage();
      }
    } while (choice != 0);
  }

  public void searchReservation() {
    String confirmNo = frontDeskUI.inputConfirmationNumber();
    Reservation found = reservationTree.search(new Reservation(confirmNo));
    if (found != null) {
      frontDeskUI.printReservationDetails(found);
    } else {
      MessageUI.displayNotFoundMessage();
    }
  }

  public void addNewReservation() {
    Reservation newReservation = frontDeskUI.inputReservationDetails();
    if (reservationTree.search(newReservation) != null) {
      MessageUI.displayDuplicateConfirmationMessage();
      return;
    }
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
    frontDeskUI.printReservationDetails(res);

    String newRoom = frontDeskUI.inputRoomNumber();
    if (!newRoom.isEmpty()) {
      res.setRoomNumber(newRoom);
    }

    String newStatus = frontDeskUI.inputStatus();
    if (!newStatus.isEmpty()) {
      res.setStatus(newStatus);
    }

    MessageUI.displayUpdatedMessage();
  }

  public void generateOccupancyReport() {
    String statusFilter = frontDeskUI.inputStatusFilter();
    String catFilter = frontDeskUI.inputCategoryFilter();

    StringBuilder sb = new StringBuilder();
    sb.append("\n=========================================================================================\n");
    sb.append(String.format("          OCCUPANCY REPORT (Status: %s | Category: %s)\n", statusFilter, catFilter));
    sb.append("=========================================================================================\n");

    Iterator<Reservation> it = reservationTree.getInorderIterator();
    int count = 0;
    double totalRevenue = 0;

    while (it.hasNext()) {
      Reservation r = it.next();
      boolean matchStatus = statusFilter.equalsIgnoreCase("ALL") || r.getStatus().equalsIgnoreCase(statusFilter);
      boolean matchCategory = catFilter.equalsIgnoreCase("ALL") || r.getRoomCategory().equalsIgnoreCase(catFilter);

      if (matchStatus && matchCategory) {
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

  public void generateHighValueBillingReport() {
    double minBill = frontDeskUI.inputMinBill();
    int minDays = frontDeskUI.inputMinDays();

    StringBuilder sb = new StringBuilder();
    sb.append("\n=========================================================================================\n");
    sb.append(String.format("          HIGH-VALUE GUEST REPORT (Min Bill: $%.2f | Min Stay: %d Days)\n", minBill, minDays));
    sb.append("=========================================================================================\n");

    Iterator<Reservation> it = reservationTree.getInorderIterator();
    int count = 0;

    while (it.hasNext()) {
      Reservation r = it.next();
      if (r.getTotalBillAmount() >= minBill && r.getStayDurationDays() >= minDays) {
        sb.append(r.toString()).append("\n");
        count++;
      }
    }
    sb.append("-----------------------------------------------------------------------------------------\n");
    sb.append(String.format(" Total High-Value Guests Identified: %d\n", count));
    sb.append("=========================================================================================\n");
    System.out.println(sb.toString());
  }
}