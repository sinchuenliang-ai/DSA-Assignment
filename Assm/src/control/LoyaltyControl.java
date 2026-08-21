package control;

import adt.ListInterface;
import adt.QueueInterface;
import adt.StackInterface;
import adt.LinkedList;
import adt.LinkedQueue;
import adt.LinkedStack;
import boundary.LoyaltyUI;
import utility.FileHandler;
import entity.Member;
import entity.Staff;
import entity.Transaction;
import entity.Notification;


public class LoyaltyControl {
    // Data structures
    private ListInterface<Member> memberList;
    private QueueInterface<Transaction> transactionQueue;
    private ListInterface<Notification> notificationList;
    private StackInterface<Transaction> redemptionStack;
    
    // UI reference
    private LoyaltyUI ui;
    
    // Service references
    private MemberService memberService;
    private PointsService pointsService;
    private RedemptionService redemptionService;
    private NotificationService notificationService;
    private ReportService reportService;

    
     // Default constructor - initializes all data structures and services
    public LoyaltyControl() {
        ui = new LoyaltyUI();
        
        // Load data from files
        memberList = FileHandler.loadMembers();
        transactionQueue = FileHandler.loadTransactions();
        notificationList = FileHandler.loadNotifications();
        redemptionStack = new LinkedStack<>();
        
        // Initialize if null
        if (memberList == null) memberList = new LinkedList<>();
        if (transactionQueue == null) transactionQueue = new LinkedQueue<>();
        if (notificationList == null) notificationList = new LinkedList<>();
        
        // Initialize services
        memberService = new MemberService(memberList);
        pointsService = new PointsService(memberList, transactionQueue, notificationList);
        redemptionService = new RedemptionService(memberList, transactionQueue, notificationList, redemptionStack);
        notificationService = new NotificationService(notificationList);
        reportService = new ReportService(memberList, transactionQueue);
        
        // Initialize with default data if empty
        FileHandler.initializeLoyaltyDataIfEmpty(memberList, transactionQueue, notificationList);
    }

