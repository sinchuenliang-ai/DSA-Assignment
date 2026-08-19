package control;

import adt.LinkedQueue;
import adt.QueueInterface;
import entity.Booking;
import entity.Guest;
import entity.Room;
import java.time.LocalDate;

/**
 * Controller class managing separate Queue ADTs per Room Type for Walk-In Bookings & Room Assignments.
 */
public class BookingControl {

    private final QueueInterface<Booking> singleQueue;
    private final QueueInterface<Booking> doubleQueue;
    private final QueueInterface<Booking> deluxeQueue;
    private final QueueInterface<Booking> executiveQueue;

    private final QueueInterface<Room> availableRooms;
    private final QueueInterface<Booking> confirmedBookings;

    private int guestCounter = 101;
    private int bookingCounter = 1001;

    public BookingControl() {
        this.singleQueue = new LinkedQueue<>();
        this.doubleQueue = new LinkedQueue<>();
        this.deluxeQueue = new LinkedQueue<>();
        this.executiveQueue = new LinkedQueue<>();

        this.availableRooms = new LinkedQueue<>();
        this.confirmedBookings = new LinkedQueue<>();

        initializeRooms();
        initializeMockData();
    }

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

        singleQueue.enqueue(b1);
        doubleQueue.enqueue(b2);
        deluxeQueue.enqueue(b3);

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
        
        newBooking.setRequestedRoomType(requestedRoomType);
        getQueueByRoomType(requestedRoomType).enqueue(newBooking);

        return newBooking;
    }

    /**
     * Assigns a room to the next guest in line for a specific room type.
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
            nextBooking.setBookingStatus("Confirmed");
            confirmedBookings.enqueue(nextBooking);
            return nextBooking;
        }

        return null;
    }

    public boolean cancelBooking(String bookingID) {
        if (removeFromQueue(singleQueue, bookingID)) return true;
        if (removeFromQueue(doubleQueue, bookingID)) return true;
        if (removeFromQueue(deluxeQueue, bookingID)) return true;
        return removeFromQueue(executiveQueue, bookingID);
    }

    private boolean removeFromQueue(QueueInterface<Booking> queue, String bookingID) {
        if (queue.isEmpty()) return false;

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
        if (found == null) found = searchInQueue(confirmedBookings, bookingID);
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

    public QueueInterface<Booking> getAllWaitingBookings() {
        QueueInterface<Booking> copyQueue = new LinkedQueue<>();
        copyQueueFrom(singleQueue, copyQueue);
        copyQueueFrom(doubleQueue, copyQueue);
        copyQueueFrom(deluxeQueue, copyQueue);
        copyQueueFrom(executiveQueue, copyQueue);
        return copyQueue;
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
        switch (type.trim().toLowerCase()) {
            case "standard double": return doubleQueue;
            case "deluxe suite": return deluxeQueue;
            case "executive suite": return executiveQueue;
            default: return singleQueue;
        }
    }

    public int getAvailableRoomCountByType(String roomType) {
        QueueInterface<Room> tempQueue = new LinkedQueue<>();
        int count = 0;

        while (!availableRooms.isEmpty()) {
            Room r = availableRooms.dequeue();
            if (r.getRoomType().equalsIgnoreCase(roomType)) {
                count++;
            }
            tempQueue.enqueue(r);
        }

        while (!tempQueue.isEmpty()) {
            availableRooms.enqueue(tempQueue.dequeue());
        }

        return count;
    }

    private Room extractRoomByType(String roomType) {
        QueueInterface<Room> tempQueue = new LinkedQueue<>();
        Room targetRoom = null;

        while (!availableRooms.isEmpty()) {
            Room current = availableRooms.dequeue();
            if (targetRoom == null && current.getRoomType().equalsIgnoreCase(roomType)) {
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

    public int getWaitingCount() { 
        return singleQueue.size() + doubleQueue.size() + deluxeQueue.size() + executiveQueue.size(); 
    }
    public int getWaitingCountByType(String roomType) {
        return getQueueByRoomType(roomType).size();
    }
    public int getConfirmedCount() { return confirmedBookings.size(); }
    public int getAvailableRoomsCount() { return availableRooms.size(); }
}
