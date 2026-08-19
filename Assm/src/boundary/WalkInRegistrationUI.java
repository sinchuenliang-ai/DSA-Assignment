package boundary;

import control.BookingControl;
import entity.Booking;
import adt.QueueInterface;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * User-Friendly Boundary class for Walk-In Registration & Queue Management.
 */
public class WalkInRegistrationUI {

    private final BookingControl bookingControl;
    private final Scanner scanner;

    public WalkInRegistrationUI() {
        this.bookingControl = new BookingControl();
        this.scanner = new Scanner(System.in);
    }

    public WalkInRegistrationUI(BookingControl control) {
        this.bookingControl = control;
        this.scanner = new Scanner(System.in);
    }

    public void displayMenu() {
        int choice;

        do {
            clearConsole();
            System.out.println("+==========================================================+");
            System.out.println("|                TAR UMT RESORTS MANAGEMENT                |");
            System.out.println("|              Walk-In & Queue Management System           |");
            System.out.println("+----------------------------------------------------------+");
            System.out.println("|  [1] Register Walk-In Guest                              |");
            System.out.println("|  [2] Assign Room to Next Guest                           |");
            System.out.println("|  [3] Cancel Waiting Booking                              |");
            System.out.println("|  [4] View & Search Booking Records                       |");
            System.out.println("|  [5] Display Real-Time System Status                     |");
            System.out.println("|  [0] Exit Module                                         |");
            System.out.println("+==========================================================+");

            choice = readIntInput("Select an option (0-5): ", 0, 5);
            System.out.println();

            switch (choice) {
                case 1 -> handleRegisterWalkIn();
                case 2 -> handleAssignRoom();
                case 3 -> handleCancelBooking();
                case 4 -> handleViewAndSearchBookings();
                case 5 -> displayQueueStatus();
                case 0 -> System.out.println("Returning to System Master Menu...");
                default -> System.out.println("  [!] Invalid choice. Please try again.");
            }

            if (choice != 0) {
                pressEnterToContinue();
            }

        } while (choice != 0);
    }

    private void handleRegisterWalkIn() {
        printHeader("1. REGISTER WALK-IN GUEST");

        String name = readNameInput("Guest Full Name           : ");
        String gender = readGenderInput("Gender (M/F)               : ");
        String phone = readPhoneInput("Contact Phone Number      : ");
        String email = readEmailInput("Email Address             : ");
        String icPassport = readIcPassportInput("IC / Passport Number      : ");

        LocalDate checkInDate = readValidCheckInDate("Check-In Date (YYYY-MM-DD)");
        LocalDate checkOutDate = readValidCheckOutDate("Check-Out Date (YYYY-MM-DD)", checkInDate);

        int guestCount = readIntInput("Number of Guests (1-10)   : ", 1, 10);

        System.out.println("\nSelect Preferred Room Type:");
        System.out.println("  [1] Standard Single");
        System.out.println("  [2] Standard Double");
        System.out.println("  [3] Deluxe Suite");
        System.out.println("  [4] Executive Suite");
        int typeChoice = readIntInput("Choice (1-4): ", 1, 4);

        String roomTypePreference = "Standard Single";
        switch (typeChoice) {
            case 2 -> roomTypePreference = "Standard Double";
            case 3 -> roomTypePreference = "Deluxe Suite";
            case 4 -> roomTypePreference = "Executive Suite";
        }

        Booking newBooking = bookingControl.registerWalkInGuest(
                name, gender, phone, email, icPassport, 
                checkInDate.toString(), checkOutDate.toString(), 
                guestCount, roomTypePreference
        );

        System.out.println("\n----------------------------------------------------------");
        System.out.println("  [ SUCCESS ] Registration Completed!");
        System.out.println("  * Generated Booking ID : " + newBooking.getBookingID());
        System.out.println("  * Generated Guest ID   : " + newBooking.getGuest().getGuestID());
        System.out.println("  * Preferred Room Type  : " + roomTypePreference);
        System.out.println("  * Queue Position       : #" + bookingControl.getWaitingCountByType(roomTypePreference) + " in " + roomTypePreference + " Queue");
        System.out.println("----------------------------------------------------------");
    }

