package boundary;

import control.BookingControl;
import entity.Booking;
import entity.Member;
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
            System.out.println("|  [6] Booking Analytics Report                            |");
            System.out.println("|  [7] Room Type Demand Report                             |");
            System.out.println("|  [0] Exit Module                                         |");
            System.out.println("+==========================================================+");

            choice = readIntInput("Select an option (0-7): ", 0, 7);
            System.out.println();

            switch (choice) {
                case 1 -> handleRegisterWalkIn();
                case 2 -> handleAssignRoom();
                case 3 -> handleCancelBooking();
                case 4 -> handleViewAndSearchBookings();
                case 5 -> displayQueueStatus();
                case 6 -> handleBookingAnalyticsReport();
                case 7 -> handleRoomTypeDemandReport();
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

        LocalDate checkInDate = readValidCheckInDate("Check-In Date (YYYY-MM-DD)");
        LocalDate checkOutDate = readValidCheckOutDate("Check-Out Date (YYYY-MM-DD)", checkInDate);

        boolean hasRooms = bookingControl.displayAvailableRoomsForStay(checkInDate, checkOutDate);
        
        if (!hasRooms) {
            System.out.println("  [!] No rooms available for the selected dates.");
            return;
        }

        System.out.print("\n  Proceed with booking for these dates? (Y/N): ");
        String proceed = scanner.nextLine().trim();
        if (!proceed.equalsIgnoreCase("Y") && !proceed.equalsIgnoreCase("YES")) {
            System.out.println("  [ Cancelled ] Returning to main menu.");
            return;
        }

        System.out.println("\n  --- Please enter guest details ---");
        String name = readNameInput("Guest Full Name           : ");
        String gender = readGenderInput("Gender (M/F)               : ");
        String phone = readPhoneInput("Contact Phone Number      : ");
        String email = readEmailInput("Email Address             : ");
        String icPassport = readIcPassportInput("IC / Passport Number      : ");
        
        int guestCount = readIntInput("Number of Guests (1-10)   : ", 1, 10);

        // Loyalty Linkage
        control.LoyaltyControl loyaltyControl = bookingControl.getLoyaltyControl();
        entity.Member member = null;
        int typeChoice = -1;
        String roomTypePreference = null;
        if (loyaltyControl != null) {
            member = loyaltyControl.findMemberByEmailOrPhone(email, phone);
            if (member != null) {
                System.out.println("\n  >>> [ Loyalty Member Recognized! ] <<<");
                System.out.println("  Member ID     : " + member.getMemberId());
                System.out.println("  Tier Status   : " + member.getTier() + " Member");
                System.out.println("  Loyalty Points: " + member.getPoints());
                if (member.getPreferredRoomType() != null && !member.getPreferredRoomType().trim().isEmpty()) {
                    System.out.println("  Preferred Room Type: " + member.getPreferredRoomType());
                    System.out.print("  Would you like to default their selection to " + member.getPreferredRoomType() + "? (Y/N): ");
                    String usePreferred = scanner.nextLine().trim();
                    if (usePreferred.equalsIgnoreCase("Y") || usePreferred.equalsIgnoreCase("YES")) {
                        roomTypePreference = member.getPreferredRoomType();
                    }
                }
            } else {
                System.out.print("\n  [ Loyalty Program ] This guest is not yet a loyalty member. Sign them up for free? (Y/N): ");
                String signUpChoice = scanner.nextLine().trim();
                if (signUpChoice.equalsIgnoreCase("Y") || signUpChoice.equalsIgnoreCase("YES")) {
                    System.out.print("  Enter Date of Birth (YYYY-MM-DD, optional): ");
                    String dobStr = scanner.nextLine().trim();
                    member = loyaltyControl.registerNewMember(name, email, phone, dobStr);
                    if (member != null) {
                        System.out.println("  [ SUCCESS ] Loyalty member account created: " + member.getMemberId() + " (" + member.getTier() + " tier).");
                    } else {
                        System.out.println("  [ Warning ] Could not auto-register member (Invalid email format/ID). Continuing check-in.");
                    }
                }
            }
        }

        if (roomTypePreference == null) {
            System.out.println("\nSelect Preferred Room Type:");
            boolean opt1 = bookingControl.isRoomTypeAvailableForStay("Standard Single", checkInDate, checkOutDate);
            boolean opt2 = bookingControl.isRoomTypeAvailableForStay("Standard Double", checkInDate, checkOutDate);
            boolean opt3 = bookingControl.isRoomTypeAvailableForStay("Deluxe Suite", checkInDate, checkOutDate);
            boolean opt4 = bookingControl.isRoomTypeAvailableForStay("Executive Suite", checkInDate, checkOutDate);
            boolean opt5 = bookingControl.isRoomTypeAvailableForStay("Presidential Suite", checkInDate, checkOutDate);

            if (opt1) System.out.println("  [1] Standard Single      ($120.00 / night)");
            if (opt2) System.out.println("  [2] Standard Double      ($150.00 / night)");
            if (opt3) System.out.println("  [3] Deluxe Suite         ($250.00 / night)");
            if (opt4) System.out.println("  [4] Executive Suite      ($500.00 / night)");
            if (opt5) System.out.println("  [5] Presidential Suite   ($1200.00 / night)");

            java.util.List<Integer> validChoices = new java.util.ArrayList<>();
            if (opt1) validChoices.add(1);
            if (opt2) validChoices.add(2);
            if (opt3) validChoices.add(3);
            if (opt4) validChoices.add(4);
            if (opt5) validChoices.add(5);

            if (validChoices.isEmpty()) {
                System.out.println("  [!] Error: No room types available for selection.");
                return;
            }

            while (true) {
                typeChoice = readIntInput("Choice (" + validChoices.toString().replace("[", "").replace("]", "") + "): ", 1, 5);
                if (validChoices.contains(typeChoice)) {
                    break;
                }
                System.out.println("  [!] Invalid choice. Please select from the available options.");
            }

            roomTypePreference = switch (typeChoice) {
                case 2 -> "Standard Double";
                case 3 -> "Deluxe Suite";
                case 4 -> "Executive Suite";
                case 5 -> "Presidential Suite";
                default -> "Standard Single";
            };
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

        System.out.println("  Select Room Type Queue to Process:");
        System.out.println("  -----------------------------------------------------------------------------");
        System.out.printf("  %-8s %-22s | %-16s | %-14s\n", "Option", "Room Type", "Waiting Guests", "Next Guest Stay");
        System.out.println("  -----------------------------------------------------------------------------");

        String[] types = {"Standard Single", "Standard Double", "Deluxe Suite", "Executive Suite", "Presidential Suite"};
        for (int i = 0; i < types.length; i++) {
            int waitCount = bookingControl.getWaitingCountByType(types[i]);
            String nextStay = "-";
            if (waitCount > 0) {
                Booking next = bookingControl.peekNextInQueue(types[i]);
                if (next != null) {
                    nextStay = next.getCheckInDate() + " to " + next.getCheckOutDate();
                }
            }
            System.out.printf("  [%d]      %-22s | %-16d | %-14s\n", i + 1, types[i], waitCount, nextStay);
        }
        System.out.printf("  [0]      %-22s | %-16s | %-14s\n", "Cancel / Return", "-", "-");
        System.out.println("  -----------------------------------------------------------------------------");

        int typeChoice = readIntInput("Choice (0-5): ", 0, 5);

        if (typeChoice == 0) {
            System.out.println("\n  [ System ] Room assignment operation cancelled.");
            return;
        }

        String selectedType = switch (typeChoice) {
            case 2 -> "Standard Double";
            case 3 -> "Deluxe Suite";
            case 4 -> "Executive Suite";
            case 5 -> "Presidential Suite";
            default -> "Standard Single";
        };

        if (bookingControl.getWaitingCountByType(selectedType) == 0) {
            System.out.println("\n  [ NOTICE ] No guests are currently waiting in the " + selectedType + " queue.");
            return;
        }

        // Peek at the next guest's dates to do a date-aware availability check
        Booking nextGuest = bookingControl.peekNextInQueue(selectedType);
        if (nextGuest == null) {
            System.out.println("\n  [ ERROR ] Could not read next guest's booking details.");
            return;
        }

        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(nextGuest.getCheckInDate());
            checkOut = LocalDate.parse(nextGuest.getCheckOutDate());
        } catch (Exception e) {
            System.out.println("\n  [ ERROR ] Invalid dates on booking " + nextGuest.getBookingID() + ". Cannot assign room.");
            return;
        }

        System.out.println("\n  Next Guest: " + nextGuest.getGuest().getGuestName() 
                + "  |  Stay: " + checkIn + " to " + checkOut);

        // Show only rooms available for the guest's actual stay dates
        boolean hasRoom = bookingControl.displayAvailableRoomsForType(selectedType, checkIn, checkOut);

        if (!hasRoom) {
            System.out.println("\n  [ WARNING ] No " + selectedType + " rooms are available for "
                    + checkIn + " to " + checkOut + ". Cannot assign room.");
            return;
        }

        Booking assigned = bookingControl.assignRoomByRoomTypeAndDates(selectedType, checkIn, checkOut);

        if (assigned != null) {
            System.out.println("\n  [ SUCCESS ] Room Assigned Successfully!");
            System.out.println("   Booking ID    : " + assigned.getBookingID());
            System.out.println("   Guest Name    : " + assigned.getGuest().getGuestName());
            System.out.println("   Room Assigned : " + assigned.getRoom().getRoomNumber()
                               + " (" + assigned.getRoom().getRoomType() + ")");
            System.out.println("   Stay Dates    : " + checkIn + " to " + checkOut);
            System.out.println("   * Status      : Transferred to Front Desk for Confirmation Number Assignment.");
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

        String bookingID;
        Booking targetBooking;

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
        System.out.printf("   - Standard Single    : %d guest(s)\n", bookingControl.getWaitingCountByType("Standard Single"));
        System.out.printf("   - Standard Double    : %d guest(s)\n", bookingControl.getWaitingCountByType("Standard Double"));
        System.out.printf("   - Deluxe Suite       : %d guest(s)\n", bookingControl.getWaitingCountByType("Deluxe Suite"));
        System.out.printf("   - Executive Suite    : %d guest(s)\n", bookingControl.getWaitingCountByType("Executive Suite"));
        System.out.printf("   - Presidential Suite : %d guest(s)\n", bookingControl.getWaitingCountByType("Presidential Suite"));
        System.out.println("   -------------------------------------");
        System.out.println("   Total Waiting Guests        : " + bookingControl.getWaitingCount());
        System.out.println("   Pending Front Desk Conf.    : " + bookingControl.getPendingConfirmationsCount());
        System.out.println("   Total Available Rooms       : " + bookingControl.getAvailableRoomsCount());
        System.out.println("   Confirmed Bookings          : " + bookingControl.getConfirmedCount());
    }

    private void handleBookingAnalyticsReport() {
        printHeader("6. BOOKING ANALYTICS REPORT");
        System.out.println("  Enter the date range for the report (YYYY-MM-DD):");
        
        LocalDate defaultStart = LocalDate.now().minusMonths(1);
        System.out.print("  Start Date [Press ENTER for '" + defaultStart + "']: ");
        String startStr = scanner.nextLine().trim();
        if (startStr.isEmpty()) startStr = defaultStart.toString();
        
        LocalDate defaultEnd = LocalDate.now();
        System.out.print("  End Date   [Press ENTER for '" + defaultEnd + "']: ");
        String endStr = scanner.nextLine().trim();
        if (endStr.isEmpty()) endStr = defaultEnd.toString();

        System.out.println("  Select Room Type to Filter (or [0] for ALL):");
        System.out.println("  [1] Standard Single");
        System.out.println("  [2] Standard Double");
        System.out.println("  [3] Deluxe Suite");
        System.out.println("  [4] Executive Suite");
        System.out.println("  [5] Presidential Suite");
        System.out.println("  [0] ALL Room Types");
        
        int typeChoice = readIntInput("  Choice (0-5): ", 0, 5);
        String roomFilter = switch (typeChoice) {
            case 1 -> "Standard Single";
            case 2 -> "Standard Double";
            case 3 -> "Deluxe Suite";
            case 4 -> "Executive Suite";
            case 5 -> "Presidential Suite";
            default -> "ALL";
        };
        
        bookingControl.generateBookingAnalyticsReport(startStr, endStr, roomFilter);
    }

    private void handleRoomTypeDemandReport() {
        printHeader("7. ROOM TYPE DEMAND REPORT");
        System.out.println("  Select Room Type to Filter (or [0] for ALL):");
        System.out.println("  [1] Standard Single");
        System.out.println("  [2] Standard Double");
        System.out.println("  [3] Deluxe Suite");
        System.out.println("  [4] Executive Suite");
        System.out.println("  [5] Presidential Suite");
        System.out.println("  [0] ALL Room Types");
        
        int typeChoice = readIntInput("  Choice (0-5): ", 0, 5);
        String roomFilter = switch (typeChoice) {
            case 1 -> "Standard Single";
            case 2 -> "Standard Double";
            case 3 -> "Deluxe Suite";
            case 4 -> "Executive Suite";
            case 5 -> "Presidential Suite";
            default -> "ALL";
        };
        
        System.out.println("\n  Select Booking Status to Filter (or [0] for ALL):");
        System.out.println("  [1] Waiting");
        System.out.println("  [2] Assigned");
        System.out.println("  [3] Confirmed");
        System.out.println("  [0] ALL Status");
        int statusChoice = readIntInput("  Choice (0-3): ", 0, 3);
        String statusFilter = switch (statusChoice) {
            case 1 -> "Waiting";
            case 2 -> "Assigned";
            case 3 -> "Confirmed";
            default -> "ALL";
        };

        int minGuests = readIntInput("\n  Enter Minimum Number of Guests (1-10): ", 1, 10);

        bookingControl.generateRoomTypeDemandReport(roomFilter, statusFilter, minGuests);
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
