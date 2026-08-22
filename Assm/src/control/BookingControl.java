package control;

import adt.LinkedQueue;
import adt.QueueInterface;
import entity.Booking;
import entity.Guest;
import entity.Room;
import java.time.LocalDate;
import utility.FileHandler;

/**
 * @author Sin Chuen Liang
 * Controller class managing separate Queue ADTs per Room Type for Walk-In Bookings & Room Assignments,
 * and linking seamlessly with Front Desk Service.
 */
public class BookingControl {

    private final QueueInterface<Booking> singleQueue;
    private final QueueInterface<Booking> doubleQueue;
    private final QueueInterface<Booking> deluxeQueue;
    private final QueueInterface<Booking> executiveQueue;
    private final QueueInterface<Booking> presidentialQueue;

    private final QueueInterface<Booking> pendingWalkInConfirmations;
    private final QueueInterface<Booking> confirmedBookings;
    private final QueueInterface<Room> availableRooms;

    private FrontDeskControl frontDeskControl;
    private HouseKeepingControl houseKeepingControl;
    private LoyaltyControl loyaltyControl;

    private int guestCounter = 101;
    private int bookingCounter = 1001;

    public BookingControl() {
        this(null, null);
    }

    public BookingControl(FrontDeskControl frontDeskControl) {
        this(frontDeskControl, null);
    }

    public BookingControl(FrontDeskControl frontDeskControl, HouseKeepingControl houseKeepingControl) {
        this.singleQueue = new LinkedQueue<>();
        this.doubleQueue = new LinkedQueue<>();
        this.deluxeQueue = new LinkedQueue<>();
        this.executiveQueue = new LinkedQueue<>();
        this.presidentialQueue = new LinkedQueue<>();

        this.pendingWalkInConfirmations = new LinkedQueue<>();
        this.confirmedBookings = new LinkedQueue<>();
        this.availableRooms = new LinkedQueue<>();

        FileHandler.loadRooms(availableRooms);
        FileHandler.loadBookings(singleQueue, doubleQueue, deluxeQueue, executiveQueue, presidentialQueue, pendingWalkInConfirmations, confirmedBookings);
        recalculateCounters();

        if (frontDeskControl != null) {
            setFrontDeskControl(frontDeskControl);
        }
        if (houseKeepingControl != null) {
            setHouseKeepingControl(houseKeepingControl);
        }
    }

    public void setFrontDeskControl(FrontDeskControl frontDeskControl) {
        this.frontDeskControl = frontDeskControl;
        if (frontDeskControl != null && frontDeskControl.getBookingControl() != this) {
            frontDeskControl.setBookingControl(this);
        }
    }

    public FrontDeskControl getFrontDeskControl() {
        return frontDeskControl;
    }

    public void setHouseKeepingControl(HouseKeepingControl houseKeepingControl) {
        this.houseKeepingControl = houseKeepingControl;
        if (houseKeepingControl != null && houseKeepingControl.getBookingControl() != this) {
            houseKeepingControl.setBookingControl(this);
        }
    }

    public HouseKeepingControl getHouseKeepingControl() {
        return houseKeepingControl;
    }

    public void setLoyaltyControl(LoyaltyControl loyaltyControl) {
        this.loyaltyControl = loyaltyControl;
    }

    public LoyaltyControl getLoyaltyControl() {
        return loyaltyControl;
    }

