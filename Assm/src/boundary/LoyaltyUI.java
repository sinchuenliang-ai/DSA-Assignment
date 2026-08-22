package boundary;

import entity.Member;
import entity.Staff;
import entity.Transaction;

import java.util.Scanner;

/**
 * LoyaltyUI - Boundary class for Loyalty & Rewards System
 * Handles all user interface interactions
 * 
 * @author TARUMT Resorts Team
 */
public class LoyaltyUI {
    private Scanner scanner;
    private Staff currentStaff;
    private Member currentMember;

    public LoyaltyUI() {
        scanner = new Scanner(System.in);
        currentStaff = null;
        currentMember = null;
    }

    /**
     * Sets the currently authenticated staff
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
    }

    /**
     * Gets the currently authenticated staff
     */
    public Staff getCurrentStaff() {
        return currentStaff;
    }

    /**
     * Sets the currently authenticated member
     */
    public void setCurrentMember(Member member) {
        this.currentMember = member;
    }

    /**
     * Gets the currently authenticated member
     */
    public Member getCurrentMember() {
        return currentMember;
    }

    /**
     * Checks if a staff is authenticated
     */
    public boolean isStaffAuthenticated() {
        return currentStaff != null;
    }

    /**
     * Checks if a member is authenticated
     */
    public boolean isMemberAuthenticated() {
        return currentMember != null;
    }

    /**
     * Gets staff login credentials
     */
    public String[] getStaffLoginCredentials() {
        System.out.println("\n+================================================+");
        System.out.println("|         STAFF AUTHENTICATION REQUIRED         |");
        System.out.println("|         Access to Reports & Analytics         |");
        System.out.println("+================================================+");
        System.out.print("Enter Staff ID: ");
        String staffID = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
        return new String[]{staffID, password};
    }

    /**
     * Gets member login credentials
     */
    public String[] getMemberLoginCredentials() {
        System.out.println("\n+================================================+");
        System.out.println("|         MEMBER LOGIN                          |");
        System.out.println("+================================================+");
        System.out.print("Enter Member ID: ");
        String memberId = scanner.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        return new String[]{memberId, email};
    }

    /**
     * Gets new member registration details
     */
    public Member getMemberRegistrationDetails() {
        System.out.println("\n+================================================+");
        System.out.println("|         NEW MEMBER REGISTRATION              |");
        System.out.println("+================================================+");
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Phone Number: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Date of Birth (YYYY-MM-DD, optional): ");
        String dob = scanner.nextLine().trim();
        System.out.print("Preferred Room Type (optional): ");
        String roomType = scanner.nextLine().trim();
        
        Member member = new Member("", name, email, phone);
        if (!dob.isEmpty()) {
            try {
                member.setDateOfBirth(java.time.LocalDate.parse(dob));
            } catch (Exception e) {
                System.out.println("[!] Invalid date format. Skipping DOB.");
            }
        }
        if (!roomType.isEmpty()) {
            member.setPreferredRoomType(roomType);
        }
        return member;
    }

    /**
     * Displays staff authentication result
     */
    public void displayStaffAuthResult(boolean success, Staff staff) {
        if (success && staff != null) {
            System.out.println("\n+================================================+");
            System.out.println("|  [ SUCCESS ] Staff Authentication Successful!  |");
            System.out.println("|  Welcome, " + staff.getStaffName() + " (" + staff.getRole() + ")");
            System.out.println("+================================================+");
        } else {
            System.out.println("\n+================================================+");
            System.out.println("|  [ ERROR ] Staff Authentication Failed!        |");
            System.out.println("|  Invalid Staff ID or Password.                 |");
            System.out.println("+================================================+");
        }
    }

    /**
     * Displays member authentication result
     */
    public void displayMemberAuthResult(boolean success, Member member) {
        if (success && member != null) {
            System.out.println("\n+================================================+");
            System.out.println("|  [ SUCCESS ] Member Login Successful!         |");
            System.out.println("|  Welcome back, " + member.getName() + "!");
            System.out.println("|  Tier: " + member.getTier() + " | Points: " + member.getPoints());
            System.out.println("+================================================+");
        } else {
            System.out.println("\n+================================================+");
            System.out.println("|  [ ERROR ] Member Login Failed!               |");
            System.out.println("|  Invalid Member ID or Email.                  |");
            System.out.println("+================================================+");
        }
    }

