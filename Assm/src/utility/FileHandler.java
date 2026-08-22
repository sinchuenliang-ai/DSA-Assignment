package utility;

import adt.ArrayStack;
import adt.LinkedQueue;
import adt.QueueInterface;
import adt.TreeInterface;
import adt.ListInterface;
import adt.LinkedList;
import entity.Booking;
import entity.Guest;
import entity.HousekeepingTask;
import entity.Reservation;
import entity.Room;
import entity.Staff;
import entity.Member;
import entity.Transaction;
import entity.Notification;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class responsible for reading from and writing to text files (.txt).
 * Delimiter used: pipe symbol "|"
 */
public class FileHandler {

    private static final String DATA_DIR = "data";
    private static final String RESERVATIONS_FILE = DATA_DIR + File.separator + "reservations.txt";
    private static final String ROOMS_FILE = DATA_DIR + File.separator + "rooms.txt";
    private static final String BOOKINGS_FILE = DATA_DIR + File.separator + "bookings.txt";
    private static final String STAFF_FILE = DATA_DIR + File.separator + "staff.txt";
    private static final String HOUSEKEEPING_FILE = DATA_DIR + File.separator + "housekeeping_tasks.txt";
    private static final String MEMBERS_FILE = DATA_DIR + File.separator + "members.txt";
    private static final String TRANSACTIONS_FILE = DATA_DIR + File.separator + "transactions.txt";
    private static final String NOTIFICATIONS_FILE = DATA_DIR + File.separator + "notifications.txt";

    static {
        ensureDataDirectoryExists();
    }

    private static void ensureDataDirectoryExists() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // =========================================================================
    // 1. RESERVATIONS (Front Desk)
    // =========================================================================

