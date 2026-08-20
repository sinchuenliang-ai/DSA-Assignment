package control;

import adt.ArrayStack;
import adt.StackInterface;
import entity.HousekeepingTask;
import entity.Room;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utility.FileHandler;

public class HouseKeepingControl {

    // Stack stores the status history
    private ArrayStack<HousekeepingTask> taskStack;
    private int TaskNumber = 1;

    private BookingControl bookingControl;
    private FrontDeskControl frontDeskControl;

    // Constructors
    public HouseKeepingControl() {
        this(null, null);
    }

    public HouseKeepingControl(BookingControl bookingControl, FrontDeskControl frontDeskControl) {
        this.taskStack = new ArrayStack<>();
        FileHandler.loadHousekeepingTasks(taskStack);
        recalculateTaskNumber();

        if (bookingControl != null) {
            setBookingControl(bookingControl);
        }
        if (frontDeskControl != null) {
            setFrontDeskControl(frontDeskControl);
        }
    }

    public void setBookingControl(BookingControl bookingControl) {
        this.bookingControl = bookingControl;
        if (bookingControl != null && bookingControl.getHouseKeepingControl() != this) {
            bookingControl.setHouseKeepingControl(this);
        }
    }

    public BookingControl getBookingControl() {
        return bookingControl;
    }

    public void setFrontDeskControl(FrontDeskControl frontDeskControl) {
        this.frontDeskControl = frontDeskControl;
        if (frontDeskControl != null && frontDeskControl.getHouseKeepingControl() != this) {
            frontDeskControl.setHouseKeepingControl(this);
        }
    }

    public FrontDeskControl getFrontDeskControl() {
        return frontDeskControl;
    }

    private void recalculateTaskNumber() {
        if (taskStack.isEmpty()) {
            TaskNumber = 1;
            return;
        }
        ArrayStack<HousekeepingTask> temp = new ArrayStack<>();
        int max = 0;
        while (!taskStack.isEmpty()) {
            HousekeepingTask t = taskStack.pop();
            if (t.getTaskID() != null && t.getTaskID().startsWith("T")) {
                try {
                    int num = Integer.parseInt(t.getTaskID().substring(1));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {
                }
            }
            temp.push(t);
        }
        while (!temp.isEmpty()) {
            taskStack.push(temp.pop());
        }
        TaskNumber = max + 1;
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

        if (task.getTaskID() == null || task.getTaskID().trim().isEmpty()) {
            System.out.println("Task ID cannot be empty for " + task.getLocation() + ".");
            return false;
        }

        if (task.getLocation() == null || task.getLocation().trim().isEmpty()) {
            System.out.println("Location cannot be empty.");
            return false;
        }

        if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
            System.out.println("Task name cannot be empty for " + task.getLocation() + ".");
            return false;
        }

        if (!isValidStatus(task.getStatus())) {
            System.out.println("Invalid status for " + task.getLocation() + ".");
            System.out.println("Status must be: Dirty, Cleaning In Progress, Inspected, or Ready for Check-In.");
            return false;
        }

        return true;
    }

    // automatically generate taskID 
    private String generateTaskID() {
        String taskID = String.format("T%03d", TaskNumber);
        TaskNumber++;
        return taskID;
    }

    // ADD INITIAL HOUSEKEEPING TASK
    public void addTask(HousekeepingTask task) {
        if (task == null) {
            System.out.println("task cannot be null.");
            return;
        }
        
        task.setTaskID(generateTaskID());
        
        if (!validateTask(task)) {
            return;
        }

        taskStack.push(task);
        FileHandler.saveHousekeepingTasks(taskStack);

        System.out.println("\nTask " + task.getTaskID() + " added successfully!");
    }

    // AUTO-CREATE TASK FROM CHECK-OUT
    public HousekeepingTask createTaskForRoom(String roomNo, String taskName, String staffID) {
        if (roomNo == null || roomNo.trim().isEmpty()) return null;

        String sID = (staffID != null && !staffID.trim().isEmpty()) ? staffID.trim() : "S001";
        String tName = (taskName != null && !taskName.trim().isEmpty()) ? taskName.trim() : "Room Cleaning";

        HousekeepingTask newTask = new HousekeepingTask(null, roomNo.trim(), tName, "Dirty", sID);
        addTask(newTask);
        return newTask;
    }
    
