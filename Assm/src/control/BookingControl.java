package control;

import adt.LinkedQueue;
import adt.QueueInterface;
import entity.Booking;
import entity.Guest;
import entity.Room;
import java.time.LocalDate;

/**
 * Controller class managing Queue ADT operations for Walk-In Bookings & Room Assignments.
 */
public class BookingControl {

    private final QueueInterface<Booking> waitingQueue;
    private final QueueInterface<Room> availableRooms;
    private final QueueInterface<Booking> confirmedBookings;

    private int guestCounter = 101;
    private int bookingCounter = 1001;

    public BookingControl() {
        this.waitingQueue = new LinkedQueue<>();
        this.availableRooms = new LinkedQueue<>();
        this.confirmedBookings = new LinkedQueue<>();

        initializeRooms();
        initializeMockData();
    }

    /**
     * Set up multiple room instances to represent quantities for each Room Type.
     */
    private void initializeRooms() {
        // Standard Single Rooms (Quantity: 3)
        availableRooms.enqueue(new Room("R101", "101", "Standard Single", "Available"));
        availableRooms.enqueue(new Room("R102", "102", "Standard Single", "Available"));
        availableRooms.enqueue(new Room("R103", "103", "Standard Single", "Available"));

        // Standard Double Rooms (Quantity: 3)
        availableRooms.enqueue(new Room("R104", "104", "Standard Double", "Available"));
        availableRooms.enqueue(new Room("R105", "105", "Standard Double", "Available"));
        availableRooms.enqueue(new Room("R106", "106", "Standard Double", "Available"));

        // Deluxe Suites (Quantity: 2)
        availableRooms.enqueue(new Room("R201", "201", "Deluxe Suite", "Available"));
        availableRooms.enqueue(new Room("R202", "202", "Deluxe Suite", "Available"));

        // Executive Suites (Quantity: 2)
        availableRooms.enqueue(new Room("R301", "301", "Executive Suite", "Available"));
        availableRooms.enqueue(new Room("R302", "302", "Executive Suite", "Available"));
    }

    private void initializeMockData() {
        Guest g1 = new Guest("G101", "Alice Tan", "Female", "012-3456789", "alice@gmail.com", "IC950101");
        Guest g2 = new Guest("G102", "Bob Lee", "Male", "013-9876543", "bob@gmail.com", "IC880202");
        Guest g3 = new Guest("G103", "Charlie Wong", "Male", "014-1122334", "charlie@gmail.com", "IC990303");

        String today = LocalDate.now().toString();

        Booking b1 = new Booking("B1001", today, today, LocalDate.now().plusDays(4).toString(), 2, "Waiting", g1, null);
        b1.setRequestedRoomType("Standard Single");

        Booking b2 = new Booking("B1002", today, LocalDate.now().plusDays(1).toString(), LocalDate.now().plusDays(2).toString(), 1, "Waiting", g2, null);
        b2.setRequestedRoomType("Standard Double");

        Booking b3 = new Booking("B1003", today, today, LocalDate.now().plusDays(3).toString(), 3, "Waiting", g3, null);
        b3.setRequestedRoomType("Deluxe Suite");

        waitingQueue.enqueue(b1);
        waitingQueue.enqueue(b2);
        waitingQueue.enqueue(b3);

        // Advance counters past pre-loaded data
        this.guestCounter = 104;
        this.bookingCounter = 1004;
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
        
        // Save requested room type preference
        newBooking.setRequestedRoomType(requestedRoomType);

        waitingQueue.enqueue(newBooking);
        return newBooking;
    }

    /**
     * Assigns a room matching the preferred room type of the next guest in line.
     * If no room of that type is available, moves the guest to the back of the queue.
     */
    public Booking assignRoomToNextGuest() {
        if (waitingQueue.isEmpty() || availableRooms.isEmpty()) {
            return null;
        }

        Booking nextBooking = waitingQueue.dequeue();
        String preferredType = nextBooking.getRequestedRoomType();

        QueueInterface<Room> tempRoomQueue = new LinkedQueue<>();
        Room assignedRoom = null;

        // Search availableRooms queue for a room matching guest preference
        while (!availableRooms.isEmpty()) {
            Room currentRoom = availableRooms.dequeue();
            if (assignedRoom == null && currentRoom.getRoomType().equalsIgnoreCase(preferredType)) {
                assignedRoom = currentRoom; // Found a match!
            } else {
                tempRoomQueue.enqueue(currentRoom); // Keep searching
            }
        }

        // Restore remaining available rooms back to availableRooms queue
        while (!tempRoomQueue.isEmpty()) {
            availableRooms.enqueue(tempRoomQueue.dequeue());
        }

        // If a matching room was found
        if (assignedRoom != null) {
            assignedRoom.setRoomStatus("Occupied");
            nextBooking.setRoom(assignedRoom);
            nextBooking.setBookingStatus("Confirmed");
            confirmedBookings.enqueue(nextBooking);
            return nextBooking;
        } else {
            // No room matching preferred type currently available.
            // Move guest to the back of the queue so other guests can be served.
            waitingQueue.enqueue(nextBooking);
            return null;
        }
    }

