/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities;

/**
 *
 * @author User
 */

public class HousekeepingTask {
    
    private String taskID;
    private String taskName;
    private String location;
    private String status;


public HousekeepingTask(){

}

public HousekeepingTask(String taskID, String location,String taskName, String status){
    this.taskID = taskID;
    this.location = location;
    this.taskName = taskName;
    this.status = status;
}

public String getTaskID(){
    return taskID;
}

public String getLocation(){
    return location;
}

public String getTaskName(){
    return taskName;
}

public String getStatus(){
    return status;
}

public void setTaskID(String taskID){
    this.taskID = taskID;
}

public void setLocation(String location){
    this.location = location;
}

public void setTaskName(String taskName){
    this.taskName = taskName;
}

public void setStatus(String status){
    this.status = status;
}

@Override
public String toString(){
    return "Task ID      : " + taskID +
               "\nLocation     : " + location +
               "\nTask Title   : " + taskName +
               "\nStatus       : " + status;
}
}