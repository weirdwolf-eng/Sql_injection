import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SQLInjectionDemo extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextArea resultArea;
    private JRadioButton vulnerableButton, secureButton;
    private Connection connection;
    
    public SQLInjectionDemo() {
        setTitle("SQL Injection Demo - Secure Login");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Initialize database
        initDatabase();
        
        // Top Panel - Mode Selection
        JPanel modePanel = new JPanel();
        modePanel.setBorder(BorderFactory.createTitledBorder("Select Mode"));
        ButtonGroup group = new ButtonGroup();
        vulnerableButton = new JRadioButton("Vulnerable (String Concatenation)");
        secureButton = new JRadioButton("Secure (Prepared Statement)", true);
        group.add(vulnerableButton);
        group.add(secureButton);
        modePanel.add(vulnerableButton);
        modePanel.add(secureButton);
        
        // Center Panel - Login Form
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createTitledBorder("Login Form"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        loginPanel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        loginPanel.add(passwordField, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> attemptLogin());
        loginPanel.add(loginButton, gbc);
        
        // Bottom Panel - Results
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Result"));
        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Info Panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel("<html><b>Try this SQL Injection:</b> Username: ' OR '1'='1' --   Password: anything</html>");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        infoPanel.add(infoLabel, BorderLayout.NORTH);
        
        JLabel validLabel = new JLabel("<html><b>Valid Users:</b> admin/admin123, user/user123</html>");
        validLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        infoPanel.add(validLabel, BorderLayout.SOUTH);
        
        // Add panels to frame
        add(modePanel, BorderLayout.NORTH);
        add(loginPanel, BorderLayout.CENTER);
        
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(infoPanel, BorderLayout.NORTH);
        bottomContainer.add(resultPanel, BorderLayout.CENTER);
        add(bottomContainer, BorderLayout.SOUTH);
        
        setLocationRelativeTo(null);
    }
    
    private void initDatabase() {
        try {
            // Using embedded H2 database (in-memory)
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            
            // Create users table
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE users (id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(50), password VARCHAR(50))");
            
            // Insert sample users
            stmt.execute("INSERT INTO users (username, password) VALUES ('admin', 'admin123')");
            stmt.execute("INSERT INTO users (username, password) VALUES ('user', 'user123')");
            
            resultArea.append("Database initialized successfully!\n");
            resultArea.append("Sample users created: admin, user\n\n");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Database Error: " + e.getMessage() + 
                "\n\nMake sure H2 database library (h2.jar) is in classpath!", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        resultArea.append("===========================================\n");
        resultArea.append("Login Attempt: " + username + "\n");
        
        if (vulnerableButton.isSelected()) {
            vulnerableLogin(username, password);
        } else {
            secureLogin(username, password);
        }
    }
    
    private void vulnerableLogin(String username, String password) {
        resultArea.append("Mode: VULNERABLE (String Concatenation)\n");
        try {
            // VULNERABLE: Direct string concatenation
            String query = "SELECT * FROM users WHERE username = '" + username + 
                          "' AND password = '" + password + "'";
            
            resultArea.append("Query: " + query + "\n");
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                resultArea.append("Result: LOGIN SUCCESSFUL! ✓\n");
                resultArea.append("User ID: " + rs.getInt("id") + "\n");
                resultArea.append("Username: " + rs.getString("username") + "\n");
                JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                resultArea.append("Result: LOGIN FAILED ✗\n");
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Failed", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            resultArea.append("Error: " + e.getMessage() + "\n");
        }
        resultArea.append("\n");
    }
    
    private void secureLogin(String username, String password) {
        resultArea.append("Mode: SECURE (Prepared Statement)\n");
        try {
            // SECURE: Using prepared statement
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            resultArea.append("Query: " + query + "\n");
            resultArea.append("Parameters: username=" + username + ", password=" + password + "\n");
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                resultArea.append("Result: LOGIN SUCCESSFUL! ✓\n");
                resultArea.append("User ID: " + rs.getInt("id") + "\n");
                resultArea.append("Username: " + rs.getString("username") + "\n");
                JOptionPane.showMessageDialog(this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                resultArea.append("Result: LOGIN FAILED ✗\n");
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Failed", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            resultArea.append("Error: " + e.getMessage() + "\n");
        }
        resultArea.append("\n");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SQLInjectionDemo demo = new SQLInjectionDemo();
            demo.setVisible(true);
        });
    }
}
