/**
 * TEAM JAVA RICE
 */
package com.mycompany.electbudzpt;

import com.mycompany.electbudzpt.DBUtil;

import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElectBudzPT {

    // REPLACEMENT: Removed hardcoded candidate map
    // Replaced with dynamic loading from database
    // Add this class-level variable
    private static Map<String, LinkedHashMap<String, Integer>> positionVoteCount = new LinkedHashMap<>();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/elect_budz";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password"; // Replace with your actual password
    private static final String ADMIN_PASSWORD = "Admin123";
    public static String currentUsername; // store logged-in voter's username

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void loadCandidatesFromDatabase() {
        positionVoteCount.clear();
        try (Connection conn = getConnection()) {
            String query = "SELECT name, type, votes FROM candidates";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type");
                int votes = rs.getInt("votes");

                positionVoteCount.putIfAbsent(type, new LinkedHashMap<>());
                positionVoteCount.get(type).put(name, votes);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // NEW: Load candidates from database   
    private static void loadCandidatesFromDB() {
        positionVoteCount.clear();
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT id, name, type, votes FROM candidates ORDER BY type, name";  // include id if needed
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    // int id = rs.getInt("id");  // You can omit if unused
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    int votes = rs.getInt("votes");

                    positionVoteCount
                            .computeIfAbsent(type, k -> new LinkedHashMap<>())
                            .put(name, votes);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading candidates: " + e.getMessage());
        }
    }

    public static boolean isCandidateExists(String name, String position) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT COUNT(*) FROM candidates WHERE name = ? AND type = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, position);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // NEW: Method to authenticate voters and store the current voter's username
    private static boolean authenticateVoter(String username, String password) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM voters WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    currentUsername = rs.getString("username"); // Save current user
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Login failed: " + e.getMessage());
            return false;
        }
    }

    static class CandidateData {

        int id;
        String name;

        CandidateData(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static final Map<String, List<String>> candidatesByType = new LinkedHashMap<>();

    static {
        candidatesByType.put("Governor", Arrays.asList("Recto, Rosa Vilma Tuazon S.", "Leviste, Jose Antonio S."));
        candidatesByType.put("Vice Governor", Arrays.asList("Mandanas, Hermilando I.", "Manzano, Luis Philippe S."));
        candidatesByType.put("Provincial Board Member", Arrays.asList("Balba, Rodolfo M.", "Corona, Alfredo C.", "Macalintal, Dennis C."));
        candidatesByType.put("Mayor", Arrays.asList("Ilagan, Janet M.", "Collantes, Nelson P.", "Africa, Eric B."));
        candidatesByType.put("Vice Mayor", Arrays.asList("Trinidad Jr., Herminigildo G.", "Lopez, Camille Angeline M.", "Ilagan, Jay M."));
        candidatesByType.put("City/Town Councilor", Arrays.asList(
                "Dimaano, Ferdinand L.", "Laqui, Karen Joy A.", "Malabag, Rowell B.",
                "Del Mundo, Herwin D.", "De Ocampo, Lemuel V.", "Calinisan, Lourdes O.",
                "Vergara, Pepito D.", "Caraan-Laqui, Merlyn L.", "Santos, Maria S.", "Reyes, Pedro R.",
                "Lopez, Ana L.", "Garcia, Lito G.", "Mendoza, Rico M.", "Perez, Carla P.",
                "Villanueva, Marco V."
        ));
    }

    // Utility method to create candidate lists with vote count initialized to 0
    private static LinkedHashMap<String, Integer> createCandidateList(String... candidates) {
        LinkedHashMap<String, Integer> candidateMap = new LinkedHashMap<>();
        for (String candidate : candidates) {
            candidateMap.put(candidate, 0); // Initialize each candidate with 0 votes
        }
        return candidateMap;
    }
    // Array implementation for each Positions
    private static final String[] positions = {
        "Governor", "Vice Governor", "Provincial Board Member", "Mayor", "Vice Mayor", "City/Town Councilor"
    };

    private static void initializeDefaultCandidates() {

        positionVoteCount.clear();
        try (Connection conn = getConnection()) {
            String query = "SELECT name, type, votes FROM candidates";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type");
                int votes = rs.getInt("votes");

                positionVoteCount.putIfAbsent(type, new LinkedHashMap<>());
                positionVoteCount.get(type).put(name, votes);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Map<String, List<String>> candidatesByType = new LinkedHashMap<>();
        candidatesByType.put("Governor", Arrays.asList("Recto, Rosa Vilma Tuazon S.", "Leviste, Jose Antonio S."));
        candidatesByType.put("Vice Governor", Arrays.asList("Mandanas, Hermilando I.", "Manzano, Luis Philippe S."));
        candidatesByType.put("Provincial Board Member", Arrays.asList("Balba, Rodolfo M.", "Corona, Alfredo C.", "Macalintal, Dennis C."));
        candidatesByType.put("Mayor", Arrays.asList("Ilagan, Janet M.", "Collantes, Nelson P.", "Africa, Eric B."));
        candidatesByType.put("Vice Mayor", Arrays.asList("Trinidad Jr., Herminigildo G.", "Lopez, Camille Angeline M.", "Ilagan, Jay M."));
        candidatesByType.put("City/Town Councilor", Arrays.asList(
                "Dimaano, Ferdinand L.", "Laqui, Karen Joy A.", "Malabag, Rowell B.",
                "Del Mundo, Herwin D.", "De Ocampo, Lemuel V.", "Calinisan, Lourdes O.",
                "Vergara, Pepito D.", "Caraan-Laqui, Merlyn L.", "Santos, Maria S.", "Reyes, Pedro R.",
                "Lopez, Ana L.", "Garcia, Lito G.", "Mendoza, Rico M.", "Perez, Carla P.",
                "Villanueva, Marco V."
        ));

        try (Connection conn = DBUtil.getConnection()) {
            // First, check if candidates already exist
            String checkSql = "SELECT COUNT(*) FROM candidates";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql); ResultSet rs = checkStmt.executeQuery()) {

                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Candidates already exist. Skipping insertion.");
                    return; // Exit the method early
                }
            }

            // If no candidates exist, proceed with insertion
            String insertSql = "INSERT INTO candidates (name, type, votes) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                for (Map.Entry<String, List<String>> entry : candidatesByType.entrySet()) {
                    String position = entry.getKey();
                    for (String candidate : entry.getValue()) {
                        System.out.println("Inserting candidate: " + candidate + " for position: " + position);
                        stmt.setString(1, candidate);
                        stmt.setString(2, position);
                        stmt.setInt(3, 0); // initial votes = 0
                        stmt.addBatch();
                    }
                }

                int[] results = stmt.executeBatch();

                // Check how many inserts succeeded
                int totalInserted = 0;
                for (int count : results) {
                    if (count == PreparedStatement.SUCCESS_NO_INFO || count > 0) {
                        totalInserted++;
                    }
                }
                System.out.println("Total candidates inserted: " + totalInserted);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error initializing candidates: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // NEW: Registration screen for new voters
    private static void showRegistrationScreen() {
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JTextField();
        JTextField contactField = new JTextField();
        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField,
            "Contact No:", contactField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Register", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String contact = contactField.getText().trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username and password are required.");
                return;
            }

            try (Connection conn = DBUtil.getConnection()) {
                String sql = "INSERT INTO voters (username, password, contact_number) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, contact);
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Registration successful.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Registration failed: " + e.getMessage());
            }
        }
        showLoginScreen();
    }

    private static boolean hasAlreadyVoted(String username) {
        boolean voted = false;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT has_voted FROM voters WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                voted = rs.getBoolean("has_voted");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return voted;
    }

    private static void markVoterAsVoted(String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String update = "UPDATE voters SET has_voted = TRUE WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(update);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void showLoginScreen() {
        JFrame loginFrame = new JFrame("ElectBudz - Voter Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(500, 650);
        loginFrame.setLocationRelativeTo(null); // Center the window
        loginFrame.setLayout(new BorderLayout());
        loginFrame.getContentPane().setBackground(Color.WHITE);

        // Set the application icon
        String iconPath = "C:/Users/erich/OneDrive/Documents/NetBeansProjects/ElectBudz/src/main/java/com/mycompany/electbudz/ElectBudz Logo/Elect Budz Logo.png";
        ImageIcon icon = new ImageIcon(iconPath);
        loginFrame.setIconImage(icon.getImage());

        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(Color.WHITE);

        // App Logo
        String imagePath = "C:/Users/erich/OneDrive/Documents/NetBeansProjects/ElectBudz/src/main/java/com/mycompany/electbudz/ElectBudz Logo/App logo.png";
        ImageIcon imageIcon = new ImageIcon(imagePath);
        Image resizedImage = imageIcon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(resizedImage));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username and Password Panel
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        // Buttons Panel
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // Admin Panel Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        JButton adminButton = new JButton("Admin Panel");

        adminButton.setBackground(Color.decode("#0032A0"));
        adminButton.setForeground(Color.WHITE);
        adminButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        adminButton.setFocusPainted(false);
        adminButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        adminButton.addActionListener(e -> {
            loginFrame.dispose();
            promptAdminPassword();
        });
        bottomPanel.add(adminButton);

        // Add components to mainPanel
        mainPanel.add(imageLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(buttonPanel);

        loginFrame.add(mainPanel, BorderLayout.CENTER);
        loginFrame.add(bottomPanel, BorderLayout.SOUTH); // ← FIXED: Ensure Admin Panel is visible
        loginFrame.setVisible(true);

        // Action Listeners
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (authenticateVoter(username, password)) {
                if (hasAlreadyVoted(username)) {
                    JOptionPane.showMessageDialog(loginFrame, "You have already voted. You cannot vote again.", "Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Login successful. Proceeding to vote.");
                    loginFrame.dispose();
                    loadCandidatesFromDB();
                    showVotingScreen(); // Pass username if needed to track the vote
                }
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid login.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            loginFrame.dispose();
            showRegistrationScreen();
        });
    }

    private static void promptAdminPassword() {
        JDialog passwordDialog = new JDialog((JFrame) null, "Admin Password", true);
        passwordDialog.setSize(450, 250);
        passwordDialog.setLayout(new GridBagLayout());
        passwordDialog.getContentPane().setBackground(new Color(250, 250, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Set the application icon
        String iconPath = "C:/Users/erich/OneDrive/Documents/NetBeansProjects/ElectBudz/src/main/java/com/mycompany/electbudz/ElectBudz Logo/Elect Budz Logo.png";
        ImageIcon icon = new ImageIcon(iconPath);
        passwordDialog.setIconImage(icon.getImage());

        // Title and Password Field
        JLabel titleLabel = new JLabel("Enter Admin Password", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(45, 45, 45));
        gbc.gridy = 0;
        passwordDialog.add(titleLabel, gbc);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordField.setBackground(new Color(240, 240, 240));
        passwordField.setForeground(Color.BLACK);
        gbc.gridy = 1;
        passwordDialog.add(passwordField, gbc);

        // Show Password Checkbox
        JCheckBox showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        showPasswordCheckBox.setBackground(new Color(250, 250, 250));
        showPasswordCheckBox.setForeground(new Color(80, 80, 80));
        showPasswordCheckBox.addActionListener(e -> passwordField.setEchoChar(showPasswordCheckBox.isSelected() ? (char) 0 : '*'));
        gbc.gridy = 2;
        passwordDialog.add(showPasswordCheckBox, gbc);

        // OK and Cancel Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(250, 250, 250));
        JButton okButton = new JButton("OK");
        okButton.setBackground(Color.decode("#0032A0"));
        okButton.setForeground(Color.WHITE);
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okButton.addActionListener(e -> checkPassword(passwordDialog, passwordField));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.decode("#BF0D3E"));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.addActionListener(e -> {
            passwordDialog.dispose();
            showLoginScreen();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        gbc.gridy = 3;
        passwordDialog.add(buttonPanel, gbc);

        passwordField.addActionListener(e -> okButton.doClick()); // Trigger OK button on Enter

        passwordDialog.setLocationRelativeTo(null);
        passwordDialog.setVisible(true);
    }

    // checkPassword method
    private static void checkPassword(JDialog passwordDialog, JPasswordField passwordField) {
        String password = new String(passwordField.getPassword());

        if (password.equals(ADMIN_PASSWORD)) {
            passwordDialog.dispose();
            showAdminOptionSelectionScreen(); // Show option selection screen for a new election

            // Check if any votes have been cast
            boolean hasVotes = positionVoteCount.values().stream()
                    .flatMap(candidates -> candidates.values().stream())
                    .anyMatch(voteCount -> voteCount > 0);
        } else {
            JOptionPane.showMessageDialog(passwordDialog,
                    //design alert mess
                    "<html><div style='text-align: center;'>"
                    + "<span style='color: #BF0D3E; font-size: 14px;'>Incorrect password!</span><br>"
                    + "Please try again."
                    + "</div></html>",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText(""); // Clear the password field for retry
        }
    }

    private static void showAdminOptionSelectionScreen() {
        JFrame adminOptionFrame = new JFrame("ElectBudz - Admin Panel");
        adminOptionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        adminOptionFrame.setSize(500, 750);
        adminOptionFrame.setLayout(new GridBagLayout());
        adminOptionFrame.getContentPane().setBackground(Color.WHITE);

        // Set the application icon
        String iconPath = "C:/Users/erich/OneDrive/Documents/NetBeansProjects/ElectBudz/src/main/java/com/mycompany/electbudz/ElectBudz Logo/Elect Budz Logo.png";
        ImageIcon icon = new ImageIcon(iconPath);
        adminOptionFrame.setIconImage(icon.getImage());

        // Load and resize the image
        String imagePath = "C:/Users/erich/OneDrive/Documents/NetBeansProjects/ElectBudz/src/main/java/com/mycompany/electbudz/ElectBudz Logo/Elect Budz Logo v.1.png";
        ImageIcon imageIcon = new ImageIcon(imagePath);
        Image img = imageIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(img));

        // Title label
        JLabel promptLabel = new JLabel("Admin Panel", SwingConstants.CENTER);
        promptLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        promptLabel.setForeground(Color.BLACK);

        // Title label
        JLabel promptOption = new JLabel("Choose an option:", SwingConstants.CENTER);
        promptOption.setFont(new Font("Segoe UI", Font.BOLD, 16));
        promptOption.setForeground(Color.BLACK);

        // Buttons
        JButton manageCandidatesButton = createButton("Manage Candidates", "#0032A0", Color.WHITE);
        manageCandidatesButton.addActionListener(e -> {
            adminOptionFrame.dispose();
            showAdminCandidateScreen();
        });

        JButton setVoterCountButton = createButton("Registered Voters", "#FED141", Color.BLACK);
        setVoterCountButton.addActionListener(e -> {
            if (positionVoteCount.isEmpty()) {
                JOptionPane.showMessageDialog(adminOptionFrame,
                        //Design for Alert message
                        "<html><div style='text-align: center;'>"
                        + "<span style='color: #BF0D3E;'>No candidates added!</span><br>"
                        + "Please add at least one candidate before setting the voter count."
                        + "</div></html>",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {

                adminOptionFrame.dispose();
                showAdminRegisteredVoterScreen();
            }
        });

        // View Results button (newly added)
        JButton viewResultsButton = createButton("View Results", "#3CB371", Color.WHITE); // Medium Sea Green
        viewResultsButton.addActionListener(e -> {
            adminOptionFrame.dispose();
            showResultsScreen(); // Existing method to show election results
        });

        JButton startElectionButton = createButton("Back to Main Menu", "#BF0D3E", Color.WHITE);
        startElectionButton.addActionListener(e -> {
            {
                adminOptionFrame.dispose(); // Close the current frame
                showLoginScreen(); // Show the login screen
            }
        });

        // Layout constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 15, 15, 15);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Add components to the frame in order
        gbc.gridy = 0;
        adminOptionFrame.add(imageLabel, gbc);
        gbc.gridy = 1;
        adminOptionFrame.add(promptLabel, gbc);
        gbc.gridy = 2;
        adminOptionFrame.add(promptOption, gbc);
        gbc.gridy = 3;
        adminOptionFrame.add(manageCandidatesButton, gbc);
        gbc.gridy = 4;
        adminOptionFrame.add(setVoterCountButton, gbc);
        gbc.gridy = 5;
        adminOptionFrame.add(viewResultsButton, gbc);
        gbc.gridy = 6;
        adminOptionFrame.add(startElectionButton, gbc);

        // Center the frame on the screen and make it visible
        adminOptionFrame.setLocationRelativeTo(null);
        adminOptionFrame.setVisible(true);
    }

    // Helper method to create a button with custom styling
    private static JButton createButton(String text, String bgColorHex, Color textColor) {
        JButton button = new JButton(text);
        button.setBackground(Color.decode(bgColorHex));
        button.setForeground(textColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(300, 80));
        button.setFocusPainted(false);
        return button;
    }

    // Method for showAdminCandidateScreen
    private static void showAdminCandidateScreen() {
        JFrame adminCandidateFrame = new JFrame("ElectBudz - Manage Candidates");
        adminCandidateFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        adminCandidateFrame.setSize(1000, 700);
        adminCandidateFrame.setLayout(new BorderLayout(10, 10));

// Set the application icon
        String iconPath = "C:/Users/erich/OneDrive/Documents/NetBeansProjects/ElectBudz/src/main/java/com/mycompany/electbudz/ElectBudz Logo/Elect Budz Logo.png";
        ImageIcon icon = new ImageIcon(iconPath);
        adminCandidateFrame.setIconImage(icon.getImage());

// Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(25, 10, 150, 10));

        String imagePath = "C:/Users/erich/OneDrive/Documents/NetBeansProjects/ElectBudz/src/main/java/com/mycompany/electbudz/ElectBudz Logo/Elect Budz Logo v.1.png";
        ImageIcon imageIcon = new ImageIcon(imagePath);
        Image img = imageIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(img));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(imageLabel);

        JLabel positionPromptLabel = new JLabel("Select position for the candidate:", SwingConstants.CENTER);
        positionPromptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(positionPromptLabel);

        JComboBox<String> positionComboBox = new JComboBox<>(positions);
        positionComboBox.setMaximumSize(new Dimension(300, 30));
        positionComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(positionComboBox);

        JTextField candidateField = new JTextField();
        candidateField.setMaximumSize(new Dimension(300, 30));
        candidateField.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(candidateField);

        JButton addCandidateButton = new JButton("Add Candidate");
        addCandidateButton.setPreferredSize(new Dimension(150, 50));
        addCandidateButton.setMaximumSize(new Dimension(150, 50));
        addCandidateButton.setBackground(new Color(34, 139, 34));
        addCandidateButton.setForeground(Color.WHITE);
        addCandidateButton.setFocusPainted(false);
        addCandidateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        inputPanel.add(addCandidateButton);

        JButton doneButton = new JButton("Done");
        doneButton.setPreferredSize(new Dimension(150, 50));
        doneButton.setMaximumSize(new Dimension(150, 50));
        doneButton.setBackground(new Color(200, 50, 50));
        doneButton.setForeground(Color.WHITE);
        doneButton.setFocusPainted(false);
        doneButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(doneButton);

        adminCandidateFrame.add(inputPanel, BorderLayout.WEST);

        JPanel displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(displayPanel);
        adminCandidateFrame.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(Color.decode("#0032A0"));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshPanel.add(refreshButton);
        adminCandidateFrame.add(refreshPanel, BorderLayout.NORTH);

// Using a final wrapper to allow assignment in inner lambdas
        final Runnable[] updateDisplayPanel = new Runnable[1];

        updateDisplayPanel[0] = () -> {
            displayPanel.removeAll();

            positionVoteCount.forEach((position, candidates) -> {
                JPanel groupPanel = new JPanel(new BorderLayout());
                groupPanel.setBorder(BorderFactory.createLineBorder(new Color(192, 192, 192), 1));
                groupPanel.setBackground(Color.WHITE);

                JPanel positionPanel = new JPanel(new BorderLayout());
                positionPanel.setBackground(new Color(245, 245, 245));
                positionPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(192, 192, 192)));

                JLabel positionLabel = new JLabel(position);
                positionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                positionLabel.setForeground(new Color(70, 130, 180));
                positionLabel.setHorizontalAlignment(SwingConstants.CENTER);

                positionPanel.add(positionLabel, BorderLayout.CENTER);
                groupPanel.add(positionPanel, BorderLayout.NORTH);

                JPanel candidatesPanel = new JPanel();
                candidatesPanel.setLayout(new BoxLayout(candidatesPanel, BoxLayout.Y_AXIS));
                candidatesPanel.setBackground(Color.WHITE);

                candidates.keySet().stream().sorted().forEach(candidate -> {
                    JPanel candidatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    candidatePanel.setBackground(new Color(245, 245, 245));
                    candidatePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(192, 192, 192)));

                    JLabel candidateLabel = new JLabel(candidate);
                    candidateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    candidateLabel.setPreferredSize(new Dimension(200, 20));
                    candidatePanel.add(candidateLabel);

                    JButton editButton = new JButton("Edit");
                    editButton.setBackground(Color.decode("#FED141"));
                    editButton.setForeground(Color.BLACK);
                    editButton.setFocusPainted(false);
                    editButton.addActionListener(e -> {
                        String newCandidateName = JOptionPane.showInputDialog(adminCandidateFrame, "Edit candidate name:", candidate);
                        if (newCandidateName != null && !newCandidateName.trim().isEmpty()) {
                            try (Connection conn = getConnection()) {
                                String checkQuery = "SELECT COUNT(*) FROM candidates WHERE name = ? AND type = ?";
                                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                                checkStmt.setString(1, newCandidateName);
                                checkStmt.setString(2, position);
                                ResultSet rs = checkStmt.executeQuery();
                                rs.next();
                                if (rs.getInt(1) > 0) {
                                    JOptionPane.showMessageDialog(adminCandidateFrame, "Candidate already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                                    return;
                                }
                                String updateQuery = "UPDATE candidates SET name = ? WHERE name = ? AND type = ?";
                                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                                updateStmt.setString(1, newCandidateName);
                                updateStmt.setString(2, candidate);
                                updateStmt.setString(3, position);
                                updateStmt.executeUpdate();

                                JOptionPane.showMessageDialog(adminCandidateFrame, "Candidate updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                loadCandidatesFromDatabase();
                                updateDisplayPanel[0].run();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(adminCandidateFrame, "Database error.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });

                    JButton deleteButton = new JButton("Delete");
                    deleteButton.setBackground(Color.decode("#BF0D3E"));
                    deleteButton.setForeground(Color.WHITE);
                    deleteButton.setFocusPainted(false);
                    deleteButton.addActionListener(e -> {
                        int confirm = JOptionPane.showConfirmDialog(adminCandidateFrame, "Delete: " + candidate + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            try (Connection conn = getConnection()) {
                                String deleteQuery = "DELETE FROM candidates WHERE name = ? AND type = ?";
                                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                                deleteStmt.setString(1, candidate);
                                deleteStmt.setString(2, position);
                                deleteStmt.executeUpdate();

                                JOptionPane.showMessageDialog(adminCandidateFrame, "Candidate deleted!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                                loadCandidatesFromDatabase();
                                updateDisplayPanel[0].run();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(adminCandidateFrame, "Database error.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });

                    candidatePanel.add(editButton);
                    candidatePanel.add(deleteButton);
                    candidatesPanel.add(candidatePanel);
                });

                groupPanel.add(candidatesPanel, BorderLayout.CENTER);
                displayPanel.add(groupPanel);
            });

            displayPanel.revalidate();
            displayPanel.repaint();
        };

        refreshButton.addActionListener(e -> updateDisplayPanel[0].run());

        addCandidateButton.addActionListener(e -> {
            String selectedPosition = (String) positionComboBox.getSelectedItem();
            String candidateName = candidateField.getText().trim();
            if (candidateName.isEmpty()) {
                JOptionPane.showMessageDialog(adminCandidateFrame, "Candidate name is empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = getConnection()) {
                String checkQuery = "SELECT COUNT(*) FROM candidates WHERE name = ? AND type = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, candidateName);
                checkStmt.setString(2, selectedPosition);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(adminCandidateFrame, "Candidate already exists.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String insertQuery = "INSERT INTO candidates (name, type, votes) VALUES (?, ?, 0)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, candidateName);
                insertStmt.setString(2, selectedPosition);
                insertStmt.executeUpdate();

                candidateField.setText("");
                JOptionPane.showMessageDialog(adminCandidateFrame, "Candidate added!", "Success", JOptionPane.INFORMATION_MESSAGE);

                loadCandidatesFromDatabase();
                updateDisplayPanel[0].run();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(adminCandidateFrame, "Database error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        candidateField.addActionListener(e -> addCandidateButton.doClick());

        doneButton.addActionListener(e -> {
            adminCandidateFrame.dispose();
            showAdminOptionSelectionScreen();
        });

        updateDisplayPanel[0].run();
        adminCandidateFrame.setLocationRelativeTo(null);
        adminCandidateFrame.setVisible(true);
    }

    // Utility method to reload voter data
    private static void loadVoterData(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing rows
        try (Connection conn = DBUtil.getConnection()) {
            // Include password in the SELECT query
            String sql = "SELECT id, username, contact_number, password, has_voted FROM voters";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String contact = rs.getString("contact_number");
                String password = rs.getString("password");
                boolean hasVoted = rs.getBoolean("has_voted");
                model.addRow(new Object[]{id, username, contact, password, hasVoted ? "Yes" : "No"});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading voters: " + e.getMessage());
        }
    }

    private static void showAdminRegisteredVoterScreen() {
        JFrame voterCountFrame = new JFrame("ElectBudz - Registered Voters");
        voterCountFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        voterCountFrame.setSize(600, 400);
        voterCountFrame.setLayout(new BorderLayout());
        voterCountFrame.getContentPane().setBackground(new Color(245, 245, 245));

        // Set icon
        String iconPath = "C:/Users/erich/OneDrive/Documents/NetBeansProjects/ElectBudz/src/main/java/com/mycompany/electbudz/ElectBudz Logo/Elect Budz Logo.png";
        ImageIcon icon = new ImageIcon(iconPath);
        voterCountFrame.setIconImage(icon.getImage());

        // Title
        JLabel titleLabel = new JLabel("Registered Voters", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 51, 102));
        voterCountFrame.add(titleLabel, BorderLayout.NORTH);

        // Table model and JTable
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Username", "Contact Number", "Password", "Voted?"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        voterCountFrame.add(scrollPane, BorderLayout.CENTER);

        // Load data from database
        loadVoterData(model);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton backButton = new JButton("Back");

        // Style buttons
        editButton.setBackground(Color.ORANGE);
        deleteButton.setBackground(Color.RED);
        backButton.setBackground(Color.decode("#0032A0"));
        editButton.setForeground(Color.WHITE);
        deleteButton.setForeground(Color.WHITE);
        backButton.setForeground(Color.WHITE);

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        voterCountFrame.add(buttonPanel, BorderLayout.SOUTH);

        // Edit button action
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) model.getValueAt(selectedRow, 0);
                String currentUsername = model.getValueAt(selectedRow, 1).toString();
                String currentContact = model.getValueAt(selectedRow, 2).toString();

                String newUsername = JOptionPane.showInputDialog("Enter new username:", currentUsername);
                if (newUsername == null || newUsername.trim().isEmpty()) {
                    return;
                }

                String newContact = JOptionPane.showInputDialog("Enter new contact number:", currentContact);
                if (newContact == null || newContact.trim().isEmpty()) {
                    return;
                }

                try (Connection conn = DBUtil.getConnection()) {
                    String sql = "UPDATE voters SET username = ?, contact_number = ? WHERE id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, newUsername.trim());
                    stmt.setString(2, newContact.trim());
                    stmt.setInt(3, id);
                    int updated = stmt.executeUpdate();

                    if (updated > 0) {
                        loadVoterData(model); // Refresh table
                        JOptionPane.showMessageDialog(null, "Voter updated successfully!");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error updating voter: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a voter to edit.");
            }
        });

        // Delete button action
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) model.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this voter?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection conn = DBUtil.getConnection()) {
                        String sql = "DELETE FROM voters WHERE id = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, id);
                        int deleted = stmt.executeUpdate();

                        if (deleted > 0) {
                            loadVoterData(model); // Refresh table
                            JOptionPane.showMessageDialog(null, "Voter deleted successfully!");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error deleting voter: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a voter to delete.");
            }
        });

        // Back button action
        backButton.addActionListener(e -> {
            voterCountFrame.dispose();
            showAdminOptionSelectionScreen();
        });

        voterCountFrame.setLocationRelativeTo(null);
        voterCountFrame.setVisible(true);
    }

    // Entry point (edited)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            loadCandidatesFromDB();   // Load from DB
            initializeDefaultCandidates(); // Make sure this is called!
            showLoginScreen();        // Prompt login first

        });
    }

    // Method for Voting Screen
    private static void showVotingScreen() {
        JFrame votingFrame = new JFrame("ElectBudz - Voting - " + currentUsername);
        votingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        votingFrame.setSize(1000, 800);

        // Set the application icon
        String iconPath = "C:/Users/erich/OneDrive/Documents/NetBeansProjects/ElectBudz/src/main/java/com/mycompany/electbudz/ElectBudz Logo/Elect Budz Logo.png";
        ImageIcon icon = new ImageIcon(iconPath);
        votingFrame.setIconImage(icon.getImage());

        // Main panel to hold all components
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(220, 240, 255)); // Light blue background

        // Title
        JLabel promptLabel = new JLabel("Hello " + currentUsername + "! Please Vote Wisely!");
        promptLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        promptLabel.setForeground(new Color(0, 102, 204)); // Blue color for title
        promptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        promptLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        mainPanel.add(promptLabel);

        // Center panel to hold the two columns
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 2, 100, 0)); // 100 px horizontal gap
        centerPanel.setBackground(new Color(220, 240, 255));

        // Left column panel (Governor, Vice Governor, Provincial Board Member, Mayor)
        JPanel leftColumnPanel = new JPanel();
        leftColumnPanel.setLayout(new BoxLayout(leftColumnPanel, BoxLayout.Y_AXIS));
        leftColumnPanel.setBackground(new Color(220, 240, 255));

        // Right column panel (Remaining positions)
        JPanel rightColumnPanel = new JPanel();
        rightColumnPanel.setLayout(new BoxLayout(rightColumnPanel, BoxLayout.Y_AXIS));
        rightColumnPanel.setBackground(new Color(220, 240, 255));

        // Store selected candidates for each position
        LinkedHashMap<String, LinkedHashMap<String, JCheckBox>> positionGroups = new LinkedHashMap<>();

        // Define the desired order of positions
        List<String> positionOrder = Arrays.asList(
                "Governor",
                "Vice Governor",
                "Provincial Board Member",
                "Mayor",
                "Vice Mayor",
                "City/Town Councilor"
        );

        for (String position : positionOrder) {
            if (!positionVoteCount.containsKey(position)) {
                continue; // Skip if position is not present
            }
            LinkedHashMap<String, Integer> candidates = positionVoteCount.get(position);
            JPanel targetColumn = (position.equals("Governor") || position.equals("Vice Governor")
                    || position.equals("Provincial Board Member") || position.equals("Mayor"))
                    ? leftColumnPanel : rightColumnPanel;

            // Create a sub-panel for the position
            JPanel positionPanel = new JPanel();
            positionPanel.setLayout(new BoxLayout(positionPanel, BoxLayout.Y_AXIS));
            positionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            positionPanel.setBackground(new Color(220, 240, 255));
            positionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            // Position label
            JLabel positionLabel = new JLabel(position);
            positionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            positionLabel.setForeground(new Color(70, 130, 180));
            positionPanel.add(positionLabel);

            int maxVotes = switch (position) {
                case "Governor", "Vice Governor", "Mayor", "Vice Mayor", "Provincial Board Member" ->
                    1;
                case "City/Town Councilor" ->
                    10;
                default ->
                    Integer.MAX_VALUE;
            };

            JLabel indicatorLabel = new JLabel(position.equals("City/Town Councilor")
                    ? "Choose maximum of " + maxVotes + " City/Town Councilors"
                    : "Choose " + maxVotes + " " + position);
            indicatorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            indicatorLabel.setForeground(new Color(100, 100, 100));
            positionPanel.add(indicatorLabel);

            LinkedHashMap<String, JCheckBox> checkBoxes = new LinkedHashMap<>();

            candidates.keySet().stream().sorted().forEach(candidate -> {
                JCheckBox checkBox = new JCheckBox(candidate);
                checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                checkBox.setBackground(new Color(220, 240, 255));
                checkBox.setAlignmentX(Component.LEFT_ALIGNMENT);

                if (position.equals("Governor") || position.equals("Vice Governor")
                        || position.equals("Mayor") || position.equals("Vice Mayor") || position.equals("Provincial Board Member")) {
                    checkBox.addItemListener(e -> {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            checkBoxes.forEach((otherCandidate, otherCheckBox) -> {
                                if (otherCheckBox != checkBox) {
                                    otherCheckBox.setSelected(false);
                                }
                            });
                        }
                    });
                }

                checkBoxes.put(candidate, checkBox);
                positionPanel.add(Box.createVerticalStrut(10));
                positionPanel.add(checkBox);
            });

            positionGroups.put(position, checkBoxes);
            targetColumn.add(positionPanel);
        }

        centerPanel.add(leftColumnPanel);
        centerPanel.add(rightColumnPanel);

        // Wrapper panel to center the two-column layout
        JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); // Center-align the columns
        wrapperPanel.setBackground(new Color(220, 240, 255));
        wrapperPanel.add(centerPanel);

        mainPanel.add(wrapperPanel);

        // Submit Button
        JButton submitButton = new JButton("Submit Votes");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        submitButton.setForeground(Color.WHITE);
        submitButton.setBackground(new Color(34, 139, 34)); // Green button
        submitButton.setFocusPainted(false);
        submitButton.setPreferredSize(new Dimension(200, 50));
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        submitButton.addActionListener(e -> {
            LinkedHashMap<String, java.util.List<String>> votes = new LinkedHashMap<>();
            boolean validVotes = true;

            for (var entry : positionGroups.entrySet()) {
                String position = entry.getKey();
                LinkedHashMap<String, JCheckBox> checkBoxes = entry.getValue();

                java.util.List<String> selectedCandidates = new java.util.ArrayList<>();
                checkBoxes.forEach((candidate, checkBox) -> {
                    if (checkBox.isSelected()) {
                        selectedCandidates.add(candidate);
                    }
                });

                int maxVotes;
                if (position.equals("Governor") || position.equals("Vice Governor")
                        || position.equals("Mayor") || position.equals("Vice Mayor") || (position.equals("Provincial Board Member"))) {
                    maxVotes = 1;
                } else if (position.equals("City/Town Councilor")) {
                    maxVotes = 10;
                } else {
                    maxVotes = Integer.MAX_VALUE; // Default: No limit
                }

                if (selectedCandidates.size() > maxVotes) {
                    JOptionPane.showMessageDialog(
                            votingFrame,
                            "<html><div style='text-align: left;'>"
                            + "<span style='color: #BF0D3E; font-size: 14px;'>"
                            + "You can select up to " + maxVotes + " candidate" + (maxVotes > 1 ? "s" : "") + " for " + position + "."
                            + "</span></div></html>",
                            "Selection Limit Reached",
                            JOptionPane.WARNING_MESSAGE
                    );
                    validVotes = false;
                    break;
                }
                votes.put(position, selectedCandidates);
            }

            if (validVotes) {
                int confirm = JOptionPane.showConfirmDialog(
                        votingFrame,
                        "<html><div style='text-align: left;'>"
                        + "<span style='font-size: 14px;'>Are you sure you want to submit your votes? You won't be able to change them afterward.</span>"
                        + "</div></html>",
                        "Confirm Submission",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection conn = DBUtil.getConnection()) {
                        String updateSQL = "UPDATE candidates SET votes = votes + 1 WHERE name = ? AND type = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
                            for (var entry : votes.entrySet()) {
                                String position = entry.getKey();
                                for (String candidate : entry.getValue()) {
                                    stmt.setString(1, candidate);
                                    stmt.setString(2, position);
                                    stmt.addBatch();
                                }
                            }
                            int[] updateCounts = stmt.executeBatch();
                            System.out.println("Update counts: " + Arrays.toString(updateCounts)); // Debug
                        }
                        // Mark the voter as having voted
                        String updateVoterSQL = "UPDATE voters SET has_voted = ? WHERE username = ?";
                        try (PreparedStatement voterStmt = conn.prepareStatement(updateVoterSQL)) {
                            voterStmt.setBoolean(1, true);
                            voterStmt.setString(2, currentUsername);
                            voterStmt.executeUpdate();
                        }
                        JOptionPane.showMessageDialog(
                                votingFrame,
                                "<html><div style='text-align: left;'>"
                                + "<span style='color: #28A745;font-size: 16px;'>Your votes have been submitted successfully!</span><br>"
                                + "<span style='font-size: 14px;'>Thank you for participating in the election.</span>"
                                + "</div></html>",
                                "Vote Submitted",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        votingFrame.dispose();
                        showLoginScreen();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(
                                votingFrame,
                                "Failed to record votes: " + ex.getMessage(),
                                "Database Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });

        // Add spacing and the button
        mainPanel.add(Box.createVerticalStrut(20)); // Adjust this value to control the height
        mainPanel.add(submitButton);

        // Scrollable frame
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        votingFrame.add(scrollPane);
        votingFrame.setLocationRelativeTo(null);
        votingFrame.setVisible(true);
    }

    private static void showResultsScreen() {
        JFrame resultsFrame = new JFrame("ElectBudz - Election Results");
        resultsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        resultsFrame.setSize(1000, 700);
        resultsFrame.setLayout(new BorderLayout(10, 10));

        String iconPath = "C:/Users/erich/OneDrive/Documents/NetBeansProjects/ElectBudz/src/main/java/com/mycompany/electbudz/ElectBudz Logo/Elect Budz Logo.png";
        ImageIcon icon = new ImageIcon(iconPath);
        resultsFrame.setIconImage(icon.getImage());

        int totalVoters = 0;

        // Calculate total votes by summing votes column from candidates table
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/elect_budz", "root", "password"); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT SUM(votes) AS total_votes FROM candidates")) {

            if (rs.next()) {
                totalVoters = rs.getInt("total_votes");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        int totalVotersWhoVoted = 0;

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/elect_budz", "root", "password"); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS voted_count FROM voters WHERE has_voted = 1")) {

            if (rs.next()) {
                totalVotersWhoVoted = rs.getInt("voted_count");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        JLabel titleLabel = new JLabel(
                "Election Results (Total Voters who voted: " + totalVotersWhoVoted + ")",
                SwingConstants.CENTER
        );
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(70, 130, 180));
        titleLabel.setForeground(Color.WHITE);
        resultsFrame.add(titleLabel, BorderLayout.NORTH);

        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        resultsPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        resultsFrame.add(scrollPane, BorderLayout.CENTER);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/elect_budz", "root", "password")) {

            // Query all candidates grouped by type with their votes
            String sql = "SELECT type, name, votes FROM candidates ORDER BY type, votes DESC";
            Map<String, List<CandidateResult>> resultsMap = new LinkedHashMap<>();

            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String position = rs.getString("type");
                    String name = rs.getString("name");
                    int voteCount = rs.getInt("votes");

                    resultsMap.computeIfAbsent(position, k -> new ArrayList<>())
                            .add(new CandidateResult(name, voteCount));
                }
            }

            List<String> positionOrder = Arrays.asList(
                    "Governor", "Vice Governor", "Provincial Board Member", "Mayor", "Vice Mayor", "City/Town Councilor"
            );

            for (String position : positionOrder) {
                if (!resultsMap.containsKey(position)) {
                    continue;
                }

                List<CandidateResult> candidates = resultsMap.get(position);

                // Sort alphabetically by name
                candidates.sort(Comparator.comparing(c -> c.name));

                JLabel positionLabel = new JLabel(position, SwingConstants.CENTER);
                positionLabel.setFont(new Font("Arial", Font.BOLD, 18));
                positionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
                positionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                resultsPanel.add(positionLabel);

                int totalVotesForPosition = candidates.stream().mapToInt(c -> c.votes).sum();
                int skippedVotes = totalVotersWhoVoted - totalVotesForPosition;

                boolean isTie = candidates.size() > 1 && candidates.get(0).votes == candidates.get(1).votes;
                int highestVotes = candidates.isEmpty() ? 0 : candidates.get(0).votes;

                for (int i = 0; i < candidates.size(); i++) {
                    CandidateResult candidate = candidates.get(i);
                    int rank = i + 1;

                    String percentage = totalVotersWhoVoted > 0
                            ? String.format("%.2f%%", (candidate.votes * 100.0) / totalVotersWhoVoted)
                            : "0.00%";

                    JPanel candidatePanel = new JPanel();
                    candidatePanel.setLayout(new BoxLayout(candidatePanel, BoxLayout.Y_AXIS));
                    candidatePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    candidatePanel.setBackground(new Color(240, 248, 255));

                    JLabel nameLabel = new JLabel(candidate.name, SwingConstants.CENTER);
                    nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
                    nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JLabel resultLabel = new JLabel(candidate.votes + " votes (" + percentage + ")", SwingConstants.CENTER);
                    resultLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                    resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JProgressBar progressBar = new JProgressBar(0, totalVotersWhoVoted);
                    progressBar.setValue(candidate.votes);
                    progressBar.setStringPainted(true);

                    // Custom rank-based coloring
                    if (position.equals("Provincial Board Member")) {
                        progressBar.setForeground(rank <= 2 ? new Color(60, 179, 113) : new Color(220, 20, 60));
                    } else if (position.equals("Councilor")) {
                        progressBar.setForeground(rank <= 10 ? new Color(60, 179, 113) : new Color(220, 20, 60));
                    } else {
                        progressBar.setForeground(candidate.votes == highestVotes
                                ? (isTie ? new Color(255, 223, 0) : new Color(60, 179, 113))
                                : new Color(220, 20, 60));
                    }

                    progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

                    candidatePanel.add(nameLabel);
                    candidatePanel.add(resultLabel);
                    candidatePanel.add(progressBar);
                    resultsPanel.add(candidatePanel);
                }

                if (skippedVotes > 0) {
                    JLabel skippedLabel = new JLabel("Skipped Votes (Abstain): " + skippedVotes + " votes");
                    skippedLabel.setFont(new Font("Arial", Font.ITALIC, 14));
                    skippedLabel.setForeground(Color.GRAY);
                    skippedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    resultsPanel.add(Box.createVerticalStrut(10));
                    resultsPanel.add(skippedLabel);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading results: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        JButton mainMenuButton = new JButton("Back");
        mainMenuButton.setBackground(Color.decode("#0032A0"));
        mainMenuButton.setForeground(Color.WHITE);
        mainMenuButton.setFocusPainted(false);
        mainMenuButton.addActionListener(e -> {
            resultsFrame.dispose();
            showAdminOptionSelectionScreen();
        });

        JButton resetVotesButton = new JButton("Reset Votes");
        resetVotesButton.setBackground(new Color(220, 20, 60));
        resetVotesButton.setForeground(Color.WHITE);
        resetVotesButton.setFocusPainted(false);
        resetVotesButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(resultsFrame,
                    "Are you sure you want to reset all votes and voter statuses?", "Confirm Reset",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/elect_budz", "root", "password"); Statement stmt = conn.createStatement()) {

                    // Reset all votes in candidates table
                    stmt.executeUpdate("UPDATE candidates SET votes = 0");

                    // Reset all voters' has_voted flag
                    stmt.executeUpdate("UPDATE voters SET has_voted = 0");

                    JOptionPane.showMessageDialog(resultsFrame, "Votes and voter statuses have been reset successfully.");
                    resultsFrame.dispose();
                    showResultsScreen(); // Reload the screen with fresh data

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(resultsFrame, "Error resetting data: " + ex.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(mainMenuButton);
        buttonPanel.add(resetVotesButton);
        resultsFrame.add(buttonPanel, BorderLayout.SOUTH);

        resultsFrame.setLocationRelativeTo(null);
        resultsFrame.setVisible(true);
    }

// Helper class to store results
    private static class CandidateResult {

        String name;
        int votes;

        CandidateResult(String name, int votes) {
            this.name = name;
            this.votes = votes;
        }
    }
}
