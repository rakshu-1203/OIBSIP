import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

// --------- Main Class ---------
public class QuizApp {
    public static void main(String[] args) {
        new Login();
    }
}

// --------- DB Connection ---------
class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/internship"; // your DB name
    private static final String USER = "root"; // DB username
    private static final String PASS = "Rakshu@12"; // DB password

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

// --------- Login & Registration ---------
class Login {
    public Login() {
        JFrame frame = new JFrame("Login / Register");
        frame.setSize(400, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(10, 90, 150, 25);
        panel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(180, 90, 150, 25);
        panel.add(registerButton);

        // Login action
        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());
            int userId = authenticate(username, password);
            if (userId != -1) {
                JOptionPane.showMessageDialog(panel, "Login successful!");
                new Dashboard(username, userId);
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(panel, "Invalid credentials!");
            }
        });

        // Register action
        registerButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());
            String email = JOptionPane.showInputDialog(frame, "Enter your email:");
            if(username.isEmpty() || password.isEmpty() || email == null || email.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required!");
            } else {
                registerUser(username, password, email);
            }
        });

        frame.setVisible(true);
    }

    private int authenticate(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id FROM users WHERE username=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    private void registerUser(String username, String password, String email) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "User registered successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

// --------- Dashboard ---------
class Dashboard {
    JFrame frame;
    String username;
    int userId;

    public Dashboard(String username, int userId) {
        this.username = username;
        this.userId = userId;

        frame = new JFrame("Dashboard - " + username);
        frame.setSize(450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(null);
        frame.add(panel);

        JButton profileBtn = new JButton("Update Profile");
        profileBtn.setBounds(50, 50, 150, 25);
        panel.add(profileBtn);
        profileBtn.addActionListener(e -> new Profile(username, userId));

        JButton quizBtn = new JButton("Take Quiz");
        quizBtn.setBounds(50, 100, 150, 25);
        panel.add(quizBtn);
        quizBtn.addActionListener(e -> new Quiz(username, userId));

        JButton scoreBtn = new JButton("View Score");
        scoreBtn.setBounds(50, 150, 150, 25);
        panel.add(scoreBtn);
        scoreBtn.addActionListener(e -> {
            int score = getScoreFromDB(userId);
            JOptionPane.showMessageDialog(frame, "Your score: " + score);
        });

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(50, 200, 150, 25);
        panel.add(logoutBtn);
        logoutBtn.addActionListener(e -> {
            frame.dispose();
            new Login();
        });

        frame.setVisible(true);
    }

    private int getScoreFromDB(int userId) {
        int score = 0;
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT score FROM user_scores WHERE user_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) score = rs.getInt("score");
        } catch (SQLException e) { e.printStackTrace(); }
        return score;
    }
}

// --------- Profile ---------
class Profile {
    JFrame frame;
    String username;
    int userId;

    public Profile(String username, int userId) {
        this.username = username;
        this.userId = userId;

        frame = new JFrame("Profile - " + username);
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(null);
        frame.add(panel);

        JLabel passLabel = new JLabel("New Password:");
        passLabel.setBounds(10, 20, 120, 25);
        panel.add(passLabel);

        JPasswordField passField = new JPasswordField();
        passField.setBounds(140, 20, 165, 25);
        panel.add(passField);

        JButton updateBtn = new JButton("Update Password");
        updateBtn.setBounds(10, 60, 200, 25);
        panel.add(updateBtn);

        updateBtn.addActionListener(e -> {
            String newPass = new String(passField.getPassword());
            updatePassword(newPass);
        });

        frame.setVisible(true);
    }

    private void updatePassword(String newPass) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE users SET password=? WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newPass);
            stmt.setInt(2, userId);
            int rows = stmt.executeUpdate();
            if (rows > 0) JOptionPane.showMessageDialog(frame, "Password updated!");
            else JOptionPane.showMessageDialog(frame, "Update failed!");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}

// --------- Question Model ---------
class Question {
    private int id;
    private String question, option1, option2, option3, option4;
    private int correctOption;

    public int getId() {
    return id;
    }


