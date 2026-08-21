package boundary;

import control.HouseKeepingControl;
import entity.HousekeepingTask;
import entity.Room;
import entity.Staff;
import utility.FileHandler;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HouseKeepingUI {

    private final HouseKeepingControl manager;
    private final Scanner scanner;
    private final List<Staff> staffList;

    public HouseKeepingUI() {
        this.manager = new HouseKeepingControl();
        this.scanner = new Scanner(System.in);
        this.staffList = FileHandler.loadStaff();
    }

    public HouseKeepingUI(HouseKeepingControl sharedControl) {
        this.manager = sharedControl;
        this.scanner = new Scanner(System.in);
        this.staffList = FileHandler.loadStaff();
    }

    public static void main(String[] args) {
        HouseKeepingUI ui = new HouseKeepingUI();
        ui.displayMenu();
    }

    public void displayMenu() {
        int choice;

        do {
            System.out.println("\n========================================");
            System.out.println("   HOUSEKEEPING STATUS MANAGEMENT");
            System.out.println("========================================");
            System.out.println("1. View Current Room Status");
            System.out.println("2. Update Room Status");
            System.out.println("3. Rollback Last Status");
            System.out.println("4. Display Status History");
            System.out.println("5. Room Checked-In (Complete Cycle)");
            System.out.println("6. Exit");
            System.out.println("========================================");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {

                // =================================================
                // 1. VIEW CURRENT ROOM STATUS
                // =================================================
                case 1:
                    System.out.println("\n===== CURRENT ROOM STATUS =====");
                    manager.displayCurrentRoomStatus();
                    break;

                // =================================================
                // 2. UPDATE ROOM STATUS
                // =================================================
                case 2:
                    System.out.println("\n===== UPDATE ROOM STATUS =====");

                    HousekeepingTask current = manager.viewCurrentStatus();

                    if (current == null) {
                        System.out.println("No housekeeping task available.");
                        break;
                    }

                    // CURRENT TASK INFORMATION
                    System.out.println("\n===== CURRENT TASK =====");
                    System.out.println("Task ID        : " + current.getTaskID());
                    System.out.println("Room           : " + current.getLocation());
                    System.out.println("Task           : " + current.getTaskName());

                    // FIND ASSIGNED STAFF
                    Staff assignedStaff = findStaff(current.getAssignedStaffID(), staffList);
                    if (assignedStaff != null) {
                        System.out.println("Staff ID       : " + assignedStaff.getStaffID());
                        System.out.println("Staff Name     : " + assignedStaff.getStaffName());
                        System.out.println("Email          : " + assignedStaff.getEmail());
                        System.out.println("Shift          : " + assignedStaff.getShift());
                        System.out.println("Position       : " + assignedStaff.getPosition());
                    } else {
                        System.out.println("Assigned Staff : " + current.getAssignedStaffID());
                    }

                    System.out.println("Current Status : " + current.getStatus());

                    // SELECT NEW STATUS
                    System.out.println("\nSelect New Status:");
                    System.out.println("1. Dirty");
                    System.out.println("2. Cleaning In Progress");
                    System.out.println("3. Inspected");
                    System.out.println("4. Ready for Check-In");
                    System.out.print("Enter choice: ");

                    int updateChoice;
                    try {
                        updateChoice = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        updateChoice = -1;
                    }

                    String newStatus = null;
                    switch (updateChoice) {
                        case 1 -> newStatus = "Dirty";
                        case 2 -> newStatus = "Cleaning In Progress";
                        case 3 -> newStatus = "Inspected";
                        case 4 -> newStatus = "Ready for Check-In";
                        default -> System.out.println("Invalid status choice.");
                    }

                    if (newStatus != null) {
                        manager.updateStatus(newStatus);
                        if ("Ready for Check-In".equalsIgnoreCase(newStatus)) {
                            System.out.println("  [ System ] Room " + current.getLocation()
                                    + " is now AVAILABLE and can be assigned to guests.");
                        }
                    }
                    break;

                // =================================================
                // 3. ROLLBACK LAST STATUS
                // =================================================
                case 3:
                    System.out.println("\n===== ROLLBACK LAST STATUS =====");
                    manager.rollbackStatus();
                    break;

                // =================================================
                // 4. DISPLAY STATUS HISTORY
                // =================================================
                case 4:
                    System.out.println("\n===== STATUS HISTORY =====");
                    manager.displayStatusHistory();
                    break;

                // =================================================
                // 5. ROOM CHECKED-IN (Complete cycle)
                // =================================================
                case 5:
                    System.out.println("\n===== ROOM CHECKED-IN =====");
                    manager.completeHousekeepingCycle();
                    break;

                // =================================================
                // 6. EXIT
                // =================================================
                case 6:
                    System.out.println("\nThank you for using the Housekeeping Status Management System.");
                    break;

                // =================================================
                // INVALID CHOICE
                // =================================================
                default:
                    System.out.println("\nInvalid choice. Please enter 1-7.");
                    break;
            }

        } while (choice != 6);
    }

    // =========================================================
    // FIND STAFF BY STAFF ID
    // =========================================================
    private static Staff findStaff(String staffID, List<Staff> staffList) {
        if (staffID == null || staffList == null) return null;
        for (Staff s : staffList) {
            if (s.getStaffID().equalsIgnoreCase(staffID)) {
                return s;
            }
        }
        return null;
    }
}