package control;

import adt.ListInterface;
import adt.LinkedList;
import entity.Member;
import utility.ValidationUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MemberService {
    private ListInterface<Member> memberList;

    public MemberService(ListInterface<Member> memberList) {
        this.memberList = memberList;
    }

    //add member
    public boolean addMember(Member member) {
        if (!ValidationUtils.isValidMemberId(member.getMemberId())) return false;
        if (!ValidationUtils.isValidEmail(member.getEmail())) return false;
        if (memberList.contains(member)) return false;
        memberList.add(member);
        return true;
    }

    //find a member by id
    public Member findMemberById(String memberId) {
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            if (m.getMemberId().equals(memberId)) return m;
        }
        return null;
    }

    //get all member by formatted string
    public String getAllMembers() {
        if (memberList.isEmpty()) return "No members registered.";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s %-20s %-10s %10s %10s %12s\n", 
                "ID", "Name", "Tier", "Points", "Earned", "To Next"));
        sb.append("------------------------------------------------------------\n");
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            int toNext = getPointsToNextTier(m);
            sb.append(String.format("%-10s %-20s %-10s %10d %10d %12d\n",
                    m.getMemberId(),
                    m.getName().length() > 20 ? m.getName().substring(0, 17) + "..." : m.getName(),
                    m.getTier(),
                    m.getPoints(),
                    m.getTotalPointsEarned(),
                    toNext));
        }
        return sb.toString();
    }

    //search member by term
    public String searchMembers(String searchTerm) {
        ListInterface<Member> results = new LinkedList<>();
        String lowerTerm = searchTerm.toLowerCase();
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            if (m.getMemberId().toLowerCase().contains(lowerTerm) ||
                m.getName().toLowerCase().contains(lowerTerm) ||
                m.getEmail().toLowerCase().contains(lowerTerm)) {
                results.add(m);
            }
        }
        if (results.isEmpty()) return "No members found matching: " + searchTerm;
        StringBuilder sb = new StringBuilder();
        sb.append("Search Results for '").append(searchTerm).append("':\n");
        sb.append(String.format("%-10s %-20s %-10s %10s %10s\n", "ID", "Name", "Tier", "Points", "Earned"));
        sb.append("------------------------------------------------------------\n");
        for (int i = 1; i <= results.getNumberOfEntries(); i++) {
            sb.append(results.getEntry(i)).append("\n");
        }
        return sb.toString();
    }

    //delete member by id
    public boolean deleteMember(String memberId) {
        for (int i = 1; i <= memberList.getNumberOfEntries(); i++) {
            Member m = memberList.getEntry(i);
            if (m.getMemberId().equals(memberId)) {
                memberList.remove(i);
                return true;
            }
        }
        return false;
    }

    //personalized promotion by member
    public String getPersonalizedPromotion(Member member) {
        StringBuilder promo = new StringBuilder();
        promo.append("+==================================================+\n");
        promo.append("|          PERSONALIZED PROMOTION                  |\n");
        promo.append("|        For: ").append(member.getName()).append("\n");
        promo.append("+==================================================+\n");
        String tier = member.getTier();
        switch (tier) {
            case "Silver": promo.append("[SILVER] Earn 50 bonus points on next booking!\n"); break;
            case "Gold": promo.append("[GOLD] Double points on weekend stays!\n"); break;
            case "Platinum": promo.append("[PLATINUM] Free upgrade + 500 bonus points!\n"); break;
            case "Diamond": promo.append("[DIAMOND] Complimentary spa + 1000 points!\n"); break;
            case "Elite": promo.append("[ELITE] VIP package - All-Inclusive!\n"); break;
        }
        if (member.getPoints() > 10000) {
            promo.append("[*] Use ").append(member.getPoints()).append(" points for extra savings!\n");
        }
        LocalDate joinDate = member.getJoinDate();
        long years = ChronoUnit.YEARS.between(joinDate, LocalDate.now());
        if (years >= 1) {
            promo.append("[*] ").append(years).append(" year anniversary! Special rewards!\n");
        }
        LocalDate dob = member.getDateOfBirth();
        if (dob != null) {
            LocalDate today = LocalDate.now();
            if (today.getMonth() == dob.getMonth() && today.getDayOfMonth() == dob.getDayOfMonth()) {
                promo.append("[*] 🎂 HAPPY BIRTHDAY! 200 bonus points!\n");
            }
        }
        promo.append("[*] Refer a friend: Earn 500 bonus points!");
        return promo.toString();
    }

    private int getPointsToNextTier(Member member) {
        int totalPoints = member.getTotalPointsEarned();
        String tier = member.getTier();
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
}
