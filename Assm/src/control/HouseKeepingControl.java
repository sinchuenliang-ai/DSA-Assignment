package control;

import adt.ArrayStack;
import adt.StackInterface;
import Entities.HousekeepingTask;

public class HouseKeepingControl {

    // Stack stores the status history
    private ArrayStack<HousekeepingTask> taskStack;
    private int TaskNumber = 1;

    // Constructor
    public HouseKeepingControl(){
        taskStack = new ArrayStack<>();
    }

    // =========================================================
    // VALIDATE STATUS
    // =========================================================

    private boolean isValidStatus(String status) {

        if (status == null) {
            return false;
        }

        return status.equalsIgnoreCase("Dirty")
                || status.equalsIgnoreCase("Cleaning In Progress")
                || status.equalsIgnoreCase("Inspected")
                || status.equalsIgnoreCase("Ready for Check-In");
    }

    // =========================================================
    // VALIDATE TASK DATA
    // =========================================================

    private boolean validateTask(HousekeepingTask task) {

        if (task == null) {

            System.out.println("Task cannot be null.");
            return false;
        }

        // Task ID validation
        if (task.getTaskID() == null
                || task.getTaskID().trim().isEmpty()) {

            System.out.println(
                    "Task ID cannot be empty for "
                    + task.getLocation() + "."
            );

            return false;
        }

        // Location validation
        if (task.getLocation() == null
                || task.getLocation().trim().isEmpty()) {

            System.out.println(
                    "Location cannot be empty."
            );

            return false;
        }

        // Task name validation
        if (task.getTaskName() == null
                || task.getTaskName().trim().isEmpty()) {

            System.out.println(
                    "Task name cannot be empty for "
                    + task.getLocation() + "."
            );

            return false;
        }

        // Status validation
        if (!isValidStatus(task.getStatus())) {

            System.out.println(
                    "Invalid status for "
                    + task.getLocation() + "."
            );

            System.out.println(
                    "Status must be:"
            );

            System.out.println(
                    "- Dirty"
            );

            System.out.println(
                    "- Cleaning In Progress"
            );

            System.out.println(
                    "- Inspected"
            );

            System.out.println(
                    "- Ready for Check-In"
            );

            return false;
        }

        return true;
    }

    //automatically generate taskID 
    private String generateTaskID(){
        String taskID = String.format("T%03d", TaskNumber);
        
        TaskNumber++;
        return taskID;
    }

    // ADD INITIAL HOUSEKEEPING TASK
    public void addTask(HousekeepingTask task) {

        if(task == null){
            System.out.println("task cannot be null.");
            return;
        }
        
        //Generate task ID automatically
        task.setTaskID(generateTaskID());
        
        if (!validateTask(task)) {
            return;
        }

        taskStack.push(task);

        System.out.println(
                "\nTask " + task.getTaskID()
                + " added successfully!"
        );
    }
    
    // VIEW CURRENT STATUS
    public HousekeepingTask viewCurrentStatus() {

        if (taskStack.isEmpty()) {

            System.out.println(
                    "No housekeeping task available."
            );

            return null;
        }

        return taskStack.peek();
    }
    // DISPLAY ALL CURRENT ROOM STATUS 
    // =========================================================
// DISPLAY CURRENT ROOM STATUS
// Shows ONLY the latest status of each room
// =========================================================

public void displayCurrentRoomStatus() {

    if (taskStack.isEmpty()) {

        System.out.println(
                "No housekeeping tasks in the stack."
        );

        return;
    }

    // Temporary stack used to restore the original stack
    ArrayStack<HousekeepingTask> tempStack =
            new ArrayStack<>();

    // Array to remember which Task IDs have been displayed
    String[] displayedTaskIDs =
            new String[taskStack.size()];

    int displayedCount = 0;

    System.out.println(
            "\n===== CURRENT ROOM STATUS ====="
    );

    // Go through the stack from TOP to BOTTOM
    while (!taskStack.isEmpty()) {

        HousekeepingTask task =
                taskStack.pop();

        boolean alreadyDisplayed = false;

        // Check whether this Task ID was already displayed
        for (int i = 0; i < displayedCount; i++) {

            if (displayedTaskIDs[i]
                    .equals(task.getTaskID())) {

                alreadyDisplayed = true;
                break;
            }
        }

        // Only display the FIRST record encountered
        // because TOP = latest status
        if (!alreadyDisplayed) {

            System.out.println(
                    "Task ID        : "
                    + task.getTaskID()
            );

            System.out.println(
                    "Room           : "
                    + task.getLocation()
            );

            System.out.println(
                    "Task Name      : "
                    + task.getTaskName()
            );

            System.out.println(
                    "Status         : "
                    + task.getStatus()
            );

            System.out.println(
                    "Staff ID       : "
                    + task.getAssignedStaffID()
            );

            System.out.println(
                    "----------------------------"
            );

            // Remember this Task ID
            displayedTaskIDs[displayedCount] =
                    task.getTaskID();

            displayedCount++;
        }

        // Save task temporarily
        tempStack.push(task);
    }

    // =====================================================
    // RESTORE ORIGINAL TASK STACK
    // =====================================================

    while (!tempStack.isEmpty()) {

        taskStack.push(
                tempStack.pop()
        );
    }
}

