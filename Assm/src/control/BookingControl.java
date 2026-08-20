package control;

import adt.LinkedQueue;
import adt.QueueInterface;
import entity.Booking;
import entity.Guest;
import entity.Room;
import java.time.LocalDate;
import utility.FileHandler;

/**
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
}
