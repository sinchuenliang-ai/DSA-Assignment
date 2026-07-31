/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import adt.LinkedQueue;
import adt.QueueInterface;
import entity.Guest;
import entity.Booking;
import entity.Room;
/**
 *
 * @author User
 */
public class BookingControl {
    private QueueInterface<Booking> waitingQueue;
    private QueueInterface<Room> availableRooms;
    private QueueInterface<Booking> confirmedBookings;
    
    private int guestCounter = 101;
    private int bookingCounter = 1001;
    
    public BookingControl(){
        this.waitingQueue = new LinkedQueue<>();
        this.availableRooms = new LinkedQueue<>();
        this.confirmedBookings = new LinkedQueue<>();
        
        initializeRooms();
        initializeMockData();
    }
    
    private void initializeRooms(){
        availableRooms.enqueue(new Room("R101", "101", "Standard Single", "Available"));
        availableRooms.enqueue(new Room("R102", "102", "Standard Double", "Available"));
        availableRooms.enqueue(new Room("R103", "103", "Deluxe Suite", "Available"));
        availableRooms.enqueue(new Room("R104", "104", "Executive Suite", "Available"));
    }
    
    private void initializeMockData() {
        Guest g1 = new Guest("G101", "Alice Tan", "Female", "012-3456789", "alice@gmail.com", "IC950101");
        Guest g2 = new Guest("G102", "Bob Lee", "Male", "013-9876543", "bob@gmail.com", "IC880202");
        Guest g3 = new Guest("G103", "Charlie Wong", "Male", "014-1122334", "charlie@gmail.com", "IC990303");

        waitingQueue.enqueue(new Booking("B1001", "2026-08-01", "2026-08-01", "2026-08-05", 2, "Waiting", g1, null));
        waitingQueue.enqueue(new Booking("B1002", "2026-08-01", "2026-08-02", "2026-08-03", 1, "Waiting", g2, null));
        waitingQueue.enqueue(new Booking("B1003", "2026-08-01", "2026-08-01", "2026-08-04", 3, "Waiting", g3, null));
    }
    
    public String generateGuestID() {
        return "G" + (guestCounter++);
    }

    public String generateBookingID() {
        return "B" + (bookingCounter++);
    }
    
    public Booking registerWalkInGuest(String guestName, String gender, String phone, 
                                       String email, String icPassport, String checkInDate, 
                                       String checkOutDate, int numberOfGuests) {
        
        Guest newGuest = new Guest(generateGuestID(), guestName, gender, phone, email, icPassport);
        Booking newBooking = new Booking(generateBookingID(), "2026-08-01", checkInDate, 
                                         checkOutDate, numberOfGuests, "Waiting", newGuest, null);

        waitingQueue.enqueue(newBooking);
        return newBooking;
    }
    
    public Booking assignRoomToNextGuest() {
        if (waitingQueue.isEmpty() || availableRooms.isEmpty()) {
            return null;
        }

        // Dequeue guest from front of line
        Booking booking = waitingQueue.dequeue();
        
        // Dequeue next available room
        Room room = availableRooms.dequeue();

        room.setRoomStatus("Occupied");
        booking.setRoom(room);
        booking.setBookingStatus("Confirmed");

        // Keep track of assigned stays
        confirmedBookings.enqueue(booking);
        return booking;
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

        // Restore remaining bookings back into waitingQueue
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

    public int calculateNights(Booking b) {
        try {
            int inDay = Integer.parseInt(b.getCheckInDate().substring(8));
            int outDay = Integer.parseInt(b.getCheckOutDate().substring(8));
            return Math.max(1, outDay - inDay);
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