    private void recalculateCounters() {
        int maxGuest = 100;
        int maxBooking = 1000;

        QueueInterface<Booking> all = getAllBookingsIncludingConfirmed();
        while (!all.isEmpty()) {
            Booking b = all.dequeue();
            if (b.getBookingID() != null && b.getBookingID().startsWith("B")) {
                try {
                    int bNum = Integer.parseInt(b.getBookingID().substring(1));
                    if (bNum > maxBooking) maxBooking = bNum;
                } catch (NumberFormatException ignored) {
                }
            }
            if (b.getGuest() != null && b.getGuest().getGuestID() != null && b.getGuest().getGuestID().startsWith("G")) {
                try {
                    int gNum = Integer.parseInt(b.getGuest().getGuestID().substring(1));
                    if (gNum > maxGuest) maxGuest = gNum;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        this.guestCounter = maxGuest + 1;
        this.bookingCounter = maxBooking + 1;
    }

    public String generateGuestID() {
        return "G" + (guestCounter++);
    }

    public String generateBookingID() {
        return "B" + (bookingCounter++);
    }

    public Booking registerWalkInGuest(String guestName, String gender, String phone,
                                       String email, String icPassport, String checkInDate,
                                       String checkOutDate, int numberOfGuests, 
                                       String requestedRoomType) {

        Guest newGuest = new Guest(generateGuestID(), guestName, gender, phone, email, icPassport);
        Booking newBooking = new Booking(generateBookingID(), LocalDate.now().toString(), checkInDate,
                                         checkOutDate, numberOfGuests, "Waiting", newGuest, null);
        
        newBooking.setRequestedRoomType(requestedRoomType);
        getQueueByRoomType(requestedRoomType).enqueue(newBooking);

        saveAllBookings();
        return newBooking;
    }

    /**
     * Assigns a room to the next guest in line for a specific room type and passes
     * it to Front Desk for Confirmation Number assignment.
     * @param roomType
     * @return 
     */
    public Booking assignRoomByRoomType(String roomType) {
        QueueInterface<Booking> targetQueue = getQueueByRoomType(roomType);

        if (targetQueue.isEmpty() || getAvailableRoomCountByType(roomType) == 0) {
            return null; 
        }

        Booking nextBooking = targetQueue.dequeue();
        Room assignedRoom = extractRoomByType(roomType);

        if (assignedRoom != null) {
            assignedRoom.setRoomStatus("Occupied");
            nextBooking.setRoom(assignedRoom);
            nextBooking.setBookingStatus("Assigned - Pending Front Desk Confirmation");

            pendingWalkInConfirmations.enqueue(nextBooking);

            if (frontDeskControl != null) {
                frontDeskControl.addPendingWalkInBooking(nextBooking);
            }

            saveAllBookings();
            FileHandler.saveRooms(availableRooms);
            return nextBooking;
        }

        return null;
    }

    public void markBookingConfirmed(Booking booking) {
        if (booking == null) return;
        removeFromQueue(pendingWalkInConfirmations, booking.getBookingID());
        confirmedBookings.enqueue(booking);
        saveAllBookings();
    }

    public boolean cancelBooking(String bookingID) {
        boolean removed = removeFromQueue(singleQueue, bookingID)
                || removeFromQueue(doubleQueue, bookingID)
                || removeFromQueue(deluxeQueue, bookingID)
                || removeFromQueue(executiveQueue, bookingID)
                || removeFromQueue(presidentialQueue, bookingID)
                || removeFromQueue(pendingWalkInConfirmations, bookingID);

        if (removed) {
            saveAllBookings();
        }
        return removed;
    }

    private boolean removeFromQueue(QueueInterface<Booking> queue, String bookingID) {
        if (queue.isEmpty() || bookingID == null) return false;

        QueueInterface<Booking> tempQueue = new LinkedQueue<>();
        boolean found = false;

        while (!queue.isEmpty()) {
            Booking current = queue.dequeue();
            if (current.getBookingID().equalsIgnoreCase(bookingID)) {
                found = true;
            } else {
                tempQueue.enqueue(current);
            }
        }

        while (!tempQueue.isEmpty()) {
            queue.enqueue(tempQueue.dequeue());
        }

        return found;
    }

    public Booking searchBooking(String bookingID) {
        Booking found = searchInQueue(singleQueue, bookingID);
        if (found == null) found = searchInQueue(doubleQueue, bookingID);
        if (found == null) found = searchInQueue(deluxeQueue, bookingID);
        if (found == null) found = searchInQueue(executiveQueue, bookingID);
        if (found == null) found = searchInQueue(presidentialQueue, bookingID);
        if (found == null) found = searchInQueue(pendingWalkInConfirmations, bookingID);
        if (found == null) found = searchInQueue(confirmedBookings, bookingID);
        return found;
    }

    private Booking searchInQueue(QueueInterface<Booking> queue, String bookingID) {
        if (queue.isEmpty() || bookingID == null) return null;

        QueueInterface<Booking> tempQueue = new LinkedQueue<>();
        Booking result = null;

        while (!queue.isEmpty()) {
            Booking current = queue.dequeue();
            if (current.getBookingID().equalsIgnoreCase(bookingID)) {
                result = current;
            }
            tempQueue.enqueue(current);
        }

        while (!tempQueue.isEmpty()) {
            queue.enqueue(tempQueue.dequeue());
        }

        return result;
    }

    public QueueInterface<Booking> getAllWaitingBookings() {
        QueueInterface<Booking> copyQueue = new LinkedQueue<>();
        copyQueueFrom(singleQueue, copyQueue);
        copyQueueFrom(doubleQueue, copyQueue);
        copyQueueFrom(deluxeQueue, copyQueue);
        copyQueueFrom(executiveQueue, copyQueue);
        copyQueueFrom(presidentialQueue, copyQueue);
        return copyQueue;
    }

    public QueueInterface<Booking> getAllBookingsIncludingConfirmed() {
        QueueInterface<Booking> all = getAllWaitingBookings();
        copyQueueFrom(pendingWalkInConfirmations, all);
        copyQueueFrom(confirmedBookings, all);
        return all;
    }

    public QueueInterface<Booking> getPendingWalkInConfirmations() {
        return pendingWalkInConfirmations;
    }

    public int getPendingConfirmationsCount() {
        return pendingWalkInConfirmations.size();
    }

    private void copyQueueFrom(QueueInterface<Booking> source, QueueInterface<Booking> destination) {
        QueueInterface<Booking> tempQueue = new LinkedQueue<>();
        while (!source.isEmpty()) {
            Booking b = source.dequeue();
            destination.enqueue(b);
            tempQueue.enqueue(b);
        }
        while (!tempQueue.isEmpty()) {
            source.enqueue(tempQueue.dequeue());
        }
    }

    public QueueInterface<Booking> getQueueByRoomType(String type) {
        if (type == null) return singleQueue;
        String lower = type.trim().toLowerCase();
        if (lower.contains("presidential")) return presidentialQueue;
        if (lower.contains("executive")) return executiveQueue;
        if (lower.contains("deluxe")) return deluxeQueue;
        if (lower.contains("double")) return doubleQueue;
        return singleQueue;
    }

    public int getAvailableRoomCountByType(String roomType) {
        QueueInterface<Room> tempQueue = new LinkedQueue<>();
        int count = 0;

        while (!availableRooms.isEmpty()) {
            Room r = availableRooms.dequeue();
            if (r.getRoomType().equalsIgnoreCase(roomType) || matchesRoomType(r.getRoomType(), roomType)) {
                count++;
            }
            tempQueue.enqueue(r);
        }

        while (!tempQueue.isEmpty()) {
            availableRooms.enqueue(tempQueue.dequeue());
        }

        return count;
    }

    private boolean matchesRoomType(String actual, String expected) {
        if (actual == null || expected == null) return false;
        return actual.trim().equalsIgnoreCase(expected.trim());
    }

    private Room extractRoomByType(String roomType) {
        QueueInterface<Room> tempQueue = new LinkedQueue<>();
        Room targetRoom = null;

        while (!availableRooms.isEmpty()) {
            Room current = availableRooms.dequeue();
            if (targetRoom == null && (current.getRoomType().equalsIgnoreCase(roomType) || matchesRoomType(current.getRoomType(), roomType))) {
                targetRoom = current;
            } else {
                tempQueue.enqueue(current);
            }
        }

        while (!tempQueue.isEmpty()) {
            availableRooms.enqueue(tempQueue.dequeue());
        }

        return targetRoom;
    }

    private void saveAllBookings() {
        FileHandler.saveBookings(singleQueue, doubleQueue, deluxeQueue, executiveQueue, presidentialQueue, pendingWalkInConfirmations, confirmedBookings);
    }

    public int getWaitingCount() { 
        return singleQueue.size() + doubleQueue.size() + deluxeQueue.size() + executiveQueue.size() + presidentialQueue.size(); 
    }

    public int getWaitingCountByType(String roomType) {
        return getQueueByRoomType(roomType).size();
    }

    public int getConfirmedCount() { 
        return confirmedBookings.size(); 
    }

    public void markRoomAvailable(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) return;

        // Check if room is already in availableRooms
        boolean exists = false;
        QueueInterface<Room> temp = new LinkedQueue<>();
        while (!availableRooms.isEmpty()) {
            Room r = availableRooms.dequeue();
            if (r.getRoomNumber().equalsIgnoreCase(roomNumber.trim())) {
                exists = true;
                r.setRoomStatus("Available");
            }
            temp.enqueue(r);
        }
        while (!temp.isEmpty()) {
            availableRooms.enqueue(temp.dequeue());
        }

        if (!exists) {
            java.util.List<Room> all = FileHandler.loadAllHotelRooms();
            for (Room r : all) {
                if (r.getRoomNumber().equalsIgnoreCase(roomNumber.trim())) {
                    r.setRoomStatus("Available");
                    availableRooms.enqueue(r);
                    break;
                }
            }
        }
        FileHandler.saveRooms(availableRooms);
    }

    public int getAvailableRoomsCount() { 
        return availableRooms.size(); 
    }

    /**
     * Peeks at the next booking in queue without removing it.
     * @param roomType
     * @return 
     */
    public Booking peekNextInQueue(String roomType) {
        QueueInterface<Booking> queue = getQueueByRoomType(roomType);
        if (queue.isEmpty()) return null;
        Booking next = queue.dequeue();
        // Put it back at front using a temp queue
        QueueInterface<Booking> temp = new LinkedQueue<>();
        temp.enqueue(next);
        while (!queue.isEmpty()) {
            temp.enqueue(queue.dequeue());
        }
        while (!temp.isEmpty()) {
            queue.enqueue(temp.dequeue());
        }
        return next;
    }

    /**
     * Displays available rooms filtered by type AND date range (for the Assign Room screen).
     * @param roomType
     * @param checkIn
     * @param checkOut
     * @return 
     */
    public boolean displayAvailableRoomsForType(String roomType, LocalDate checkIn, LocalDate checkOut) {
        System.out.println("\n==================================================================================");
        System.out.printf(  " Available %-25s Rooms for Stay: %s to %s\n", roomType, checkIn, checkOut);
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf(" %-15s | %-25s | %-15s\n", "Room Number", "Room Type", "Status");
        System.out.println("----------------------------------------------------------------------------------");

        java.util.List<Room> allRooms = utility.FileHandler.loadAllHotelRooms();
        QueueInterface<Booking> allBookings = getAllBookingsIncludingConfirmed();

        java.util.Map<String, entity.HousekeepingTask> activeTasks = new java.util.HashMap<>();
        if (this.houseKeepingControl != null) {
            activeTasks = this.houseKeepingControl.getAllActiveTasksMap();
        }

        adt.TreeInterface<entity.Reservation> resTree = new adt.BinarySearchTreeADT<>();
        utility.FileHandler.loadReservations(resTree);

        int count = 0;
        java.util.List<String> notCleanRooms = new java.util.ArrayList<>();

        for (Room room : allRooms) {
            // Filter to requested room type only
            if (!room.getRoomType().equalsIgnoreCase(roomType)) continue;

            boolean isAvailableForDates = true;

            // Check FrontDesk Reservations — use actual dates
            java.util.Iterator<entity.Reservation> it = resTree.getInorderIterator();
            while (it.hasNext()) {
                entity.Reservation r = it.next();
                if (room.getRoomNumber().equalsIgnoreCase(r.getRoomNumber()) && !"Checked-Out".equalsIgnoreCase(r.getStatus())) {
                    LocalDate rIn = null;
                    LocalDate rOut = null;
                    try {
                        if (r.getCheckInDate() != null && !r.getCheckInDate().isEmpty())
                            rIn = LocalDate.parse(r.getCheckInDate());
                        if (r.getCheckOutDate() != null && !r.getCheckOutDate().isEmpty())
                            rOut = LocalDate.parse(r.getCheckOutDate());
                    } catch (Exception ignored) {}
                    if (rIn == null) rIn = LocalDate.now();
                    if (rOut == null) rOut = rIn.plusDays(r.getStayDurationDays());

                    boolean overlaps = !(rOut.isBefore(checkIn) || rIn.isAfter(checkOut));
                    if (overlaps) {
                        isAvailableForDates = false;
                        break;
                    }
                }
            }

            // Check existing Waiting/Pending Bookings overlap (only ones with assigned rooms)
            if (isAvailableForDates) {
                QueueInterface<Booking> temp = new LinkedQueue<>();
                while (!allBookings.isEmpty()) {
                    Booking b = allBookings.dequeue();
                    temp.enqueue(b);
                    if (isAvailableForDates && b.getRoom() != null && b.getRoom().getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
                        if (b.getBookingStatus() != null && b.getBookingStatus().startsWith("Confirmed")) {
                            try {
                                LocalDate bIn = LocalDate.parse(b.getCheckInDate());
                                LocalDate bOut = LocalDate.parse(b.getCheckOutDate());
                                if (!(bOut.isBefore(checkIn) || bIn.isAfter(checkOut))) {
                                    isAvailableForDates = false;
                                }
                            } catch (Exception ignored) {
                                isAvailableForDates = false;
                            }
                        }
                    }
                }
                while (!temp.isEmpty()) allBookings.enqueue(temp.dequeue());
            }

            if (isAvailableForDates) {
                // Now check Housekeeping Status
                boolean isClean = true;
                String hkStatus = "Dirty";
                if (activeTasks.containsKey(room.getRoomNumber().toUpperCase())) {
                    entity.HousekeepingTask t = activeTasks.get(room.getRoomNumber().toUpperCase());
                    if (t != null) {
                        hkStatus = t.getStatus();
                        if (!t.getStatus().equalsIgnoreCase("Ready for Check-In")) {
                            isClean = false;
                        }
                    } else {
                        isClean = false;
                    }
                }

                if (isClean) {
                    System.out.printf(" %-15s | %-25s | %-15s\n", room.getRoomNumber(), room.getRoomType(), "AVAILABLE");
                    count++;
                } else {
                    notCleanRooms.add(room.getRoomNumber() + " (" + hkStatus + ")");
                }
            }
        }

        System.out.println("==================================================================================");
        System.out.println(" Total Available: " + count);
        
        if (!notCleanRooms.isEmpty()) {
            System.out.println("\n  [ NOTE ] The following rooms are free for these dates but NOT CLEAN YET:");
            for (String roomStr : notCleanRooms) {
                System.out.println("   * Room " + roomStr);
            }
        }

        return count > 0;
    }

    /**
     * Assigns the next guest in queue to the first room that is free for their specific dates.
     */
    public Booking assignRoomByRoomTypeAndDates(String roomType, LocalDate checkIn, LocalDate checkOut) {
        QueueInterface<Booking> targetQueue = getQueueByRoomType(roomType);
        if (targetQueue.isEmpty()) return null;

        java.util.List<Room> allRooms = utility.FileHandler.loadAllHotelRooms();
        QueueInterface<Booking> allBookings = getAllBookingsIncludingConfirmed();

        java.util.Map<String, entity.HousekeepingTask> activeTasks = new java.util.HashMap<>();
        if (this.houseKeepingControl != null) {
            activeTasks = this.houseKeepingControl.getAllActiveTasksMap();
        }
        adt.TreeInterface<entity.Reservation> resTree = new adt.BinarySearchTreeADT<>();
        utility.FileHandler.loadReservations(resTree);

        // Find first available room of this type for the requested dates
        Room chosenRoom = null;
        for (Room room : allRooms) {
            if (!room.getRoomType().equalsIgnoreCase(roomType)) continue;

            boolean isAvailable = true;

            // Check Housekeeping Status — always reflects current cleanliness state
            if (activeTasks.containsKey(room.getRoomNumber().toUpperCase())) {
                entity.HousekeepingTask t = activeTasks.get(room.getRoomNumber().toUpperCase());
                if (t != null && !t.getStatus().equalsIgnoreCase("Ready for Check-In")) {
                    isAvailable = false;
                }
            }

            if (isAvailable) {
                java.util.Iterator<entity.Reservation> it = resTree.getInorderIterator();
                while (it.hasNext()) {
                    entity.Reservation r = it.next();
                    if (room.getRoomNumber().equalsIgnoreCase(r.getRoomNumber()) && !"Checked-Out".equalsIgnoreCase(r.getStatus())) {
                        LocalDate rIn = null;
                        LocalDate rOut = null;
                        try {
                            if (r.getCheckInDate() != null && !r.getCheckInDate().isEmpty())
                                rIn = LocalDate.parse(r.getCheckInDate());
                            if (r.getCheckOutDate() != null && !r.getCheckOutDate().isEmpty())
                                rOut = LocalDate.parse(r.getCheckOutDate());
                        } catch (Exception ignored) {}
                        if (rIn == null) rIn = LocalDate.now();
                        if (rOut == null) rOut = rIn.plusDays(r.getStayDurationDays());
                        if (!(rOut.isBefore(checkIn) || rIn.isAfter(checkOut))) {
                            isAvailable = false;
                            break;
                        }
                    }
                }
            }

            QueueInterface<Booking> temp = new LinkedQueue<>();
            while (!allBookings.isEmpty()) {
                Booking b = allBookings.dequeue();
                temp.enqueue(b);
                if (isAvailable && b.getRoom() != null && b.getRoom().getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
                    if (b.getBookingStatus() != null && b.getBookingStatus().startsWith("Confirmed")) {
                        try {
                            LocalDate bIn = LocalDate.parse(b.getCheckInDate());
                            LocalDate bOut = LocalDate.parse(b.getCheckOutDate());
                            if (!(bOut.isBefore(checkIn) || bIn.isAfter(checkOut))) {
                                isAvailable = false;
                            }
                        } catch (Exception ignored) {
                            isAvailable = false;
                        }
                    }
                }
            }
            while (!temp.isEmpty()) allBookings.enqueue(temp.dequeue());

            if (isAvailable) {
                chosenRoom = room;
                break;
            }
        }

        if (chosenRoom == null) return null;

        // Dequeue next guest and assign the chosen room
        Booking nextBooking = targetQueue.dequeue();
        chosenRoom.setRoomStatus("Occupied");
        nextBooking.setRoom(chosenRoom);
        nextBooking.setBookingStatus("Assigned - Pending Front Desk Confirmation");

        // Remove it from the availableRooms queue too
        QueueInterface<Room> tempRooms = new LinkedQueue<>();
        while (!availableRooms.isEmpty()) {
            Room r = availableRooms.dequeue();
            if (!r.getRoomNumber().equalsIgnoreCase(chosenRoom.getRoomNumber())) {
                tempRooms.enqueue(r);
            }
        }
        while (!tempRooms.isEmpty()) availableRooms.enqueue(tempRooms.dequeue());

        pendingWalkInConfirmations.enqueue(nextBooking);
        if (frontDeskControl != null) {
            frontDeskControl.addPendingWalkInBooking(nextBooking);
        }

        saveAllBookings();
        FileHandler.saveRooms(availableRooms);
        return nextBooking;
    }

    // =========================================================================
    // MULTI-DAY AVAILABILITY CHECK (for Register Walk-In - Option 1)
    // =========================================================================
    public boolean displayAvailableRoomsForStay(LocalDate checkIn, LocalDate checkOut) {
        System.out.println("\n==================================================================================");
        System.out.println("                         MULTI-DAY ROOM AVAILABILITY                             ");
        System.out.println("==================================================================================");
        System.out.printf(" Requested Stay : %s to %s\n", checkIn, checkOut);
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf(" %-15s | %-25s | %-15s\n", "Room Number", "Room Type", "Status");
        System.out.println("----------------------------------------------------------------------------------");

        QueueInterface<Booking> allBookings = getAllBookingsIncludingConfirmed();
        java.util.List<Room> allRooms = utility.FileHandler.loadAllHotelRooms();
        
        // 1. Integrate with HouseKeeping Module (No housekeeping block for registration)
        java.util.Map<String, entity.HousekeepingTask> activeTasks = new java.util.HashMap<>();
        if (this.houseKeepingControl != null) {
            activeTasks = this.houseKeepingControl.getAllActiveTasksMap();
        }
        
        // Load active reservations from Front Desk
        adt.TreeInterface<entity.Reservation> resTree = new adt.BinarySearchTreeADT<>();
        utility.FileHandler.loadReservations(resTree);

        // Count overlapping waiting bookings (Waiting status, room == null) for each room type
        java.util.Map<String, Integer> waitingCountMap = new java.util.HashMap<>();
        waitingCountMap.put("Standard Single", 0);
        waitingCountMap.put("Standard Double", 0);
        waitingCountMap.put("Deluxe Suite", 0);
        waitingCountMap.put("Executive Suite", 0);
        waitingCountMap.put("Presidential Suite", 0);

        QueueInterface<Booking> tempBookings = new LinkedQueue<>();
        while (!allBookings.isEmpty()) {
            Booking b = allBookings.dequeue();
            tempBookings.enqueue(b);

            if (b.getRoom() == null && b.getBookingStatus() != null && b.getBookingStatus().equalsIgnoreCase("Waiting")) {
                try {
                    LocalDate bCheckIn = LocalDate.parse(b.getCheckInDate());
                    LocalDate bCheckOut = LocalDate.parse(b.getCheckOutDate());
                    boolean overlaps = !(bCheckOut.isBefore(checkIn) || bCheckIn.isAfter(checkOut));
                    if (overlaps) {
                        String type = b.getRequestedRoomType();
                        if (type != null) {
                            String matchedKey = null;
                            for (String key : waitingCountMap.keySet()) {
                                if (key.equalsIgnoreCase(type.trim()) || type.trim().toLowerCase().contains(key.toLowerCase())) {
                                    matchedKey = key;
                                    break;
                                }
                            }
                            if (matchedKey != null) {
                                waitingCountMap.put(matchedKey, waitingCountMap.get(matchedKey) + 1);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        while (!tempBookings.isEmpty()) {
            allBookings.enqueue(tempBookings.dequeue());
        }

        // Get physically available rooms (not reserved/occupied/assigned)
        java.util.List<Room> physAvailableRooms = new java.util.ArrayList<>();
        for (Room room : allRooms) {
            boolean isAvailable = true;
            
            // Check FrontDesk Reservations — use actual dates
            java.util.Iterator<entity.Reservation> it = resTree.getInorderIterator();
            while (it.hasNext()) {
                entity.Reservation r = it.next();
                if (room.getRoomNumber().equalsIgnoreCase(r.getRoomNumber()) && !"Checked-Out".equalsIgnoreCase(r.getStatus())) {
                    LocalDate rIn = null;
                    LocalDate rOut = null;
                    try {
                        if (r.getCheckInDate() != null && !r.getCheckInDate().isEmpty())
                            rIn = LocalDate.parse(r.getCheckInDate());
                        if (r.getCheckOutDate() != null && !r.getCheckOutDate().isEmpty())
                            rOut = LocalDate.parse(r.getCheckOutDate());
                    } catch (Exception ignored) {}
                    if (rIn == null) rIn = LocalDate.now();
                    if (rOut == null) rOut = rIn.plusDays(r.getStayDurationDays());

                    boolean overlaps = !(rOut.isBefore(checkIn) || rIn.isAfter(checkOut));
                    if (overlaps) {
                        isAvailable = false;
                        break;
                    }
                }
            }

            // Check existing Waiting/Pending Bookings overlap (only ones with assigned rooms)
            if (isAvailable) {
                QueueInterface<Booking> temp = new LinkedQueue<>();
                while (!allBookings.isEmpty()) {
                    Booking b = allBookings.dequeue();
                    temp.enqueue(b);
                    
                    if (isAvailable && b.getRoom() != null && b.getRoom().getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
                        if (b.getBookingStatus() != null && b.getBookingStatus().startsWith("Confirmed")) {
                            try {
                                LocalDate bCheckIn = LocalDate.parse(b.getCheckInDate());
                                LocalDate bCheckOut = LocalDate.parse(b.getCheckOutDate());
                                
                                boolean overlaps = !(bCheckOut.isBefore(checkIn) || bCheckIn.isAfter(checkOut));
                                if (overlaps) {
                                    isAvailable = false;
                                }
                            } catch (Exception e) {
                                isAvailable = false;
                            }
                        }
                    }
                }
                while (!temp.isEmpty()) {
                    allBookings.enqueue(temp.dequeue());
                }
            }

            if (isAvailable) {
                physAvailableRooms.add(room);
            }
        }

        // Group physically available rooms by room type
        java.util.Map<String, java.util.List<Room>> roomsByType = new java.util.HashMap<>();
        roomsByType.put("Standard Single", new java.util.ArrayList<>());
        roomsByType.put("Standard Double", new java.util.ArrayList<>());
        roomsByType.put("Deluxe Suite", new java.util.ArrayList<>());
        roomsByType.put("Executive Suite", new java.util.ArrayList<>());
        roomsByType.put("Presidential Suite", new java.util.ArrayList<>());

        for (Room r : physAvailableRooms) {
            String type = r.getRoomType();
            String matchedKey = null;
            for (String key : roomsByType.keySet()) {
                if (key.equalsIgnoreCase(type.trim()) || type.trim().toLowerCase().contains(key.toLowerCase())) {
                    matchedKey = key;
                    break;
                }
            }
            if (matchedKey != null) {
                roomsByType.get(matchedKey).add(r);
            }
        }

        // Print rooms up to the net available count (physAvailable - waitingCount)
        int totalAvailableCount = 0;
        String[] typesOrder = {"Standard Single", "Standard Double", "Deluxe Suite", "Executive Suite", "Presidential Suite"};
        for (String type : typesOrder) {
            java.util.List<Room> typeRooms = roomsByType.get(type);
            int waiting = waitingCountMap.get(type);
            int net = typeRooms.size() - waiting;
            if (net > 0) {
                for (int i = 0; i < net && i < typeRooms.size(); i++) {
                    Room room = typeRooms.get(i);
                    System.out.printf(" %-15s | %-25s | %-15s\n", room.getRoomNumber(), room.getRoomType(), "AVAILABLE");
                    totalAvailableCount++;
                }
            }
        }
        
        System.out.println("==================================================================================");
        System.out.println(" Total Available Rooms for Stay: " + totalAvailableCount);
        
        return totalAvailableCount > 0;
    }


    /**
     * Checks if at least one room of the requested type is available for the stay dates.
     */
    public boolean isRoomTypeAvailableForStay(String roomType, LocalDate checkIn, LocalDate checkOut) {
        java.util.List<Room> allRooms = utility.FileHandler.loadAllHotelRooms();
        QueueInterface<Booking> allBookings = getAllBookingsIncludingConfirmed();
        
        adt.TreeInterface<entity.Reservation> resTree = new adt.BinarySearchTreeADT<>();
        utility.FileHandler.loadReservations(resTree);

        // Count overlapping waiting bookings (Waiting status, room == null) for this room type
        int waitingCount = 0;
        QueueInterface<Booking> tempBookings = new LinkedQueue<>();
        while (!allBookings.isEmpty()) {
            Booking b = allBookings.dequeue();
            tempBookings.enqueue(b);

            if (b.getRoom() == null && b.getBookingStatus() != null && b.getBookingStatus().equalsIgnoreCase("Waiting")) {
                String type = b.getRequestedRoomType();
                if (type != null && (type.equalsIgnoreCase(roomType.trim()) || type.trim().toLowerCase().contains(roomType.trim().toLowerCase()))) {
                    try {
                        LocalDate bCheckIn = LocalDate.parse(b.getCheckInDate());
                        LocalDate bCheckOut = LocalDate.parse(b.getCheckOutDate());
                        boolean overlaps = !(bCheckOut.isBefore(checkIn) || bCheckIn.isAfter(checkOut));
                        if (overlaps) {
                            waitingCount++;
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        while (!tempBookings.isEmpty()) {
            allBookings.enqueue(tempBookings.dequeue());
        }

        // Get physically available rooms of this type
        int physCount = 0;
        for (Room room : allRooms) {
            if (!room.getRoomType().equalsIgnoreCase(roomType) && !room.getRoomType().toLowerCase().contains(roomType.toLowerCase())) continue;

            boolean isAvailable = true;

            // Check FrontDesk Reservations
            java.util.Iterator<entity.Reservation> it = resTree.getInorderIterator();
            while (it.hasNext()) {
                entity.Reservation r = it.next();
                if (room.getRoomNumber().equalsIgnoreCase(r.getRoomNumber()) && !"Checked-Out".equalsIgnoreCase(r.getStatus())) {
                    LocalDate rIn = null;
                    LocalDate rOut = null;
                    try {
                        if (r.getCheckInDate() != null && !r.getCheckInDate().isEmpty())
                            rIn = LocalDate.parse(r.getCheckInDate());
                        if (r.getCheckOutDate() != null && !r.getCheckOutDate().isEmpty())
                            rOut = LocalDate.parse(r.getCheckOutDate());
                    } catch (Exception ignored) {}
                    if (rIn == null) rIn = LocalDate.now();
                    if (rOut == null) rOut = rIn.plusDays(r.getStayDurationDays());

                    boolean overlaps = !(rOut.isBefore(checkIn) || rIn.isAfter(checkOut));
                    if (overlaps) {
                        isAvailable = false;
                        break;
                    }
                }
            }

            if (isAvailable) {
                QueueInterface<Booking> temp = new LinkedQueue<>();
                while (!allBookings.isEmpty()) {
                    Booking b = allBookings.dequeue();
                    temp.enqueue(b);
                    if (isAvailable && b.getRoom() != null && b.getRoom().getRoomNumber().equalsIgnoreCase(room.getRoomNumber())) {
                        if (b.getBookingStatus() != null && !b.getBookingStatus().equalsIgnoreCase("Cancelled")) {
                            try {
                                LocalDate bCheckIn = LocalDate.parse(b.getCheckInDate());
                                LocalDate bCheckOut = LocalDate.parse(b.getCheckOutDate());
                                boolean overlaps = !(bCheckOut.isBefore(checkIn) || bCheckIn.isAfter(checkOut));
                                if (overlaps) {
                                    isAvailable = false;
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
                while (!temp.isEmpty()) {
                    allBookings.enqueue(temp.dequeue());
                }
            }

            if (isAvailable) {
                physCount++; 
            }
        }

        return (physCount - waitingCount) > 0;
    }


    // =========================================================================

    // REPORT 1: Booking Analytics Report
    // =========================================================================
    public void generateBookingAnalyticsReport(String startDate, String endDate, String roomTypeFilter) {
        // Queue ADT to hold filtered results for sorting
        QueueInterface<Booking> filteredQueue = new LinkedQueue<>();
        QueueInterface<Booking> allBookings = getAllBookingsIncludingConfirmed();
        String targetRoom = (roomTypeFilter == null || roomTypeFilter.equalsIgnoreCase("ALL")) ? "" : roomTypeFilter.toLowerCase();

        // 1. FILTERING & SEARCHING (Multiple Criteria: Date Range AND Room Type)
        while (!allBookings.isEmpty()) {
            Booking b = allBookings.dequeue();
            String checkIn = b.getCheckInDate();
            String reqType = b.getRequestedRoomType() != null ? b.getRequestedRoomType().toLowerCase() : "";

            // Multiple Filter Criteria: Date range check & Room Type check
            boolean matchesDate = (checkIn != null && isWithinDateRange(checkIn, startDate, endDate));
            boolean matchesRoom = targetRoom.isEmpty() || reqType.contains(targetRoom);

            if (matchesDate && matchesRoom) {
                filteredQueue.enqueue(b);
            }
        }

        int totalCount = filteredQueue.size();

        // 2. SORTING: Convert Queue to array temporarily to perform Bubble Sort
        Booking[] array = queueToArray(filteredQueue);
        sortBookingsByGuestsDescending(array);

        // 3. CONSOLE REPORT OUTPUT
        System.out.println("\n==================================================================================");
        System.out.println("                         BOOKING ANALYTICS REPORT                                ");
        System.out.println("==================================================================================");
        System.out.printf(" Filter Period : %s to %s\n", startDate, endDate);
        System.out.printf(" Room Filter   : %s\n", targetRoom.isEmpty() ? "ALL ROOM TYPES" : roomTypeFilter.toUpperCase());
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf(" %-30s | %-18d \n", "Total Filtered Bookings", totalCount);
        System.out.println("==================================================================================");
        
        System.out.println("\n--- FILTERED BOOKINGS SUMMARY (SORTED BY NUMBER OF GUESTS DESCENDING) ---");
        System.out.printf(" %-12s | %-12s | %-20s | %-12s | %-10s\n", "Booking ID", "Guest ID", "Guest Name", "Check-In", "Guests");
        System.out.println("----------------------------------------------------------------------------------");

        if (array.length == 0) {
            System.out.println("                     No records found for the given date range.                  ");
        } else {
            for (Booking b : array) {
                String guestId = (b.getGuest() != null) ? b.getGuest().getGuestID() : "N/A";
                String guestName = (b.getGuest() != null) ? b.getGuest().getGuestName() : "N/A";
                System.out.printf(" %-12s | %-12s | %-20s | %-12s | %-10d\n",
                        b.getBookingID(), guestId, guestName, b.getCheckInDate(), b.getNumberOfGuests());
            }
        }
        System.out.println("==================================================================================\n");
    }

    // =========================================================================
    // REPORT 2: Room Type Demand & Walk-In Conversion Report
    // =========================================================================
    public void generateRoomTypeDemandReport(String filterRoomType, String statusFilter, int minGuests) {
        int totalRequests = 0;
        int assignedCount = 0;
        int waitingCount = 0;

        QueueInterface<Booking> matchedQueue = new LinkedQueue<>();
        QueueInterface<Booking> allBookings = getAllBookingsIncludingConfirmed();

        String targetType = (filterRoomType == null || filterRoomType.equalsIgnoreCase("ALL")) ? "" : filterRoomType.trim().toLowerCase();
        String targetStatus = (statusFilter == null || statusFilter.equalsIgnoreCase("ALL")) ? "" : statusFilter.trim().toLowerCase();

        // 1. SEARCHING & FILTERING (Multiple Criteria: Room Type, Status, and Min Guests)
        while (!allBookings.isEmpty()) {
            Booking b = allBookings.dequeue();
            String reqType = b.getRequestedRoomType() != null ? b.getRequestedRoomType().toLowerCase() : "";
            String assignedType = (b.getRoom() != null && b.getRoom().getRoomType() != null) ? b.getRoom().getRoomType().toLowerCase() : "";
            String currentStatus = (b.getBookingStatus() != null) ? b.getBookingStatus().toLowerCase() : "";

            // Multiple Filter Criteria
            boolean matchesRoom = targetType.isEmpty() || reqType.contains(targetType) || assignedType.contains(targetType);
            boolean matchesStatus = targetStatus.isEmpty() || currentStatus.contains(targetStatus);
            boolean matchesGuests = b.getNumberOfGuests() >= minGuests;

            if (matchesRoom && matchesStatus && matchesGuests) {
                matchedQueue.enqueue(b);
                totalRequests++;

                if (b.getRoom() != null) {
                    assignedCount++;
                } else {
                    waitingCount++;
                }
            }
        }

        // 2. SORTING: Sort matched queue by Booking ID
        Booking[] array = queueToArray(matchedQueue);
        sortBookingsByID(array);

        // 3. CONSOLE REPORT OUTPUT
        double conversionRate = totalRequests > 0 ? ((double) assignedCount / totalRequests) * 100 : 0.0;

        System.out.println("\n==================================================================================");
        System.out.println("                  ROOM TYPE DEMAND & WALK-IN CONVERSION REPORT                   ");
        System.out.println("==================================================================================");
        System.out.printf(" Room Type Filter : %s\n", targetType.isEmpty() ? "ALL ROOM TYPES" : filterRoomType.toUpperCase());
        System.out.printf(" Status Filter    : %s\n", targetStatus.isEmpty() ? "ALL STATUSES" : statusFilter.toUpperCase());
        System.out.printf(" Minimum Guests   : %d\n", minGuests);
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf(" %-30s : %d\n", "Total Room Demands/Requests", totalRequests);
        System.out.printf(" %-30s : %d\n", "Successfully Assigned Rooms", assignedCount);
        System.out.printf(" %-30s : %d\n", "Pending / In Waiting Queue", waitingCount);
        System.out.printf(" %-30s : %.2f%%\n", "Walk-In Room Conversion Rate", conversionRate);
        System.out.println("==================================================================================");
        System.out.printf(" %-12s | %-15s | %-15s | %-12s | %-15s\n", "Booking ID", "Requested", "Assigned Room", "Check-In", "Status");
        System.out.println("----------------------------------------------------------------------------------");

        if (array.length == 0) {
            System.out.println("                       No matching booking records found.                         ");
        } else {
            for (Booking b : array) {
                String req = b.getRequestedRoomType() != null ? b.getRequestedRoomType() : "N/A";
                String roomNum = (b.getRoom() != null) ? b.getRoom().getRoomNumber() : "UNASSIGNED";
                String status = (b.getBookingStatus() != null) ? b.getBookingStatus() : "Waiting";

                System.out.printf(" %-12s | %-15s | %-15s | %-12s | %-15s\n",
                        b.getBookingID(), req, roomNum, b.getCheckInDate(), status);
            }
        }
        System.out.println("==================================================================================\n");
    }

    // =========================================================================
    // HELPER ALGORITHMS & UTILITIES
    // =========================================================================
    
    // Bubble Sort: Sorts Bookings by Number of Guests (Descending)
    private void sortBookingsByGuestsDescending(Booking[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j].getNumberOfGuests() < arr[j + 1].getNumberOfGuests()) {
                    Booking temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    // Bubble Sort: Sorts Bookings by Booking ID (Ascending)
    private void sortBookingsByID(Booking[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j].getBookingID().compareToIgnoreCase(arr[j + 1].getBookingID()) > 0) {
                    Booking temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    // Helper to extract Queue elements into an Array for sorting without losing Queue reference
    private Booking[] queueToArray(QueueInterface<Booking> queue) {
        int size = queue.size();
        Booking[] arr = new Booking[size];
        QueueInterface<Booking> temp = new LinkedQueue<>();

        int idx = 0;
        while (!queue.isEmpty()) {
            Booking b = queue.dequeue();
            arr[idx++] = b;
            temp.enqueue(b);
        }

        while (!temp.isEmpty()) {
            queue.enqueue(temp.dequeue());
        }

        return arr;
    }

    // Date comparison helper function
    private boolean isWithinDateRange(String date, String start, String end) {
        try {
            LocalDate current = LocalDate.parse(date);
            LocalDate s = LocalDate.parse(start);
            LocalDate e = LocalDate.parse(end);
            return (!current.isBefore(s)) && (!current.isAfter(e));
        } catch (Exception ex) {
            return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
        }
    }
}