    public boolean cancelBooking(String bookingID) {
        if (waitingQueue.isEmpty()) return false;

        QueueInterface<Booking> tempQueue = new LinkedQueue<>();
        boolean found = false;

        while (!waitingQueue.isEmpty()) {
            Booking current = waitingQueue.dequeue();
            if (current.getBookingID().equalsIgnoreCase(bookingID)) {
                found = true; // Dropped / Cancelled
            } else {
                tempQueue.enqueue(current);
            }
        }

        while (!tempQueue.isEmpty()) {
            waitingQueue.enqueue(tempQueue.dequeue());
        }

        return found;
    }

    public Booking searchBooking(String bookingID) {
        Booking found = searchInQueue(waitingQueue, bookingID);
        if (found == null) {
            found = searchInQueue(confirmedBookings, bookingID);
        }
        return found;
    }

    private Booking searchInQueue(QueueInterface<Booking> queue, String bookingID) {
        if (queue.isEmpty()) return null;

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

    public QueueInterface<Booking> generateGroupSizeFilterReport(int minGuests) {
        QueueInterface<Booking> filteredResults = new LinkedQueue<>();
        QueueInterface<Booking> tempQueue = new LinkedQueue<>();

        while (!waitingQueue.isEmpty()) {
            Booking current = waitingQueue.dequeue();
            if (current.getNumberOfGuests() >= minGuests) {
                filteredResults.enqueue(current);
            }
            tempQueue.enqueue(current);
        }

        while (!tempQueue.isEmpty()) {
            waitingQueue.enqueue(tempQueue.dequeue());
        }

        return filteredResults;
    }

    public Booking[] generateSortedDurationReport() {
        int count = waitingQueue.size();
        if (count == 0) return new Booking[0];

        Booking[] bookingArray = new Booking[count];
        QueueInterface<Booking> tempQueue = new LinkedQueue<>();

        int idx = 0;
        while (!waitingQueue.isEmpty()) {
            Booking current = waitingQueue.dequeue();
            bookingArray[idx++] = current;
            tempQueue.enqueue(current);
        }

        while (!tempQueue.isEmpty()) {
            waitingQueue.enqueue(tempQueue.dequeue());
        }

        // Selection Sort (Descending by stay duration)
        for (int i = 0; i < count - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < count; j++) {
                int durationJ = calculateNights(bookingArray[j]);
                int durationMax = calculateNights(bookingArray[maxIdx]);

                if (durationJ > durationMax) {
                    maxIdx = j;
                }
            }
            // Swap
            Booking temp = bookingArray[maxIdx];
            bookingArray[maxIdx] = bookingArray[i];
            bookingArray[i] = temp;
        }

        return bookingArray;
    }

    public QueueInterface<Booking> getAllWaitingBookings() {
        QueueInterface<Booking> copyQueue = new LinkedQueue<>();
        QueueInterface<Booking> tempQueue = new LinkedQueue<>();

        while (!waitingQueue.isEmpty()) {
            Booking b = waitingQueue.dequeue();
            copyQueue.enqueue(b);
            tempQueue.enqueue(b);
        }

        while (!tempQueue.isEmpty()) {
            waitingQueue.enqueue(tempQueue.dequeue());
        }

        return copyQueue;
    }

    public int calculateNights(Booking b) {
        try {
            LocalDate inDate = LocalDate.parse(b.getCheckInDate());
            LocalDate outDate = LocalDate.parse(b.getCheckOutDate());
            long nights = java.time.temporal.ChronoUnit.DAYS.between(inDate, outDate);
            return (int) Math.max(1, nights);
        } catch (Exception e) {
            return 1;
        }
    }

    // Getters for Queue States
    public QueueInterface<Booking> getWaitingQueue() { return waitingQueue; }
    public QueueInterface<Room> getAvailableRooms() { return availableRooms; }
    public int getWaitingCount() { return waitingQueue.size(); }
    public int getConfirmedCount() { return confirmedBookings.size(); }
    public int getAvailableRoomsCount() { return availableRooms.size(); }
}