    // VIEW CURRENT STATUS
    public HousekeepingTask viewCurrentStatus() {
        if (taskStack.isEmpty()) {
            System.out.println("No housekeeping task available.");
            return null;
        }
        return taskStack.peek();
    }

    // GET ACTIVE TASK FOR A SPECIFIC ROOM
    public HousekeepingTask getActiveTaskForRoom(String roomNo) {
        if (taskStack.isEmpty() || roomNo == null) return null;

        ArrayStack<HousekeepingTask> temp = new ArrayStack<>();
        HousekeepingTask target = null;

        while (!taskStack.isEmpty()) {
            HousekeepingTask t = taskStack.pop();
            if (target == null && t.getLocation().equalsIgnoreCase(roomNo.trim())) {
                target = t;
            }
            temp.push(t);
        }

        while (!temp.isEmpty()) {
            taskStack.push(temp.pop());
        }

        return target;
    }

    // GET MAP OF ALL ACTIVE ROOM TASKS (LATEST STATUS PER ROOM)
    public Map<String, HousekeepingTask> getAllActiveTasksMap() {
        Map<String, HousekeepingTask> map = new HashMap<>();
        if (taskStack.isEmpty()) return map;

        ArrayStack<HousekeepingTask> temp = new ArrayStack<>();

        while (!taskStack.isEmpty()) {
            HousekeepingTask t = taskStack.pop();
            if (!map.containsKey(t.getLocation().toUpperCase())) {
                map.put(t.getLocation().toUpperCase(), t);
            }
            temp.push(t);
        }

        while (!temp.isEmpty()) {
            taskStack.push(temp.pop());
        }

        return map;
    }

    // DISPLAY CURRENT ROOM STATUS
    public void displayCurrentRoomStatus() {
        if (taskStack.isEmpty()) {
            System.out.println("No housekeeping tasks in the stack.");
            return;
        }

        ArrayStack<HousekeepingTask> tempStack = new ArrayStack<>();
        String[] displayedTaskIDs = new String[taskStack.size()];
        int displayedCount = 0;

        System.out.println("\n===== CURRENT ROOM STATUS =====");

        while (!taskStack.isEmpty()) {
            HousekeepingTask task = taskStack.pop();
            boolean alreadyDisplayed = false;

            for (int i = 0; i < displayedCount; i++) {
                if (displayedTaskIDs[i].equals(task.getTaskID())) {
                    alreadyDisplayed = true;
                    break;
                }
            }

            if (!alreadyDisplayed) {
                System.out.println("Task ID        : " + task.getTaskID());
                System.out.println("Room           : " + task.getLocation());
                System.out.println("Task Name      : " + task.getTaskName());
                System.out.println("Status         : " + task.getStatus());
                System.out.println("Staff ID       : " + task.getAssignedStaffID());
                System.out.println("----------------------------");

                displayedTaskIDs[displayedCount] = task.getTaskID();
                displayedCount++;
            }

            tempStack.push(task);
        }

        while (!tempStack.isEmpty()) {
            taskStack.push(tempStack.pop());
        }
    }

    // UPDATE STATUS
    public void updateStatus(String newStatus) {
        if (!isValidStatus(newStatus)) {
            System.out.println("Invalid status.");
            return;
        }

        if (taskStack.isEmpty()) {
            System.out.println("No housekeeping task available.");
            return;
        }

        HousekeepingTask currentTask = taskStack.peek();

        HousekeepingTask updatedTask = new HousekeepingTask(
                currentTask.getTaskID(),
                currentTask.getLocation(),
                currentTask.getTaskName(),
                newStatus,
                currentTask.getAssignedStaffID()
        );

        taskStack.push(updatedTask);

        System.out.println("\n" + updatedTask.getLocation() + " status updated to: " + newStatus);

        if (newStatus.equalsIgnoreCase("Ready for Check-In")) {
            System.out.println("\n" + updatedTask.getLocation() + " is now Ready for Check-In.");
            System.out.println("Housekeeping task " + updatedTask.getTaskID() + " has been completed.");

            // Update master room availability
            onRoomCleaned(updatedTask.getLocation());

            String taskID = currentTask.getTaskID();
            while (!taskStack.isEmpty() && taskStack.peek().getTaskID().equals(taskID)) {
                taskStack.pop();
            }

            FileHandler.saveHousekeepingTasks(taskStack);
            System.out.println("Task " + taskID + " removed from the active task stack.");
            return;
        }

        FileHandler.saveHousekeepingTasks(taskStack);
    }

