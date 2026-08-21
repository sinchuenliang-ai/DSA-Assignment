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
            System.out.println("1. Add New Housekeeping Task");
            System.out.println("2. View Current Room Status");
            System.out.println("3. Update Room Status");
            System.out.println("4. Rollback Last Status");
            System.out.println("5. Display Status History");
            System.out.println("6. Room Checked-In (Complete Cycle)");
            System.out.println("7. Exit");
            System.out.println("========================================");
            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {

                // =================================================
                // 1. ADD NEW HOUSEKEEPING TASK
                // =================================================
                case 1:
                    System.out.println("\n===== ADD NEW HOUSEKEEPING TASK =====");

                    // Show master hotel room list with housekeeping status
                    List<Room> allRooms = FileHandler.loadAllHotelRooms();
                    Map<String, HousekeepingTask> activeHkMap = manager.getAllActiveTasksMap();

                    System.out.println("\n--- Available Hotel Rooms ---");
                    System.out.printf("%-5s %-10s %-20s %-10s %-25s\n",
                            "No.", "Room No.", "Room Type", "Inventory", "Housekeeping Status");
                    System.out.println("--------------------------------------------------------------------------");

                    int idx = 0;
                    for (Room r : allRooms) {
                        idx++;
                        HousekeepingTask hkTask = activeHkMap.get(r.getRoomNumber().toUpperCase());
                        String hkStatus = hkTask != null ? hkTask.getStatus() : "-";
                        System.out.printf("%-5d %-10s %-20s %-10s %-25s\n",
                                idx, r.getRoomNumber(), r.getRoomType(), r.getRoomStatus(), hkStatus);
                    }
                    System.out.println("--------------------------------------------------------------------------");

                    System.out.print("Select Room Number (e.g. A-001) or type 0 to cancel: ");
                    String location = scanner.nextLine().trim();

                    if (location.equals("0") || location.isEmpty()) {
                        System.out.println("Cancelled.");
                        break;
                    }

                    // Validate room exists in hotel
                    boolean roomExists = allRooms.stream()
                            .anyMatch(r -> r.getRoomNumber().equalsIgnoreCase(location));
                    if (!roomExists) {
                        System.out.println("  [!] Warning: Room '" + location + "' not found in master hotel room list.");
                        System.out.print("Continue anyway? (Y/N): ");
                        String confirm = scanner.nextLine().trim();
                        if (!confirm.equalsIgnoreCase("Y")) {
                            System.out.println("Cancelled.");
                            break;
                        }
                    }

                    String taskName = "Room Cleaning";

                    // SELECT STAFF
                    if (staffList.isEmpty()) {
                        System.out.println("No staff found in staff directory.");
                        break;
                    }

                    System.out.println("\nAvailable Staff:");
                    for (int i = 0; i < staffList.size(); i++) {
                        Staff s = staffList.get(i);
                        System.out.println((i + 1) + ". " + s.getStaffID() + " - " + s.getStaffName() + " (" + s.getShift() + " Shift)");
                    }

                    System.out.print("Select Staff (1-" + staffList.size() + "): ");
                    int staffChoice;
                    try {
                        staffChoice = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        staffChoice = -1;
                    }

                    if (staffChoice < 1 || staffChoice > staffList.size()) {
                        System.out.println("Invalid staff choice.");
                        break;
                    }

                    String assignedStaffID = staffList.get(staffChoice - 1).getStaffID();

                    // CREATE NEW TASK
                    HousekeepingTask newTask = new HousekeepingTask(null, location, taskName, "Dirty", assignedStaffID);
                    manager.addTask(newTask);
                    break;

                // =================================================
                // 2. VIEW CURRENT ROOM STATUS
                // =================================================
                case 2:
                    System.out.println("\n===== CURRENT ROOM STATUS =====");
                    manager.displayCurrentRoomStatus();
                    break;

                // =================================================
                // 3. UPDATE ROOM STATUS
                // =================================================
                case 3:
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
                // 4. ROLLBACK LAST STATUS
                // =================================================
                case 4:
                    System.out.println("\n===== ROLLBACK LAST STATUS =====");
                    manager.rollbackStatus();
                    break;

                // =================================================
                // 5. DISPLAY STATUS HISTORY
                // =================================================
                case 5:
                    System.out.println("\n===== STATUS HISTORY =====");
                    manager.displayStatusHistory();
                    break;

                // =================================================
                // 6. ROOM CHECKED-IN (Complete cycle)
                // =================================================
                case 6:
                    System.out.println("\n===== ROOM CHECKED-IN =====");
                    manager.completeHousekeepingCycle();
                    break;

                // =================================================
                // 7. EXIT
                // =================================================
                case 7:
                    System.out.println("\nThank you for using the Housekeeping Status Management System.");
                    break;

                // =================================================
                // INVALID CHOICE
                // =================================================
                default:
                    System.out.println("\nInvalid choice. Please enter 1-7.");
                    break;
            }

        } while (choice != 7);
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
