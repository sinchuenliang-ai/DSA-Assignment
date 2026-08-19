/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Entities;

/**
 *
 * @author User
 */  

public class Staff {
    
    private String staffID;
    private String staffName;
    private String email;
    private String shift;
    private String position;
    
    public Staff(){
        
    }
    
    public Staff(String staffID, String staffName, String email, String shift, String position){
        this.staffID = staffID;
        this.staffName = staffName;
        this.email = email;
        this.shift = shift;
        this.position = position;
    }
    
    public String getStaffID(){
        return staffID;
    }
    
    public String getStaffName(){
        return staffName;
    }
    
    public String getEmail(){
        return email;
    }
    
    public String getShift(){
        return shift;
    }
    
    public String getPosition(){
        return position;
    }
    
    public void setStaffID(String staffID){
        this.staffID = staffID;
    }
    
    public void setStaffName(String staffName){
        this.staffName = staffName;
    }
    
    public void setEmail(String email){
        this.email = email;
    }
    
    public void setShift(String shift){
        this.shift = shift;
    }
    
    public void setPosition(String position){
        this.position = position;
    }
    
    @Override
    public String toString(){
        return "Staff ID      : " + staffID +
               "\nStaff Name   : " + staffName +
               "\nEmail        : " + email +
               "\nShift        : " + shift +
               "\nPosition     : " + position;
    }
}
