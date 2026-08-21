package utility;

public class ValidationUtils {
    
    // Private constructor to prevent instantiation
    private ValidationUtils() {}
    
    public static boolean isValidMemberId(String memberId) {
        if (memberId == null || memberId.isEmpty()) {
            return false;
        }
        // Member ID should be 3-10 alphanumeric characters
        return memberId.matches("^[A-Za-z0-9]{3,10}$");
    }
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Basic email format validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // Malaysian phone number format: 01X-XXXXXXX or 01X-XXXXXXXX
        return phone.matches("^01[0-9]{1}-[0-9]{7,8}$");
    }

     // Validates an amount (must be positive)
    public static boolean isValidAmount(double amount) {
        return amount > 0;
    }
    
   // Validates points (must be positive and within reasonable range)
    public static boolean isValidPoints(int points) {
        return points > 0 && points <= 1000000;
    }
    
    public static boolean isValidTier(String tier) {
        if (tier == null || tier.isEmpty()) {
            return false;
        }
        return tier.equals("Silver") || tier.equals("Gold") || 
               tier.equals("Platinum") || tier.equals("Diamond") || 
               tier.equals("Elite");
    }
    
    public static boolean isValidGuestName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return name.matches("^[a-zA-Z\\s'/.-]+$") && name.length() >= 2;
    }
    
    public static boolean isValidIcPassport(String icPassport) {
        if (icPassport == null || icPassport.isEmpty()) {
            return false;
        }
        return icPassport.matches("^[A-Za-z0-9\\-]{6,15}$");
    }
    
     // Validates a room number format
    public static boolean isValidRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.isEmpty()) {
            return false;
        }
        return roomNumber.matches("^[A-Z]-[0-9]{3}$");
    }
}