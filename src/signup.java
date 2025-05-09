// User.java
import java.util.Scanner;
import java.io.*;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;


//----------------------------Dadooo-----------------------------------------
// Abstract User
abstract class User {
    protected String email;
    protected String password;
    protected String fullName;

    public User(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public String toFileString() {
        return email + "," + password + "," + fullName;
    }
}

// Investor.java
class Investor extends User {
    public Investor(String email, String password, String fullName) {
        super(email, password, fullName);
    }

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

// UserRepository.java
class UserRepository {
    private static final String FILE_NAME = "users.txt";

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

    public void save(User user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(user.toFileString());
            writer.newLine();
        }
    }

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

// UserService.java
class UserService {
    private UserRepository userRepo;

    public UserService(UserRepository repo) {
        this.userRepo = repo;
    }

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


//---------------------------------Telboo-----------------------------------------
// Asset.java
class Asset {
    private String id;
    private String name;
    private String value;
    private String type;

    public Asset(String id, String name, String value, String type) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String toFileString() {
        return id + "," + name + "," + value + "," + type;
    }
}

// InvestmentRepository.java
class InvestmentRepository {
    private String FILE_NAME;

    public InvestmentRepository(String userEmail) {
        this.FILE_NAME = "investments_" + userEmail.replaceAll("@", "_at_") + ".txt";
    }

    public void save(Asset investment) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(investment.toFileString());
            writer.newLine();
        }
    }

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

    public void saveAll(List<Asset> investments) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Asset a : investments) {
                writer.write(a.toFileString());
                writer.newLine();
            }
        }
    }
}

// InvestmentService.java
class InvestmentService {
    private InvestmentRepository repository;

    public InvestmentService(InvestmentRepository repository) {
        this.repository = repository;
    }

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
//-------------------------------Esraa-----------------------------------
// ZakatCalculator.java
class ZakatCalculator {
    private static final double ZAKAT_PERCENTAGE = 0.025;

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

// ComplianceReport.java
class ComplianceReport {
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

class FinancialReportGenerator {

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

// Main.java
public class signup {
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
                }
                else{
                    System.out.println("Try again.\n");
                }
            } 
        }
        else {
            System.out.println("Invalid choice.");
        }

        if (loggedInUser != null) {
            InvestmentRepository invRepo = new InvestmentRepository(loggedInUser.getEmail());
            InvestmentService invService = new InvestmentService(invRepo);
        
            while (true) {
                System.out.println("\nChoose an option:");
                System.out.println("1. Add Investment");
                System.out.println("2. Edit Investment");
                System.out.println("3. Remove Investment");
                System.out.println("5. Export Financial Report");
                System.out.println("6. Exit");
                System.out.print("Your choice: ");
                String choicce = scanner.nextLine();

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

                } else if (choicce.equals("3")) {
                    System.out.print("Enter Investment ID to remove: ");
                    String id = scanner.nextLine();
                    invService.removeInvestmentById(id);

                } else if (choicce.equals("4")) {
                    ((Investor) loggedInUser).viewZakatPanel(invRepo);

                } else if (choicce.equals("5")) {
                    System.out.print("Enter export format (PDF or Excel): ");
                    String format = scanner.nextLine();
                    ((Investor) loggedInUser).generateFinancialReport(invRepo, format);
                
                } else if (choicce.equals("6")) {
                    System.out.println("Goodbye!");
                    break;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
        }    }
    }  
} 
