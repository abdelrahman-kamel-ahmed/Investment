// User.java
import java.util.Scanner;
import java.io.*;
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
// Main.java
public class signup {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserRepository repo = new UserRepository();
        UserService service = new UserService(repo);

        System.out.println("Register a new user");

        System.out.print("Full name");
        String fullName = scanner.nextLine();

        System.out.print("email");
        String email = scanner.nextLine();

        System.out.print("password");
        String password = scanner.nextLine();

        Investor investor = new Investor(email, password, fullName);
        service.registerUser(investor);
    }
}
