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

    //main menu
    public void runService() {
        int choice;
        do {
            choice = ui.getMainMenuChoice();
            switch (choice) {
                case 0 -> handleLogout();
                case 1 -> handleMemberLogin();
                case 2 -> handleStaffLogin();
                case 3 -> handleMemberManagement();
                case 4 -> handlePointsManagement();
                case 5 -> handleRedemptionManagement();
                case 6 -> handleNotificationManagement();
                case 7 -> handlePersonalizedPromotion();
                case 8 -> handleReportAccess();
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
        saveAllData();
    }

    /**
     * Handles member login
     */
    private void handleMemberLogin() {
        if (ui.isMemberAuthenticated()) {
            ui.displayMessage("\n[ System ] You are already logged in as: " + ui.getCurrentMember().getName());
            return;
        }
        
        String[] credentials = ui.getMemberLoginCredentials();
        String memberId = credentials[0];
        String email = credentials[1];
        
        Member member = memberService.findMemberById(memberId);
        if (member != null && member.getEmail().equalsIgnoreCase(email)) {
            ui.setCurrentMember(member);
            ui.displayMemberAuthResult(true, member);
        } else {
            ui.displayMemberAuthResult(false, null);
        }
    }

    /**
     * Handles staff login
     */
    private void handleStaffLogin() {
        if (ui.isStaffAuthenticated()) {
            ui.displayMessage("\n[ System ] You are already logged in as staff: " + ui.getCurrentStaff().getStaffName());
            return;
        }
        
        String[] credentials = ui.getStaffLoginCredentials();
        Staff authenticated = FileHandler.authenticateStaff(credentials[0], credentials[1]);
        ui.displayStaffAuthResult(authenticated != null, authenticated);
        if (authenticated != null) {
            ui.setCurrentStaff(authenticated);
        }
    }

    /**
     * Handles logout
     */
    private void handleLogout() {
        if (ui.isStaffAuthenticated()) {
            ui.displayMessage("\n[ System ] Logging out staff: " + ui.getCurrentStaff().getStaffName());
            ui.setCurrentStaff(null);
        }
        if (ui.isMemberAuthenticated()) {
            ui.displayMessage("\n[ System ] Logging out member: " + ui.getCurrentMember().getName());
            ui.setCurrentMember(null);
        }
        ui.displayLogoutMessage();
        ui.displayExitMessage();
    }

    /**
     * Handles member management (member view) with delete option
     */
    private void handleMemberManagement() {
        if (!ui.isMemberAuthenticated()) {
            ui.displayMemberRequiredMessage();
            return;
        }
        
        int choice;
        Member current = ui.getCurrentMember();
        do {
            choice = ui.getMemberManagementChoice();
            switch (choice) {
                case 0 -> {}
                case 1 -> ui.displayMemberDetails(current);
                case 2 -> {
                    ui.displayMessage("\nUpdating profile for: " + current.getName());
                    String name = ui.inputString("New Name (Enter to skip): ");
                    if (!name.isEmpty()) current.setName(name);
                    String email = ui.inputString("New Email (Enter to skip): ");
                    if (!email.isEmpty()) current.setEmail(email);
                    String phone = ui.inputString("New Phone (Enter to skip): ");
                    if (!phone.isEmpty()) current.setPhoneNumber(phone);
                    String roomType = ui.inputString("Preferred Room Type (Enter to skip): ");
                    if (!roomType.isEmpty()) current.setPreferredRoomType(roomType);
                    ui.displayMessage("Profile updated!");
                    saveAllData();
                }
                case 3 -> ui.displayPointsBalance(current);
                case 4 -> ui.displayTierBenefits();
                case 5 -> ui.displayTierProgress(current);
                case 6 -> handleDeleteAccount(current);
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    /**
     * Handles account deletion for authenticated member
     */
    private void handleDeleteAccount(Member member) {
        if (member == null) {
            ui.displayMessage("[ ERROR ] No member found to delete.");
            return;
        }
        
        // Show warning and get confirmation
        if (!ui.confirmAccountDeletion(member)) {
            ui.displayMessage("\n[ System ] Account deletion cancelled.");
            return;
        }
        
        // Delete the member
        boolean deleted = memberService.deleteMember(member.getMemberId());
        
        if (deleted) {
            // Logout the member after deletion
            ui.displayAccountDeletionResult(true, member);
            ui.setCurrentMember(null);
            saveAllData();
            
            // Display goodbye message
            System.out.println("\nYou have been logged out. Thank you for being a valued member.");
            System.out.println("We hope to see you again in the future!");
            ui.getMainMenuChoice();
        } else {
            ui.displayAccountDeletionResult(false, member);
        }
    }

    /**
     * Handles points management (member view)
     */
    private void handlePointsManagement() {
        if (!ui.isMemberAuthenticated()) {
            ui.displayMemberRequiredMessage();
            return;
        }
        
        int choice;
        Member current = ui.getCurrentMember();
        do {
            choice = ui.getPointsManagementChoice();
            switch (choice) {
                case 0 -> {}
                case 1 -> ui.displayPointsBalance(current);
                case 2 -> {
                    String history = pointsService.getPointsHistory(current.getMemberId());
                    ui.displayMessage(history);
                }
                case 3 -> {
                    int expiryCount = pointsService.checkPointsExpiry();
                    ui.displayMessage("Found " + expiryCount + " members with expiring points.");
                    saveAllData();
                }
                case 4 -> ui.displayTierProgress(current);
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    /**
     * Handles redemption management (member view)
     */
    private void handleRedemptionManagement() {
        if (!ui.isMemberAuthenticated()) {
            ui.displayMemberRequiredMessage();
            return;
        }
        
        int choice;
        Member current = ui.getCurrentMember();
        do {
            choice = ui.getRedemptionManagementChoice();
            switch (choice) {
                case 0 -> {}
                
                case 1 -> {  // Process Redemption
                    if (current.getPoints() < 100) {
                        ui.displayMessage("\n[ERROR] You need at least 100 points to redeem!");
                        ui.displayMessage("Current points: " + current.getPoints());
                        break;
                    }
                    
                    ui.displayRedemptionOptions(current);
                    int option = ui.getIntInput();
                    int points = 0;
                    String type = "";
                    
                    switch (option) {
                        case 1 -> { points = 2000; type = "Room Upgrade"; }
                        case 2 -> { points = 1000; type = "Room Discount"; }
                        case 3 -> { points = 3000; type = "Spa Treatment"; }
                        case 4 -> { points = 2500; type = "Gift Voucher"; }
                        case 5 -> {
                            points = ui.inputPointsToRedeem();
                            type = ui.inputRedemptionType();
                        }
                        default -> {
                            ui.displayMessage("Invalid option!");
                            continue;
                        }
                    }
                    
                    if (points > 0 && !type.isEmpty()) {
                        double discount = redemptionService.processRedemption(current, points, type);
                        if (discount > 0) {
                            ui.displayMessage("\n+================================================+");
                            ui.displayMessage("|  ✅ Redemption Successful!                      |");
                            ui.displayMessage("+================================================+");
                            ui.displayMessage("  Savings      : RM " + String.format("%.2f", discount));
                            ui.displayMessage("  Points Used  : " + points);
                            ui.displayMessage("  Remaining    : " + current.getPoints());
                            ui.displayMessage("  💾 Saved in stack for undo.");
                            saveAllData();
                        } else {
                            ui.displayMessage("\n[ERROR] Redemption failed!");
                            ui.displayMessage("  Possible reasons:");
                            ui.displayMessage("  • Not enough points");
                            ui.displayMessage("  • Exceeded tier limit");
                            ui.displayMessage("  • Invalid redemption amount");
                        }
                    }
                }
                
                case 2 -> {  // View Redemption History
                    String history = redemptionService.getRedemptionHistory(current.getMemberId());
                    ui.displayMessage(history);
                }
                
                case 3 -> ui.displayRedemptionRates();
                
                case 4 -> ui.displayRedemptionEligibility(current);
                
                case 5 -> {  // 🔄 Undo Last Redemption
                    handleUndoRedemption(current);
                }
                
                case 6 -> {  // 📊 View Undo Stack
                    String history = redemptionService.getUndoHistory();
                    ui.displayUndoHistory(history);
                }
                
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    /**
     Handle undo redemption with confirmation
     */
    private void handleUndoRedemption(Member current) {
        // Get the last redemption without removing it
        Transaction lastRedemption = redemptionService.getLastRedemption();
        
        if (lastRedemption == null) {
            ui.displayMessage("\n[INFO] No redemptions to undo.");
            return;
        }
        
        // Confirm with user
        if (!ui.confirmUndo(lastRedemption)) {
            ui.displayMessage("\n[System] Undo cancelled.");
            return;
        }
        
        // Perform undo
        boolean success = redemptionService.undoRedemption();
        
        if (success) {
            // Refresh member data
            Member updatedMember = memberService.findMemberById(current.getMemberId());
            if (updatedMember != null) {
                ui.setCurrentMember(updatedMember);
                ui.displayUndoResult(true, lastRedemption, updatedMember);
            } else {
                ui.displayMessage("[ERROR] Could not refresh member data.");
            }
        } else {
            ui.displayUndoResult(false, null, null);
        }
        
        saveAllData();
    }

    /**
      Handles notification management (member view)
     */
    private void handleNotificationManagement() {
        if (!ui.isMemberAuthenticated()) {
            ui.displayMemberRequiredMessage();
            return;
        }
        
        int choice;
        Member current = ui.getCurrentMember();
        do {
            choice = ui.getNotificationManagementChoice();
            switch (choice) {
                case 0 -> {}
                case 1 -> {
                    String notifs = notificationService.getMemberNotifications(current.getMemberId());
                    ui.displayNotifications(notifs, current);
                    notificationService.markMemberNotificationsRead(current.getMemberId());
                    saveAllData();
                }
                case 2 -> {
                    String notifId = ui.inputString("Enter Notification ID: ");
                    boolean marked = notificationService.markNotificationRead(notifId);
                    ui.displayMessage(marked ? "Notification marked as read!" : "Notification not found!");
                    saveAllData();
                }
                case 3 -> {
                    String notifId2 = ui.inputString("Enter Notification ID: ");
                    boolean dismissed = notificationService.dismissNotification(notifId2);
                    ui.displayMessage(dismissed ? "Notification dismissed!" : "Notification not found!");
                    saveAllData();
                }
                case 4 -> {
                    int expiryCount = pointsService.checkPointsExpiry();
                    ui.displayMessage("Found " + expiryCount + " members with expiring points.");
                    saveAllData();
                }
                default -> ui.displayInvalidChoiceMessage();
            }
        } while (choice != 0);
    }

    /**
     * Handles report access (staff only)
     */
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

    /**
     * Handles personalized promotion (member view)
     */
    private void handlePersonalizedPromotion() {
        if (!ui.isMemberAuthenticated()) {
            ui.displayMemberRequiredMessage();
            return;
        }
        
        Member current = ui.getCurrentMember();
        String promotion = memberService.getPersonalizedPromotion(current);
        ui.displayPersonalizedPromotion(promotion);
    }

    /**
     * Generates reports (staff only)
     */
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

    /**
     * Saves all data to files
     */
    public void saveAllData() {
        FileHandler.saveMembers(memberList);
        FileHandler.saveTransactions(transactionQueue);
        FileHandler.saveNotifications(notificationList);
    }

    // =========================================================================
    // PUBLIC METHODS FOR EXTERNAL INTEGRATION
    // =========================================================================

    /**
     * Finds a member by email or phone
     */
    public Member findMemberByEmailOrPhone(String email, String phone) {
        if (memberList == null) return null;
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            if (email != null && !email.trim().isEmpty() && m.getEmail().equalsIgnoreCase(email.trim())) {
                return m;
            }
            if (phone != null && !phone.trim().isEmpty()) {
                String cleanPhone = phone.trim().replace("-", "").replace(" ", "");
                String cleanMemberPhone = m.getPhoneNumber().replace("-", "").replace(" ", "");
                if (cleanMemberPhone.equalsIgnoreCase(cleanPhone)) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * Finds a loyalty member by name (case-insensitive, partial match)
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

    /**
     * Rewards points for a stay
     */
    public int rewardPointsForStay(Member member, double amountSpent, String bookingId) {
        if (member == null || pointsService == null) return 0;
        int points = pointsService.earnPoints(member, amountSpent, bookingId);
        saveAllData();
        return points;
    }

    /**
     * Registers a new member
     */
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

    /**
     * Gets redemption stack size
     */
    public int getRedemptionStackSize() {
        return redemptionService.getRedemptionStackSize();
    }

    /**
     * Undoes the last redemption
     */
    public boolean undoRedemption() {
        return redemptionService.undoRedemption();
    }

    /**
     * Processes a redemption
     */
    public double processRedemption(Member member, int points, String type) {
        return redemptionService.processRedemption(member, points, type);
    }
}
