// User.java
import java.util.Scanner;
import java.io.*;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
}
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
        } catch (IOException e) {
            // الملف غير موجود بعد؟ عادي أول تشغيل
        }
        return false;
    }

    public void save(User user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(user.toFileString());
            writer.newLine();
        }
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
            System.out.println(" this email has been already exist");
            return false;
        }

        try {
            userRepo.save(user);
            System.out.println("user added successfully");
            return true;
        } catch (Exception e) {
            System.out.println("failed to add user");
            return false;
        }
    }
}
////////////////////////////////////////////////////////////////////////////////
/// //telbo

class Asset {
    private String Id;
    private String name;
    private String value;
    private String type;

    public Asset(String Id, String name, String value, String type) {
        this.Id = Id;
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String toFileString() {
        return Id + "," + name + "," + value + "," + type;
    }
}
class InvestmentRepository {
    private static final String FILE_NAME = "investments.txt";

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
// Main.java


public class signup {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserRepository repo = new UserRepository();
        UserService service = new UserService(repo);

        System.out.println("Register a new user");

        System.out.print("Full name: ");
        String fullName = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        Investor investor = new Investor(email, password, fullName);
        if (!service.registerUser(investor)) {
            return;
        }
        InvestmentRepository invRepo = new InvestmentRepository();
        InvestmentService invService = new InvestmentService(invRepo);
        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Add Investment");
            System.out.println("2. Edit Investment");
            System.out.println("3. Remove Investment");
            System.out.println("4. Exit");
            System.out.print("Your choice: ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
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

            } else if (choice.equals("2")) {
                System.out.print("Enter Investment ID to edit: ");
                String id = scanner.nextLine();
                System.out.print("New Name: ");
                String newName = scanner.nextLine();
                System.out.print("New Value: ");
                String newValue = scanner.nextLine();
                System.out.print("New Type: ");
                String newType = scanner.nextLine();
                invService.editInvestmentById(id, newName, newValue, newType);

            } else if (choice.equals("3")) {
                System.out.print("Enter Investment ID to remove: ");
                String id = scanner.nextLine();
                invService.removeInvestmentById(id);

            } else if (choice.equals("4")) {
                System.out.println("Goodbye!");
                break;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }
}

    
