package control;

import adt.ListInterface;
import adt.QueueInterface;
import adt.LinkedList;
import adt.LinkedQueue;
import entity.Member;
import entity.Transaction;

import java.time.LocalDateTime;

public class ReportService {
    private ListInterface<Member> memberList;
    private QueueInterface<Transaction> transactionQueue;

    public ReportService(ListInterface<Member> memberList, 
                         QueueInterface<Transaction> transactionQueue) {
        this.memberList = memberList;
        this.transactionQueue = transactionQueue;
    }

  
     //Generates a member report
    public String generateMemberReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("+==================================================+\n");
        sb.append("|        TARUMT RESORTS - MEMBER REPORT          |\n");
        sb.append("+==================================================+\n");
        sb.append("Generated: ").append(LocalDateTime.now()).append("\n");
        sb.append("Total Members: ").append(memberList.getNumberOfEntries()).append("\n\n");
        sb.append(String.format("%-10s %-20s %-10s %10s %10s\n", "ID", "Name", "Tier", "Points", "Earned"));
        sb.append("------------------------------------------------------------\n");
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            sb.append(memberList.getEntry(i)).append("\n");
        }
        int totalPoints = 0;
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            totalPoints += memberList.getEntry(i).getPoints();
        }
        sb.append("\n[*] POINTS SUMMARY\n");
        sb.append("Total Active Points: ").append(totalPoints).append("\n");
        return sb.toString();
    }

     //Generates a transaction report
    public String generateTransactionReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("+==================================================+\n");
        sb.append("|       TARUMT RESORTS - TRANSACTION REPORT      |\n");
        sb.append("+==================================================+\n");
        sb.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        ListInterface<Transaction> allTxns = new LinkedList<>();
        QueueInterface<Transaction> temp = new LinkedQueue<>();
        while (!transactionQueue.isEmpty()) {
            Transaction t = transactionQueue.dequeue();
            temp.enqueue(t);
            allTxns.add(t);
        }
        while (!temp.isEmpty()) transactionQueue.enqueue(temp.dequeue());
        sb.append("Total Transactions: ").append(allTxns.getNumberOfEntries()).append("\n\n");
        for (int i = 1; i <= allTxns.getNumberOfEntries(); i++) {
            sb.append(allTxns.getEntry(i)).append("\n");
        }
        return sb.toString();
    }

     //Generates a tier distribution report
    public String generateTierReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("+==================================================+\n");
        sb.append("|       TARUMT RESORTS - TIER DISTRIBUTION       |\n");
        sb.append("+==================================================+\n");
        sb.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        int silver = 0, gold = 0, platinum = 0, diamond = 0, elite = 0;
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            switch (m.getTier()) {
                case "Silver": silver++; break;
                case "Gold": gold++; break;
                case "Platinum": platinum++; break;
                case "Diamond": diamond++; break;
                case "Elite": elite++; break;
            }
        }
        sb.append("Silver: ").append(silver).append("\n");
        sb.append("Gold: ").append(gold).append("\n");
        sb.append("Platinum: ").append(platinum).append("\n");
        sb.append("Diamond: ").append(diamond).append("\n");
        sb.append("Elite: ").append(elite).append("\n");
        return sb.toString();
    }

     // Generates a points summary report
    public String generatePointsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("+==================================================+\n");
        sb.append("|        TARUMT RESORTS - POINTS SUMMARY         |\n");
        sb.append("+==================================================+\n");
        sb.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        int totalPoints = 0;
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            totalPoints += memberList.getEntry(i).getPoints();
        }
        sb.append("Total Active Points: ").append(totalPoints).append("\n");
        sb.append("Average Points/Member: ").append(
            memberList.getNumberOfEntries() > 0 ? 
            totalPoints / memberList.getNumberOfEntries() : 0).append("\n");
        return sb.toString();
    }

      //Generates a redemption report
    public String generateRedemptionReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("+==================================================+\n");
        sb.append("|       TARUMT RESORTS - REDEMPTION REPORT       |\n");
        sb.append("+==================================================+\n");
        sb.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        ListInterface<Transaction> redemptions = new LinkedList<>();
        QueueInterface<Transaction> temp = new LinkedQueue<>();
        while (!transactionQueue.isEmpty()) {
            Transaction t = transactionQueue.dequeue();
            temp.enqueue(t);
            if (t.getType() == Transaction.TransactionType.REDEEM_POINTS) {
                redemptions.add(t);
            }
        }
        while (!temp.isEmpty()) transactionQueue.enqueue(temp.dequeue());
        if (redemptions.isEmpty()) return "No redemptions recorded.";
        sb.append("Redemptions: ").append(redemptions.getNumberOfEntries()).append("\n");
        return sb.toString();
    }

   
  //Generates a tier progression report
    public String generateTierProgressionReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("+==================================================+\n");
        sb.append("|    TARUMT RESORTS - TIER PROGRESSION REPORT    |\n");
        sb.append("+==================================================+\n");
        sb.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        sb.append(String.format("%-10s %-20s %-10s %10s\n", "ID", "Name", "Tier", "Points"));
        sb.append("------------------------------------------------------------\n");
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            sb.append(String.format("%-10s %-20s %-10s %10d\n",
                    m.getMemberId(), m.getName(), m.getTier(), m.getPoints()));
        }
        return sb.toString();
    }
}