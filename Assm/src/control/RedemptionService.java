package control;

import adt.ListInterface;
import adt.LinkedList;
import adt.QueueInterface;
import adt.StackInterface;
import adt.LinkedQueue;
import adt.LinkedStack;
import entity.Member;
import entity.Transaction;
import entity.Notification;

/**
 * RedemptionService - Handles points redemption with undo capability
 * Uses Stack ADT (LIFO) for undo functionality
 * 
 * @author TARUMT Resorts Team
 */
public class RedemptionService {
    private ListInterface<Member> memberList;
    private QueueInterface<Transaction> transactionQueue;
    private ListInterface<Notification> notificationList;
    private StackInterface<Transaction> redemptionStack;
    private static final int POINTS_PER_RM = 100;

    /**
     * Constructor with all required data structures
     */
    public RedemptionService(ListInterface<Member> memberList,
                              QueueInterface<Transaction> transactionQueue,
                              ListInterface<Notification> notificationList,
                              StackInterface<Transaction> redemptionStack) {
        this.memberList = memberList;
        this.transactionQueue = transactionQueue;
        this.notificationList = notificationList;
        this.redemptionStack = redemptionStack;
        
        // Initialize stack if null
        if (this.redemptionStack == null) {
            this.redemptionStack = new LinkedStack<>();
        }
    }

    /**
     *Process a redemption and store it in the stack for undo
     */
    public double processRedemption(Member member, int pointsToRedeem, String redemptionType) {
        // Validation
        if (pointsToRedeem <= 0 || pointsToRedeem > member.getPoints()) {
            System.out.println("[ERROR] Invalid points amount.");
            return 0;
        }
        
        if (!isValidRedemption(member, pointsToRedeem)) {
            System.out.println("[ERROR] Redemption exceeds tier limit.");
            return 0;
        }
        
        // Calculate discount (100 points = RM 1.00)
        double discount = (double) pointsToRedeem / POINTS_PER_RM;
        
        // Update member
        member.setPoints(member.getPoints() - pointsToRedeem);
        member.setTotalRedemptions(member.getTotalRedemptions() + 1);
        member.setPointsRedeemed(member.getPointsRedeemed() + pointsToRedeem);
        
        // Create transaction
        String description = "Redeemed " + pointsToRedeem + " points for " + redemptionType;
        Transaction txn = new Transaction(member.getMemberId(),
            Transaction.TransactionType.REDEEM_POINTS, 
            pointsToRedeem,
            description);
        
        // Store redemption type in reference number (existing field)
        txn.setReferenceNumber(redemptionType);
        
        // Add to transaction queue
        transactionQueue.enqueue(txn);
        
        redemptionStack.push(txn);
        System.out.println("[System] Redemption stored in stack for undo. Stack size: " + redemptionStack.size());
        
        // Send notification
        Notification notif = new Notification(member.getMemberId(),
            Notification.NotificationType.REDEMPTION_CONFIRMATION,
            "Redemption Confirmed!",
            "Redeemed " + pointsToRedeem + " points for " + redemptionType + 
            ". Savings: RM " + String.format("%.2f", discount));
        notificationList.add(notif);
        
        return discount;
    }

    public boolean undoRedemption() {
        // Check if there's anything to undo
        if (redemptionStack.isEmpty()) {
            System.out.println("\n[INFO] No redemptions to undo.");
            return false;
        }
        
        // pop the last redemption from stack
        Transaction last = redemptionStack.pop();
        System.out.println("\n[System] Retrieved last redemption from stack. Stack size now: " + redemptionStack.size());
        
        // Find the member
        Member member = findMember(last.getMemberId());
        if (member == null) {
            System.out.println("[ERROR] Member not found. Cannot undo.");
            // Push back to stack if member not found
            redemptionStack.push(last);
            return false;
        }
        
        // Reverse the transaction
        int restoredPoints = last.getPoints();
        member.setPoints(member.getPoints() + restoredPoints);
        member.setTotalRedemptions(Math.max(0, member.getTotalRedemptions() - 1));
        member.setPointsRedeemed(Math.max(0, member.getPointsRedeemed() - restoredPoints));
        
        // Create reversal transaction record
        Transaction reversal = new Transaction(member.getMemberId(),
            Transaction.TransactionType.ADJUSTMENT, 
            restoredPoints,
            "UNDO: " + last.getDescription());
        transactionQueue.enqueue(reversal);
        
        // Send notification about undo
        Notification notif = new Notification(member.getMemberId(),
            Notification.NotificationType.POINTS_EARNED,
            "Redemption Reversed!",
            "The redemption of " + restoredPoints + 
            " points has been undone.\nPoints restored: " + member.getPoints());
        notificationList.add(notif);
        
        return true;
    }