    public Question(int id, String question, String option1, String option2,
                    String option3, String option4, int correctOption) {
        this.id = id;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
    }

    public String getQuestion() { return question; }
    public String getOption(int index) {
        switch(index) {
            case 0: return option1;
            case 1: return option2;
            case 2: return option3;
            case 3: return option4;
            default: return "";
        }
    }
    public int getCorrectOption() { return correctOption; }
}

// --------- Quiz Timer ---------
class QuizTimer {
    int time;
    JLabel label;
    Runnable callback;
    javax.swing.Timer swingTimer;

    public QuizTimer(int seconds, JLabel label, Runnable callback) {
        this.time = seconds;
        this.label = label;
        this.callback = callback;
    }

    public void start() {
        swingTimer = new javax.swing.Timer(1000, e -> {
            time--;
            label.setText("Time left: " + time);
            if(time <= 0){
                stopTimer();
                callback.run();
            }
        });
        swingTimer.start();
    }

    public void stopTimer() {
        if(swingTimer != null) swingTimer.stop();
    }
}

// --------- Quiz ---------
class Quiz {
    JFrame frame;
    String username;
    int userId;
    ArrayList<Question> questions = new ArrayList<>();
    int current = 0;
    int score = 0;
    QuizTimer timer;

    public Quiz(String username, int userId) {
        this.username = username;
        this.userId = userId;
        loadQuestionsFromDB();
        createGUI();
    }

    private void loadQuestionsFromDB() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM quiz_questions";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()){
                questions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("question"),
                    rs.getString("option1"),
                    rs.getString("option2"),
                    rs.getString("option3"),
                    rs.getString("option4"),
                    rs.getInt("correct_option")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void createGUI() {
        frame = new JFrame("Quiz - " + username);
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(null);
        frame.add(panel);

        JLabel questionLabel = new JLabel(questions.get(current).getQuestion());
        questionLabel.setBounds(20, 20, 450, 25);
        panel.add(questionLabel);

        JRadioButton[] radios = new JRadioButton[4];
        ButtonGroup group = new ButtonGroup();
        for(int i=0;i<4;i++){
            radios[i] = new JRadioButton(questions.get(current).getOption(i));
            radios[i].setBounds(20, 60 + i*30, 450, 25);
            group.add(radios[i]);
            panel.add(radios[i]);
        }

        JButton nextBtn = new JButton("Next");
        nextBtn.setBounds(20, 200, 100, 25);
        panel.add(nextBtn);

        JLabel timerLabel = new JLabel("Time left: 60");
        timerLabel.setBounds(350, 20, 120, 25);
        panel.add(timerLabel);

        timer = new QuizTimer(60, timerLabel, this::autoSubmit);
        timer.start();

        nextBtn.addActionListener(e -> {
            for(int i=0;i<4;i++){
                if(radios[i].isSelected() && (i+1)==questions.get(current).getCorrectOption())
                    score++;
                saveAnswerToDB(questions.get(current).getId(), i+1);

            }
            current++;
            if(current < questions.size()){
                questionLabel.setText(questions.get(current).getQuestion());
                for(int i=0;i<4;i++){
                    radios[i].setText(questions.get(current).getOption(i));
                    radios[i].setSelected(false);
                }
            } else finishQuiz();
        });

        frame.setVisible(true);
    }

    private void saveAnswerToDB(int questionId, int selectedOption) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO user_answers (user_id, question_id, selected_option) " +
                         "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE selected_option=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, questionId);
            stmt.setInt(3, selectedOption);
            stmt.setInt(4, selectedOption);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void autoSubmit() {
        saveScoreToDB();
        JOptionPane.showMessageDialog(frame, "Time over! Your score: " + score);
        frame.dispose();
    }

    private void finishQuiz() {
        timer.stopTimer();
        saveScoreToDB();
        JOptionPane.showMessageDialog(frame, "Quiz Finished! Your score: " + score);
        frame.dispose();
    }

    private void saveScoreToDB() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO user_scores (user_id, score) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE score=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, score);
            stmt.setInt(3, score);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
