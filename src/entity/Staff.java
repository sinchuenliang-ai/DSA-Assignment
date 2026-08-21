package entity;

public class Staff {
    private String staffID;
    private String staffName;
    private String email;
    private String shift;
    private String position;
    private String password;  // Added for authentication
    private String role;      // Added for role-based access

    public Staff() {}
    
    public Staff(String staffID, String staffName, String email, String shift, String position) {
        this.staffID = staffID;
        this.staffName = staffName;
        this.email = email;
        this.shift = shift;
        this.position = position;
        this.password = "default123";  // Default password
        this.role = "STAFF";            // Default role
    }

    public Staff(String staffID, String staffName, String email, String shift, String position, String password, String role) {
        this.staffID = staffID;
        this.staffName = staffName;
        this.email = email;
        this.shift = shift;
        this.position = position;
        this.password = password;
        this.role = role;
    }

    // getter setter
    public String getStaffID() { 
        return staffID; 
    }
    public void setStaffID(String staffID) { 
        this.staffID = staffID;
    }
    public String getStaffName() { 
        return staffName; 
    }
    public void setStaffName(String staffName) { 
        this.staffName = staffName; 
    }
    public String getEmail() {
        return email; 
    }
    public void setEmail(String email) {
        this.email = email; 
    }
    public String getShift() { 
        return shift; 
    }
    public void setShift(String shift) {
        this.shift = shift;
    }
    public String getPosition() { 
        return position; 
    }
    public void setPosition(String position) {
        this.position = position; 
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) { 
        this.password = password; 
    }
    public String getRole() { 
        return role; 
    }
    public void setRole(String role) { 
        this.role = role;
    }
    
    
     // Authenticates staff with password
    public boolean authenticate(String password) {
        return this.password != null && this.password.equals(password);
    }
    
    
     // Checks if staff has admin role
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
   
     
// Checks if staff can access reports
    public boolean canAccessReports() {
        return "ADMIN".equalsIgnoreCase(role) || "MANAGER".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "Staff ID      : " + staffID +
               "\nStaff Name   : " + staffName +
               "\nEmail        : " + email +
               "\nShift        : " + shift +
               "\nPosition     : " + position +
               "\nRole         : " + role;
    }
}