    /**
     * Displays member registration result
     */
    public void displayRegistrationResult(boolean success, Member member) {
        if (success && member != null) {
            System.out.println("\n+================================================+");
            System.out.println("|  [ SUCCESS ] Member Registered Successfully!   |");
            System.out.println("|  Welcome, " + member.getName() + "!");
            System.out.println("|  Member ID: " + member.getMemberId());
            System.out.println("|  Tier: " + member.getTier() + " | Points: " + member.getPoints());
            System.out.println("+================================================+");
        } else {
            System.out.println("\n+================================================+");
            System.out.println("|  [ ERROR ] Member Registration Failed!        |");
            System.out.println("|  Please check your details and try again.     |");
            System.out.println("+================================================+");
        }
    }

    /**
     * Displays the main menu and gets user choice
     */
    public int getMainMenuChoice() {
        clearScreen();
        System.out.println("\n+================================================+");
        System.out.println("|                                                |");
        System.out.println("|       TARUMT RESORTS - LOYALTY & REWARDS      |");
        System.out.println("|                                                |");
        System.out.println("+================================================+");
        
        // Display authentication status
        if (currentStaff != null) {
            System.out.println("|  Staff: " + String.format("%-25s", currentStaff.getStaffName() + " (" + currentStaff.getRole() + ")") + " |");
        } else if (currentMember != null) {
            System.out.println("|  Member: " + String.format("%-25s", currentMember.getName() + " (" + currentMember.getTier() + ")") + " |");
        } else {
            System.out.println("|  Status: " + String.format("%-30s", "Not Logged In") + " |");
        }
        System.out.println("+------------------------------------------------+");
        
        System.out.println("\n[+] MAIN MENU");
        System.out.println("============");
        System.out.println("1. Member Login");
        System.out.println("2. Staff Login");
        System.out.println("3. Member Management (Member View)");
        System.out.println("4. Points Management (Member View)");
        System.out.println("5. Redemption Management (Member View)");
        System.out.println("6. Notification Management (Member View)");
        System.out.println("7. View Personalized Promotion (Member View)");
        System.out.println("8. Reports (Staff Only)");
        System.out.println("0. Logout & Exit");
        System.out.print("\nEnter choice: ");
        return getIntInput();
    }

    /**
     * Gets member management menu choice with Delete option
     */
    public int getMemberManagementChoice() {
        System.out.println("\n--- MEMBER MANAGEMENT ---");
        System.out.println("1. View My Profile");
        System.out.println("2. Update My Profile");
        System.out.println("3. View Tier Status");
        System.out.println("4. View Tier Benefits");
        System.out.println("5. View Tier Progression");
        System.out.println("6. Delete My Account");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
        return getIntInput();
    }

    /**
     * Gets points management menu choice
     */
    public int getPointsManagementChoice() {
        System.out.println("\n--- POINTS MANAGEMENT ---");
        System.out.println("1. View Points Balance");
        System.out.println("2. View Points History");
        System.out.println("3. Check Points Expiry");
        System.out.println("4. View Tier Progress");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
        return getIntInput();
    }

    /**
     * Gets redemption management menu choice with UNDO options
     */
    public int getRedemptionManagementChoice() {
        System.out.println("\n--- REDEMPTION MANAGEMENT ---");
        System.out.println("1. Process Redemption");
        System.out.println("2. View Redemption History");
        System.out.println("3. Display Redemption Rates");
        System.out.println("4. Check Redemption Eligibility");
        System.out.println("5. Undo Last Redemption");
        System.out.println("6. View Undo Stack");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
        return getIntInput();
    }

    /**
     * Gets notification management menu choice
     */
    public int getNotificationManagementChoice() {
        System.out.println("\n--- NOTIFICATION MANAGEMENT ---");
        System.out.println("1. View My Notifications");
        System.out.println("2. Mark Notification as Read");
        System.out.println("3. Dismiss Notification");
        System.out.println("4. Check Expiring Points Alerts");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
        return getIntInput();
    }

