import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ExpenseTracker extends JFrame {
    private static final String USER_FILE = "users.ser";
    private UserManager userManager;
    private User currentUser;
    private ExpenseManager expenseManager;
    private JTextField dateField, categoryField, amountField;
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;

    public static void main(String[] args) {
        new LoginUI().setVisible(true);
    }

    // Login UI class
    static class LoginUI extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private UserManager userManager = new UserManager();

        public LoginUI() {
            setTitle("Login or Register");
            setSize(400, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setupUI();
        }

        private void setupUI() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            usernameField = new JTextField(20);
            passwordField = new JPasswordField(20);

            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(new LoginListener());
            JButton registerButton = new JButton("Register");
            registerButton.addActionListener(new RegisterListener());

            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);
            panel.add(loginButton);
            panel.add(registerButton);

            add(panel);
        }

        private class LoginListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                User user = userManager.loginUser(username, password);
                if (user != null) {
                    dispose();
                    new ExpenseTracker(user).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid username or password.");
                }
            }
        }

        private class RegisterListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (userManager.registerUser(username, password)) {
                    JOptionPane.showMessageDialog(null, "Registration successful! Please login.");
                } else {
                    JOptionPane.showMessageDialog(null, "Username already exists.");
                }
            }
        }
    }

    public ExpenseTracker(User user) {
        this.currentUser = user;
        this.userManager = new UserManager();
        this.expenseManager = new ExpenseManager();
        loadUserExpenses();
        setupUI();
    }

    private void setupUI() {
        setTitle("Expense Tracker - User: " + currentUser.getUsername());
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new BorderLayout());

        // Top Panel for Input Fields
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Expense"));
        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        categoryField = new JTextField();
        amountField = new JTextField();
        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(new AddExpenseListener());

        inputPanel.add(new JLabel("Date (yyyy-MM-dd):"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(amountField);
        inputPanel.add(addButton);
        panel.add(inputPanel, BorderLayout.NORTH);

        // Table for Displaying Expenses
        String[] columns = {"Date", "Category", "Amount"};
        tableModel = new DefaultTableModel(columns, 0);
        expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel for Category Totals and Save
        JPanel bottomPanel = new JPanel();
        totalLabel = new JLabel("Total by Category: ");
        JButton totalButton = new JButton("View Totals");
        totalButton.addActionListener(new ViewTotalsListener());
        
        JButton saveButton = new JButton("Save Expenses");
        saveButton.addActionListener(new SaveExpensesListener());

        bottomPanel.add(totalLabel);
        bottomPanel.add(totalButton);
        bottomPanel.add(saveButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
    }

    private void loadUserExpenses() {
        String expenseFile = currentUser.getUsername() + "_expenses.ser";
        try {
            List<Expense> expenses = FileHandler.loadExpenses(expenseFile);
            for (Expense expense : expenses) {
                expenseManager.addExpense(expense);
                tableModel.addRow(new Object[]{expense.getDate(), expense.getCategory(), expense.getAmount()});
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous expenses found for " + currentUser.getUsername());
        }
    }

    private class AddExpenseListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateField.getText());
                String category = categoryField.getText();
                double amount = Double.parseDouble(amountField.getText());

                Expense expense = new Expense(date, category, amount);
                expenseManager.addExpense(expense);
                tableModel.addRow(new Object[]{date, category, amount});
                JOptionPane.showMessageDialog(null, "Expense added successfully!");

                dateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                categoryField.setText("");
                amountField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid input! Please check your entries.");
            }
        }
    }

    private class ViewTotalsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Map<String, Double> totals = expenseManager.getTotalByCategory();
            StringBuilder totalText = new StringBuilder("<html>Totals by Category:<br>");
            for (Map.Entry<String, Double> entry : totals.entrySet()) {
                totalText.append(entry.getKey()).append(": ").append(entry.getValue()).append("<br>");
            }
            totalText.append("</html>");
            totalLabel.setText(totalText.toString());
        }
    }

    private class SaveExpensesListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String expenseFile = currentUser.getUsername() + "_expenses.ser";
                FileHandler.saveExpenses(expenseManager.getExpenses(), expenseFile);
                JOptionPane.showMessageDialog(null, "Expenses saved successfully.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error saving expenses.");
            }
        }
    }
}

// Supporting Classes

class User implements Serializable {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public boolean validatePassword(String password) {
        return this.password.equals(password);
    }
}

class UserManager {
    private HashMap<String, User> users;

    public UserManager() {
        users = new HashMap<>();
        loadUsers();
    }

    public boolean registerUser(String username, String password) {
        if (users.containsKey(username)) return false;
        users.put(username, new User(username, password));
        saveUsers();
        return true;
    }

    public User loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.validatePassword(password)) return user;
        return null;
    }

    private void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("users.ser"))) {
            out.writeObject(users);
        } catch (IOException e) {
            System.out.println("Error saving users.");
        }
    }

    private void loadUsers() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("users.ser"))) {
            users = (HashMap<String, User>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing users found.");
        }
    }
}

class Expense implements Serializable {
    private Date date;
    private String category;
    private double amount;

    public Expense(Date date, String category, double amount) {
        this.date = date;
        this.category = category;
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }
}



class ExpenseManager {
    private List<Expense> expenses = new ArrayList<>();

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public Map<String, Double> getTotalByCategory() {
        Map<String, Double> totals = new HashMap<>();
        for (Expense expense : expenses) {
            String category = expense.getCategory();
            double amount = expense.getAmount();
            totals.put(category, totals.getOrDefault(category, 0.0) + amount);
        }
        return totals;
    }
}



class FileHandler {
    public static void saveExpenses(List<Expense> expenses, String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(expenses);
        }
    }

    public static List<Expense> loadExpenses(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (List<Expense>) in.readObject();
        }
    }
}

