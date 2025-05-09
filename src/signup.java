// User.java
import java.util.Scanner;
import java.io.*;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
/**
 * Abstract base class for all users in the system.
 * Defines common attributes and behaviors.
 */
abstract class User {
    protected String email;
    protected String password;
    protected String fullName;

    /**
     * Constructs a new User.
     * @param email User's email
     * @param password User's password
     * @param fullName User's full name
     */
    public User(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    /**
     * Gets the email address of the user.
     * @return Email address of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * Converts user data into file-storable format.
     * @return Comma-separated string of user data
     */
    public String toFileString() {
        return email + "," + password + "," + fullName;
    }
}

/**
 * Represents an investor user with financial features like zakat and reports.
 */
class Investor extends User {
    /**
     * Constructs an Investor.
     * @param email Email of the investor
     * @param password Password of the investor
     * @param fullName Full name of the investor
     */
    public Investor(String email, String password, String fullName) {
        super(email, password, fullName);
    }

    /**
     * Calculates zakat for investor and generates a zakat report.
     * @param repository The investment repository linked to this investor
     */
    public void viewZakatPanel(InvestmentRepository repository) {
        try {
            List<Asset> assets = repository.loadAll();
            ZakatCalculator calculator = new ZakatCalculator();
            double zakatAmount = calculator.estimateZakat(assets);

            System.out.println("Your total zakat due is: " + zakatAmount + " EGP");

            ComplianceReport report = new ComplianceReport();
            report.generateReport(assets, zakatAmount);
        } catch (IOException e) {
            System.out.println("Failed to load assets for zakat calculation.");
        }
    }

    /**
     * Generates and exports a financial report.
     * @param repository Investment repository to fetch assets
     * @param format Export format (PDF or Excel simulated as txt/csv)
     */
    public void generateFinancialReport(InvestmentRepository repository, String format) {
        try {
            List<Asset> assets = repository.loadAll();
            FinancialReportGenerator generator = new FinancialReportGenerator();
            generator.generateReport(assets, format);
        } catch (IOException e) {
            System.out.println("Failed to load assets for financial report.");
        }
    }
}

/**
 * Utility class for storing and retrieving users from file.
 */
class UserRepository {
    private static final String FILE_NAME = "users.txt";

    /**
     * Checks if an email is already registered.
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equalsIgnoreCase(email)) {
                    return true;
                }
            }
        } catch (IOException e) {}
        return false;
    }

    /**
     * Saves a user to the users file.
     * @param user User to save
     * @throws IOException If file cannot be written
     */
    public void save(User user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(user.toFileString());
            writer.newLine();
        }
    }

    /**
     * Finds a user by email.
     * @param email Email to search for
     * @return User object or null if not found
     */
    public User findUserByEmail(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equalsIgnoreCase(email)) {
                    return new Investor(parts[0], parts[1], parts[2]);
                }
            }
        } catch (IOException e) {}
        return null;
    }
}

/**
 * Handles business logic for user registration and login.
 */
class UserService {
    private UserRepository userRepo;

    /**
     * Constructs a UserService with a given repository.
     * @param repo Repository for user data
     */
    public UserService(UserRepository repo) {
        this.userRepo = repo;
    }

    /**
     * Registers a user if email is not already used.
     * @param user User to register
     * @return true if registration succeeds, false otherwise
     */
    public boolean registerUser(User user) {
        if (userRepo.emailExists(user.getEmail())) {
            System.out.println("This email already exists.");
            return false;
        }
        try {
            userRepo.save(user);
            System.out.println("User added successfully.");
            return true;
        } catch (Exception e) {
            System.out.println("Failed to add user.");
            return false;
        }
    }

    /**
     * Validates login by checking credentials.
     * @param email Email of user
     * @param password Password of user
     * @return true if credentials match, false otherwise
     */
    public boolean login(String email, String password) {
        User user = userRepo.findUserByEmail(email);
        if (user == null) {
            System.out.println("This email has no account.");
            return false;
        }
        if (!user.password.equals(password)) {
            System.out.println("Password is not correct.");
            return false;
        }
        System.out.println("Login successful, hello " + user.fullName + "!");
        return true;
    }
}

/**
 * Represents a single asset in the investor's portfolio.
 */
class Asset {
    private String id;
    private String name;
    private String value;
    private String type;

    /**
     * Constructs an Asset object.
     * @param id Asset ID
     * @param name Asset name
     * @param value Asset value
     * @param type Asset type
     */
    public Asset(String id, String name, String value, String type) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.type = type;
    }

    /**
     * @return Asset name
     */
    public String getName() {
        return name;
    }

    /**
     * @return Asset value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return CSV format of asset details
     */
    public String toFileString() {
        return id + "," + name + "," + value + "," + type;
    }
}

/**
 * Manages asset storage for each user in separate files.
 */
class InvestmentRepository {
    private String FILE_NAME;

