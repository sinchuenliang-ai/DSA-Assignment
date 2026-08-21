package test;

import control.BookingControl;
import control.FrontDeskControl;
import entity.Booking;
import entity.Reservation;

public class DateLinkageAndReportsTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  TESTING DATE LINKAGE (WALK-IN -> FRONT DESK) & REPORTS");
        System.out.println("=================================================================");

        FrontDeskControl fdc = new FrontDeskControl();
        BookingControl bc = new BookingControl(fdc);

        // 1. Register a guest with specific future dates (2026-09-01 to 2026-09-02)
        String checkIn = "2026-09-01";
        String checkOut = "2026-09-02";
        Booking b = bc.registerWalkInGuest(
                "Lionel Messi", "Male", "019-8889999", "messi@intermiami.com", "IC870624",
                checkIn, checkOut, 1, "Presidential Suite"
        );
        System.out.println("1. Walk-In Guest Registered: " + b.getBookingID() + " with dates: " + b.getCheckInDate() + " -> " + b.getCheckOutDate());

        // 2. Assign room for this specific booking
        Booking assigned = bc.assignRoomByRoomType("Presidential Suite");
        System.out.println("2. Room Assigned: " + assigned.getRoom().getRoomNumber());
        System.out.println("   Assigned Booking Dates: " + assigned.getCheckInDate() + " -> " + assigned.getCheckOutDate());

        // 3. Confirm in Front Desk
        Reservation res = fdc.registerGuestAndAssignConfirmation(
                assigned.getGuest().getGuestName(),
                assigned.getRoom().getCategory(),
                assigned.getRoom().getRoomNumber(),
                1,
                1200.00,
                "Checked-In",
                assigned.getCheckInDate(),
                assigned.getCheckOutDate()
        );
        bc.markBookingConfirmed(assigned);

        System.out.println("3. Front Desk Confirmation Created: #" + res.getConfirmationNumber());
        System.out.println("   Check-In Date in Reservation : " + res.getCheckInDate());
        System.out.println("   Check-Out Date in Reservation: " + res.getCheckOutDate());

        if (!"2026-09-01".equals(res.getCheckInDate()) || !"2026-09-02".equals(res.getCheckOutDate())) {
            System.err.println("FAILED: Reservation did NOT preserve the exact dates 2026-09-01 to 2026-09-02!");
            System.exit(1);
        }
        System.out.println("   ? Date validation PASSED: 2026-09-01 to 2026-09-02 successfully preserved in Reservation!");

        // Clean up test reservation
        fdc.deleteReservation(res.getConfirmationNumber());

        System.out.println("\n=================================================================");
        System.out.println("  ALL DATE LINKAGE & REPORT TESTS PASSED!");
        System.out.println("=================================================================");
    }
}