    /**
     * Runs the loyalty service main menu
     */
    public void runService() {
        int choice;
        do {
            choice = ui.getMainMenuChoice();
            switch (choice) {
                case 0 -> ui.displayExitMessage();
                case 1 -> manageMembers();
                case 2 -> managePoints();
                case 3 -> manageRedemptions();
                case 4 -> manageNotifications();
                case 5 -> handleReportAccess();
                case 6 -> viewPersonalizedPromotion();
                case 7 -> handleStaffLoginLogout();
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
        saveAllData();
    }

    /**
     * Handles staff login/logout
     */
    private void handleStaffLoginLogout() {
        if (ui.isStaffAuthenticated()) {
            // Logout
            Staff staff = ui.getCurrentStaff();
            ui.displayMessage("\n[ System ] Logging out staff: " + staff.getStaffName());
            ui.setCurrentStaff(null);
            ui.displayMessage("[ System ] Successfully logged out.");
        } else {
            // Login
            String[] credentials = ui.getStaffLoginCredentials();
            Staff authenticated = FileHandler.authenticateStaff(credentials[0], credentials[1]);
            ui.displayStaffAuthResult(authenticated != null, authenticated);
            if (authenticated != null) {
                ui.setCurrentStaff(authenticated);
            }
        }
    }

    
    private void handleReportAccess() {
        if (!ui.isStaffAuthenticated()) {
            ui.displayAccessDeniedMessage();
            // Option to login immediately
            System.out.print("Would you like to login now? (Y/N): ");
            String response = ui.inputString("");
            if (response.equalsIgnoreCase("Y") || response.equalsIgnoreCase("YES")) {
                String[] credentials = ui.getStaffLoginCredentials();
                Staff authenticated = FileHandler.authenticateStaff(credentials[0], credentials[1]);
                ui.displayStaffAuthResult(authenticated != null, authenticated);
                if (authenticated != null) {
                    ui.setCurrentStaff(authenticated);
                    generateReports();
                }
            }
            return;
        }
        
        // Staff is authenticated, proceed to reports
        generateReports();
    }

   
    private void manageMembers() {
        int choice;
        do {
            choice = ui.getMemberManagementChoice();
            switch (choice) {
                case 0 -> {}
                case 1 -> {
                    Member newMember = ui.inputMemberDetails();
                    if (memberService.addMember(newMember)) {
                        ui.displayMessage("Member registered successfully!");
                        saveAllData();
                    } else {
                        ui.displayMessage("Failed to register member. Please check ID and email.");
                    }
                }
                case 2 -> ui.displayMembers(memberService.getAllMembers());
                case 3 -> {
                    String term = ui.inputSearchTerm();
                    ui.displayMembers(memberService.searchMembers(term));
                }
                case 4 -> {
                    String id = ui.inputMemberId("Enter Member ID: ");
                    Member member = memberService.findMemberById(id);
                    if (member != null) {
                        ui.displayMessage("\nUpdating profile for: " + member.getName());
                        String name = ui.inputString("New Name (Enter to skip): ");
                        if (!name.isEmpty()) member.setName(name);
                        String email = ui.inputString("New Email (Enter to skip): ");
                        if (!email.isEmpty()) member.setEmail(email);
                        String phone = ui.inputString("New Phone (Enter to skip): ");
                        if (!phone.isEmpty()) member.setPhoneNumber(phone);
                        String roomType = ui.inputString("Preferred Room Type (Enter to skip): ");
                        if (!roomType.isEmpty()) member.setPreferredRoomType(roomType);
                        ui.displayMessage("Profile updated!");
                        saveAllData();
                    } else {
                        ui.displayMessage("Member not found!");
                    }
                }
                case 5 -> {  //  Delete Member
                    String id = ui.inputMemberId("Enter Member ID to delete: ");
                    Member member = memberService.findMemberById(id);
                    if (member != null) {
                        ui.displayMessage("\n+================================================+");
                        ui.displayMessage("|  [ WARNING ] You are about to delete a member   |");
                        ui.displayMessage("+================================================+");
                        ui.displayMemberDetails(member);
                        if (ui.confirmAction("\nAre you sure you want to permanently delete this member?")) {
                            boolean deleted = memberService.deleteMember(id);
                            if (deleted) {
                                ui.displayMessage("\n+================================================+");
                                ui.displayMessage("|  [ SUCCESS ] Member deleted successfully!        |");
                                ui.displayMessage("+================================================+");
                                saveAllData();
                            } else {
                                ui.displayMessage("\n[ ERROR ] Failed to delete member.");
                            }
                        } else {
                            ui.displayMessage("\n[ System ] Deletion cancelled.");
                        }
                    } else {
                        ui.displayMessage("[ ERROR ] Member not found!");
                    }
                }
                case 6 -> {  // View Tier Status
                    String id2 = ui.inputMemberId("Enter Member ID: ");
                    Member m = memberService.findMemberById(id2);
                    if (m != null) ui.displayPointsBalance(m);
                    else ui.displayMessage("Member not found!");
                }
                case 7 -> ui.displayTierBenefits();
                case 8 -> {  // View Tier Progression
                    String id3 = ui.inputMemberId("Enter Member ID: ");
                    Member m2 = memberService.findMemberById(id3);
                    if (m2 != null) ui.displayTierProgress(m2);
                    else ui.displayMessage("Member not found!");
                }
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }


    private void managePoints() {
        int choice;
        do {
            choice = ui.getPointsManagementChoice();
            switch (choice) {
                case 0 -> {}
                case 1 -> {
                    String id = ui.inputMemberId("Enter Member ID: ");
                    Member member = memberService.findMemberById(id);
                    if (member != null) {
                        double amount = ui.inputAmountSpent();
                        String bookingId = ui.inputString("Booking ID (optional): ");
                        int points = pointsService.earnPoints(member, amount, bookingId);
                        if (points > 0) {
                            ui.displayMessage("Points earned successfully! +" + points + " points");
                            ui.displayMessage("New balance: " + member.getPoints() + " points");
                            saveAllData();
                        } else {
                            ui.displayMessage("Failed to earn points. Please check the amount.");
                        }
                    } else {
                        ui.displayMessage("Member not found!");
                    }
                }
                case 2 -> {
                    String id2 = ui.inputMemberId("Enter Member ID: ");
                    Member m = memberService.findMemberById(id2);
                    if (m != null) ui.displayPointsBalance(m);
                    else ui.displayMessage("Member not found!");
                }
                case 3 -> {
                    String id3 = ui.inputMemberId("Enter Member ID: ");
                    String history = pointsService.getPointsHistory(id3);
                    ui.displayMessage(history);
                }
                case 4 -> {
                    int expiryCount = pointsService.checkPointsExpiry();
                    ui.displayMessage("Found " + expiryCount + " members with expiring points.");
                    saveAllData();
                }
                case 5 -> {
                    String id4 = ui.inputMemberId("Enter Member ID: ");
                    Member m2 = memberService.findMemberById(id4);
                    if (m2 != null) ui.displayTierProgress(m2);
                    else ui.displayMessage("Member not found!");
                }
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }


    private void manageRedemptions() {
        int choice;
        do {
            choice = ui.getRedemptionManagementChoice();
            switch (choice) {
                case 0 -> {}
                case 1 -> {
                    String id = ui.inputMemberId("Enter Member ID: ");
                    Member member = memberService.findMemberById(id);
                    if (member != null) {
                        ui.displayRedemptionOptions(member);
                        int option = ui.getIntInput();
                        int points = 0;
                        String type = "";
                        switch (option) {
                            case 1: points = 2000; type = "Room Upgrade"; break;
                            case 2: points = 1000; type = "Room Discount"; break;
                            case 3: points = 3000; type = "Spa Treatment"; break;
                            case 4: points = 2500; type = "Gift Voucher"; break;
                            case 5: 
                                points = ui.inputPointsToRedeem();
                                type = ui.inputRedemptionType();
                                break;
                            default: ui.displayMessage("Invalid option!");
                        }
                        if (points > 0) {
                            double discount = redemptionService.processRedemption(member, points, type);
                            if (discount > 0) {
                                ui.displayMessage("Redemption successful!");
                                ui.displayMessage("Savings: RM " + String.format("%.2f", discount));
                                ui.displayMessage("Remaining points: " + member.getPoints());
                                saveAllData();
                            } else {
                                ui.displayMessage("Redemption failed!");
                            }
                        }
                    } else {
                        ui.displayMessage("Member not found!");
                    }
                }
                case 2 -> {
                    String id2 = ui.inputMemberId("Enter Member ID: ");
                    String history = redemptionService.getRedemptionHistory(id2);
                    ui.displayMessage(history);
                }
                case 3 -> {
                    boolean undone = redemptionService.undoRedemption();
                    if (undone) {
                        ui.displayMessage("Redemption undone successfully!");
                        saveAllData();
                    } else {
                        ui.displayMessage("No redemptions to undo!");
                    }
                }
                case 4 -> ui.displayRedemptionRates();
                case 5 -> {
                    String id3 = ui.inputMemberId("Enter Member ID: ");
                    Member m = memberService.findMemberById(id3);
                    if (m != null) ui.displayRedemptionEligibility(m);
                    else ui.displayMessage("Member not found!");
                }
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }


    private void manageNotifications() {
        int choice;
        do {
            choice = ui.getNotificationManagementChoice();
            switch (choice) {
                case 0 -> {}
                case 1 -> {
                    String id = ui.inputMemberId("Enter Member ID: ");
                    Member member = memberService.findMemberById(id);
                    if (member != null) {
                        String notifs = notificationService.getMemberNotifications(id);
                        ui.displayNotifications(notifs, member);
                        notificationService.markMemberNotificationsRead(id);
                        saveAllData();
                    } else {
                        ui.displayMessage("Member not found!");
                    }
                }
                case 2 -> ui.displayAllNotifications(notificationService.getAllNotifications());
                case 3 -> {
                    String notifId = ui.inputString("Enter Notification ID: ");
                    boolean marked = notificationService.markNotificationRead(notifId);
                    ui.displayMessage(marked ? "Notification marked as read!" : "Notification not found!");
                    saveAllData();
                }
                case 4 -> {
                    int count = notificationService.generatePromotionalNotifications();
                    ui.displayMessage("Generated " + count + " promotional notifications!");
                    saveAllData();
                }
                case 5 -> {
                    String notifId2 = ui.inputString("Enter Notification ID: ");
                    boolean dismissed = notificationService.dismissNotification(notifId2);
                    ui.displayMessage(dismissed ? "Notification dismissed!" : "Notification not found!");
                    saveAllData();
                }
                case 6 -> {
                    int expiryCount = pointsService.checkPointsExpiry();
                    ui.displayMessage("Found " + expiryCount + " members with expiring points.");
                    saveAllData();
                }
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

//staff only
    private void generateReports() {
        // Double-check authentication
        if (!ui.isStaffAuthenticated()) {
            ui.displayAccessDeniedMessage();
            return;
        }
        
        int choice;
        do {
            choice = ui.getReportChoice();
            switch (choice) {
                case 0 -> {}
                case 1 -> ui.displayReport(reportService.generateMemberReport());
                case 2 -> ui.displayReport(reportService.generateTransactionReport());
                case 3 -> ui.displayReport(reportService.generateTierReport());
                case 4 -> ui.displayReport(reportService.generatePointsReport());
                case 5 -> ui.displayReport(reportService.generateRedemptionReport());
                case 6 -> ui.displayReport(reportService.generateTierProgressionReport());
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

     // Views personalized promotion for a member
    private void viewPersonalizedPromotion() {
        String id = ui.inputMemberId("Enter Member ID: ");
        Member member = memberService.findMemberById(id);
        if (member != null) {
            String promotion = memberService.getPersonalizedPromotion(member);
            ui.displayPersonalizedPromotion(promotion);
        } else {
            ui.displayMessage("Member not found!");
        }
    }

      //Saves all data to files
    public void saveAllData() {
        FileHandler.saveMembers(memberList);
        FileHandler.saveTransactions(transactionQueue);
        FileHandler.saveNotifications(notificationList);
    }

    public Member findMemberByEmailOrPhone(String email, String phone) {
        if (memberList == null) return null;
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            if ((email != null && !email.trim().isEmpty() && m.getEmail().equalsIgnoreCase(email.trim())) ||
                (phone != null && !phone.trim().isEmpty() && m.getPhoneNumber().replace("-", "").equalsIgnoreCase(phone.trim().replace("-", "")))) {
                return m;
            }
        }
        return null;
    }

    /**
     * Finds a loyalty member by name (case-insensitive, partial match).
     * Used by check-out flow which only has guest name available.
     */
    public Member findMemberByName(String name) {
        if (memberList == null || name == null || name.trim().isEmpty()) return null;
        String nameLower = name.trim().toLowerCase();
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            if (m.getName() != null && m.getName().toLowerCase().contains(nameLower)) {
                return m;
            }
        }
        return null;
    }


    public int rewardPointsForStay(Member member, double amountSpent, String bookingId) {
        if (member == null || pointsService == null) return 0;
        int points = pointsService.earnPoints(member, amountSpent, bookingId);
        saveAllData();
        return points;
    }

    public Member registerNewMember(String name, String email, String phone, String dob) {
        if (memberService == null) return null;
        
        int maxIdNum = 5;
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            String mId = memberList.getEntry(i).getMemberId();
            if (mId != null && mId.startsWith("M")) {
                try {
                    int num = Integer.parseInt(mId.substring(1));
                    if (num > maxIdNum) maxIdNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        String nextMemberId = String.format("M%03d", maxIdNum + 1);
        
        Member newMember = new Member(nextMemberId, name, email, phone);
        if (dob != null && !dob.trim().isEmpty()) {
            try {
                newMember.setDateOfBirth(java.time.LocalDate.parse(dob.trim()));
            } catch (Exception ignored) {}
        }
        
        boolean success = memberService.addMember(newMember);
        if (success) {
            saveAllData();
            return newMember;
        }
        return null;
    }
}