    /**
     * Constructs repository for a specific user's assets.
     * @param userEmail The user's email to derive filename
     */
    public InvestmentRepository(String userEmail) {
        this.FILE_NAME = "investments_" + userEmail.replaceAll("@", "_at_") + ".txt";
    }

    /**
     * Saves a single asset to the file.
     * @param investment Asset to save
     * @throws IOException If writing fails
     */
    public void save(Asset investment) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(investment.toFileString());
            writer.newLine();
        }
    }

    /**
     * Loads all assets from the user's file.
     * @return List of all saved assets
     * @throws IOException If reading fails
     */
    public List<Asset> loadAll() throws IOException {
        List<Asset> investments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length == 4) {
                    investments.add(new Asset(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        }
        return investments;
    }

    /**
     * Overwrites the file with updated asset list.
     * @param investments List of updated assets
     * @throws IOException If writing fails
     */
    public void saveAll(List<Asset> investments) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Asset a : investments) {
                writer.write(a.toFileString());
                writer.newLine();
            }
        }
    }
}

/**
 * Provides services to add, edit, or delete assets.
 */
class InvestmentService {
    private InvestmentRepository repository;

    /**
     * Constructs InvestmentService with the given repository.
     * @param repository The investment repository for a user
     */
    public InvestmentService(InvestmentRepository repository) {
        this.repository = repository;
    }

    /**
     * Adds a new investment.
     * @param investment The asset to be added
     * @return true if successful, false otherwise
     */
    public boolean addInvestment(Asset investment) {
        if (investment == null || investment.toFileString().isBlank()) {
            System.out.println("Invalid investment data.");
            return false;
        }
        try {
            repository.save(investment);
            System.out.println("Investment added successfully.");
            return true;
        } catch (IOException e) {
            System.out.println("Failed to add investment.");
            return false;
        }
    }

    /**
     * Removes an investment by its ID.
     * @param id The ID of the asset to remove
     * @return true if removed, false otherwise
     */
    public boolean removeInvestmentById(String id) {
        try {
            List<Asset> all = repository.loadAll();
            boolean removed = all.removeIf(asset -> asset.toFileString().startsWith(id + ","));
            if (removed) {
                repository.saveAll(all);
                System.out.println("Investment removed successfully.");
            } else {
                System.out.println("Investment ID not found.");
            }
            return removed;
        } catch (IOException e) {
            System.out.println("Error while removing investment.");
            return false;
        }
    }

    /**
     * Edits an existing investment.
     * @param id Asset ID to edit
     * @param newName New name
     * @param newValue New value
     * @param newType New type
     * @return true if edited, false otherwise
     */
    public boolean editInvestmentById(String id, String newName, String newValue, String newType) {
        try {
            List<Asset> all = repository.loadAll();
            boolean found = false;
            for (int i = 0; i < all.size(); i++) {
                Asset a = all.get(i);
                if (a.toFileString().startsWith(id + ",")) {
                    Asset updated = new Asset(id, newName, newValue, newType);
                    all.set(i, updated);
                    found = true;
                    break;
                }
            }
            if (found) {
                repository.saveAll(all);
                System.out.println("Investment updated successfully.");
            } else {
                System.out.println("Investment ID not found.");
            }
            return found;
        } catch (IOException e) {
            System.out.println("Error while editing investment.");
            return false;
        }
    }
}

/**
 * Calculates zakat for a list of assets.
 */
class ZakatCalculator {
    private static final double ZAKAT_PERCENTAGE = 0.025;

    /**
     * Estimates zakat owed from a list of assets.
     * @param assets List of user's assets
     * @return Calculated zakat amount
     */
    public double estimateZakat(List<Asset> assets) {
        double total = 0;
        for (Asset asset : assets) {
            try {
                total += Double.parseDouble(asset.getValue());
            } catch (NumberFormatException e) {
                System.out.println("Skipping invalid asset value for: " + asset.getName());
            }
        }
        return total * ZAKAT_PERCENTAGE;
    }
}

/**
 * Generates a zakat compliance report and saves it to file.
 */
class ComplianceReport {

    /**
     * Writes zakat report to a file based on user's assets.
     * @param assets List of user's assets
     * @param zakatAmount Calculated zakat amount
     */
    public void generateReport(List<Asset> assets, double zakatAmount) {
        try (FileWriter writer = new FileWriter("zakat_report.txt")) {
            writer.write("=== Zakat Report ===\nAssets:\n");
            for (Asset asset : assets) {
                writer.write("- " + asset.getName() + ": " + asset.getValue() + " EGP\n");
            }
            writer.write("Total Zakat Due: " + zakatAmount + " EGP\n");
            System.out.println("Zakat report generated successfully.");
        } catch (IOException e) {
            System.out.println("Error generating report.");
        }
    }
}

/**
 * Generates a financial report and exports it as txt or csv.
 */
