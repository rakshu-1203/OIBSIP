import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class ATM {
    private String userId;
    private String userPin;
    private double balance;
    private ArrayList<String> history;

    public ATM(String userId, String userPin, double balance) {
        this.userId = userId;
        this.userPin = userPin;
        this.balance = balance;
        this.history = new ArrayList<>();
    }

    public boolean login(String id, String pin) {
        return this.userId.equals(id) && this.userPin.equals(pin);
    }

    public String getHistory() {
        if (history.isEmpty()) return "No Transaction yet.";
        StringBuilder sb = new StringBuilder();
        for (String h : history) sb.append(h).append("\n");
        return sb.toString();
    }

    public String withdraw(double amount) {
        if (amount <= 0) return "Enter valid amount!";
        if (amount <= balance) {
            balance -= amount;
            history.add("Withdrawn: ₹" + amount);
            return "Withdrawal successful. Current Balance: ₹" + balance;
        } else return "Insufficient Balance!";
    }

    public String deposit(double amount) {
        if (amount <= 0) return "Enter valid amount!";
        balance += amount;
        history.add("Deposited: ₹" + amount);
        return "Deposit successful. Current Balance: ₹" + balance;
    }

    public String transfer(double amount, ATM receiver) {
        if (amount <= 0) return "Enter valid amount!";
        if (amount <= balance) {
            balance -= amount;
            receiver.balance += amount;
            history.add("Transferred ₹" + amount + " to " + receiver.userId);
            receiver.history.add("Received ₹" + amount + " from " + this.userId);
            return "Transferred ₹" + amount + " to " + receiver.userId +
                    ". Current Balance: ₹" + balance;
        } else return "Insufficient Balance!";
    }
}

public class ATMInterface {
    private static Map<String, ATM> users = new HashMap<>();

    public static void main(String[] args) {
        // Predefined users
        users.put("user1", new ATM("user1", "1111", 5000));
        users.put("user2", new ATM("user2", "2222", 10000));
        users.put("user3", new ATM("user3", "3333", 1500));

        JFrame frame = new JFrame("ATM Interface");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true); // Window is resizable

        CardLayout cl = new CardLayout();
        JPanel panelContainer = new JPanel(cl);

        // --- LOGIN PANEL ---
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel loginTitle = new JLabel("ATM Login");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JTextField userField = new JTextField();
        userField.setPreferredSize(new Dimension(150, 30));
        JPasswordField pinField = new JPasswordField();
        pinField.setPreferredSize(new Dimension(150, 30));
        JButton loginBtn = new JButton("Login");
        loginBtn.setPreferredSize(new Dimension(120, 35));
        JLabel loginMsg = new JLabel("", SwingConstants.CENTER);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(loginTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        loginPanel.add(new JLabel("PIN:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(pinField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        loginPanel.add(loginBtn, gbc);

        gbc.gridy = 4;
        loginPanel.add(loginMsg, gbc);

        // --- MENU PANEL ---
        JPanel menuPanel = new JPanel(new GridBagLayout());
        GridBagConstraints mg = new GridBagConstraints();
        mg.insets = new Insets(10, 10, 10, 10);
        mg.fill = GridBagConstraints.NONE;
        mg.anchor = GridBagConstraints.CENTER;

        JLabel menuMsg = new JLabel("Welcome to ATM!");
        menuMsg.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JButton historyBtn = new JButton("Transaction History");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton depositBtn = new JButton("Deposit");
        JButton transferBtn = new JButton("Transfer");
        JButton logoutBtn = new JButton("Logout");

        // Set fixed sizes for all buttons
        JButton[] buttons = {historyBtn, withdrawBtn, depositBtn, transferBtn, logoutBtn};
        for (JButton b : buttons) b.setPreferredSize(new Dimension(180, 40));

        mg.gridx = 0; mg.gridy = 0; mg.gridwidth = 1;
        menuPanel.add(menuMsg, mg);

        int y = 1;
        for (JButton b : buttons) {
            mg.gridy = y++;
            menuPanel.add(b, mg);
        }

        panelContainer.add(loginPanel, "Login");
        panelContainer.add(menuPanel, "Menu");

        final ATM[] currentUser = new ATM[1];

        // ---- ACTIONS ----
        loginBtn.addActionListener(e -> {
            String id = userField.getText().trim();
            String pin = new String(pinField.getPassword()).trim();

            if (users.containsKey(id) && users.get(id).login(id, pin)) {
                currentUser[0] = users.get(id);
                menuMsg.setText("Welcome, " + id + "!");
                cl.show(panelContainer, "Menu");
                userField.setText("");
                pinField.setText("");
                loginMsg.setText("");
            } else {
                loginMsg.setText("Invalid ID or PIN!");
                loginMsg.setForeground(Color.RED);
            }
        });

        withdrawBtn.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog(frame, "Enter amount to withdraw:");
            if (amt != null && !amt.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amt);
                    JOptionPane.showMessageDialog(frame, currentUser[0].withdraw(amount));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Enter a valid number!");
                }
            }
        });

        depositBtn.addActionListener(e -> {
            String amt = JOptionPane.showInputDialog(frame, "Enter amount to deposit:");
            if (amt != null && !amt.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amt);
                    JOptionPane.showMessageDialog(frame, currentUser[0].deposit(amount));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Enter a valid number!");
                }
            }
        });

        transferBtn.addActionListener(e -> {
            String receiverId = JOptionPane.showInputDialog(frame, "Enter receiver's User ID:");
            if (receiverId != null && users.containsKey(receiverId)) {
                String amt = JOptionPane.showInputDialog(frame, "Enter amount to transfer:");
                try {
                    double amount = Double.parseDouble(amt);
                    JOptionPane.showMessageDialog(frame,
                            currentUser[0].transfer(amount, users.get(receiverId)));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Enter a valid number!");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Receiver not found!");
            }
        });

        historyBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, currentUser[0].getHistory()));
        logoutBtn.addActionListener(e -> {
            currentUser[0] = null;
            cl.show(panelContainer, "Login");
        });

        frame.add(panelContainer);
        cl.show(panelContainer, "Login");
        frame.setVisible(true);
    }
}

    
