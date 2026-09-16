import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final ActivityManager activityManager = new ActivityManager();
    private static final ImpactCalculator calculator = new ImpactCalculator();
    private static final ReportManager reportManager = new ReportManager();
    private static final int activeUserId = 1;

    // ANSI Color Palettes
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";
    private static final String GREEN = "\u001B[38;5;48m";
    private static final String EMERALD = "\u001B[38;5;42m";
    private static final String CYAN = "\u001B[38;5;51m";
    private static final String YELLOW = "\u001B[38;5;220m";
    private static final String RED = "\u001B[38;5;196m";
    private static final String GRAY = "\u001B[38;5;244m";

    public static void main(String[] args) {
        clearScreen();
        displayBanner();

        printStatus("System", "Loading benchmark factors from dataset...");
        try {
            calculator.loadEmissionFactors("data/emission_factors.csv");
            printSuccess("Verified " + calculator.getRegistry().size() + " environmental baseline factors.");
        } catch (IOException e) {
            printError("Failed reading reference dataset at data/emission_factors.csv");
            return;
        }

        printStatus("Database", "Testing MySQL connection parameters...");
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        if (databaseManager.testConnection()) {
            printSuccess("Relational link operational on port 3306.");
        } else {
            printError("MySQL connection failed with the configured credentials.");
            promptForDatabaseCredentials(databaseManager);
        }

        pausePrompt();

        boolean running = true;
        while (running) {
            clearScreen();
            displayHeader();
            displayMenu();

            System.out.print(CYAN + BOLD + " terra@gauge" + RESET + GRAY + " :> " + RESET);
            String choice = SCANNER.nextLine().trim();

            switch (choice) {
                case "1":
                    recordActivityView();
                    break;
                case "2":
                    viewAllActivitiesView();
                    break;
                case "3":
                    modifyActivityView();
                    break;
                case "4":
                    removeActivityView();
                    break;
                case "5":
                    batchCalculateView();
                    break;
                case "6":
                    analyticsDashboardView();
                    break;
                case "7":
                    running = false;
                    displayExitCard();
                    break;
                default:
                    printError("Invalid selection. Provide a valid option [1-7].");
                    pausePrompt();
            }
        }
    }

    private static void promptForDatabaseCredentials(DatabaseManager databaseManager) {
        System.out.println(YELLOW + "  Enter MySQL credentials to try again, or press Enter to skip." + RESET);
        System.out.print(CYAN + "  MySQL username [root]: " + RESET);
        String username = SCANNER.nextLine().trim();
        if (username.isEmpty()) {
            username = "root";
        }

        System.out.print(CYAN + "  MySQL password: " + RESET);
        String password = SCANNER.nextLine();
        if (password.isEmpty()) {
            printError("No password entered. Continuing without a database connection.");
            return;
        }

        databaseManager.configureCredentials(username, password);
        if (databaseManager.testConnection()) {
            printSuccess("MySQL connection restored for user '" + username + "'.");
        } else {
            printError("MySQL login failed. Check the username, password, database, and server status.");
        }
    }

    private static void displayBanner() {
        System.out.println(EMERALD + BOLD);
        String border = "  ╔" + "═".repeat(63) + "╗";
        System.out.println(border);
        printBannerLine("T E R R A G A U G E");
        printBannerLine("Environmental Accounting and CO2e Footprint Engine");
        System.out.println("  ╚" + "═".repeat(63) + "╝" + RESET);
        System.out.println();
    }

    private static void printBannerLine(String text) {
        System.out.printf("  ║%-63s║%n", text);
    }

    private static void displayHeader() {
        System.out.println(EMERALD + "┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("│  " + BOLD + "TerraGauge" + RESET + EMERALD + " | Environmental Activity Ledger & Audit Platform    │");
        System.out.println("└─────────────────────────────────────────────────────────────────┘" + RESET);
    }

    private static void displayMenu() {
        System.out.println(BOLD + "  OPERATIONAL CONTROLS" + RESET);
        System.out.println("  " + CYAN + "[1]" + RESET + " Log Consumption Event       " + GRAY + "Record transport, energy, or waste" + RESET);
        System.out.println("  " + CYAN + "[2]" + RESET + " Audit Activity Register     " + GRAY + "View entire user activity log" + RESET);
        System.out.println("  " + CYAN + "[3]" + RESET + " Modify Event Metric         " + GRAY + "Update quantity of logged activity" + RESET);
        System.out.println("  " + CYAN + "[4]" + RESET + " Purge Event Record          " + GRAY + "Delete activity and its linked records" + RESET);
        System.out.println("  " + CYAN + "[5]" + RESET + " Batch Compute Footprint     " + GRAY + "Multithreaded background factor matching" + RESET);
        System.out.println("  " + CYAN + "[6]" + RESET + " Analytical Intelligence     " + GRAY + "Aggregations, categories, and top drivers" + RESET);
        System.out.println("  " + RED + "[7]" + RESET + " Terminate Session");
        System.out.println(GRAY + "───────────────────────────────────────────────────────────────────" + RESET);
    }

    private static void recordActivityView() {
        clearScreen();
        printSectionBanner("EVENT REGISTRATION WIZARD");

        try {
            printCategoryGuide();
            System.out.print(CYAN + "Enter Category        : " + RESET);
            ActivityCategory category = ActivityCategory.fromString(SCANNER.nextLine());

            printIdentifierGuide(category);
            System.out.print(CYAN + "Activity Identifier   : " + RESET);
            String name = SCANNER.nextLine().trim();

            EmissionFactor factor = calculator.getFactor(name);
            System.out.println(GREEN + "  * Factor Located: " + factor.getFactor() + " kg CO2e / " + factor.getUnit() + " (" + factor.getSource() + ")" + RESET);

            System.out.print(CYAN + "Metric Quantity (" + factor.getUnit() + ") : " + RESET);
            double quantity = Double.parseDouble(SCANNER.nextLine().trim());

            System.out.print(CYAN + "Event Date (YYYY-MM-DD or Enter for today): " + RESET);
            String dateInput = SCANNER.nextLine().trim();
            LocalDate date = dateInput.isEmpty() ? LocalDate.now() : LocalDate.parse(dateInput);

            Activity act = new Activity(activeUserId, category, factor.getActivityName(), quantity, factor.getUnit(), date);
            int newId = activityManager.addActivity(act);

            System.out.println();
            printSuccess("Event registered successfully with Primary Key #" + newId);

        } catch (IllegalArgumentException | DateTimeParseException e) {
            printError("Validation Failure: " + e.getMessage());
        } catch (SQLException e) {
            printError("Relational Persistence Error: " + e.getMessage());
        }
        pausePrompt();
    }

    private static void printCategoryGuide() {
        System.out.println(DIM + "Choose a category that describes what you are measuring:" + RESET);
        System.out.println(DIM + "  TRANSPORT   = travel or fuel use" + RESET);
        System.out.println(DIM + "  ELECTRICITY = electrical energy consumed" + RESET);
        System.out.println(DIM + "  WASTE       = waste disposed of or recycled" + RESET);
        System.out.println(DIM + "  WATER       = water supplied or consumed" + RESET);
        System.out.println();
    }

    private static void printIdentifierGuide(ActivityCategory category) {
        System.out.println(DIM + "For " + category + ", choose one of these activity identifiers:" + RESET);
        for (Object value : calculator.getRegistry().values()) {
            EmissionFactor factor = (EmissionFactor) value;
            if (factor.getCategory() == category) {
                System.out.println(DIM + "  " + factor.getActivityName() + " -> enter the amount in " + factor.getUnit() + RESET);
            }
        }
        System.out.println(DIM + "You may enter the full name or a unique part of it, such as 'landfill' for 'Landfill Waste'." + RESET);
        System.out.println(DIM + "Next, enter a quantity greater than 0. Example: 250 for 250 kWh of Grid Electricity." + RESET);
        System.out.println(DIM + "For the date, use YYYY-MM-DD, such as 2026-09-16, or press Enter for today." + RESET);
        System.out.println();
    }

    private static void viewAllActivitiesView() {
        clearScreen();
        printSectionBanner("RECORDED ENVIRONMENTAL LOG");

        try {
            List<Activity> list = activityManager.getActivitiesByUser(activeUserId);
            if (list.isEmpty()) {
                System.out.println(YELLOW + "  No activity records logged in the database." + RESET);
            } else {
                System.out.println(GRAY + "┌─────┬──────────────┬──────────────────────┬─────────────┬────────────┐" + RESET);
                System.out.printf(GRAY + "│" + BOLD + " %-3s " + GRAY + "│" + BOLD + " %-12s " + GRAY + "│" + BOLD + " %-20s " + GRAY + "│" + BOLD + " %-11s " + GRAY + "│" + BOLD + " %-10s " + GRAY + "│%n" + RESET,
                        "ID", "CATEGORY", "ACTIVITY", "QUANTITY", "DATE");
                System.out.println(GRAY + "├─────┼──────────────┼──────────────────────┼─────────────┼────────────┤" + RESET);

                for (Activity a : list) {
                    String qtyStr = String.format("%.2f %s", a.getQuantity(), a.getUnit());
                    System.out.printf(GRAY + "│" + RESET + " %-3d " + GRAY + "│" + CYAN + " %-12s " + GRAY + "│" + RESET + " %-20s " + GRAY + "│" + YELLOW + " %-11s " + GRAY + "│" + RESET + " %-10s " + GRAY + "│%n" + RESET,
                            a.getActivityId(),
                            a.getCategory(),
                            truncate(a.getActivityName(), 20),
                            qtyStr,
                            a.getActivityDate());
                }
                System.out.println(GRAY + "└─────┴──────────────┴──────────────────────┴─────────────┴────────────┘" + RESET);
                System.out.println(DIM + "  Total Ledger Count: " + list.size() + " entries" + RESET);
            }
        } catch (SQLException e) {
            printError("Failed to fetch activity records: " + e.getMessage());
        }
        pausePrompt();
    }

    private static void modifyActivityView() {
        clearScreen();
        printSectionBanner("MODIFY CONSUMPTION METRIC");

        try {
            System.out.println(DIM + "Use the ID shown by option [2] Audit Activity Register." + RESET);
            System.out.print(CYAN + "Enter Target Activity ID : " + RESET);
            int id = Integer.parseInt(SCANNER.nextLine().trim());

            System.out.println(DIM + "Enter the corrected amount in the activity's recorded unit (for example, 250 kWh)." + RESET);
            System.out.print(CYAN + "Enter Corrected Quantity : " + RESET);
            double qty = Double.parseDouble(SCANNER.nextLine().trim());

            if (activityManager.updateActivityQuantity(id, qty)) {
                printSuccess("Activity record updated.");
                System.out.println(YELLOW + "  Note: Run option [5] to recalculate corresponding impact metrics." + RESET);
            } else {
                printError("No corresponding record found for Activity ID #" + id);
            }
        } catch (NumberFormatException e) {
            printError("Invalid numerical format provided.");
        } catch (Exception e) {
            printError("Operation failed: " + e.getMessage());
        }
        pausePrompt();
    }

    private static void removeActivityView() {
        clearScreen();
        printSectionBanner("PURGE ENVIRONMENTAL RECORD");

        try {
            System.out.println(DIM + "Use the ID shown by option [2] Audit Activity Register. This permanently deletes the record." + RESET);
            System.out.print(RED + "Enter Activity ID to permanently delete: " + RESET);
            int id = Integer.parseInt(SCANNER.nextLine().trim());

            if (activityManager.deleteActivity(id)) {
                printSuccess("Activity #" + id + " and all linked impact logs purged.");
            } else {
                printError("No target record located with ID #" + id);
            }
        } catch (NumberFormatException e) {
            printError("Invalid ID format.");
        } catch (Exception e) {
            printError("Deletion failure: " + e.getMessage());
        }
        pausePrompt();
    }

    private static void batchCalculateView() {
        clearScreen();
        printSectionBanner("PARALLEL BATCH PROCESSOR");

        try {
            List<Activity> pending = activityManager.getUncalculatedActivities(activeUserId);
            if (pending.isEmpty()) {
                System.out.println(GREEN + "  All recorded activities have associated carbon records." + RESET);
                pausePrompt();
                return;
            }

            System.out.println(CYAN + "  Identified " + pending.size() + " uncalculated records. Dispatching to thread pool..." + RESET);
            System.out.println();

            List<ImpactRecord> calculated = calculator.processBatch(pending);

            for (ImpactRecord record : calculated) {
                System.out.printf(GRAY + "  [Thread Worker] Activity #%-3d calculated -> " + EMERALD + "%.3f kg CO2e" + RESET + "%n",
                        record.getActivityId(), record.getCo2eKg());
            }

            activityManager.saveImpactBatch(calculated);
            System.out.println();
            printSuccess("Committed " + calculated.size() + " calculated impacts to MySQL via transactional batch.");

        } catch (SQLException e) {
            printError("Relational persistence failed: " + e.getMessage());
        }
        pausePrompt();
    }

    private static void analyticsDashboardView() {
        clearScreen();
        printSectionBanner("ANALYTICAL INTELLIGENCE DASHBOARD");

        System.out.println(BOLD + "AGGREGATE AUDIT METRICS" + RESET);
        reportManager.printTotalImpact(activeUserId);
        System.out.println();

        System.out.println(BOLD + "CATEGORY EMISSION PROFILE" + RESET);
        reportManager.printCategoryWiseImpact(activeUserId);
        System.out.println();

        System.out.println(BOLD + "TOP POLLUTION DRIVERS" + RESET);
        reportManager.printTopEmittingActivities(activeUserId, 5);

        pausePrompt();
    }

    private static void printSectionBanner(String title) {
        System.out.println(EMERALD + "┌─ " + BOLD + title + RESET + EMERALD);
        System.out.println("└──────────────────────────────────────────────────────────────────" + RESET);
    }

    private static void printStatus(String module, String msg) {
        System.out.printf("  " + CYAN + "[%-10s]" + RESET + " %s%n", module, msg);
    }

    private static void printSuccess(String msg) {
        System.out.println("  " + GREEN + "✔ " + msg + RESET);
    }

    private static void printError(String msg) {
        System.out.println("  " + RED + "✖ " + msg + RESET);
    }

    private static void pausePrompt() {
        System.out.println();
        System.out.print(GRAY + "Press [Enter] to continue..." + RESET);
        SCANNER.nextLine();
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void displayExitCard() {
        clearScreen();
        System.out.println(EMERALD + BOLD);
        System.out.println("  ┌────────────────────────────────────────────────────────┐");
        System.out.println("  │     TerraGauge CLI Session Safely Terminated           │");
        System.out.println("  │     Clean relational state preserved. Goodbye!         │");
        System.out.println("  └────────────────────────────────────────────────────────┘" + RESET);
    }

    private static String truncate(String text, int width) {
        if (text.length() <= width) return text;
        return text.substring(0, width - 2) + "..";
    }
}