        // UPDATE STATUS
        public void updateStatus(String newStatus) {

            if (!isValidStatus(newStatus)) {

                System.out.println(
                        "Invalid status."
                );

                return;
            }

            if (taskStack.isEmpty()) {

                System.out.println(
                        "No housekeeping task available."
                );

                return;
            }

            // Get current task from TOP
            HousekeepingTask currentTask =
                    taskStack.peek();

            // Create new status record
            HousekeepingTask updatedTask =
                    new HousekeepingTask(
                            currentTask.getTaskID(),
                            currentTask.getLocation(),
                            currentTask.getTaskName(),
                            newStatus,
                            currentTask.getAssignedStaffID()
                    );

            // PUSH new status
            taskStack.push(updatedTask);

            System.out.println(
                    "\n"+ updatedTask.getLocation()
                    + " status updated to: "
                    + newStatus
            );

            if (newStatus.equalsIgnoreCase("Ready for Check-In")) {

                System.out.println(
                        "\n" + updatedTask.getLocation()
                        + " is now Ready for Check-In."
                );

                System.out.println(
                        "Housekeeping task "
                        + updatedTask.getTaskID()
                        + " has been completed."
                );

                // Remove all status records belonging
                // to this task
                String taskID =
                        currentTask.getTaskID();

                while (!taskStack.isEmpty()
                        && taskStack.peek()
                                .getTaskID()
                                .equals(taskID)) {

                    taskStack.pop();
                }

                System.out.println(
                        "Task " + taskID
                        + " removed from the active task stack."
                );

                return;
            }
        }

    // ROLLBACK LAST STATUS
    public void rollbackStatus() {

        if (taskStack.isEmpty()) {

            System.out.println(
                    "\nNo status available to rollback."
            );

            return;
        }

        // Remove the latest status
        HousekeepingTask removedTask =
                taskStack.pop();

        System.out.println(
                "Room          : "
                + removedTask.getLocation()
        );

        System.out.println(
                "Rolled Back   : "
                + removedTask.getStatus()
        );

        // Check the current status after rollback
        if (!taskStack.isEmpty()) {

            HousekeepingTask currentTask =
                    taskStack.peek();

            System.out.println(
                    "\n===== CURRENT ROOM STATUS ====="
            );

            System.out.println(
                    "Task ID             : "
                    + currentTask.getTaskID()
            );

            System.out.println(
                    "Room                : "
                    + currentTask.getLocation()
            );

            System.out.println(
                    "Task Name           : "
                    + currentTask.getTaskName()
            );

            System.out.println(
                    "Current Status      : "
                    + currentTask.getStatus()
            );

        } else {

            System.out.println(
                    "\nNo previous status available."
            );
        }
    }

    // DISPLAY STATUS HISTORY
    public void displayStatusHistory() {

        if (taskStack.isEmpty()) {

            System.out.println(
                    "No status history available."
            );

            return;
        }

        StackInterface<HousekeepingTask> tempStack =
                new ArrayStack<>();

        while (!taskStack.isEmpty()) {

            HousekeepingTask task =
                    taskStack.pop();

            System.out.println(task);

            System.out.println(
                    "----------------------------"
            );

            tempStack.push(task);
        }

        // Restore original Stack
        while (!tempStack.isEmpty()) {

            taskStack.push(
                    tempStack.pop()
            );
        }
    }

    // END HOUSEKEEPING CYCLE
    public void completeHousekeepingCycle() {

    if (taskStack.isEmpty()) {
        System.out.println(
                "No active housekeeping task."
        );
        return;
    }

    HousekeepingTask currentTask = taskStack.peek();

    if (!currentTask.getStatus()
            .equalsIgnoreCase("Ready for Check-In")) {

        System.out.println(
                "Room is not ready for check-in."
        );
        return;
    }

    String completedTaskID =
            currentTask.getTaskID();

    String completedLocation =
            currentTask.getLocation();

    System.out.println(
            "\n" + completedLocation
            + " has been checked in."
    );

    System.out.println(
            "Housekeeping task " + completedTaskID
            + " completed."
    );

    // Remove all status records belonging
    // to the top task
    while (!taskStack.isEmpty()
            && taskStack.peek()
                    .getTaskID()
                    .equals(completedTaskID)) {

        taskStack.pop();
    }

    System.out.println(
            "Task " + completedTaskID
            + " removed from the active stack."
    );
    }

    // CHECK STACK
    public boolean hasTask() {

        return !taskStack.isEmpty();
    }
}