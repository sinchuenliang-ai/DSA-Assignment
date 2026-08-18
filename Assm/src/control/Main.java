import control.HousekeepingTaskManager;
import Entities.HousekeepingTask;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        HousekeepingTaskManager manager =
                new HousekeepingTaskManager();

        Scanner scanner = new Scanner(System.in);

        // =====================================================
        // HARD-CODED INITIAL TASK
        // =====================================================

        HousekeepingTask task1 =
                new HousekeepingTask(
                        "T001",
                        "Room 1001",
                        "Room Cleaning",
                        "Dirty"
                );

        manager.addTask(task1);

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
            manager.completeHousekeepingCycle();

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
                // 1. ADD NEW TASK
                // =================================================

                case 1:

                    System.out.println(
                            "\n===== ADD NEW HOUSEKEEPING TASK ====="
                    );

                    System.out.print(
                            "Enter Location: "
                    );

                    String location =
                            scanner.nextLine();

                    System.out.print(
                            "Enter Task Name: "
                    );

                    String taskName =
                            scanner.nextLine();

                    System.out.println(
                            "\nSelect Initial Status:"
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

                    int statusChoice =
                            scanner.nextInt();

                    scanner.nextLine();

                    String status;

                    switch (statusChoice) {

                        case 1:
                            status = "Dirty";
                            break;

                        case 2:
                            status =
                                    "Cleaning In Progress";
                            break;

                        case 3:
                            status = "Inspected";
                            break;

                        case 4:
                            status =
                                    "Ready for Check-In";
                            break;

                        default:

                            System.out.println(
                                    "Invalid status choice."
                            );

                            continue;
                    }

                    HousekeepingTask newTask =
                            new HousekeepingTask(null,location,taskName,status);

                    manager.addTask(newTask);

                    break;

                // =================================================
                // 2. VIEW CURRENT STATUS
                // =================================================

                case 2:

                    System.out.println(
                            "\n===== CURRENT ROOM STATUS ====="
                    );

                    HousekeepingTask currentTask =
                            manager.viewCurrentStatus();

                    if (currentTask != null) {

                        System.out.println(
                                currentTask
                        );
                    }

                    break;

                // =================================================
                // 3. UPDATE STATUS
                // =================================================

                case 3:

                    System.out.println(
                            "\n===== UPDATE ROOM STATUS ====="
                    );

                    System.out.println(
                            "Current Status:"
                    );

                    HousekeepingTask current =
                            manager.viewCurrentStatus();

                    if (current == null) {
                        break;
                    }

                    System.out.println(
                            current.getStatus()
                    );

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

                    String newStatus;

                    switch (updateChoice) {

                        case 1:
                            newStatus = "Dirty";
                            break;

                        case 2:
                            newStatus =
                                    "Cleaning In Progress";
                            break;

                        case 3:
                            newStatus = "Inspected";
                            break;

                        case 4:
                            newStatus =
                                    "Ready for Check-In";
                            break;

                        default:

                            System.out.println(
                                    "Invalid status choice."
                            );

                            continue;
                    }

                    manager.updateStatus(
                            newStatus
                    );

                    break;

                // =================================================
                // 4. ROLLBACK
                // =================================================

                case 4:

                    System.out.println(
                            "\n===== ROLLBACK LAST STATUS ====="
                    );

                    manager.rollbackStatus();

                    break;

                // =================================================
                // 5. DISPLAY HISTORY
                // =================================================

                case 5:

                    manager.displayStatusHistory();

                    break;

                // =================================================
                // 6. EXIT
                // =================================================

                case 6:

                    System.out.println("\n===== DELETE / COMPLETE CURRENT TASK =====");

                    manager.deleteCurrentTask();

                    break;

                default:

                    System.out.println(
                            "\nInvalid choice."
                    );
                    
                case 7:

                System.out.println(
                        "\nThank you for using the Housekeeping Status Management System."
                );

                break;
                        }

                    } while (choice !=7);

                    scanner.close();
                }
}