    public Transaction getLastRedemption() {
        if (redemptionStack.isEmpty()) {
            return null;
        }
        return redemptionStack.peek();
    }

 
    public int getRedemptionStackSize() {
        return redemptionStack.size();
    }

  
    private String extractRedemptionType(Transaction t) {
        if (t == null || t.getDescription() == null) return "Unknown";
        
        // Method 1: Extract from description: "Redeemed X points for Y"
        String desc = t.getDescription();
        if (desc.contains(" for ")) {
            String[] parts = desc.split(" for ");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        
        // Method 2: Use reference number (backup)
        if (t.getReferenceNumber() != null && !t.getReferenceNumber().isEmpty()) {
            return t.getReferenceNumber();
        }
        
        return "General";
    }

    /**
     * Get undo history (display stack contents from top to bottom)
     */
    public String getUndoHistory() {
        if (redemptionStack.isEmpty()) {
            return "No redemptions available for undo.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n📊 REDEMPTION STACK (Top to Bottom - LIFO Order)\n");
        sb.append("==================================================\n");
        
        // Create temporary stack to view contents
        StackInterface<Transaction> tempStack = new LinkedStack<>();
        int position = 1;
        
        while (!redemptionStack.isEmpty()) {
            Transaction t = redemptionStack.pop();
            String type = extractRedemptionType(t);
            sb.append(String.format("#%d: %-6d pts - %-15s (%s)\n", 
                position, t.getPoints(), type, t.getTimestamp().toLocalDate()));
            tempStack.push(t);
            position++;
        }
        
        // Restore original stack
        while (!tempStack.isEmpty()) {
            redemptionStack.push(tempStack.pop());
        }
        
        return sb.toString();
    }

    /**
     * Validate redemption against tier limits
     */
    private boolean isValidRedemption(Member member, int points) {
        if (points < 100) {
            System.out.println("[ERROR] Minimum redemption is 100 points.");
            return false;
        }
        
        String tier = member.getTier();
        int limit = getRedemptionLimit(tier);
        
        if (points > limit) {
            System.out.println("[ERROR] " + tier + " tier limit is " + limit + " pts/transaction.");
            return false;
        }
        
        return true;
    }

    /**
     * Get redemption limit based on tier
     */
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
     * Find member by ID
     */
    private Member findMember(String memberId) {
        if (memberList == null) return null;
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            if (m.getMemberId().equals(memberId)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Get redemption history for a specific member
     */
    public String getRedemptionHistory(String memberId) {
        ListInterface<Transaction> redemptions = new LinkedList<>();
        QueueInterface<Transaction> temp = new LinkedQueue<>();
        
        // Traverse queue without modifying it
        while (!transactionQueue.isEmpty()) {
            Transaction t = transactionQueue.dequeue();
            temp.enqueue(t);
            if (t.getMemberId().equals(memberId) && 
                t.getType() == Transaction.TransactionType.REDEEM_POINTS) {
                redemptions.add(t);
            }
        }
        
        // Restore queue
        while (!temp.isEmpty()) {
            transactionQueue.enqueue(temp.dequeue());
        }
        
        if (redemptions.isEmpty()) {
            return "No redemption history found for this member.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n📋 REDEMPTION HISTORY\n");
        sb.append("==================================================\n");
        sb.append(String.format("%-12s %-18s %10s %10s\n", 
            "Date", "Type", "Points", "Savings (RM)"));
        sb.append("--------------------------------------------------\n");
        
        for (int i = 1; i <= redemptions.getNumberOfEntries(); i++) {
            Transaction r = redemptions.getEntry(i);
            double savings = (double) r.getPoints() / POINTS_PER_RM;
            String type = extractRedemptionType(r);
            sb.append(String.format("%-12s %-18s %,10d %10.2f\n",
                    r.getTimestamp().toLocalDate().toString(),
                    type.length() > 18 ? type.substring(0, 15) + "..." : type,
                    r.getPoints(),
                    savings));
        }
        
        return sb.toString();
    }
}