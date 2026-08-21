package entity;

public class Guest {
    private String guestID;
    private String guestName;
    private String gender;
    private String phoneNumber;
    private String email;
    private String icPassportNo;
    
    public Guest(String guestID, String guestName,
             String gender, String phoneNumber,
             String email, String icPassportNo) {

        this.guestID = guestID;
        this.guestName = guestName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.icPassportNo = icPassportNo;
    }

    public String getGuestID() {
        return guestID;
    }

    public void setGuestID(String guestID) {
        this.guestID = guestID;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIcPassportNo() {
        return icPassportNo;
    }

    public void setIcPassportNo(String icPassportNo) {
        this.icPassportNo = icPassportNo;
    }
}