    // ROLLBACK LAST STATUS
    public void rollbackStatus() {
        if (taskStack.isEmpty()) {
            System.out.println("\nNo status available to rollback.");
            return;
        }

        HousekeepingTask removedTask = taskStack.pop();
        FileHandler.saveHousekeepingTasks(taskStack);

        System.out.println("Room          : " + removedTask.getLocation());
        System.out.println("Rolled Back   : " + removedTask.getStatus());

        if (!taskStack.isEmpty()) {
            HousekeepingTask currentTask = taskStack.peek();
            System.out.println("\n===== CURRENT ROOM STATUS =====");
            System.out.println("Task ID             : " + currentTask.getTaskID());
            System.out.println("Room                : " + currentTask.getLocation());
            System.out.println("Task Name           : " + currentTask.getTaskName());
            System.out.println("Current Status      : " + currentTask.getStatus());
        } else {
            System.out.println("\nNo previous status available.");
        }
    }

    // DISPLAY STATUS HISTORY
    public void displayStatusHistory() {
        if (taskStack.isEmpty()) {
            System.out.println("No status history available.");
            return;
        }

        StackInterface<HousekeepingTask> tempStack = new ArrayStack<>();

        while (!taskStack.isEmpty()) {
            HousekeepingTask task = taskStack.pop();
            System.out.println(task);
            System.out.println("----------------------------");
            tempStack.push(task);
        }

        while (!tempStack.isEmpty()) {
            taskStack.push(tempStack.pop());
        }
    }

    // END HOUSEKEEPING CYCLE
    public void completeHousekeepingCycle() {
        if (taskStack.isEmpty()) {
            System.out.println("No active housekeeping task.");
            return;
        }

        HousekeepingTask currentTask = taskStack.peek();

        if (!currentTask.getStatus().equalsIgnoreCase("Ready for Check-In")) {
            System.out.println("Room is not ready for check-in (Current Status: " + currentTask.getStatus() + ").");
            return;
        }

        String completedTaskID = currentTask.getTaskID();
        String completedLocation = currentTask.getLocation();

        System.out.println("\n" + completedLocation + " has been checked in / marked clean.");
        System.out.println("Housekeeping task " + completedTaskID + " completed.");

        onRoomCleaned(completedLocation);

        while (!taskStack.isEmpty() && taskStack.peek().getTaskID().equals(completedTaskID)) {
            taskStack.pop();
        }

        FileHandler.saveHousekeepingTasks(taskStack);
        System.out.println("Task " + completedTaskID + " removed from the active stack.");
    }

    private void onRoomCleaned(String roomLocation) {
        if (roomLocation == null || roomLocation.trim().isEmpty()) return;

        // Update in BookingControl available rooms queue
        if (bookingControl != null) {
            bookingControl.markRoomAvailable(roomLocation);
        } else {
            List<Room> all = FileHandler.loadAllHotelRooms();
            for (Room r : all) {
                if (r.getRoomNumber().equalsIgnoreCase(roomLocation.trim())) {
                    r.setRoomStatus("Available");
                    break;
                }
            }
            FileHandler.saveAllHotelRooms(all);
        }

        System.out.println("  [ System ] Room " + roomLocation + " is now AVAILABLE in Room Inventory for guest assignment.");
    }

    // CHECK STACK
    public boolean hasTask() {
        return !taskStack.isEmpty();
    }
}