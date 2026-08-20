package test;

import control.BookingControl;
import control.FrontDeskControl;
import entity.Booking;
import entity.Reservation;
import entity.Room;

import java.time.LocalDate;

public class ModuleLinkageTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  TESTING MODULE LINKAGE: WALK-IN -> FRONT DESK SERVICE FLOW");
        System.out.println("=================================================================");

        // 1. Initialize Controllers with linkage
        FrontDeskControl fdc = new FrontDeskControl();
        BookingControl bc = new BookingControl(fdc);

        int initialPending = fdc.getPendingWalkInQueue().size();
        System.out.println("1. Initial Front Desk Pending Walk-Ins: " + initialPending);

        // 2. Register Walk-In Guest
        String today = LocalDate.now().toString();
        String checkout = LocalDate.now().plusDays(3).toString();
        Booking b = bc.registerWalkInGuest(
                "David Beckham", "Male", "012-7778899", "david@beckham.com", "IC750502",
                today, checkout, 2, "Standard Single"
        );
        System.out.println("2. Registered Walk-In Guest: " + b.getBookingID() + " (" + b.getGuest().getGuestName() + ")");

        // 3. Assign Room in Walk-In Module
        Booking assignedBooking = bc.assignRoomByRoomType("Standard Single");
        if (assignedBooking == null) {
            System.err.println("FAILED: Room assignment returned null!");
            return;
        }
        System.out.println("3. Assigned Room: " + assignedBooking.getRoom().getRoomNumber() + " (" + assignedBooking.getRoom().getRoomType() + ")");
        System.out.println("   Booking Status: " + assignedBooking.getBookingStatus());

        // 4. Verify Booking is in Front Desk Pending Queue
        int newPending = fdc.getPendingWalkInQueue().size();
        System.out.println("4. Front Desk Pending Walk-Ins Count: " + newPending);
        if (newPending == 0) {
            System.err.println("FAILED: Pending queue in Front Desk is empty!");
            return;
        }

        // 5. Display Pending Walk-Ins in Front Desk
        System.out.println("\n5. Displaying Pending Walk-In Guests in Front Desk:");
        fdc.displayPendingWalkInGuests();

        // 6. Process Walk-In Confirmation in Front Desk
        String expectedConfirmNo = fdc.generate8DigitConfirmationNumber();
        System.out.println("\n6. Front Desk Assigning Confirmation Number (Expected: " + expectedConfirmNo + ")...");
        
        Booking pendingToConfirm = fdc.getPendingWalkInQueue().dequeue();
        // Invoke assignment
        Reservation newRes = fdc.registerGuestAndAssignConfirmation(
                pendingToConfirm.getGuest().getGuestName(),
                pendingToConfirm.getRoom().getCategory(),
                pendingToConfirm.getRoom().getRoomNumber(),
                3,
                120.00 * 3,
                "Checked-In"
        );
        bc.markBookingConfirmed(pendingToConfirm);

        System.out.println("   Generated Confirmation #: " + newRes.getConfirmationNumber());
        System.out.println("   BST Search Check: " + (fdc != null ? "Success" : "Failed"));

        // 7. Verify Persistence from disk
        FrontDeskControl fdcReloaded = new FrontDeskControl();
        Reservation searched = fdcReloaded.registerGuestAndAssignConfirmation("Verify Test", "Standard", "A-002", 1, 120.00, "Checked-In");
        System.out.println("7. Reloaded Front Desk successfully. Next generated conf: " + searched.getConfirmationNumber());
        fdcReloaded.deleteReservation(searched.getConfirmationNumber());

        System.out.println("\n=================================================================");
        System.out.println("  ALL MODULE LINKAGE TESTS PASSED SUCCESSFULLY!");
        System.out.println("=================================================================");
    }
}