    private void handleAssignRoom() {
    printHeader("2. ASSIGN ROOM TO NEXT GUEST");

    if (bookingControl.getWaitingCount() == 0) {
        System.out.println("  [ NOTICE ] All waiting queues are currently empty.");
        return;
    }

    int singleAvail = bookingControl.getAvailableRoomCountByType("Standard Single");
    int singleWait  = bookingControl.getWaitingCountByType("Standard Single");

    int doubleAvail = bookingControl.getAvailableRoomCountByType("Standard Double");
    int doubleWait  = bookingControl.getWaitingCountByType("Standard Double");

    int deluxeAvail = bookingControl.getAvailableRoomCountByType("Deluxe Suite");
    int deluxeWait  = bookingControl.getWaitingCountByType("Deluxe Suite");

    int execAvail   = bookingControl.getAvailableRoomCountByType("Executive Suite");
    int execWait    = bookingControl.getWaitingCountByType("Executive Suite");

    System.out.println("  Select Room Type Queue to Process:");
    System.out.println("  -----------------------------------------------------------------------");
    System.out.printf("  %-8s %-20s | %-16s | %-14s\n", "Option", "Room Type", "Available Rooms", "Waiting Guests");
    System.out.println("  -----------------------------------------------------------------------");
    System.out.printf("  [1]      %-20s | %-16d | %-14d\n", "Standard Single", singleAvail, singleWait);
    System.out.printf("  [2]      %-20s | %-16d | %-14d\n", "Standard Double", doubleAvail, doubleWait);
    System.out.printf("  [3]      %-20s | %-16d | %-14d\n", "Deluxe Suite",    deluxeAvail, deluxeWait);
    System.out.printf("  [4]      %-20s | %-16d | %-14d\n", "Executive Suite",   execAvail,   execWait);
    System.out.printf("  [0]      %-20s | %-16s | %-14s\n", "Cancel / Return",   "-",         "-");
    System.out.println("  -----------------------------------------------------------------------");

    int typeChoice = readIntInput("Choice (0-4): ", 0, 4);

    // Exit assignment screen if user selects 0
    if (typeChoice == 0) {
        System.out.println("\n  [ System ] Room assignment operation cancelled.");
        return;
    }

    String selectedType = "Standard Single";
    switch (typeChoice) {
        case 2 -> selectedType = "Standard Double";
        case 3 -> selectedType = "Deluxe Suite";
        case 4 -> selectedType = "Executive Suite";
    }

    if (bookingControl.getWaitingCountByType(selectedType) == 0) {
        System.out.println("\n  [ NOTICE ] No guests are currently waiting in the " + selectedType + " queue.");
        return;
    }

    if (bookingControl.getAvailableRoomCountByType(selectedType) == 0) {
        System.out.println("\n  [ WARNING ] No " + selectedType + " rooms are available to assign right now.");
        return;
    }

    Booking assigned = bookingControl.assignRoomByRoomType(selectedType);

    if (assigned != null) {
        System.out.println("\n  [ SUCCESS ] Room Assigned Successfully!");
        System.out.println("   Booking ID    : " + assigned.getBookingID());
        System.out.println("   Guest Name    : " + assigned.getGuest().getGuestName());
        System.out.println("   Room Assigned : " + assigned.getRoom().getRoomNumber() 
                           + " (" + assigned.getRoom().getRoomType() + ")");
    } else {
        System.out.println("\n  [ ERROR ] Room assignment failed.");
    }
}

