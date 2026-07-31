/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

/**
 *
 * @author User
 */

public class HousekeepingTask {
    
    private String taskID;
    private String taskName;
    private String description;
    private String location;
    private String priority;
    private String reportedDate;
    private String status;
    private String assignedStaffID;


public HousekeepingTask(){

}

public HousekeepingTask(String taskID, String taskName, String description,String location, String priority, String reportedDate, String status, String assignedStaffID){
    this.taskID = taskID;
    this.taskName = taskName;
    this.description = description;
    this.location = location;
    this.priority = priority;
    this.reportedDate = reportedDate;
    this.status = status;
    this.assignedStaffID = assignedStaffID;
}

public String getTaskID(){
    return taskID;
}

public String getTaskName(){
    return taskName;
}

public String getDescription(){
    return description;
}

public String getLocation(){
    return location;
}

public String getPriority(){
    return priority;
}

public String getReportedDate(){
    return reportedDate;
}

public String getStatus(){
    return status;
}

public String getAssignedStaffID(){
    return assignedStaffID;
}

public void setTaskID(String taskID){
    this.taskID = taskID;
}

public void setTaskName(String taskName){
    this.taskName = taskName;
}

public void setDescription(String description){
    this.description = description;
}

public void setLocation(String location){
    this.location = location;
}

public void setPriority(String priority){
    this.priority = priority;
}

public void setReportedDate(String reportedDate){
    this.reportedDate = reportedDate;
}

public void setStatus(String status){
    this.status = status;
}

public void setAssignedStaffID(String assignedStaffID){
    this.assignedStaffID = assignedStaffID;
}

@Override
public String toString(){
    return "Task ID      : " + taskID +
               "\nTask Title   : " + taskName +
               "\nDescription  : " + description +
               "\nLocation     : " + location +
               "\nPriority     : " + priority +
               "\nReported Date: " + reportedDate +
               "\nStatus       : " + status +
               "\nAssigned To  : " + assignedStaffID;
}
}