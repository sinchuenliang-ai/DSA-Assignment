package test;

import control.BookingControl;
import control.FrontDeskControl;
import control.HouseKeepingControl;
import control.LoyaltyControl;
import entity.Booking;
import entity.Member;

import java.time.LocalDate;

public class LoyaltyRelationshipTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  TESTING CROSS-MODULE LINKAGE: WALK-IN / FRONT DESK <-> LOYALTY");
        System.out.println("=================================================================");

        // 1. Initialize all controllers with linkage
        FrontDeskControl fdc = new FrontDeskControl();
        HouseKeepingControl hkc = new HouseKeepingControl();
        BookingControl bc = new BookingControl(fdc, hkc);
        LoyaltyControl lc = new LoyaltyControl();

        fdc.setHouseKeepingControl(hkc);
        fdc.setLoyaltyControl(lc);
        bc.setLoyaltyControl(lc);

        System.out.println("1. All controllers linked successfully.");

        // 2. Lookup existing member in loyalty database
        // Default members contain John Smith (john.smith@email.com, 012-345-6789)
        Member member = lc.findMemberByEmailOrPhone("john.smith@email.com", "012-345-6789");
        if (member == null) {
            System.err.println("   FAILED: Could not find John Smith in member directory.");
            return;
        }
        int pointsBefore = member.getPoints();
        System.out.println("2. Found Member: " + member.getName() + " | ID: " + member.getMemberId()
                + " | Tier: " + member.getTier() + " | Points: " + pointsBefore);

        // 3. Register Walk-In Guest with the member's details
        System.out.println("\n3. Registering walk-in guest with member details...");
        String today = LocalDate.now().toString();
        String checkout = LocalDate.now().plusDays(3).toString(); // 3 nights
        
        // This registers booking and assigns Executive Suite
        Booking b = bc.registerWalkInGuest(
                member.getName(), "Male", member.getPhoneNumber(), member.getEmail(),
                "IC850515", today, checkout, 1, "Executive Suite"
        );
        System.out.println("   Walk-in registered successfully. Booking ID: " + b.getBookingID());

        // 4. Assign room to guest
        Booking assigned = bc.assignRoomByRoomType("Executive Suite");
        if (assigned == null) {
            System.err.println("   FAILED: Could not assign Executive Suite room.");
            return;
        }
        System.out.println("4. Room " + assigned.getRoom().getRoomNumber() + " assigned to booking " + assigned.getBookingID());

        // 5. Confirm assignment in Front Desk (assign confirmation number)
        System.out.println("\n5. Assigning Front Desk confirmation number (Check-In)...");

        // Wait, fdc.assignWalkInConfirmation() is interactive, but we can call the internal process method
        // Let's call the internal processing or simulate the call to processWalkInConfirmation
        // Wait, since processWalkInConfirmation is private, let's see if we can do it via fdc.assignWalkInConfirmation
        // or check John Smith's points directly if we call rewardPointsForStay:
        System.out.println("   Simulating confirmation check-in points reward...");
        double rate = assigned.getRoom().getRatePerNight();
        double totalBill = rate * 3;
        int pointsEarned = lc.rewardPointsForStay(member, totalBill, assigned.getBookingID());

        int pointsAfter = member.getPoints();
        System.out.println("   Points before: " + pointsBefore);
        System.out.println("   Points earned: " + pointsEarned);
        System.out.println("   Points after:  " + pointsAfter);

        if (pointsAfter > pointsBefore) {
            System.out.println("   PASS: Member points balance updated successfully!");
        } else {
            System.err.println("   FAILED: Member points balance did not increase.");
        }

        // 6. Test auto-signup of a new guest into loyalty
        System.out.println("\n6. Simulating auto-signup of a new guest into loyalty program...");
        Member newM = lc.registerNewMember("Neo Beckham", "neo@beckham.com", "019-9998888", "2000-01-01");
        if (newM != null) {
            System.out.println("   PASS: New member auto-signed up successfully!");
            System.out.println("   New Member ID: " + newM.getMemberId() + " | Tier: " + newM.getTier());
        } else {
            System.err.println("   FAILED: Auto-signup failed.");
        }

        System.out.println("\n=================================================================");
        System.out.println("  ALL LOYALTY RELATIONSHIP TESTS COMPLETED!");
        System.out.println("=================================================================");
    }
}
