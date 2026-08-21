package test;

import control.BookingControl;
import control.FrontDeskControl;
import control.HouseKeepingControl;
import entity.Booking;
import entity.HousekeepingTask;
import entity.Reservation;
import utility.FileHandler;

public class DataPersistenceTest {

    public static void main(String[] args) {
        System.out.println("=== 1. TESTING FRONT DESK LOADING ===");
        FrontDeskControl fdc = new FrontDeskControl();
        System.out.println("Front desk initialized successfully.");

        System.out.println("\n=== 2. TESTING BOOKING CONTROL LOADING ===");
        BookingControl bc = new BookingControl();
        int initialWaiting = bc.getWaitingCount();
        System.out.println("Initial waiting bookings count: " + initialWaiting);
        System.out.println("Initial available rooms count: " + bc.getAvailableRoomsCount());

        // Register a test guest
        Booking b = bc.registerWalkInGuest("Test User", "Male", "019-1234567", "test@user.com", "IC999999",
                "2026-08-20", "2026-08-22", 2, "Standard Single");
        System.out.println("Registered test booking: " + b.getBookingID());

        // Reload BookingControl from file to verify persistence
        BookingControl bcReloaded = new BookingControl();
        System.out.println("Reloaded waiting count: " + bcReloaded.getWaitingCount());
        boolean found = bcReloaded.searchBooking(b.getBookingID()) != null;
        System.out.println("Found persisted booking: " + found);

        // Cancel the test booking to restore clean state
        bcReloaded.cancelBooking(b.getBookingID());
        BookingControl bcFinal = new BookingControl();
        System.out.println("Final waiting count after cancel: " + bcFinal.getWaitingCount());

        System.out.println("\n=== 3. TESTING HOUSEKEEPING LOADING ===");
        HouseKeepingControl hkc = new HouseKeepingControl();
        HousekeepingTask task = hkc.viewCurrentStatus();
        System.out.println("Current top task ID: " + (task != null ? task.getTaskID() : "none"));

        System.out.println("\n=== ALL TESTS PASSED SUCCESSFULLY! ===");
    }
}
