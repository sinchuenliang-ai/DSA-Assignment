/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;
/**
 *
 * @author LENOVO
 */

import adt.ArrayStack;
import adt.StackInterface;
import Entities.HousekeepingTask;

public class HousekeepingTaskManager {

    // Stack ADT to store housekeeping tasks
    private StackInterface<HousekeepingTask> taskStack;


    // Constructor
    public HousekeepingTaskManager() {
        taskStack = new ArrayStack<>();
    }


    // Add a new housekeeping task
    public void reportTask(HousekeepingTask task) {

    // Check whether task object exists
    if (task == null) {
        System.out.println("Error: Task cannot be null.");
        return;
    }

    // Validate Task ID
    if (task.getTaskID() == null || task.getTaskID().trim().isEmpty()) {
        System.out.println("Error: Task ID cannot be empty for " + task.getLocation()+ ".");
        return;
    }

    // Validate Task Name
    if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
        System.out.println("Error: Task name cannot be empty for task " + task.getTaskID() + ".");
        return;
    }

    // Validate Location
    if (task.getLocation() == null || task.getLocation().trim().isEmpty()) {
        System.out.println("Error: Location cannot be empty for task " + task.getTaskID() + ".");
        return;
    }

    // Validate Status
    if (task.getStatus() == null ||
        (!task.getStatus().equalsIgnoreCase("Pending") &&
         !task.getStatus().equalsIgnoreCase("Completed"))) {

        System.out.println("Error: Status must be Pending or Completed.");
        return;
    }

    // Add valid task to stack
    taskStack.push(task);

    System.out.println("Task " + task.getTaskID() + " added successfully.");
}
    // Complete the most recent task
    public HousekeepingTask completeTask() {

        if (taskStack.isEmpty()) {
            return null;
        }
        
        HousekeepingTask task = taskStack.pop();
        task.setStatus("Completed");

        return task;
    }


    // View the latest reported task
    public HousekeepingTask viewLatestTask() {

        if (taskStack.isEmpty()) {
            return null;
        }

        return taskStack.peek();
    }


    // Check whether there are pending tasks
    public boolean hasPendingTasks() {
        return !taskStack.isEmpty();
    }


    // Return total number of pending tasks
    public int getPendingTaskCount() {
        return taskStack.size();
    }


    // Display all pending tasks
    public void displayPendingTasks() {

        if (taskStack.isEmpty()) {
            System.out.println("No pending housekeeping tasks.");
            return;
        }


        System.out.println("===== Pending Housekeeping Tasks =====");


        // Temporary stack to preserve original stack
        StackInterface<HousekeepingTask> tempStack = new ArrayStack<>();


        while (!taskStack.isEmpty()) {

            HousekeepingTask task = taskStack.pop();

            System.out.println(task);
            System.out.println("----------------------------");

            tempStack.push(task);
        }


        // Restore original stack
        while (!tempStack.isEmpty()) {
            taskStack.push(tempStack.pop());
        }
    }
}

