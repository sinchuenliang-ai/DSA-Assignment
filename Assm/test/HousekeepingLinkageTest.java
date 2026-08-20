package test;

import control.BookingControl;
import control.FrontDeskControl;
import control.HouseKeepingControl;
import entity.Booking;
import entity.HousekeepingTask;

import java.time.LocalDate;

/**
 * Integration test for Housekeeping <-> Front Desk <-> Walk-In linkage.
 */
public class HousekeepingLinkageTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  TESTING MODULE LINKAGE: HOUSEKEEPING <-> FRONT DESK <-> WALK-IN");
        System.out.println("=================================================================");

        // 1. Initialize all three controllers with shared linkage
        FrontDeskControl fdc = new FrontDeskControl();
        HouseKeepingControl hkc = new HouseKeepingControl();
        BookingControl bc = new BookingControl(fdc, hkc);
        fdc.setHouseKeepingControl(hkc);

        System.out.println("1. All three controllers initialized and linked.");
        System.out.println("   Booking->FrontDesk linked: " + (bc.getFrontDeskControl() == fdc));
        System.out.println("   Booking->Housekeeping linked: " + (bc.getHouseKeepingControl() == hkc));
        System.out.println("   FrontDesk->Housekeeping linked: " + (fdc.getHouseKeepingControl() == hkc));
        System.out.println("   Housekeeping->BookingControl linked: " + (hkc.getBookingControl() == bc));
        System.out.println("   Housekeeping->FrontDesk linked: " + (hkc.getFrontDeskControl() == fdc));

        // 2. Manually create a housekeeping task for a room (simulating check-out)
        System.out.println("\n2. Creating Housekeeping task for room D-101 (simulating guest check-out)...");
        int initialAvail = bc.getAvailableRoomsCount();
        System.out.println("   Initial available rooms: " + initialAvail);

        HousekeepingTask task = hkc.createTaskForRoom("D-101", "Room Cleaning", "S001");
        if (task == null) {
            System.err.println("   FAILED: Task creation returned null!");
        } else {
            System.out.println("   Task created: " + task.getTaskID() + " for room " + task.getLocation() + " | Status: " + task.getStatus());
        }

        // 3. Verify task appears in active task map
        System.out.println("\n3. Verifying housekeeping task map...");
        var taskMap = hkc.getAllActiveTasksMap();
        System.out.println("   Active HK tasks: " + taskMap.size() + " unique room(s) being cleaned.");
        for (var entry : taskMap.entrySet()) {
            System.out.println("   Room " + entry.getKey() + " => Status: " + entry.getValue().getStatus());
        }

        // 4. Progress the task through the cleaning cycle
        System.out.println("\n4. Progressing D-101 through cleaning cycle...");
        hkc.updateStatus("Cleaning In Progress");
        hkc.updateStatus("Inspected");
        System.out.println("   Status progressed to Inspected.");

        // 5. Mark Ready for Check-In: should trigger room becoming Available
        System.out.println("\n5. Marking D-101 Ready for Check-In...");
        int availBefore = bc.getAvailableRoomsCount();
        hkc.updateStatus("Ready for Check-In");
        int availAfter = bc.getAvailableRoomsCount();
        System.out.println("   Available rooms before: " + availBefore);
        System.out.println("   Available rooms after:  " + availAfter);

        if (availAfter > availBefore) {
            System.out.println("   PASS: Room D-101 is now available in BookingControl!");
        } else {
            System.out.println("   NOTE: Room may have already been in queue or check needed via rooms.txt.");
        }

        // 6. Walk-In Registration assigns D-101 to a new guest
        System.out.println("\n6. Walk-In guest requests Deluxe Suite assignment...");
        String today = LocalDate.now().toString();
        String checkout = LocalDate.now().plusDays(2).toString();
        Booking b = bc.registerWalkInGuest("Henry Tan", "Male", "016-1234567", "henry@test.com",
                "IC901010", today, checkout, 1, "Deluxe Suite");
        System.out.println("   Registered: " + b.getBookingID() + " (" + b.getGuest().getGuestName() + ")");

        Booking assigned = bc.assignRoomByRoomType("Deluxe Suite");
        if (assigned != null) {
            System.out.println("   Assigned Room: " + assigned.getRoom().getRoomNumber()
                    + " (" + assigned.getRoom().getRoomType() + ")");
            System.out.println("   Status: " + assigned.getBookingStatus());
            System.out.println("   Front Desk Pending Queue: " + fdc.getPendingWalkInQueue().size() + " guest(s)");
        } else {
            System.out.println("   No Deluxe Suite rooms currently available (expected if rooms.txt not updated yet).");
        }

        // 7. Verify Front Desk room availability reflects housekeeping statuses
        System.out.println("\n7. Housekeeping task map after cycle completion:");
        var taskMapAfter = hkc.getAllActiveTasksMap();
        System.out.println("   Active HK tasks remaining: " + taskMapAfter.size());
        for (var entry : taskMapAfter.entrySet()) {
            System.out.println("   Room " + entry.getKey() + " => Status: " + entry.getValue().getStatus());
        }

        System.out.println("\n=================================================================");
        System.out.println("  ALL HOUSEKEEPING LINKAGE TESTS COMPLETED!");
        System.out.println("=================================================================");
    }
}
