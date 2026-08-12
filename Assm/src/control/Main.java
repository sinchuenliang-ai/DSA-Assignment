/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author LENOVO
 */

import control.HousekeepingTaskManager;
import Entities.HousekeepingTask;

public class Main {
    public static void main(String[] args){
        // cretae housekeeping task manager 
        HousekeepingTaskManager manager = new HousekeepingTaskManager();
        
        //create new housekeeping task
        HousekeepingTask task1 = new HousekeepingTask("T001","Room 1001","Clean room","Pending");
        HousekeepingTask task2 = new HousekeepingTask("T002","Room 1002","Replace shampoo","Pending");
        HousekeepingTask task3 = new HousekeepingTask("T003","Room 1003","Fix water leakage","Pending");
        HousekeepingTask task4 = new HousekeepingTask("T004","Room 1105","Refill towel","Completed");
       
        //add tasks into stack
        manager.reportTask(task1);
        manager.reportTask(task2);
        manager.reportTask(task3);
        manager.reportTask(task4);
        
        //display all pending tasks
        System.out.println("\nHousekeeping Task List: \n");
        manager.displayPendingTasks();
        
        //view latest task
        System.out.println("\nLatest task: ");
        System.out.println(manager.viewLatestTask());
        
        //Complete Latest Task 
        System.out.println("\nCompleted task: ");
        System.out.println(manager.completeTask());
        
        //display remaining tasks
        System.out.println("\nRemaining pending tasks: ");
        manager.displayPendingTasks();     
    }
}