class FinancialReportGenerator {

    /**
     * Generates a financial report for user's assets in selected format.
     * @param assets List of user's assets
     * @param format Desired export format (PDF/Excel simulated)
     */
    public void generateReport(List<Asset> assets, String format) {
        String fileName = "financial_report." + (format.equalsIgnoreCase("pdf") ? "txt" : "csv");

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("=== Financial Report ===\n");
            writer.write("Asset ID, Name, Value (EGP), Type\n");
            for (Asset asset : assets) {
                writer.write(asset.toFileString() + "\n");
            }
            System.out.println("Report exported as: " + fileName);
        } catch (IOException e) {
            System.out.println("Error generating financial report.");
        }
    }
}

/**
 * Main class to run the InvestMate application in console.
 * Handles user registration, login, and investment operations menu.
 */
public class signup {

    /**
     * Entry point of the application.
     * Provides menu for signup, login, and asset operations.
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        String fullName = "", email = "", password = "";
        User loggedInUser = null;
        Scanner scanner = new Scanner(System.in);
        UserRepository repo = new UserRepository();
        UserService service = new UserService(repo);

        System.out.println("Welcome to InvestMate");
        System.out.println("1. Register a new user (Sign Up)");
        System.out.println("2. Login to your account");
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        /**
         * Handles user registration with retry loop if email already exists.
         */
        if (choice == 1) {
            while (true) {
                System.out.println("\nSign Up");
                System.out.print("Full Name: ");
                fullName = scanner.nextLine();

                System.out.print("Email: ");
                email = scanner.nextLine();

                System.out.print("Password: ");
                password = scanner.nextLine();

                Investor investor = new Investor(email, password, fullName);
                if (service.registerUser(investor)) {
                    loggedInUser = investor;
                    break;
                } else {
                    System.out.println("Try again.\n");
                }
            }
        }

        /**
         * Handles user login with retry loop if credentials are invalid.
         */
        else if (choice == 2) {
            while (true) {
                System.out.println("\nLogin");
                System.out.print("Email: ");
                email = scanner.nextLine();
                System.out.print("Password: ");
                password = scanner.nextLine();

                if (service.login(email, password)) {
                    loggedInUser = new Investor(email, password, "");
                    break;
                } else {
                    System.out.println("Try again.\n");
                }
            }
        } else {
            System.out.println("Invalid choice.");
        }

        /**
         * If user is logged in, show investment operations menu.
         */
        if (loggedInUser != null) {
            InvestmentRepository invRepo = new InvestmentRepository(loggedInUser.getEmail());
            InvestmentService invService = new InvestmentService(invRepo);

            while (true) {
                System.out.println("\nChoose an option:");
                System.out.println("1. Add Investment");
                System.out.println("2. Edit Investment");
                System.out.println("3. Remove Investment");
                System.out.println("4. Calculate Zakat");
                System.out.println("5. Export Financial Report");
                System.out.println("6. Exit");
                System.out.print("Your choice: ");
                String choicce = scanner.nextLine();

                /**
                 * Add a new investment asset for the current user.
                 */
                if (choicce.equals("1")) {
                    System.out.print("Investment Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Investment Value: ");
                    String value = scanner.nextLine();
                    System.out.print("Investment Type: ");
                    String type = scanner.nextLine();
                    System.out.print("Investment ID: ");
                    String investmentId = scanner.nextLine();
                    Asset investment = new Asset(investmentId, name, value, type);
                    invService.addInvestment(investment);

                /**
                 * Edit an existing investment by its ID.
                 */
                } else if (choicce.equals("2")) {
                    System.out.print("Enter Investment ID to edit: ");
                    String id = scanner.nextLine();
                    System.out.print("New Name: ");
                    String newName = scanner.nextLine();
                    System.out.print("New Value: ");
                    String newValue = scanner.nextLine();
                    System.out.print("New Type: ");
                    String newType = scanner.nextLine();
                    invService.editInvestmentById(id, newName, newValue, newType);

                /**
                 * Remove an investment by its ID.
                 */
                } else if (choicce.equals("3")) {
                    System.out.print("Enter Investment ID to remove: ");
                    String id = scanner.nextLine();
                    invService.removeInvestmentById(id);

                /**
                 * Calculate zakat due for the user's investments.
                 */
                } else if (choicce.equals("4")) {
                    ((Investor) loggedInUser).viewZakatPanel(invRepo);

                /**
                 * Generate and export a financial report (text or CSV).
                 */
                } else if (choicce.equals("5")) {
                    System.out.print("Enter export format (PDF or Excel): ");
                    String format = scanner.nextLine();
                    ((Investor) loggedInUser).generateFinancialReport(invRepo, format);

                /**
                 * Exit the application.
                 */
                } else if (choicce.equals("6")) {
                    System.out.println("Goodbye!");
                    break;

                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            }
        }
        scanner.close();
    }
}