    /**
     * Gets report choice
     */
    public int getReportChoice() {
        System.out.println("\n--- REPORTS (Staff Only) ---");
        System.out.println("1. Member Report");
        System.out.println("2. Transaction Report");
        System.out.println("3. Tier Distribution");
        System.out.println("4. Points Summary");
        System.out.println("5. Redemption Report");
        System.out.println("6. Tier Progression Report");
        System.out.println("0. Back");
        System.out.print("Enter choice: ");
        return getIntInput();
    }

    /**
     * Display undo confirmation with last redemption details
     */
    public boolean confirmUndo(Transaction lastRedemption) {
        System.out.println("\n🔄 UNDO LAST REDEMPTION");
        System.out.println("================================================");
        System.out.println("Last Redemption Details:");
        System.out.println("-----------------------------------------------");
        
        if (lastRedemption == null) {
            System.out.println("No redemption available to undo.");
            return false;
        }
        
        System.out.println("Transaction ID   : " + lastRedemption.getTransactionId());
        System.out.println("Member ID        : " + lastRedemption.getMemberId());
        System.out.println("Points           : " + lastRedemption.getPoints());
        System.out.println("Type             : " + lastRedemption.getType());
        System.out.println("Description      : " + lastRedemption.getDescription());
        System.out.println("Timestamp        : " + lastRedemption.getTimestamp());
        System.out.println("-----------------------------------------------");
        
        System.out.print("\n⚠️ Are you sure you want to undo this redemption? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    /**
     * Display undo result
     */
    public void displayUndoResult(boolean success, Transaction undone, Member member) {
        if (success && undone != null && member != null) {
            System.out.println("\n+================================================+");
            System.out.println("|  SUCCESS! Redemption Undone!                |");
            System.out.println("+================================================+");
            System.out.printf("  Points Restored : +%d pts\n", undone.getPoints());
            System.out.printf("  New Balance     : %d pts\n", member.getPoints());
            System.out.println("  Redemption      : " + undone.getDescription());
            System.out.println("  📧 Notification sent to member.");
            System.out.println("+================================================+");
        } else {
            System.out.println("\n+================================================+");
            System.out.println("|  Cannot undo redemption.                    |");
            System.out.println("+================================================+");
            System.out.println("  Possible reasons:");
            System.out.println("  • No redemptions to undo");
            System.out.println("  • Member not found");
            System.out.println("  • System error");
            System.out.println("+================================================+");
        }
    }

    /**
     * Display undo stack history
     */
    public void displayUndoHistory(String history) {
        System.out.println(history);
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Confirms account deletion with warning
     */
    public boolean confirmAccountDeletion(Member member) {
        System.out.println("\n+================================================+");
        System.out.println("|  [ WARNING ] ACCOUNT DELETION                  |");
        System.out.println("+================================================+");
        System.out.println("|  You are about to permanently delete your      |");
        System.out.println("|  loyalty account. This action CANNOT be undone! |");
        System.out.println("+================================================+");
        System.out.println("\nMember Details:");
        System.out.println("  Member ID  : " + member.getMemberId());
        System.out.println("  Name       : " + member.getName());
        System.out.println("  Tier       : " + member.getTier());
        System.out.println("  Points     : " + member.getPoints());
        System.out.println("  Total Earned: " + member.getTotalPointsEarned());
        System.out.println("\n+================================================+");
        System.out.println("|  WARNING: All points and benefits will be lost |");
        System.out.println("+================================================+");
        
        System.out.print("\nAre you sure you want to delete your account? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (!response.equals("y") && !response.equals("yes")) {
            return false;
        }
        
        // Second confirmation for safety
        System.out.print("\nType 'DELETE' to confirm: ");
        String confirm = scanner.nextLine().trim();
        return confirm.equalsIgnoreCase("DELETE");
    }

    /**
     * Displays account deletion result
     */
    public void displayAccountDeletionResult(boolean success, Member member) {
        if (success) {
            System.out.println("\n+================================================+");
            System.out.println("|  [ SUCCESS ] Account Deleted Successfully!    |");
            System.out.println("+================================================+");
            System.out.println("|  Member: " + member.getName());
            System.out.println("|  Member ID: " + member.getMemberId());
            System.out.println("|  Tier: " + member.getTier());
            System.out.println("|  Points Lost: " + member.getPoints());
            System.out.println("+================================================+");
            System.out.println("\nYour account has been");
            System.out.println("permanently deleted from our system.");
        } else {
            System.out.println("\n+================================================+");
            System.out.println("|  [ ERROR ] Account Deletion Failed!            |");
            System.out.println("+================================================+");
            System.out.println("  Please try again or contact support.");
        }
    }

    /**
     * Displays points balance
     */
    public void displayPointsBalance(Member member) {
        System.out.println("\n[+] POINTS BALANCE");
        System.out.println("=================");
        System.out.println("Member: " + member.getName() + " (" + member.getMemberId() + ")");
        System.out.println("-----------------------------------------------");
        System.out.println("Tier: " + member.getTier());
        System.out.println("Current Points: " + member.getPoints());
        System.out.println("Total Earned: " + member.getTotalPointsEarned());
        System.out.println("Lifetime: " + member.getLifetimePointsEarned());
        System.out.println("Redeemed: " + member.getPointsRedeemed());
        System.out.println("Redemptions: " + member.getTotalRedemptions());
        System.out.println("To Next Tier: " + getPointsToNextTierDisplay(member));
        System.out.println("Points Expiry: " + member.getLastActivityDate().plusMonths(12));
        System.out.println("Days to Expiry: " + getDaysUntilExpiry(member));
    }

    private String getPointsToNextTierDisplay(Member member) {
        int toNext = getPointsToNextTier(member);
        return toNext > 0 ? toNext + " pts" : "MAX TIER [X]";
    }

    private int getPointsToNextTier(Member member) {
        String tier = member.getTier();
        int totalPoints = member.getTotalPointsEarned();
        int nextThreshold = getNextTierThreshold(tier);
        if (nextThreshold == -1) return 0;
        return Math.max(0, nextThreshold - totalPoints);
    }

    private int getNextTierThreshold(String tier) {
        switch (tier) {
            case "Silver": return 1000;
            case "Gold": return 5000;
            case "Platinum": return 15000;
            case "Diamond": return 30000;
            case "Elite": return -1;
            default: return -1;
        }
    }

    private long getDaysUntilExpiry(Member member) {
        java.time.LocalDate expiryDate = member.getLastActivityDate().plusMonths(12);
        return java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), expiryDate);
    }

    /**
     * Displays tier progress
     */
    public void displayTierProgress(Member member) {
        System.out.println("\n[+] TIER PROGRESSION");
        System.out.println("===================");
        System.out.println("Current Tier: " + member.getTier());
        System.out.println("Total Points: " + member.getTotalPointsEarned());
        System.out.println("Progress: " + getTierProgressString(member));
        
        int progress = calculateProgressPercentage(member);
        System.out.println("\nProgress Bar:");
        System.out.print("[");
        for (int i = 0; i < 20; i++) {
            if (i < progress / 5) {
                System.out.print("=");
            } else {
                System.out.print(" ");
            }
        }
        System.out.println("] " + progress + "%");
    }

    private String getTierProgressString(Member member) {
        String tier = member.getTier();
        int totalPoints = member.getTotalPointsEarned();
        int currentThreshold = getCurrentTierThreshold(tier);
        int nextThreshold = getNextTierThreshold(tier);
        
        if (nextThreshold == -1) {
            return "MAX TIER - ELITE [X]";
        }
        
        int progress = totalPoints - currentThreshold;
        int needed = nextThreshold - currentThreshold;
        int percentage = (int) ((double) progress / needed * 100);
        
        return String.format("%s -> %s: %d%% (%d/%d points)", 
                tier, getNextTierName(tier), percentage, progress, needed);
    }

    private int getCurrentTierThreshold(String tier) {
        switch (tier) {
            case "Silver": return 0;
            case "Gold": return 1000;
            case "Platinum": return 5000;
            case "Diamond": return 15000;
            case "Elite": return 30000;
            default: return 0;
        }
    }

    private String getNextTierName(String tier) {
        switch (tier) {
            case "Silver": return "Gold";
            case "Gold": return "Platinum";
            case "Platinum": return "Diamond";
            case "Diamond": return "Elite";
            case "Elite": return "MAX";
            default: return "";
        }
    }

    private int calculateProgressPercentage(Member member) {
        String tier = member.getTier();
        int totalPoints = member.getTotalPointsEarned();
        int currentThreshold = getCurrentTierThreshold(tier);
        int nextThreshold = getNextTierThreshold(tier);
        if (nextThreshold == -1) return 100;
        
        int progress = totalPoints - currentThreshold;
        int needed = nextThreshold - currentThreshold;
        return Math.min(100, (int) ((double) progress / needed * 100));
    }

    /**
     * Displays redemption options
     */
    public void displayRedemptionOptions(Member member) {
        System.out.println("\n[+] REDEMPTION OPTIONS");
        System.out.println("====================");
        System.out.println("Member: " + member.getName());
        System.out.println("Available Points: " + member.getPoints());
        System.out.println("Tier Limit: " + getRedemptionLimit(member.getTier()) + " pts/transaction");
        System.out.println("\nOptions:");
        System.out.println("1. Room Upgrade - 2,000 pts (Value: RM 20)");
        System.out.println("2. Room Discount - 1,000 pts (Value: RM 10)");
        System.out.println("3. Spa Treatment - 3,000 pts (Value: RM 30)");
        System.out.println("4. Gift Voucher - 2,500 pts (Value: RM 25)");
        System.out.println("5. Custom Amount - Enter any amount");
        System.out.print("\nChoose option (1-5): ");
    }

    private int getRedemptionLimit(String tier) {
        switch (tier) {
            case "Silver": return 5000;
            case "Gold": return 7500;
            case "Platinum": return 10000;
            case "Diamond": return 15000;
            case "Elite": return 20000;
            default: return 5000;
        }
    }

    /**
     * Displays redemption rates
     */
    public void displayRedemptionRates() {
        System.out.println("\n[+] REDEMPTION RATES");
        System.out.println("==================");
        System.out.println("100 points = RM 1.00");
        System.out.println("Minimum redemption: 100 points");
        System.out.println("\nTier-based Limits:");
        System.out.println("  [Silver]   5,000 pts/transaction");
        System.out.println("  [Gold]     7,500 pts/transaction");
        System.out.println("  [Platinum] 10,000 pts/transaction");
        System.out.println("  [Diamond]  15,000 pts/transaction");
        System.out.println("  [Elite]    20,000 pts/transaction");
        System.out.println("\nRedemption Types:");
        System.out.println("  [*] Room Upgrade (2,000 pts)");
        System.out.println("  [*] Room Discount (1,000 pts/RM10)");
        System.out.println("  [*] Spa Treatment (3,000 pts)");
        System.out.println("  [*] Gift Voucher (2,500 pts)");
    }

    /**
     * Displays redemption eligibility
     */
    public void displayRedemptionEligibility(Member member) {
        System.out.println("\n[+] REDEMPTION ELIGIBILITY");
        System.out.println("========================");
        System.out.println("Member: " + member.getName());
        System.out.println("Current Points: " + member.getPoints());
        System.out.println("Tier: " + member.getTier());
        System.out.println("Max Redemption: " + getRedemptionLimit(member.getTier()) + " pts");
        System.out.println("Points to Next Tier: " + getPointsToNextTier(member));
        System.out.println("\nEligibility Status:");
        if (member.getPoints() >= 100) {
            System.out.println("[X] Eligible to redeem");
            System.out.println("    Can redeem up to " + 
                    Math.min(member.getPoints(), getRedemptionLimit(member.getTier())) + " pts");
        } else {
            System.out.println("[ ] Not eligible - Need at least 100 points");
            System.out.println("    Earn " + (100 - member.getPoints()) + " more points");
        }
    }

    /**
     * Displays tier benefits
     */
    public void displayTierBenefits() {
        System.out.println("\n[+] TIER BENEFITS");
        System.out.println("================");
        System.out.println("[Silver]");
        System.out.println("  [*] 10 pts/RM spent");
        System.out.println("  [*] Basic room upgrades");
        System.out.println("  [*] Free wifi");
        System.out.println("  [*] Welcome drink");
        System.out.println("\n[Gold]");
        System.out.println("  [*] 12 pts/RM spent (20% bonus)");
        System.out.println("  [*] Priority check-in");
        System.out.println("  [*] Late check-out 2pm");
        System.out.println("  [*] Free wifi (premium)");
        System.out.println("\n[Platinum]");
        System.out.println("  [*] 15 pts/RM spent (50% bonus)");
        System.out.println("  [*] Complimentary room upgrade");
        System.out.println("  [*] Late check-out 4pm");
        System.out.println("  [*] Welcome gift basket");
        System.out.println("\n[Diamond]");
        System.out.println("  [*] 20 pts/RM spent (100% bonus)");
        System.out.println("  [*] Suite upgrades");
        System.out.println("  [*] Complimentary breakfast");
        System.out.println("  [*] Priority service");
        System.out.println("\n[Elite]");
        System.out.println("  [*] 25 pts/RM spent (150% bonus)");
        System.out.println("  [*] Guaranteed suite upgrades");
        System.out.println("  [*] Personal concierge");
        System.out.println("  [*] VIP event invitations");
    }

    /**
     * Displays notifications
     */
    public void displayNotifications(String notifications, Member member) {
        System.out.println("\n[+] NOTIFICATIONS");
        System.out.println("================");
        System.out.println("Member: " + member.getName() + " (" + member.getMemberId() + ")");
        System.out.println(notifications);
    }

    /**
     * Displays all notifications
     */
    public void displayAllNotifications(String notifications) {
        System.out.println("\n[+] ALL NOTIFICATIONS");
        System.out.println("====================");
        System.out.println(notifications);
    }

    /**
     * Displays personalized promotion
     */
    public void displayPersonalizedPromotion(String promotion) {
        System.out.println("\n" + promotion);
    }

    /**
     * Displays report
     */
    public void displayReport(String report) {
        System.out.println("\n" + report);
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Displays access denied message
     */
    public void displayAccessDeniedMessage() {
        System.out.println("\n+================================================+");
        System.out.println("|  [ ACCESS DENIED ]                              |");
        System.out.println("|  Staff authentication required to view reports. |");
        System.out.println("|  Please login using option 3.                  |");
        System.out.println("+================================================+");
    }

    /**
     * Displays member required message
     */
    public void displayMemberRequiredMessage() {
        System.out.println("\n+================================================+");
        System.out.println("|  [ ACCESS DENIED ]                              |");
        System.out.println("|  Member login required to access this feature. |");
        System.out.println("|  Please login using option 2.                  |");
        System.out.println("+================================================+");
    }

    /**
     * Displays logout message
     */
    public void displayLogoutMessage() {
        System.out.println("\n+================================================+");
        System.out.println("|  [ SUCCESS ] Logged out successfully!          |");
        System.out.println("+================================================+");
    }

    /**
     * Displays exit message
     */
    public void displayExitMessage() {
        System.out.println("\n+================================================+");
        System.out.println("|  [ SUCCESS ] Thank you for using               |");
        System.out.println("|  TARUMT Resorts Loyalty Program!               |");
        System.out.println("|                                                |");
        System.out.println("|  Your rewards make every stay more valuable!   |");
        System.out.println("+================================================+");
        System.out.println("\nReturning to System Master Menu...");
    }

    /**
     * Displays member details
     */
    public void displayMemberDetails(Member member) {
        System.out.println(member.toDetailedString());
    }

    /**
     * Input methods
     */
    public double inputAmountSpent() {
        System.out.print("Enter amount spent (RM): ");
        return getDoubleInput();
    }

    public int inputPointsToRedeem() {
        System.out.print("Enter points to redeem: ");
        return getIntInput();
    }

    public String inputMemberId(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String inputSearchTerm() {
        System.out.print("Enter search term (ID/Name/Email): ");
        return scanner.nextLine().trim();
    }

    public String inputString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String inputRedemptionType() {
        System.out.print("Enter redemption type (Room Upgrade/Discount/Spa/Gift): ");
        return scanner.nextLine().trim();
    }

    public boolean confirmAction(String prompt) {
        System.out.print(prompt + " (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    public int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public double getDoubleInput() {
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayInvalidChoiceMessage() {
        System.out.println("[X] Invalid choice! Please try again.");
    }

    private void clearScreen() {
        for (int i = 0; i < 2; i++) {
            System.out.println();
        }
    }
}
