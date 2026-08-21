package entity;

/**
 * Entity class representing a physical Room in the resort system.
 */
public class Room {
    private String roomID;
    private String roomNumber;
    private String roomType;
    private String roomStatus;
    private double ratePerNight;

    public Room() {
    }

    public Room(String roomID, String roomNumber, String roomType, String roomStatus) {
        this(roomID, roomNumber, roomType, roomStatus, getDefaultRateForType(roomType));
    }

    public Room(String roomID, String roomNumber, String roomType, String roomStatus, double ratePerNight) {
        this.roomID = roomID;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.roomStatus = roomStatus;
        this.ratePerNight = ratePerNight > 0 ? ratePerNight : getDefaultRateForType(roomType);
    }

    public static double getDefaultRateForType(String type) {
        if (type == null) return 120.00;
        String lower = type.trim().toLowerCase();
        if (lower.contains("presidential")) return 1200.00;
        if (lower.contains("executive") || lower.contains("suite")) {
            if (lower.contains("deluxe")) return 250.00;
            return 500.00;
        }
        if (lower.contains("deluxe")) return 250.00;
        if (lower.contains("double")) return 150.00;
        return 120.00;
    }

    public String getCategory() {
        if (roomType != null) {
            String lower = roomType.trim().toLowerCase();
            if (lower.contains("presidential")) return "Presidential";
            if (lower.contains("executive")) return "Suite";
            if (lower.contains("deluxe")) return "Deluxe";
            if (lower.contains("standard") || lower.contains("single") || lower.contains("double")) return "Standard";
        }
        if (roomNumber != null) {
            if (roomNumber.startsWith("A")) return "Standard";
            if (roomNumber.startsWith("D")) return "Deluxe";
            if (roomNumber.startsWith("S")) return "Suite";
            if (roomNumber.startsWith("P")) return "Presidential";
        }
        return "Standard";
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public double getRatePerNight() {
        return ratePerNight;
    }

    public void setRatePerNight(double ratePerNight) {
        this.ratePerNight = ratePerNight;
    }
}
