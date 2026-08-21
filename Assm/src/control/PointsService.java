package control;

import adt.ListInterface;
import adt.LinkedList;
import adt.QueueInterface;
import adt.LinkedQueue;
import entity.Member;
import entity.Transaction;
import entity.Notification;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PointsService {
    private ListInterface<Member> memberList;
    private QueueInterface<Transaction> transactionQueue;
    private ListInterface<Notification> notificationList;
    private static final int BASE_POINTS_PER_RM = 10;

    public PointsService(ListInterface<Member> memberList, 
                         QueueInterface<Transaction> transactionQueue,
                         ListInterface<Notification> notificationList) {
        this.memberList = memberList;
        this.transactionQueue = transactionQueue;
        this.notificationList = notificationList;
    }

    
      // Earns points for a member based on amount spent
    public int earnPoints(Member member, double amountSpent, String bookingId) {
        if (amountSpent <= 0) return 0;
        
        int basePoints = (int) (amountSpent * BASE_POINTS_PER_RM);
        double multiplier = getTierMultiplier(member.getTier());
        int pointsEarned = (int) (basePoints * multiplier);
        pointsEarned += calculateBonusPoints(member);
        
        String oldTier = member.getTier();
        member.setPoints(member.getPoints() + pointsEarned);
        member.setTotalPointsEarned(member.getTotalPointsEarned() + pointsEarned);
        member.setLifetimePointsEarned(member.getLifetimePointsEarned() + pointsEarned);
        member.setLastActivityDate(LocalDate.now());
        
        String newTier = calculateTier(member.getTotalPointsEarned());
        member.setTier(newTier);
        
        Transaction txn = new Transaction(member.getMemberId(), 
            Transaction.TransactionType.EARN_POINTS, pointsEarned,
            String.format("Earned from RM %.2f spending", amountSpent));
        transactionQueue.enqueue(txn);
        
        Notification notif = new Notification(member.getMemberId(),
            Notification.NotificationType.POINTS_EARNED,
            pointsEarned + " Points Earned!",
            "You earned " + pointsEarned + " points. Balance: " + member.getPoints());
        notificationList.add(notif);
        
        if (!oldTier.equals(newTier)) {
            Notification upgradeNotif = new Notification(member.getMemberId(),
                Notification.NotificationType.TIER_UPGRADE,
                "Tier Upgrade! " + newTier + " Level Achieved!",
                "Congratulations! You've been upgraded to " + newTier + " tier!");
            notificationList.add(upgradeNotif);
        }
        
        return pointsEarned;
    }

    private double getTierMultiplier(String tier) {
        switch (tier) {
            case "Silver": return 1.0;
            case "Gold": return 1.2;
            case "Platinum": return 1.5;
            case "Diamond": return 2.0;
            case "Elite": return 2.5;
            default: return 1.0;
        }
    }

    private int calculateBonusPoints(Member member) {
        int bonus = 0;
        if (member.getDateOfBirth() != null) {
            LocalDate today = LocalDate.now();
            LocalDate dob = member.getDateOfBirth();
            if (today.getMonth() == dob.getMonth() && today.getDayOfMonth() == dob.getDayOfMonth()) {
                bonus += 200;
            }
        }
        long years = ChronoUnit.YEARS.between(member.getJoinDate(), LocalDate.now());
        if (years >= 1) {
            bonus += (int) years * 100;
        }
        return bonus;
    }

    private String calculateTier(int totalPoints) {
        if (totalPoints >= 30000) return "Elite";
        else if (totalPoints >= 15000) return "Diamond";
        else if (totalPoints >= 5000) return "Platinum";
        else if (totalPoints >= 1000) return "Gold";
        else return "Silver";
    }

   
     // Gets points history for a member
    public String getPointsHistory(String memberId) {
        ListInterface<Transaction> memberTxns = new LinkedList<>();
        QueueInterface<Transaction> temp = new LinkedQueue<>();
        while (!transactionQueue.isEmpty()) {
            Transaction t = transactionQueue.dequeue();
            temp.enqueue(t);
            if (t.getMemberId().equals(memberId)) memberTxns.add(t);
        }
        while (!temp.isEmpty()) transactionQueue.enqueue(temp.dequeue());
        
        if (memberTxns.isEmpty()) return "No transaction history.";
        StringBuilder sb = new StringBuilder();
        sb.append("Points History\n=================================\n");
        sb.append(String.format("%-12s %-15s %10s %-40s\n", "Date", "Type", "Points", "Description"));
        sb.append("------------------------------------------------------------\n");
        for (int i = 1; i <= memberTxns.getNumberOfEntries(); i++) {
            Transaction t = memberTxns.getEntry(i);
            sb.append(String.format("%-12s %-15s %+10d %-40s\n",
                    t.getTimestamp().toLocalDate().toString(),
                    t.getType().toString().replace("_", " "),
                    t.getPoints(),
                    t.getDescription().length() > 40 ? t.getDescription().substring(0, 37) + "..." : t.getDescription()));
        }
        return sb.toString();
    }

   
     // Checks for points expiry and creates notifications
    public int checkPointsExpiry() {
        int count = 0;
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            LocalDate expiryDate = m.getLastActivityDate().plusMonths(12);
            long days = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
            if (days <= 30 && days > 0 && m.getPoints() > 0) {
                count++;
                Notification notif = new Notification(m.getMemberId(),
                    Notification.NotificationType.POINTS_EXPIRY,
                    "Points Expiring in " + days + " Days!",
                    "Your " + m.getPoints() + " points will expire on " + expiryDate);
                notificationList.add(notif);
            }
        }
        return count;
    }
}