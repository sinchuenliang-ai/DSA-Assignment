package control;

import adt.ArrayStack;
import adt.StackInterface;
import Entities.HousekeepingTask;

public class HousekeepingTaskManager {

    // Stack stores the status history
    private StackInterface<HousekeepingTask> taskStack;
    private int TaskNumber = -1;

    // Constructor
    public HousekeepingTaskManager() {

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
    // =========================================================
    // ADD INITIAL HOUSEKEEPING TASK
    // =========================================================

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

    // =========================================================
    // VIEW CURRENT STATUS
    // =========================================================

    public HousekeepingTask viewCurrentStatus() {

        if (taskStack.isEmpty()) {

            System.out.println(
                    "No housekeeping status available."
            );

            return null;
        }

        // TOP = current status
        return taskStack.peek();
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

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
                        newStatus
                );

        // PUSH new status
        taskStack.push(updatedTask);

        System.out.println(
                "\n"+ updatedTask.getLocation()
                + " status updated to: "
                + newStatus
        );
    }

    // =========================================================
    // ROLLBACK LAST STATUS
    // =========================================================

    public void rollbackStatus() {

        if (taskStack.isEmpty()) {

            System.out.println(
                    "No status available to rollback."
            );

            return;
        }

        // POP latest status
        HousekeepingTask removedTask =
                taskStack.pop();

        System.out.println(
                "\nRolled back status: "
                + removedTask.getStatus()
        );

        // Check previous status
        if (!taskStack.isEmpty()) {

            HousekeepingTask previousTask =
                    taskStack.peek();

            System.out.println(
                    "Current status: "
                    + previousTask.getStatus()
            );

        } else {

            System.out.println(
                    "No previous status available."
            );
        }
    }

    // =========================================================
    // DISPLAY STATUS HISTORY
    // =========================================================

    public void displayStatusHistory() {

        if (taskStack.isEmpty()) {

            System.out.println(
                    "No status history available."
            );

            return;
        }

        System.out.println(
                "\n===== STATUS HISTORY ====="
        );

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

    // =========================================================
    // END HOUSEKEEPING CYCLE
    // =========================================================

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
    
    
    public void deleteCurrentTask() {

    if (taskStack.isEmpty()) {

        System.out.println(
                "No housekeeping task available."
        );

        return;
    }

    HousekeepingTask currentTask =
            taskStack.peek();

    System.out.println(
            "\nCurrent Task:"
    );

    System.out.println(currentTask);

    System.out.println(
            "\nTask "
            + currentTask.getTaskID()
            + " has been completed and removed."
    );

    taskStack.pop();
    }

    // =========================================================
    // CHECK STACK
    // =========================================================

    public boolean hasTask() {

        return !taskStack.isEmpty();
    }
}