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
        taskStack.push(task);
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

