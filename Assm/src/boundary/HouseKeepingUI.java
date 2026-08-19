package boundary;

import control.HouseKeepingControl;
import entities.HousekeepingTask;
import entities.Staff;

import java.util.Scanner;

public class HouseKeepingUI {

    public static void main(String[] args) {

        HouseKeepingControl manager =
                new HouseKeepingControl();

        Scanner scanner = new Scanner(System.in);

        // =====================================================
        // HARD-CODED STAFF
        // =====================================================

        Staff staff1 =
                new Staff(
                        "S001",
                        "John",
                        "john@email.com",
                        "Morning",
                        "Housekeeper"
                );

        Staff staff2 =
                new Staff(
                        "S002",
                        "Mary",
                        "mary@email.com",
                        "Afternoon",
                        "Housekeeper"
                );

        // =====================================================
        // HARD-CODED INITIAL TASKS
        // =====================================================

        HousekeepingTask task1 =
                new HousekeepingTask(
                        null,
                        "R101",
                        "Room Cleaning",
                        "Dirty",
                        staff1.getStaffID()
                );

        HousekeepingTask task2 =
                new HousekeepingTask(
                        null,
                        "R901",
                        "Room Cleaning",
                        "Dirty",
                        staff2.getStaffID()
                );

        manager.addTask(task1);
        manager.addTask(task2);

        int choice;

        // =====================================================
        // MENU
        // =====================================================

        do {

            System.out.println(
                    "\n========================================"
            );

            System.out.println(
                    "   HOUSEKEEPING STATUS MANAGEMENT"
            );

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "1. Add New Housekeeping Task"
            );

            System.out.println(
                    "2. View Current Room Status"
            );

            System.out.println(
                    "3. Update Room Status"
            );

            System.out.println(
                    "4. Rollback Last Status"
            );

            System.out.println(
                    "5. Display Status History"
            );

            System.out.println(
                    "6. Room Checked-In"
            );

            System.out.println(
                    "7. Exit"
            );

            System.out.println(
                    "========================================"
            );

            System.out.print(
                    "Enter your choice: "
            );

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                // =================================================
                // 1. ADD NEW HOUSEKEEPING TASK
                // =================================================

                case 1:

                    System.out.println(
                            "\n===== ADD NEW HOUSEKEEPING TASK ====="
                    );

                    System.out.print(
                            "Enter Room: "
                    );

                    String location =
                            scanner.nextLine();

                    // Task name is automatically set
                    String taskName =
                            "Room Cleaning";

                    // Every new task starts as Dirty
                    String status =
                            "Dirty";

                    // ---------------------------------------------
                    // SELECT STAFF
                    // ---------------------------------------------

                    System.out.println(
                            "\nAvailable Staff:"
                    );

                    System.out.println(
                            "1. "
                            + staff1.getStaffID()
                            + " - "
                            + staff1.getStaffName()
                    );

                    System.out.println(
                            "2. "
                            + staff2.getStaffID()
                            + " - "
                            + staff2.getStaffName()
                    );

                    System.out.print(
                            "Select Staff: "
                    );

                    int staffChoice =
                            scanner.nextInt();

                    scanner.nextLine();

                    String assignedStaffID;

                    if (staffChoice == 1) {

                        assignedStaffID =
                                staff1.getStaffID();

                    } else if (staffChoice == 2) {

                        assignedStaffID =
                                staff2.getStaffID();

                    } else {

                        System.out.println(
                                "Invalid staff choice."
                        );

                        break;
                    }

                    // ---------------------------------------------
                    // CREATE NEW TASK
                    // ---------------------------------------------

                    HousekeepingTask newTask =
                            new HousekeepingTask(
                                    null,
                                    location,
                                    taskName,
                                    status,
                                    assignedStaffID
                            );

                    manager.addTask(newTask);

                    break;

                // =================================================
                // 2. VIEW CURRENT ROOM STATUS
                // =================================================

                case 2:

                    System.out.println(
                            "\n===== CURRENT ROOM STATUS ====="
                    );

                    manager.displayCurrentRoomStatus();

                    break;

                // =================================================
                // 3. UPDATE ROOM STATUS
                // =================================================

                case 3:

                    System.out.println(
                            "\n===== UPDATE ROOM STATUS ====="
                    );

                    HousekeepingTask current =
                            manager.viewCurrentStatus();

                    if (current == null) {

                        System.out.println(
                                "No housekeeping task available."
                        );

                        break;
                    }

                    // ---------------------------------------------
                    // CURRENT TASK INFORMATION
                    // ---------------------------------------------

                    System.out.println(
                            "\n===== CURRENT TASK ====="
                    );

                    System.out.println(
                            "Task ID        : "
                            + current.getTaskID()
                    );

                    System.out.println(
                            "Room           : "
                            + current.getLocation()
                    );

                    System.out.println(
                            "Task           : "
                            + current.getTaskName()
                    );

                    // ---------------------------------------------
                    // FIND ASSIGNED STAFF
                    // ---------------------------------------------

                    Staff assignedStaff =
                            findStaff(
                                    current.getAssignedStaffID(),
                                    staff1,
                                    staff2
                            );

                    if (assignedStaff != null) {

                        System.out.println(
                                "Staff ID       : "
                                + assignedStaff.getStaffID()
                        );

                        System.out.println(
                                "Staff Name     : "
                                + assignedStaff.getStaffName()
                        );

                        System.out.println(
                                "Email          : "
                                + assignedStaff.getEmail()
                        );

                        System.out.println(
                                "Shift          : "
                                + assignedStaff.getShift()
                        );

                        System.out.println(
                                "Position       : "
                                + assignedStaff.getPosition()
                        );

                    } else {

                        System.out.println(
                                "Assigned Staff : "
                                + current.getAssignedStaffID()
                        );
                    }

                    System.out.println(
                            "Current Status : "
                            + current.getStatus()
                    );

                    // ---------------------------------------------
                    // SELECT NEW STATUS
                    // ---------------------------------------------

                    System.out.println(
                            "\nSelect New Status:"
                    );

                    System.out.println(
                            "1. Dirty"
                    );

                    System.out.println(
                            "2. Cleaning In Progress"
                    );

                    System.out.println(
                            "3. Inspected"
                    );

                    System.out.println(
                            "4. Ready for Check-In"
                    );

                    System.out.print(
                            "Enter choice: "
                    );

                    int updateChoice =
                            scanner.nextInt();

                    scanner.nextLine();

                    String newStatus = null;

                    switch (updateChoice) {

                        case 1:

                            newStatus =
                                    "Dirty";

                            break;

                        case 2:

                            newStatus =
                                    "Cleaning In Progress";

                            break;

                        case 3:

                            newStatus =
                                    "Inspected";

                            break;

                        case 4:

                            newStatus =
                                    "Ready for Check-In";

                            break;

                        default:

                            System.out.println(
                                    "Invalid status choice."
                            );

                            break;
                    }

                    // ---------------------------------------------
                    // UPDATE STATUS
                    // ---------------------------------------------

                    if (newStatus != null) {

                        manager.updateStatus(
                                newStatus
                        );
                    }

                    break;

                // =================================================
                // 4. ROLLBACK LAST STATUS
                // =================================================

                case 4:

                    System.out.println(
                            "\n===== ROLLBACK LAST STATUS ====="
                    );

                    manager.rollbackStatus();

                    break;

                // =================================================
                // 5. DISPLAY STATUS HISTORY
                // =================================================

                case 5:

                    System.out.println(
                            "\n===== STATUS HISTORY ====="
                    );

                    manager.displayStatusHistory();

                    break;

                // =================================================
                // 6. ROOM CHECKED-IN
                // =================================================

                case 6:

                    System.out.println(
                            "\n===== ROOM CHECKED-IN ====="
                    );

                    manager.completeHousekeepingCycle();

                    break;

                // =================================================
                // 7. EXIT
                // =================================================

                case 7:

                    System.out.println(
                            "\nThank you for using the "
                            + "Housekeeping Status Management System."
                    );

                    break;

                // =================================================
                // INVALID CHOICE
                // =================================================

                default:

                    System.out.println(
                            "\nInvalid choice. "
                            + "Please enter 1-7."
                    );

                    break;
            }

        } while (choice != 7);

        scanner.close();
    }

    // =========================================================
    // FIND STAFF BY STAFF ID
    // =========================================================

    private static Staff findStaff(
            String staffID,
            Staff staff1,
            Staff staff2) {

        if (staff1.getStaffID().equals(staffID)) {

            return staff1;
        }

        if (staff2.getStaffID().equals(staffID)) {

            return staff2;
        }

        return null;
    }
}
