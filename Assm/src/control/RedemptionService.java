package control;

import adt.ListInterface;
import adt.LinkedList;
import adt.QueueInterface;
import adt.StackInterface;
import adt.LinkedQueue;
import entity.Member;
import entity.Transaction;
import entity.Notification;

public class RedemptionService {
    private ListInterface<Member> memberList;
    private QueueInterface<Transaction> transactionQueue;
    private ListInterface<Notification> notificationList;
    private StackInterface<Transaction> redemptionStack;
    private static final int POINTS_PER_RM = 100;

    public RedemptionService(ListInterface<Member> memberList,
                              QueueInterface<Transaction> transactionQueue,
                              ListInterface<Notification> notificationList,
                              StackInterface<Transaction> redemptionStack) {
        this.memberList = memberList;
        this.transactionQueue = transactionQueue;
        this.notificationList = notificationList;
        this.redemptionStack = redemptionStack;
    }

    public double processRedemption(Member member, int pointsToRedeem, String redemptionType) {
        if (pointsToRedeem <= 0 || pointsToRedeem > member.getPoints()) return 0;
        if (!isValidRedemption(member, pointsToRedeem)) return 0;
        
        double discount = (double) pointsToRedeem / POINTS_PER_RM;
        member.setPoints(member.getPoints() - pointsToRedeem);
        member.setTotalRedemptions(member.getTotalRedemptions() + 1);
        member.setPointsRedeemed(member.getPointsRedeemed() + pointsToRedeem);
        
        Transaction txn = new Transaction(member.getMemberId(),
            Transaction.TransactionType.REDEEM_POINTS, pointsToRedeem,
            "Redeemed " + pointsToRedeem + " points for " + redemptionType);
        transactionQueue.enqueue(txn);
        redemptionStack.push(txn);
        
        Notification notif = new Notification(member.getMemberId(),
            Notification.NotificationType.REDEMPTION_CONFIRMATION,
            "Redemption Confirmed!",
            "Redeemed " + pointsToRedeem + " points for " + redemptionType + 
            ". Savings: RM " + String.format("%.2f", discount));
        notificationList.add(notif);
        
        return discount;
    }

    private boolean isValidRedemption(Member member, int points) {
        if (points < 100) return false;
        if (points > 10000) return false;
        String tier = member.getTier();
        switch (tier) {
            case "Silver": return points <= 5000;
            case "Gold": return points <= 7500;
            case "Platinum": return points <= 10000;
            case "Diamond": return points <= 15000;
            case "Elite": return points <= 20000;
            default: return false;
        }
    }

    public boolean undoRedemption() {
        if (redemptionStack.isEmpty()) return false;
        Transaction last = redemptionStack.pop();
        Member member = findMember(last.getMemberId());
        if (member == null) return false;
        member.setPoints(member.getPoints() + last.getPoints());
        member.setTotalRedemptions(Math.max(0, member.getTotalRedemptions() - 1));
        member.setPointsRedeemed(Math.max(0, member.getPointsRedeemed() - last.getPoints()));
        return true;
    }

    private Member findMember(String memberId) {
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            if (m.getMemberId().equals(memberId)) return m;
        }
        return null;
    }

    public String getRedemptionHistory(String memberId) {
        ListInterface<Transaction> redemptions = new LinkedList<>();
        QueueInterface<Transaction> temp = new LinkedQueue<>();
        while (!transactionQueue.isEmpty()) {
            Transaction t = transactionQueue.dequeue();
            temp.enqueue(t);
            if (t.getMemberId().equals(memberId) && 
                t.getType() == Transaction.TransactionType.REDEEM_POINTS) {
                redemptions.add(t);
            }
        }
        while (!temp.isEmpty()) transactionQueue.enqueue(temp.dequeue());
        if (redemptions.isEmpty()) return "No redemption history.";
        StringBuilder sb = new StringBuilder();
        sb.append("Redemption History\n=================================\n");
        sb.append(String.format("%-12s %-15s %10s %-20s %10s\n", "Date", "Type", "Points", "Reference", "Savings"));
        sb.append("------------------------------------------------------------\n");
        for (int i = 1; i <= redemptions.getNumberOfEntries(); i++) {
            Transaction r = redemptions.getEntry(i);
            double savings = (double) r.getPoints() / POINTS_PER_RM;
            sb.append(String.format("%-12s %-15s %,10d %-20s RM%8.2f\n",
                    r.getTimestamp().toLocalDate().toString(),
                    r.getRedemptionType() != null ? r.getRedemptionType() : "General",
                    r.getPoints(),
                    r.getReferenceNumber() != null ? r.getReferenceNumber() : "N/A",
                    savings));
        }
        return sb.toString();
    }
}