    private void handleCancelBooking() {
        printHeader("3. CANCEL WAITING BOOKING");

        QueueInterface<Booking> waitingQueue = bookingControl.getAllWaitingBookings();

        if (waitingQueue.isEmpty()) {
            System.out.println("  [ NOTICE ] No active waiting bookings found to cancel.");
            return;
        }

        System.out.println("-----------------------------------------------------------------------------------------------------");
        System.out.printf("%-10s %-16s %-14s %-18s %-10s %-8s\n",
                "Booking ID", "Guest Name", "Contact", "Room Preference", "Check-In", "Guests");
        System.out.println("-----------------------------------------------------------------------------------------------------");

        while (!waitingQueue.isEmpty()) {
            Booking b = waitingQueue.dequeue();
            System.out.printf("%-10s %-16s %-14s %-18s %-10s %-8d\n",
                    b.getBookingID(),
                    b.getGuest().getGuestName(),
                    b.getGuest().getPhoneNumber(),
                    (b.getRequestedRoomType() != null ? b.getRequestedRoomType() : "Standard Single"),
                    b.getCheckInDate(),
                    b.getNumberOfGuests());
        }
        System.out.println("-----------------------------------------------------------------------------------------------------");

        String bookingID = "";
        Booking targetBooking = null;

        while (true) {
            bookingID = readNonEmptyString("\nEnter Booking ID to Cancel (or enter [0] to return): ");

            if (bookingID.equals("0")) {
                System.out.println("\n  [ System ] Cancellation aborted.");
                return;
            }

            targetBooking = bookingControl.searchBooking(bookingID);

            if (targetBooking != null) {
                break;
            }

            System.out.println("  [ ERROR ] Booking ID '" + bookingID.toUpperCase() + "' was not found in any queue.");
        }

        boolean isConfirmed = readConfirmationInput("  Are you sure you want to cancel booking " 
                + targetBooking.getBookingID() + " (" + targetBooking.getGuest().getGuestName() + ")? (Y/N): ");

        if (isConfirmed) {
            boolean success = bookingControl.cancelBooking(bookingID);
            if (success) {
                System.out.println("\n  [ SUCCESS ] Booking " + targetBooking.getBookingID() + " successfully cancelled and removed from queue.");
            } else {
                System.out.println("\n  [ ERROR ] Failed to remove booking from queue.");
            }
        } else {
            System.out.println("\n  [ System ] Cancellation aborted.");
        }
    }

    private void handleViewAndSearchBookings() {
        printHeader("4. VIEW & SEARCH BOOKINGS");

        QueueInterface<Booking> records = bookingControl.getAllWaitingBookings();

        if (records.isEmpty()) {
            System.out.println("  [ NOTICE ] No booking records currently in the waiting queues.");
            return;
        }

        System.out.println("-----------------------------------------------------------------------------------------------------");
        System.out.printf("%-10s %-16s %-14s %-18s %-10s %-8s %-10s\n",
                "Booking ID", "Guest Name", "Contact", "Room Preference", "Check-In", "Guests", "Status");
        System.out.println("-----------------------------------------------------------------------------------------------------");

        while (!records.isEmpty()) {
            Booking b = records.dequeue();
            System.out.printf("%-10s %-16s %-14s %-18s %-10s %-8d %-10s\n",
                    b.getBookingID(),
                    b.getGuest().getGuestName(),
                    b.getGuest().getPhoneNumber(),
                    (b.getRequestedRoomType() != null ? b.getRequestedRoomType() : "Standard Single"),
                    b.getCheckInDate(),
                    b.getNumberOfGuests(),
                    b.getBookingStatus());
        }

        System.out.println("-----------------------------------------------------------------------------------------------------");
        System.out.println("  Total Queue Count: " + bookingControl.getWaitingCount() + " guest(s) waiting across all queues.");

        String input = readNonEmptyString("\nEnter Booking ID for full guest details (or enter [0] to return): ");

        if (input.equals("0")) {
            return;
        }

        Booking b = bookingControl.searchBooking(input);
        if (b != null) {
            System.out.println("\n+==========================================================+");
            System.out.println("|                    FULL BOOKING DETAILS                  |");
            System.out.println("+==========================================================+");
            System.out.printf("|  %-16s : %-33s |\n", "Booking ID", b.getBookingID());
            System.out.printf("|  %-16s : %-33s |\n", "Guest ID", b.getGuest().getGuestID());
            System.out.printf("|  %-16s : %-33s |\n", "Guest Name", b.getGuest().getGuestName());
            System.out.printf("|  %-16s : %-33s |\n", "Gender", b.getGuest().getGender());
            System.out.printf("|  %-16s : %-33s |\n", "IC/Passport", b.getGuest().getIcPassportNo());
            System.out.printf("|  %-16s : %-33s |\n", "Contact", b.getGuest().getPhoneNumber());
            System.out.printf("|  %-16s : %-33s |\n", "Email", b.getGuest().getEmail());
            System.out.printf("|  %-16s : %-33s |\n", "Check-In Date", b.getCheckInDate());
            System.out.printf("|  %-16s : %-33s |\n", "Check-Out Date", b.getCheckOutDate());
            System.out.printf("|  %-16s : %-33d |\n", "No. of Guests", b.getNumberOfGuests());
            System.out.printf("|  %-16s : %-33s |\n", "Status", b.getBookingStatus());
            System.out.printf("|  %-16s : %-33s |\n", "Assigned Room", 
                              (b.getRoom() != null ? b.getRoom().getRoomNumber() + " (" + b.getRoom().getRoomType() + ")" : "None (Waiting)"));
            System.out.println("+==========================================================+");
        } else {
            System.out.println("\n  [ ERROR ] Booking ID '" + input.toUpperCase() + "' was not found.");
        }
    }

