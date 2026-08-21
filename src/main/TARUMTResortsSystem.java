package main;

import boundary.HouseKeepingUI;
import boundary.WalkInRegistrationUI;
import control.BookingControl;
import control.FrontDeskControl;
import control.HouseKeepingControl;
import control.LoyaltyControl;

import java.util.Scanner;

/**
 * Main Entry Point for TAR UMT Resorts Management System.
 * Package: main
 * Class: TARUMTResortsSystem
 */
public class TARUMTResortsSystem {

    private final Scanner scanner;

    // Module UI instances
    private final WalkInRegistrationUI walkInUI;
    private final FrontDeskControl frontDeskControl;
    private final HouseKeepingUI housekeepingUI;
    private final LoyaltyControl loyaltyControl;

    public TARUMTResortsSystem() {
        this.scanner = new Scanner(System.in);

        // Initialize all controllers with SHARED cross-module linkage:
        // FrontDesk <-> BookingControl <-> HouseKeepingControl
        this.frontDeskControl = new FrontDeskControl();
        HouseKeepingControl houseKeepingControl = new HouseKeepingControl();
        BookingControl bookingControl = new BookingControl(frontDeskControl, houseKeepingControl);

        // Bi-directional link FrontDesk <-> Housekeeping
        frontDeskControl.setHouseKeepingControl(houseKeepingControl);

        this.walkInUI = new WalkInRegistrationUI(bookingControl);
        this.housekeepingUI = new HouseKeepingUI(houseKeepingControl);
        this.loyaltyControl = new LoyaltyControl();
        bookingControl.setLoyaltyControl(this.loyaltyControl);
        frontDeskControl.setLoyaltyControl(this.loyaltyControl);
    }

    public static void main(String[] args) {
        TARUMTResortsSystem system = new TARUMTResortsSystem();
        system.runMasterMenu();
    }

    public void runMasterMenu() {
        int choice;

        do {
            clearConsole();
            System.out.println("+==========================================================+");
            System.out.println("|              TAR UMT RESORTS MANAGEMENT                  |");
            System.out.println("|                  SYSTEM MASTER MENU                      |");
            System.out.println("+==========================================================+");
            System.out.println("|  [1] Walk-In Registration & Queue Management             |");
            System.out.println("|  [2] Front Desk Service                                  |");
            System.out.println("|  [3] Housekeeping Task Log                               |");
            System.out.println("|  [4] Loyalty & Rewards System                            |");
            System.out.println("|  [0] Exit System                                         |");
            System.out.println("+==========================================================+");

            choice = readIntInput("Select Module (0-4): ", 0, 4);

            switch (choice) {
                case 1 -> // Hand control over to Walk-In Registration module
                    walkInUI.displayMenu();

                case 2 -> {
                    frontDeskControl.runFrontDeskService(); 
                }

                case 3 -> {
                    housekeepingUI.displayMenu();
                }

                case 4 -> {
                    loyaltyControl.runService();
                }

                case 0 -> {
                    System.out.println("\n==========================================================");
                    System.out.println("  Thank you for using TAR UMT Resorts System. Goodbye!");
                    System.out.println("==========================================================");
                }
            }

        } while (choice != 0);
    }

    // =========================================================================
    // INPUT & CONSOLE HELPER METHODS
    // =========================================================================

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

    private void pressEnterToContinue() {
        System.out.print("\nPress [ENTER] key to return to Main Menu...");
        scanner.nextLine();
    }

    private void clearConsole() {
        for (int i = 0; i < 2; i++) {
            System.out.println();
        }
    }
}
