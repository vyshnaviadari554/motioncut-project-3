import java.io.*;
import java.util.*;

class Expense implements Serializable {
    private String date;
    private String category;
    private double amount;

    public Expense(String date, String category, double amount) {
        this.date = date;
        this.category = category;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Date: " + date + ", Category: " + category + ", Amount: $" + amount;
    }
}

class User implements Serializable {
    private String username;
    private String password;
    private List<Expense> expenses;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.expenses = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public List<Expense> getExpenses() {
        return expenses;
    }
}

class ExpenseTracker {
    private Map<String, User> users;

    public ExpenseTracker() {
        users = new HashMap<>();
        loadFromFile();
    }

    public void registerUser(String username, String password) {
        if (!users.containsKey(username)) {
            users.put(username, new User(username, password));
            System.out.println("User registered successfully.");
        } else {
            System.out.println("Username already exists.");
        }
    }

    public User loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            return user;
        }
        System.out.println("Invalid username or password.");
        return null;
    }

    public void addExpense(User user, String date, String category, double amount) {
        user.addExpense(new Expense(date, category, amount));
        System.out.println("Expense added successfully.");
    }

    public void listExpenses(User user) {
        System.out.println("Expenses for " + user.getUsername() + ":");
        for (Expense expense : user.getExpenses()) {
            System.out.println(expense);
        }
    }

    public void getTotalByCategory(User user) {
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense expense : user.getExpenses()) {
            categoryTotals.put(expense.getCategory(),
                categoryTotals.getOrDefault(expense.getCategory(), 0.0) + expense.getAmount());
        }

        System.out.println("Total Expenses by Category:");
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            System.out.printf("%s: $%.2f%n", entry.getKey(), entry.getValue());
        }
    }

    // Change access modifier from private to public
    public void saveToFile() { // Now it's accessible
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            oos.writeObject(users);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving data.");
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile() { // Keep it as public for loading
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.dat"))) {
            users = (Map<String, User>) ois.readObject();
            System.out.println("Data loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous data found or error loading data.");
        }
    }
}

public class MainApp {

     public static void main(String[] args){
         Scanner scanner = new Scanner(System.in);
         ExpenseTracker tracker = new ExpenseTracker();

         while (true) {
             System.out.println("\n1. Register\n2. Login\n3. Exit");
             int choice = scanner.nextInt();
             scanner.nextLine(); // Consume newline

             switch (choice) {
                 case 1:
                     System.out.print("Enter username: ");
                     String regUsername = scanner.nextLine();
                     System.out.print("Enter password: ");
                     String regPassword = scanner.nextLine();
                     tracker.registerUser(regUsername, regPassword);
                     break;

                 case 2:
                     System.out.print("Enter username: ");
                     String loginUsername = scanner.nextLine();
                     System.out.print("Enter password: ");
                     String loginPassword = scanner.nextLine();
                     
                     User loggedInUser = tracker.loginUser(loginUsername, loginPassword);
                     if (loggedInUser != null) { // Successful login
                         while (true) {
                             System.out.println("\n1. Add Expense\n2. List Expenses\n3. Total by Category\n4. Logout");
                             int actionChoice = scanner.nextInt();
                             scanner.nextLine(); // Consume newline

                             switch (actionChoice) {
                                 case 1:
                                     System.out.print("Enter date (YYYY-MM-DD): ");
                                     String date = scanner.nextLine();
                                     System.out.print("Enter category: ");
                                     String category = scanner.nextLine();
                                     System.out.print("Enter amount: ");
                                     double amount = scanner.nextDouble();
                                     tracker.addExpense(loggedInUser, date, category, amount);
                                     break;

                                 case 2:
                                     tracker.listExpenses(loggedInUser);
                                     break;

                                 case 3:
                                     tracker.getTotalByCategory(loggedInUser);
                                     break;

                                 case 4:
                                     loggedInUser = null; // Logout
                                     break;

                                 default:
                                     System.out.println("Invalid choice.");
                             }
                             if (loggedInUser == null) break; // Exit loop if logged out
                         }
                     }
                     break;

                 case 3:
                     tracker.saveToFile(); // Save data before exiting
                     scanner.close();
                     System.exit(0);

                 default:
                     System.out.println("Invalid choice.");
             }
         }
     }
}





/*import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

class Expense {
    String date;
    String category;
    double amount;

    public Expense(String date, String category, double amount) {
        this.date = date;
        this.category = category;
        this.amount = amount;
    }
}

class User {
    String username;
    String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}

class ExpenseTracker {
    private ArrayList<Expense> expenses = new ArrayList<>();
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public void listExpenses() {
        for (Expense expense : expenses) {
            System.out.println("Date: " + expense.date + ", Category: " + expense.category + ", Amount: " + expense.amount);
        }
    }

    public void saveExpensesToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(currentUser.username + "_expenses.ser"))) {
            oos.writeObject(expenses);
            System.out.println("Expenses saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving expenses to file.");
        }
    }

    public void loadExpensesFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(currentUser.username + "_expenses.ser"))) {
            expenses = (ArrayList<Expense>) ois.readObject();
            System.out.println("Expenses loaded from file.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No expenses found or error loading expenses from file.");
        }
    }
}

public class MainApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ExpenseTracker expenseTracker = new ExpenseTracker();

        System.out.print("Enter username: ");
        String username = scanner.next();
        System.out.print("Enter password: ");
        String password = scanner.next();

        User currentUser = new User(username, password);
        expenseTracker.setCurrentUser(currentUser);
        expenseTracker.loadExpensesFromFile();

        while (true) {
            System.out.println("1. Add Expense");
            System.out.println("2. List Expenses");
            System.out.println("3. Save Expenses to File");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter date (MM/DD/YYYY): ");
                    String date = scanner.next();
                    System.out.print("Enter category: ");
                    String category = scanner.next();
                    System.out.print("Enter amount: ");
                    double amount = scanner.nextDouble();

                    Expense newExpense = new Expense(date, category, amount);
                    expenseTracker.addExpense(newExpense);
                    System.out.println("Expense added successfully!");
                    break;

                case 2:
                    System.out.println("List of Expenses:");
                    expenseTracker.listExpenses();
                    break;

                case 3:
                    expenseTracker.saveExpensesToFile();
                    break;

                case 4:
                    System.out.println("Exiting Expense Tracker. Goodbye!");
                    expenseTracker.saveExpensesToFile();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }
}*/