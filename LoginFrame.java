import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private UserService userService = new UserService();

    public LoginFrame() {
        setTitle("Farmers Market Stall Reservation System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        UITheme.applyLookAndFeel();
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BACKGROUND);

        // Header Banner
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));

        JLabel logoLbl = new JLabel("\uD83C\uDF3E Farmers Market", SwingConstants.CENTER);
        logoLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLbl.setForeground(Color.WHITE);

        JLabel subtitleLbl = new JLabel("Stall Reservation System", SwingConstants.CENTER);
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLbl.setForeground(new Color(220, 240, 220));

        header.add(logoLbl, BorderLayout.CENTER);
        header.add(subtitleLbl, BorderLayout.SOUTH);

        // Login Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel loginTitle = new JLabel("Sign In");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        loginTitle.setForeground(UITheme.PRIMARY_DARK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 6, 20, 6);
        formPanel.add(loginTitle, gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(8, 6, 8, 6);

        JLabel userLbl = new JLabel("Username:");
        userLbl.setFont(UITheme.FONT_SUBHEADER);
        userLbl.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        formPanel.add(userLbl, gbc);

        usernameField = new JTextField();
        usernameField.setFont(UITheme.FONT_NORMAL);
        usernameField.setPreferredSize(new Dimension(280, 38));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        formPanel.add(usernameField, gbc);

        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UITheme.FONT_SUBHEADER);
        passLbl.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(passLbl, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(UITheme.FONT_NORMAL);
        passwordField.setPreferredSize(new Dimension(280, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(passwordField, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 6, 4, 6);
        formPanel.add(statusLabel, gbc);

        JButton loginBtn = UITheme.createPrimaryButton("Sign In");
        loginBtn.setPreferredSize(new Dimension(280, 42));
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 6, 6);
        formPanel.add(loginBtn, gbc);

        // Hint panel
        JPanel hintPanel = new JPanel();
        hintPanel.setBackground(UITheme.BACKGROUND);
        hintPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel hint = new JLabel("<html><b>Demo credentials:</b> admin/admin123 | manager1/manager123 | farmer1/farmer123</html>");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_SECONDARY);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        hintPanel.add(hint);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UITheme.BACKGROUND);
        center.setBorder(BorderFactory.createEmptyBorder(30, 40, 10, 40));
        center.add(formPanel);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        mainPanel.add(hintPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        User user = userService.login(username, password);
        if (user == null) {
            statusLabel.setText("Invalid username or password.");
            passwordField.setText("");
            return;
        }

        statusLabel.setText(" ");
        dispose();
        openDashboard(user);
    }

    private void openDashboard(User user) {
        if (user.getRole() == User.UserRole.ADMIN) {
            new AdminDashboard(user).setVisible(true);
        } else if (user.getRole() == User.UserRole.MARKET_MANAGER) {
            new ManagerDashboard(user).setVisible(true);
        } else {
            new FarmerDashboard(user).setVisible(true);
        }
    }
}