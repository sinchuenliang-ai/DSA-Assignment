package utility;

/**
 * Standard utility class for displaying console UI messages.
 */
public class MessageUI {

    public static void displayInvalidChoiceMessage() {
        System.out.println("\nInvalid choice");
    }

    public static void displayExitMessage() {
        System.out.println("\nExiting system");
    }

    public static void displayNotFoundMessage() {
        System.out.println("\nReservation not found.");
    }

    public static void displayDuplicateConfirmationMessage() {
        System.out.println("\nError: Confirmation number already exists.");
    }

    public static void displayAddedMessage() {
        System.out.println("\nReservation added successfully.");
    }

    public static void displayUpdatedMessage() {
        System.out.println("\nReservation updated successfully.");
    }
}