    private void displayQueueStatus() {
        printHeader("5. REAL-TIME SYSTEM STATUS");

        System.out.println("   [ WAITING QUEUES BY ROOM TYPE ]");
        System.out.printf("   - Standard Single : %d guest(s)\n", bookingControl.getWaitingCountByType("Standard Single"));
        System.out.printf("   - Standard Double : %d guest(s)\n", bookingControl.getWaitingCountByType("Standard Double"));
        System.out.printf("   - Deluxe Suite    : %d guest(s)\n", bookingControl.getWaitingCountByType("Deluxe Suite"));
        System.out.printf("   - Executive Suite : %d guest(s)\n", bookingControl.getWaitingCountByType("Executive Suite"));
        System.out.println("   -------------------------------------");
        System.out.println("   Total Waiting Guests    : " + bookingControl.getWaitingCount());
        System.out.println("   Total Available Rooms   : " + bookingControl.getAvailableRoomsCount());
        System.out.println("   Confirmed Check-Ins     : " + bookingControl.getConfirmedCount());
    }

    private int readIntInput(String prompt, int min, int max) {
        int val;
        while (true) {
            System.out.print(prompt);
            try {
                val = Integer.parseInt(scanner.nextLine().trim());
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println("  [!] Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a valid number.");
            }
        }
    }

    private boolean readConfirmationInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("Y") || input.equals("YES")) {
                return true;
            }
            if (input.equals("N") || input.equals("NO")) {
                return false;
            }
            System.out.println("  [!] Invalid choice. Please enter 'Y' for Yes or 'N' for No.");
        }
    }

    private String readNonEmptyString(String prompt) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  [!] This field cannot be left blank.");
        }
    }

    private String readNameInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("^[a-zA-Z\\s'/.-]+$") && input.length() >= 2) {
                return input;
            }
            System.out.println("  [!] Invalid name. Use alphabetic characters only (min 2 letters).");
        }
    }

    private String readPhoneInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("^[0-9\\-+ ]{8,15}$")) {
                return input;
            }
            System.out.println("  [!] Invalid phone number (e.g. 012-3456789).");
        }
    }

    private String readEmailInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                return input;
            }
            System.out.println("  [!] Invalid email address format.");
        }
    }

    private String readIcPassportInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.matches("^[A-Za-z0-9\\-]{6,15}$")) {
                return input.toUpperCase();
            }
            System.out.println("  [!] Invalid IC/Passport number.");
        }
    }

    private String readGenderInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("M") || input.equals("F") || input.equals("MALE") || input.equals("FEMALE")) {
                return input.startsWith("M") ? "Male" : "Female";
            }
            System.out.println("  [!] Invalid gender. Please enter 'M' or 'F'.");
        }
    }

    private LocalDate readValidCheckInDate(String fieldName) {
        LocalDate today = LocalDate.now();
        while (true) {
            System.out.print(fieldName + " [Press ENTER for today '" + today + "']: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return today;
            }

            try {
                LocalDate parsedDate = LocalDate.parse(input);
                if (!parsedDate.isBefore(today)) {
                    return parsedDate;
                }
                System.out.println("  [!] Check-In date cannot be in the past.");
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    private LocalDate readValidCheckOutDate(String fieldName, LocalDate checkInDate) {
        LocalDate defaultCheckOut = checkInDate.plusDays(1);
        while (true) {
            System.out.print(fieldName + " [Press ENTER for '" + defaultCheckOut + "']: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return defaultCheckOut;
            }

            try {
                LocalDate parsedDate = LocalDate.parse(input);
                if (parsedDate.isAfter(checkInDate)) {
                    return parsedDate;
                }
                System.out.println("  [!] Check-Out date must be AFTER Check-In date.");
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    private void printHeader(String title) {
        System.out.println("==========================================================");
        System.out.println("  " + title);
        System.out.println("==========================================================");
    }

    private void pressEnterToContinue() {
        System.out.print("\nPress [ENTER] key to return to menu...");
        scanner.nextLine();
    }

    private void clearConsole() {
        for (int i = 0; i < 2; i++) {
            System.out.println();
        }
    }
}
