package entity;

/**
 * Entity class representing a Booking in the resort system.
 */
public class Booking {
    private String bookingID;
    private String bookingDate;
    private String checkInDate;
    private String checkOutDate;
    private int numberOfGuests;
    private String bookingStatus;
    private String requestedRoomType;
    private Guest guest;
    private Room room;

    // Default No-Arg Constructor
    public Booking() {
    }

    // Constructor without requestedRoomType (defaults to "Standard Single")
    public Booking(String bookingID, String bookingDate, String checkInDate, 
                   String checkOutDate, int numberOfGuests, String bookingStatus, 
                   Guest guest, Room room) {
        this(bookingID, bookingDate, checkInDate, checkOutDate, numberOfGuests, bookingStatus, "Standard Single", guest, room);
    }

    // Overloaded Constructor accepting requestedRoomType directly
    public Booking(String bookingID, String bookingDate, String checkInDate, 
                   String checkOutDate, int numberOfGuests, String bookingStatus, 
                   String requestedRoomType, Guest guest, Room room) {
        this.bookingID = bookingID;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.bookingStatus = bookingStatus;
        this.requestedRoomType = requestedRoomType;
        this.guest = guest;
        this.room = room;
    }

    // Getters and Setters
    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getRequestedRoomType() {
        return requestedRoomType;
    }

    public void setRequestedRoomType(String requestedRoomType) {
        this.requestedRoomType = requestedRoomType;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