    public static void loadReservations(TreeInterface<Reservation> reservationTree) {
        File file = new File(RESERVATIONS_FILE);
        if (!file.exists()) {
            createDefaultReservationsFile();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 7) {
                    String confirmNo = parts[0].trim();
                    String guestName = parts[1].trim();
                    String roomCategory = parts[2].trim();
                    String roomNo = parts[3].trim();
                    int stayDays = Integer.parseInt(parts[4].trim());
                    double totalBill = Double.parseDouble(parts[5].trim());
                    String status = parts[6].trim();
                    String checkInDate  = parts.length >= 8 ? parts[7].trim() : "";
                    String checkOutDate = parts.length >= 9 ? parts[8].trim() : "";

                    Reservation res = new Reservation(confirmNo, guestName, roomCategory, roomNo, stayDays, totalBill, status,
                            checkInDate.isEmpty() ? null : checkInDate,
                            checkOutDate.isEmpty() ? null : checkOutDate);
                    reservationTree.add(res);
                }
            }
        } catch (Exception e) {
            System.err.println("[FileHandler] Error loading reservations: " + e.getMessage());
        }
    }

    public static void saveReservations(TreeInterface<Reservation> reservationTree) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESERVATIONS_FILE))) {
            Iterator<Reservation> it = reservationTree.getInorderIterator();
            while (it.hasNext()) {
                Reservation r = it.next();
                writer.println(String.format("%s|%s|%s|%s|%d|%.2f|%s|%s|%s",
                        r.getConfirmationNumber(),
                        r.getGuestName(),
                        r.getRoomCategory(),
                        r.getRoomNumber() != null ? r.getRoomNumber() : "",
                        r.getStayDurationDays(),
                        r.getTotalBillAmount(),
                        r.getStatus(),
                        r.getCheckInDate() != null ? r.getCheckInDate() : "",
                        r.getCheckOutDate() != null ? r.getCheckOutDate() : ""
                ));
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Error saving reservations: " + e.getMessage());
        }
    }

    private static void createDefaultReservationsFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESERVATIONS_FILE))) {
            writer.println("10008801|Tan Ah Kow|Deluxe|D-101|3|450.00|Checked-In");
            writer.println("10008805|Siti Nurhaliza|Suite|S-202|5|2500.00|Reserved");
            writer.println("10008802|Alex Muthu|Standard|A-005|1|120.00|Checked-Out");
            writer.println("10008809|John Doe|Presidential|P-501|7|8400.00|Checked-In");
            writer.println("10008804|Alice Smith|Suite|S-201|2|1100.00|Checked-In");
            writer.println("10008810|Michael Chen|Standard|A-002|2|240.00|Checked-In");
            writer.println("10008811|Sarah Lim|Standard|A-003|4|480.00|Reserved");
            writer.println("10008812|David Wong|Deluxe|D-105|0|0.00|Maintenance");
            writer.println("10008813|Emma Brown|Suite|S-205|0|0.00|Cleaning");
            writer.println("10008814|Ahmad Ali|Deluxe|D-103|2|300.00|Checked-In");
        } catch (IOException e) {
            System.err.println("[FileHandler] Error creating default reservations file: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. ROOMS (Master Hotel Room Inventory)
    // =========================================================================

    public static List<Room> loadAllHotelRooms() {
        File file = new File(ROOMS_FILE);
        if (!file.exists()) {
            createDefaultRoomsFile();
        }

        List<Room> allRooms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 4) {
                    String roomID = parts[0].trim();
                    String roomNumber = parts[1].trim();
                    String roomType = parts[2].trim();
                    String roomStatus = parts[3].trim();
                    double rate = parts.length >= 5 ? Double.parseDouble(parts[4].trim()) : Room.getDefaultRateForType(roomType);

                    allRooms.add(new Room(roomID, roomNumber, roomType, roomStatus, rate));
                }
            }
        } catch (Exception e) {
            System.err.println("[FileHandler] Error loading all hotel rooms: " + e.getMessage());
        }
        return allRooms;
    }

    public static void loadRooms(QueueInterface<Room> availableRooms) {
        List<Room> all = loadAllHotelRooms();
        availableRooms.clear();
        for (Room r : all) {
            if ("Available".equalsIgnoreCase(r.getRoomStatus())) {
                availableRooms.enqueue(r);
            }
        }
    }

    public static void saveAllHotelRooms(List<Room> roomList) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            for (Room r : roomList) {
                writer.println(String.format("%s|%s|%s|%s|%.2f",
                        r.getRoomID(),
                        r.getRoomNumber(),
                        r.getRoomType(),
                        r.getRoomStatus(),
                        r.getRatePerNight()
                ));
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Error saving hotel rooms: " + e.getMessage());
        }
    }

    public static void saveRooms(QueueInterface<Room> availableRooms) {
        // Read existing rooms to preserve occupied/maintenance states, then update matching available ones
        List<Room> all = loadAllHotelRooms();
        List<String> availableRoomNos = new ArrayList<>();

        QueueInterface<Room> temp = new LinkedQueue<>();
        while (!availableRooms.isEmpty()) {
            Room r = availableRooms.dequeue();
            availableRoomNos.add(r.getRoomNumber().toUpperCase());
            temp.enqueue(r);
        }
        while (!temp.isEmpty()) {
            availableRooms.enqueue(temp.dequeue());
        }

        for (Room r : all) {
            if (availableRoomNos.contains(r.getRoomNumber().toUpperCase())) {
                r.setRoomStatus("Available");
            } else if ("Available".equalsIgnoreCase(r.getRoomStatus())) {
                r.setRoomStatus("Occupied");
            }
        }
        saveAllHotelRooms(all);
    }

    private static void createDefaultRoomsFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            writer.println("A-001|A-001|Standard Single|Available|120.00");
            writer.println("A-002|A-002|Standard Single|Available|120.00");
            writer.println("A-003|A-003|Standard Single|Available|120.00");
            writer.println("A-004|A-004|Standard Single|Available|120.00");
            writer.println("A-005|A-005|Standard Double|Available|150.00");
            writer.println("A-006|A-006|Standard Double|Available|150.00");
            writer.println("A-007|A-007|Standard Double|Available|150.00");
            writer.println("A-008|A-008|Standard Double|Available|150.00");
            writer.println("D-101|D-101|Deluxe Suite|Available|250.00");
            writer.println("D-102|D-102|Deluxe Suite|Available|250.00");
            writer.println("D-103|D-103|Deluxe Suite|Available|250.00");
            writer.println("D-104|D-104|Deluxe Suite|Available|250.00");
            writer.println("D-105|D-105|Deluxe Suite|Available|250.00");
            writer.println("D-106|D-106|Deluxe Suite|Available|250.00");
            writer.println("S-201|S-201|Executive Suite|Available|500.00");
            writer.println("S-202|S-202|Executive Suite|Available|500.00");
            writer.println("S-203|S-203|Executive Suite|Available|500.00");
            writer.println("S-204|S-204|Executive Suite|Available|500.00");
            writer.println("S-205|S-205|Executive Suite|Available|500.00");
            writer.println("P-501|P-501|Presidential Suite|Available|1200.00");
            writer.println("P-502|P-502|Presidential Suite|Available|1200.00");
        } catch (IOException e) {
            System.err.println("[FileHandler] Error creating default rooms file: " + e.getMessage());
        }
    }

    // =========================================================================
    // 3. BOOKINGS & GUESTS (Walk-In Queues + Pending Confirmations)
    // =========================================================================

    public static void loadBookings(QueueInterface<Booking> singleQueue,
                                    QueueInterface<Booking> doubleQueue,
                                    QueueInterface<Booking> deluxeQueue,
                                    QueueInterface<Booking> executiveQueue,
                                    QueueInterface<Booking> presidentialQueue,
                                    QueueInterface<Booking> pendingConfirmations,
                                    QueueInterface<Booking> confirmedBookings) {
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) {
            createDefaultBookingsFile();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Format: bookingID|bookingDate|checkInDate|checkOutDate|numberOfGuests|bookingStatus|requestedRoomType|guestID|guestName|gender|phone|email|icPassport|assignedRoom
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 13) {
                    String bookingID = parts[0].trim();
                    String bookingDate = parts[1].trim();
                    String checkInDate = parts[2].trim();
                    String checkOutDate = parts[3].trim();
                    int numGuests = Integer.parseInt(parts[4].trim());
                    String status = parts[5].trim();
                    String roomType = parts[6].trim();

                    String guestID = parts[7].trim();
                    String guestName = parts[8].trim();
                    String gender = parts[9].trim();
                    String phone = parts[10].trim();
                    String email = parts[11].trim();
                    String icPassport = parts[12].trim();

                    Room assignedRoom = null;
                    if (parts.length >= 14 && !parts[13].trim().equalsIgnoreCase("null") && !parts[13].trim().isEmpty()) {
                        String assignedRoomInfo = parts[13].trim();
                        assignedRoom = new Room(assignedRoomInfo, assignedRoomInfo, roomType, "Occupied");
                    }

                    Guest guest = new Guest(guestID, guestName, gender, phone, email, icPassport);
                    Booking booking = new Booking(bookingID, bookingDate, checkInDate, checkOutDate, numGuests, status, roomType, guest, assignedRoom);

                    if ("Confirmed".equalsIgnoreCase(status) || status.trim().toLowerCase().startsWith("confirmed")) {
                        if (confirmedBookings != null) confirmedBookings.enqueue(booking);
                    } else if (status.toLowerCase().contains("pending confirmation") || status.toLowerCase().contains("assigned")) {
                        if (pendingConfirmations != null) pendingConfirmations.enqueue(booking);
                    } else if ("Waiting".equalsIgnoreCase(status)) {
                        String lowerType = roomType.trim().toLowerCase();
                        if (lowerType.contains("presidential") && presidentialQueue != null) {
                            presidentialQueue.enqueue(booking);
                        } else if (lowerType.contains("executive") || lowerType.contains("suite") && !lowerType.contains("deluxe")) {
                            if (executiveQueue != null) executiveQueue.enqueue(booking);
                        } else if (lowerType.contains("deluxe")) {
                            if (deluxeQueue != null) deluxeQueue.enqueue(booking);
                        } else if (lowerType.contains("double")) {
                            if (doubleQueue != null) doubleQueue.enqueue(booking);
                        } else {
                            if (singleQueue != null) singleQueue.enqueue(booking);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[FileHandler] Error loading bookings: " + e.getMessage());
        }
    }

    public static void saveBookings(QueueInterface<Booking> singleQueue,
                                    QueueInterface<Booking> doubleQueue,
                                    QueueInterface<Booking> deluxeQueue,
                                    QueueInterface<Booking> executiveQueue,
                                    QueueInterface<Booking> presidentialQueue,
                                    QueueInterface<Booking> pendingConfirmations,
                                    QueueInterface<Booking> confirmedBookings) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE))) {
            if (singleQueue != null) writeQueueBookings(writer, singleQueue);
            if (doubleQueue != null) writeQueueBookings(writer, doubleQueue);
            if (deluxeQueue != null) writeQueueBookings(writer, deluxeQueue);
            if (executiveQueue != null) writeQueueBookings(writer, executiveQueue);
            if (presidentialQueue != null) writeQueueBookings(writer, presidentialQueue);
            if (pendingConfirmations != null) writeQueueBookings(writer, pendingConfirmations);
            if (confirmedBookings != null) writeQueueBookings(writer, confirmedBookings);
        } catch (IOException e) {
            System.err.println("[FileHandler] Error saving bookings: " + e.getMessage());
        }
    }

    private static void writeQueueBookings(PrintWriter writer, QueueInterface<Booking> queue) {
        QueueInterface<Booking> temp = new LinkedQueue<>();
        while (!queue.isEmpty()) {
            Booking b = queue.dequeue();
            Guest g = b.getGuest();
            String roomInfo = b.getRoom() != null ? b.getRoom().getRoomNumber() : "null";
            writer.println(String.format("%s|%s|%s|%s|%d|%s|%s|%s|%s|%s|%s|%s|%s|%s",
                    b.getBookingID(),
                    b.getBookingDate(),
                    b.getCheckInDate(),
                    b.getCheckOutDate(),
                    b.getNumberOfGuests(),
                    b.getBookingStatus(),
                    b.getRequestedRoomType() != null ? b.getRequestedRoomType() : "Standard Single",
                    g != null ? g.getGuestID() : "",
                    g != null ? g.getGuestName() : "",
                    g != null ? g.getGender() : "",
                    g != null ? g.getPhoneNumber() : "",
                    g != null ? g.getEmail() : "",
                    g != null ? g.getIcPassportNo() : "",
                    roomInfo
            ));
            temp.enqueue(b);
        }
        while (!temp.isEmpty()) {
            queue.enqueue(temp.dequeue());
        }
    }

    private static void createDefaultBookingsFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKINGS_FILE))) {
            writer.println("B1001|2026-08-20|2026-08-20|2026-08-24|2|Waiting|Standard Single|G101|Alice Tan|Female|012-3456789|alice@gmail.com|IC950101|null");
            writer.println("B1002|2026-08-20|2026-08-21|2026-08-22|1|Waiting|Standard Double|G102|Bob Lee|Male|013-9876543|bob@gmail.com|IC880202|null");
            writer.println("B1003|2026-08-20|2026-08-20|2026-08-23|3|Waiting|Deluxe Suite|G103|Charlie Wong|Male|014-1122334|charlie@gmail.com|IC990303|null");
        } catch (IOException e) {
            System.err.println("[FileHandler] Error creating default bookings file: " + e.getMessage());
        }
    }

    // =========================================================================
    // 4. STAFF (Housekeeping / Staff Directory)
    // =========================================================================

    public static List<Staff> loadStaff() {
        File file = new File(STAFF_FILE);
        if (!file.exists()) {
            createDefaultStaffFile();
        }

        List<Staff> staffList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 5) {
                    String staffID = parts[0].trim();
                    String staffName = parts[1].trim();
                    String email = parts[2].trim();
                    String shift = parts[3].trim();
                    String position = parts[4].trim();

                    staffList.add(new Staff(staffID, staffName, email, shift, position));
                }
            }
        } catch (Exception e) {
            System.err.println("[FileHandler] Error loading staff: " + e.getMessage());
        }
        return staffList;
    }

    public static Staff authenticateStaff(String staffID, String password) {
        List<Staff> staffList = loadStaff();
        for (Staff s : staffList) {
            if (s.getStaffID().equalsIgnoreCase(staffID)) {
                if (s.authenticate(password)) {
                    return s;
                }
            }
        }
        return null;
    }

    public static void saveStaff(List<Staff> staffList) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(STAFF_FILE))) {
            for (Staff s : staffList) {
                writer.println(String.format("%s|%s|%s|%s|%s",
                        s.getStaffID(),
                        s.getStaffName(),
                        s.getEmail(),
                        s.getShift(),
                        s.getPosition()
                ));
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Error saving staff: " + e.getMessage());
        }
    }

    private static void createDefaultStaffFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(STAFF_FILE))) {
            writer.println("S001|John|john@email.com|Morning|Housekeeper");
            writer.println("S002|Mary|mary@email.com|Afternoon|Housekeeper");
        } catch (IOException e) {
            System.err.println("[FileHandler] Error creating default staff file: " + e.getMessage());
        }
    }

    // =========================================================================
    // 5. HOUSEKEEPING TASKS (Stack ADT)
    // =========================================================================

    public static void loadHousekeepingTasks(ArrayStack<HousekeepingTask> taskStack) {
        File file = new File(HOUSEKEEPING_FILE);
        if (!file.exists()) {
            createDefaultHousekeepingFile();
        }

        List<HousekeepingTask> taskList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 5) {
                    String taskID = parts[0].trim();
                    String location = parts[1].trim();
                    String taskName = parts[2].trim();
                    String status = parts[3].trim();
                    String assignedStaffID = parts[4].trim();

                    taskList.add(new HousekeepingTask(taskID, location, taskName, status, assignedStaffID));
                }
            }
        } catch (Exception e) {
            System.err.println("[FileHandler] Error loading housekeeping tasks: " + e.getMessage());
        }

        // Push tasks into stack in order
        for (HousekeepingTask t : taskList) {
            taskStack.push(t);
        }
    }

    public static void saveHousekeepingTasks(ArrayStack<HousekeepingTask> taskStack) {
        if (taskStack == null) return;

        // Pop elements into temporary stack to retrieve bottom-to-top order
        ArrayStack<HousekeepingTask> tempStack = new ArrayStack<>();
        List<HousekeepingTask> list = new ArrayList<>();

        while (!taskStack.isEmpty()) {
            HousekeepingTask task = taskStack.pop();
            list.add(0, task); // Insert at index 0 so bottom elements come first
            tempStack.push(task);
        }

        // Restore original stack
        while (!tempStack.isEmpty()) {
            taskStack.push(tempStack.pop());
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(HOUSEKEEPING_FILE))) {
            for (HousekeepingTask t : list) {
                writer.println(String.format("%s|%s|%s|%s|%s",
                        t.getTaskID(),
                        t.getLocation(),
                        t.getTaskName(),
                        t.getStatus(),
                        t.getAssignedStaffID()
                ));
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Error saving housekeeping tasks: " + e.getMessage());
        }
    }

    private static void createDefaultHousekeepingFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HOUSEKEEPING_FILE))) {
            writer.println("T001|S-205|Room Cleaning|Cleaning In Progress|S001");
            writer.println("T002|A-005|Room Cleaning|Dirty|S002");
        } catch (IOException e) {
            System.err.println("[FileHandler] Error creating default housekeeping file: " + e.getMessage());
        }
    }

    // =========================================================================
    // 6. LOYALTY - MEMBERS
    // =========================================================================

    public static ListInterface<Member> loadMembers() {
        File file = new File(MEMBERS_FILE);
        ListInterface<Member> memberList = new LinkedList<>();
        
        if (!file.exists()) {
            createDefaultMembersFile();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 10) {
                    Member member = new Member(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim()
                    );
                    member.setTier(parts[4].trim());
                    member.setPoints(Integer.parseInt(parts[5].trim()));
                    member.setTotalPointsEarned(Integer.parseInt(parts[6].trim()));
                    member.setLifetimePointsEarned(Integer.parseInt(parts[7].trim()));
                    member.setTotalRedemptions(Integer.parseInt(parts[8].trim()));
                    member.setPointsRedeemed(Integer.parseInt(parts[9].trim()));
                    if (parts.length >= 11 && !parts[10].trim().isEmpty()) {
                        member.setDateOfBirth(java.time.LocalDate.parse(parts[10].trim()));
                    }
                    if (parts.length >= 12 && !parts[11].trim().isEmpty()) {
                        member.setPreferredRoomType(parts[11].trim());
                    }
                    memberList.add(member);
                }
            }
        } catch (Exception e) {
            System.err.println("[FileHandler] Error loading members: " + e.getMessage());
        }
        
        return memberList;
    }

    public static void saveMembers(ListInterface<Member> memberList) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(MEMBERS_FILE))) {
            for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
                Member m = memberList.getEntry(i);
                writer.println(String.format("%s|%s|%s|%s|%s|%d|%d|%d|%d|%d|%s|%s",
                    m.getMemberId(),
                    m.getName(),
                    m.getEmail(),
                    m.getPhoneNumber(),
                    m.getTier(),
                    m.getPoints(),
                    m.getTotalPointsEarned(),
                    m.getLifetimePointsEarned(),
                    m.getTotalRedemptions(),
                    m.getPointsRedeemed(),
                    m.getDateOfBirth() != null ? m.getDateOfBirth().toString() : "",
                    m.getPreferredRoomType() != null ? m.getPreferredRoomType() : ""
                ));
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Error saving members: " + e.getMessage());
        }
    }

    private static void createDefaultMembersFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(MEMBERS_FILE))) {
            writer.println("M001|John Smith|john.smith@email.com|012-345-6789|Gold|1500|1500|1500|0|0|1985-05-15|Deluxe Suite");
            writer.println("M002|Sarah Lee|sarah.lee@email.com|013-987-6543|Platinum|6000|6000|6000|0|0|1990-08-22|Ocean View");
            writer.println("M003|Ahmed Hassan|ahmed.h@email.com|014-567-8901|Silver|250|250|250|0|0|1978-03-10|");
            writer.println("M004|Priya Patel|priya.p@email.com|015-234-5678|Diamond|18000|18000|18000|0|0|1988-11-30|Executive Suite");
            writer.println("M005|David Tan|david.t@email.com|016-789-0123|Elite|35000|35000|35000|0|0|1975-07-08|Presidential Suite");
        } catch (IOException e) {
            System.err.println("[FileHandler] Error creating default members file: " + e.getMessage());
        }
    }

    // =========================================================================
    // 7. LOYALTY - TRANSACTIONS
    // =========================================================================

    public static QueueInterface<Transaction> loadTransactions() {
        File file = new File(TRANSACTIONS_FILE);
        QueueInterface<Transaction> transactionQueue = new LinkedQueue<>();
        
        if (!file.exists()) {
            createDefaultTransactionsFile();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 4) {
                    String memberId = parts[0].trim();
                    String typeStr = parts[1].trim();
                    int points = Integer.parseInt(parts[2].trim());
                    String description = parts[3].trim();
                    String bookingId = parts.length >= 5 ? parts[4].trim() : "";
                    
                    Transaction.TransactionType type;
                    switch (typeStr) {
                        case "EARN_POINTS": type = Transaction.TransactionType.EARN_POINTS; break;
                        case "REDEEM_POINTS": type = Transaction.TransactionType.REDEEM_POINTS; break;
                        case "BONUS_POINTS": type = Transaction.TransactionType.BONUS_POINTS; break;
                        case "TIER_UPGRADE_BONUS": type = Transaction.TransactionType.TIER_UPGRADE_BONUS; break;
                        case "ADJUSTMENT": type = Transaction.TransactionType.ADJUSTMENT; break;
                        default: type = Transaction.TransactionType.EARN_POINTS;
                    }
                    
                    Transaction txn = new Transaction(memberId, type, points, description, bookingId);
                    transactionQueue.enqueue(txn);
                }
            }
        } catch (Exception e) {
            System.err.println("[FileHandler] Error loading transactions: " + e.getMessage());
        }
        
        return transactionQueue;
    }

    public static void saveTransactions(QueueInterface<Transaction> transactionQueue) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
            QueueInterface<Transaction> temp = new LinkedQueue<>();
            while (!transactionQueue.isEmpty()) {
                Transaction t = transactionQueue.dequeue();
                writer.println(String.format("%s|%s|%d|%s|%s",
                    t.getMemberId(),
                    t.getType().toString(),
                    t.getPoints(),
                    t.getDescription(),
                    t.getBookingId() != null ? t.getBookingId() : ""
                ));
                temp.enqueue(t);
            }
            while (!temp.isEmpty()) {
                transactionQueue.enqueue(temp.dequeue());
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Error saving transactions: " + e.getMessage());
        }
    }

    private static void createDefaultTransactionsFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
            writer.println("M001|EARN_POINTS|1500|Stay at Deluxe Suite - 3 nights|B001");
            writer.println("M002|EARN_POINTS|6000|Stay at Ocean View Suite - 5 nights|B002");
            writer.println("M003|EARN_POINTS|250|Stay at Standard Room - 2 nights|B003");
            writer.println("M004|EARN_POINTS|18000|Stay at Executive Suite - 7 nights|B004");
            writer.println("M005|EARN_POINTS|35000|Stay at Presidential Suite - 10 nights|B005");
        } catch (IOException e) {
            System.err.println("[FileHandler] Error creating default transactions file: " + e.getMessage());
        }
    }

    // =========================================================================
    // 8. LOYALTY - NOTIFICATIONS
    // =========================================================================

    public static ListInterface<Notification> loadNotifications() {
        File file = new File(NOTIFICATIONS_FILE);
        ListInterface<Notification> notificationList = new LinkedList<>();
        
        if (!file.exists()) {
            createDefaultNotificationsFile();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 4) {
                    String memberId = parts[0].trim();
                    String typeStr = parts[1].trim();
                    String title = parts[2].trim();
                    String message = parts[3].trim();
                    
                    Notification.NotificationType type;
                    switch (typeStr) {
                        case "TIER_UPGRADE": type = Notification.NotificationType.TIER_UPGRADE; break;
                        case "TIER_MAINTENANCE": type = Notification.NotificationType.TIER_MAINTENANCE; break;
                        case "POINTS_EARNED": type = Notification.NotificationType.POINTS_EARNED; break;
                        case "POINTS_EXPIRY": type = Notification.NotificationType.POINTS_EXPIRY; break;
                        case "REDEMPTION_CONFIRMATION": type = Notification.NotificationType.REDEMPTION_CONFIRMATION; break;
                        case "PROMOTIONAL_OFFER": type = Notification.NotificationType.PROMOTIONAL_OFFER; break;
                        case "BIRTHDAY_BONUS": type = Notification.NotificationType.BIRTHDAY_BONUS; break;
                        default: type = Notification.NotificationType.TIER_MAINTENANCE;
                    }
                    
                    Notification notif = new Notification(memberId, type, title, message);
                    notificationList.add(notif);
                }
            }
        } catch (Exception e) {
            System.err.println("[FileHandler] Error loading notifications: " + e.getMessage());
        }
        
        return notificationList;
    }

    public static void saveNotifications(ListInterface<Notification> notificationList) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOTIFICATIONS_FILE))) {
            for (int i = 1; i <= notificationList.getNumberOfEntries(); i++) {
                Notification n = notificationList.getEntry(i);
                writer.println(String.format("%s|%s|%s|%s",
                    n.getMemberId(),
                    n.getType().toString(),
                    n.getTitle(),
                    n.getMessage()
                ));
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Error saving notifications: " + e.getMessage());
        }
    }

    private static void createDefaultNotificationsFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOTIFICATIONS_FILE))) {
            writer.println("M001|TIER_MAINTENANCE|Welcome to TARUMT Resorts!|Dear John, welcome to our loyalty program. Start earning rewards today!");
            writer.println("M002|TIER_UPGRADE|Congratulations! Platinum Tier Achieved!|Sarah, you've been upgraded to Platinum tier with enhanced benefits!");
            writer.println("M003|PROMOTIONAL_OFFER|Special Offer: Double Points Weekend!|Ahmed, earn double points on your next weekend stay!");
            writer.println("M004|TIER_UPGRADE|Diamond Tier Unlocked!|Priya, welcome to Diamond tier. Enjoy exclusive benefits!");
            writer.println("M005|TIER_UPGRADE|Elite Tier Achieved!|David, you are now an Elite member. Your personal concierge awaits!");
        } catch (IOException e) {
            System.err.println("[FileHandler] Error creating default notifications file: " + e.getMessage());
        }
    }

    // =========================================================================
    // 9. LOYALTY - INITIALIZE DATA
    // =========================================================================

    public static void initializeLoyaltyDataIfEmpty(ListInterface<Member> memberList,
                                                    QueueInterface<Transaction> transactionQueue,
                                                    ListInterface<Notification> notificationList) {
        if (memberList.isEmpty() && transactionQueue.isEmpty() && notificationList.isEmpty()) {
            ListInterface<Member> defaultMembers = loadMembers();
            QueueInterface<Transaction> defaultTransactions = loadTransactions();
            ListInterface<Notification> defaultNotifications = loadNotifications();
            
            for (int i = 1; i <= defaultMembers.getNumberOfEntries(); i++) {
                memberList.add(defaultMembers.getEntry(i));
            }
            
            QueueInterface<Transaction> temp = new LinkedQueue<>();
            while (!defaultTransactions.isEmpty()) {
                Transaction t = defaultTransactions.dequeue();
                transactionQueue.enqueue(t);
                temp.enqueue(t);
            }
            while (!temp.isEmpty()) {
                defaultTransactions.enqueue(temp.dequeue());
            }
            
            for (int i = 1; i <= defaultNotifications.getNumberOfEntries(); i++) {
                notificationList.add(defaultNotifications.getEntry(i));
            }
        }
    }
}
