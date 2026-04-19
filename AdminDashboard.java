import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame implements MarketObserver {

    private User currentUser;
    private UserService userService = new UserService();
    private StallService stallService = new StallService();
    private ReservationService reservationService = new ReservationService();
    private MarketDataStore dataStore = MarketDataStore.getInstance();
    private JTabbedPane tabs;
    private JLabel statusBar;

    public AdminDashboard(User user) {
        this.currentUser = user;
        MarketEventManager.getInstance().subscribe(this);
        setTitle("Admin Dashboard - " + user.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JLabel title = new JLabel("\uD83C\uDF3E Farmers Market — Admin Dashboard");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel userInfo = new JLabel("Logged in: " + currentUser.getFullName() + "  |  Role: Admin");
        userInfo.setFont(UITheme.FONT_NORMAL);
        userInfo.setForeground(new Color(220, 240, 220));
        JButton logoutBtn = UITheme.createSecondaryButton("Logout");
        logoutBtn.addActionListener(e -> logout());
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        topRight.add(userInfo);
        topRight.add(logoutBtn);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        // Tabs
        tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_SUBHEADER);
        tabs.setBackground(UITheme.BACKGROUND);
        tabs.addTab("Dashboard", buildDashboardTab());
        tabs.addTab("Users", buildUsersTab());
        tabs.addTab("Stalls", buildStallsTab());
        tabs.addTab("Reservations", buildReservationsTab());
        tabs.addTab("Reports", new ReportPanel());

        // Status bar
        statusBar = new JLabel("Ready");
        statusBar.setFont(UITheme.FONT_SMALL);
        statusBar.setForeground(UITheme.TEXT_SECONDARY);
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        root.add(topBar, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);
        add(root);
    }

    private JPanel buildDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 5, 12, 0));
        statsRow.setOpaque(false);

        long totalUsers = dataStore.getUsers().size();
        long totalStalls = dataStore.getStalls().size();
        long available = dataStore.getStalls().stream().filter(s -> s.getStatus() == Stall.StallStatus.AVAILABLE).count();
        long pending = dataStore.getReservations().stream().filter(r -> r.getStatus() == Reservation.ReservationStatus.PENDING).count();
        long approved = dataStore.getReservations().stream().filter(r -> r.getStatus() == Reservation.ReservationStatus.APPROVED).count();

        statsRow.add(UITheme.createStatCard("Total Users", String.valueOf(totalUsers), new Color(34, 120, 34)));
        statsRow.add(UITheme.createStatCard("Total Stalls", String.valueOf(totalStalls), new Color(50, 100, 180)));
        statsRow.add(UITheme.createStatCard("Available Stalls", String.valueOf(available), new Color(0, 140, 100)));
        statsRow.add(UITheme.createStatCard("Pending Reservations", String.valueOf(pending), new Color(190, 120, 0)));
        statsRow.add(UITheme.createStatCard("Active Reservations", String.valueOf(approved), new Color(160, 40, 40)));

        // Recent reservations
        String[] cols = {"ID", "Farmer", "Stall", "Start Date", "End Date", "Status", "Amount"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        List<Reservation> recents = dataStore.getReservations();
        int count = Math.min(recents.size(), 8);
        for (int i = recents.size() - 1; i >= recents.size() - count; i--) {
            Reservation r = recents.get(i);
            model.addRow(new Object[]{
                    r.getReservationId(),
                    r.getFarmer().getFullName(),
                    r.getStall().getStallNumber(),
                    r.getStartDate(),
                    r.getEndDate(),
                    r.getStatus(),
                    String.format("₱%.2f", r.getTotalAmount())
            });
        }
        JTable table = new JTable(model);
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("Recent Reservations"));

        panel.add(statsRow, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        String[] cols = {"ID", "Username", "Full Name", "Email", "Phone", "Role", "Active"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshUserTable(model);

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("All Users"));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton addBtn = UITheme.createPrimaryButton("+ Add User");
        JButton editBtn = UITheme.createWarningButton("Edit");
        JButton deleteBtn = UITheme.createDangerButton("Delete");
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);

        addBtn.addActionListener(e -> showAddUserDialog(model));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
            int userId = (int) model.getValueAt(row, 0);
            User u = dataStore.getUsers().stream().filter(x -> x.getUserId() == userId).findFirst().orElse(null);
            if (u != null) showEditUserDialog(u, model);
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
            int userId = (int) model.getValueAt(row, 0);
            if (userId == currentUser.getUserId()) { JOptionPane.showMessageDialog(this, "Cannot delete yourself."); return; }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                userService.deleteUser(userId);
                refreshUserTable(model);
                statusBar.setText("User deleted.");
            }
        });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void refreshUserTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (User u : dataStore.getUsers()) {
            model.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(),
                    u.getEmail(), u.getPhone(), u.getRole(), u.isActive() ? "Yes" : "No"});
        }
    }

    private void showAddUserDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Add New User", true);
        dialog.setSize(420, 440);
        dialog.setLocationRelativeTo(this);
        JPanel form = buildUserForm(null);
        JButton saveBtn = UITheme.createPrimaryButton("Save");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UITheme.BACKGROUND);
        btnRow.add(cancelBtn); btnRow.add(saveBtn);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        root.add(form, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        dialog.add(root);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            JTextField un = (JTextField) getFieldByName(form, "username");
            JTextField pw = (JTextField) getFieldByName(form, "password");
            JTextField fn = (JTextField) getFieldByName(form, "fullname");
            JTextField em = (JTextField) getFieldByName(form, "email");
            JTextField ph = (JTextField) getFieldByName(form, "phone");
            JComboBox<?> roleBox = (JComboBox<?>) getFieldByName(form, "role");

            if (un == null || un.getText().trim().isEmpty() || pw == null || pw.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and password are required.");
                return;
            }
            String result = userService.addUser(un.getText().trim(), pw.getText().trim(),
                    fn != null ? fn.getText().trim() : "",
                    em != null ? em.getText().trim() : "",
                    ph != null ? ph.getText().trim() : "",
                    roleBox != null ? roleBox.getSelectedItem().toString() : "FARMER");
            if ("SUCCESS".equals(result)) {
                refreshUserTable(model);
                dialog.dispose();
                statusBar.setText("User added successfully.");
            } else {
                JOptionPane.showMessageDialog(dialog, result);
            }
        });
        dialog.setVisible(true);
    }

    private void showEditUserDialog(User user, DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Edit User", true);
        dialog.setSize(420, 440);
        dialog.setLocationRelativeTo(this);
        JPanel form = buildUserForm(user);
        JButton saveBtn = UITheme.createPrimaryButton("Save Changes");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UITheme.BACKGROUND);
        btnRow.add(cancelBtn); btnRow.add(saveBtn);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        root.add(form, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        dialog.add(root);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            JTextField fn = (JTextField) getFieldByName(form, "fullname");
            JTextField em = (JTextField) getFieldByName(form, "email");
            JTextField ph = (JTextField) getFieldByName(form, "phone");
            JComboBox<?> roleBox = (JComboBox<?>) getFieldByName(form, "role");
            if (fn != null) user.setFullName(fn.getText().trim());
            if (em != null) user.setEmail(em.getText().trim());
            if (ph != null) user.setPhone(ph.getText().trim());
            if (roleBox != null) user.setRole(User.UserRole.valueOf(roleBox.getSelectedItem().toString().replace(" ", "_")));
            userService.updateUser(user);
            refreshUserTable(model);
            dialog.dispose();
            statusBar.setText("User updated.");
        });
        dialog.setVisible(true);
    }

    private JPanel buildUserForm(User user) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1;

        JTextField usernameF = new JTextField(user != null ? user.getUsername() : "");
        usernameF.setName("username"); usernameF.setEnabled(user == null);
        JTextField passwordF = new JTextField(user != null ? user.getPassword() : "");
        passwordF.setName("password");
        JTextField fullnameF = new JTextField(user != null ? user.getFullName() : "");
        fullnameF.setName("fullname");
        JTextField emailF = new JTextField(user != null ? user.getEmail() : "");
        emailF.setName("email");
        JTextField phoneF = new JTextField(user != null ? user.getPhone() : "");
        phoneF.setName("phone");
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"ADMIN", "MARKET_MANAGER", "FARMER"});
        roleBox.setName("role");
        if (user != null) roleBox.setSelectedItem(user.getRole().name());

        String[][] fields = {{"Username", null}, {"Password", null}, {"Full Name", null},
                {"Email", null}, {"Phone", null}, {"Role", null}};
        JComponent[] comps = {usernameF, passwordF, fullnameF, emailF, phoneF, roleBox};

        for (int i = 0; i < comps.length; i++) {
            JLabel lbl = new JLabel(fields[i][0] + ":");
            lbl.setFont(UITheme.FONT_SUBHEADER);
            gbc.gridx = 0; gbc.gridy = i * 2;
            panel.add(lbl, gbc);
            gbc.gridx = 0; gbc.gridy = i * 2 + 1;
            comps[i].setFont(UITheme.FONT_NORMAL);
            panel.add(comps[i], gbc);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BACKGROUND);
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private Component getFieldByName(JPanel parent, String name) {
        for (Component c : getAllComponents(parent)) {
            if (name.equals(c.getName())) return c;
        }
        return null;
    }

    private java.util.List<Component> getAllComponents(Container parent) {
        java.util.List<Component> list = new java.util.ArrayList<>();
        for (Component c : parent.getComponents()) {
            list.add(c);
            if (c instanceof Container) list.addAll(getAllComponents((Container) c));
        }
        return list;
    }

    private JPanel buildStallsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        String[] cols = {"ID", "Stall No.", "Location", "Category", "Daily Rate", "Status", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshStallTable(model);

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("Market Stalls"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton addBtn = UITheme.createPrimaryButton("+ Add Stall");
        JButton editBtn = UITheme.createWarningButton("Edit");
        JButton deleteBtn = UITheme.createDangerButton("Delete");
        JButton maintBtn = UITheme.createSecondaryButton("Toggle Maintenance");
        toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(deleteBtn); toolbar.add(maintBtn);

        addBtn.addActionListener(e -> showAddStallDialog(model));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a stall first."); return; }
            int stallId = (int) model.getValueAt(row, 0);
            Stall s = stallService.getStallById(stallId);
            if (s != null) showEditStallDialog(s, model);
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a stall first."); return; }
            int stallId = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this stall?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                stallService.deleteStall(stallId);
                refreshStallTable(model);
                statusBar.setText("Stall deleted.");
            }
        });
        maintBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a stall first."); return; }
            int stallId = (int) model.getValueAt(row, 0);
            Stall s = stallService.getStallById(stallId);
            if (s != null) {
                s.setStatus(s.getStatus() == Stall.StallStatus.UNDER_MAINTENANCE ? Stall.StallStatus.AVAILABLE : Stall.StallStatus.UNDER_MAINTENANCE);
                stallService.updateStall(s);
                refreshStallTable(model);
                statusBar.setText("Stall status toggled.");
            }
        });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void refreshStallTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Stall s : dataStore.getStalls()) {
            model.addRow(new Object[]{s.getStallId(), s.getStallNumber(), s.getLocation(),
                    s.getCategory(), String.format("₱%.2f", s.getDailyRate()), s.getStatus(), s.getDescription()});
        }
    }

    private void showAddStallDialog(DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Add Stall", true);
        dialog.setSize(400, 420);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BACKGROUND);
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1; gbc.insets = new Insets(5, 4, 5, 4);

        JTextField numF = new JTextField(); numF.setFont(UITheme.FONT_NORMAL);
        JTextField locF = new JTextField(); locF.setFont(UITheme.FONT_NORMAL);
        JTextField catF = new JTextField(); catF.setFont(UITheme.FONT_NORMAL);
        JTextField rateF = new JTextField(); rateF.setFont(UITheme.FONT_NORMAL);
        JTextField descF = new JTextField(); descF.setFont(UITheme.FONT_NORMAL);

        String[][] rows = {{"Stall Number:", null}, {"Location:", null}, {"Category:", null}, {"Daily Rate (₱):", null}, {"Description:", null}};
        JTextField[] fields = {numF, locF, catF, rateF, descF};
        for (int i = 0; i < fields.length; i++) {
            JLabel lbl = new JLabel(rows[i][0]); lbl.setFont(UITheme.FONT_SUBHEADER);
            gbc.gridx = 0; gbc.gridy = i * 2; form.add(lbl, gbc);
            gbc.gridx = 0; gbc.gridy = i * 2 + 1; form.add(fields[i], gbc);
        }

        JButton saveBtn = UITheme.createPrimaryButton("Add Stall");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UITheme.BACKGROUND);
        btnRow.add(cancelBtn); btnRow.add(saveBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        root.add(form, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        dialog.add(root);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            try {
                double rate = Double.parseDouble(rateF.getText().trim());
                Stall stall = new Stall(0, numF.getText().trim(), locF.getText().trim(),
                        catF.getText().trim(), rate, descF.getText().trim());
                stallService.addStall(stall);
                refreshStallTable(model);
                dialog.dispose();
                statusBar.setText("Stall added.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid daily rate.");
            }
        });
        dialog.setVisible(true);
    }

    private void showEditStallDialog(Stall stall, DefaultTableModel model) {
        JDialog dialog = new JDialog(this, "Edit Stall", true);
        dialog.setSize(400, 420);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BACKGROUND);
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1; gbc.insets = new Insets(5, 4, 5, 4);

        JTextField numF = new JTextField(stall.getStallNumber()); numF.setFont(UITheme.FONT_NORMAL);
        JTextField locF = new JTextField(stall.getLocation()); locF.setFont(UITheme.FONT_NORMAL);
        JTextField catF = new JTextField(stall.getCategory()); catF.setFont(UITheme.FONT_NORMAL);
        JTextField rateF = new JTextField(String.valueOf(stall.getDailyRate())); rateF.setFont(UITheme.FONT_NORMAL);
        JTextField descF = new JTextField(stall.getDescription()); descF.setFont(UITheme.FONT_NORMAL);

        String[] labels = {"Stall Number:", "Location:", "Category:", "Daily Rate (₱):", "Description:"};
        JTextField[] fields = {numF, locF, catF, rateF, descF};
        for (int i = 0; i < fields.length; i++) {
            JLabel lbl = new JLabel(labels[i]); lbl.setFont(UITheme.FONT_SUBHEADER);
            gbc.gridx = 0; gbc.gridy = i * 2; form.add(lbl, gbc);
            gbc.gridx = 0; gbc.gridy = i * 2 + 1; form.add(fields[i], gbc);
        }

        JButton saveBtn = UITheme.createPrimaryButton("Save Changes");
        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UITheme.BACKGROUND);
        btnRow.add(cancelBtn); btnRow.add(saveBtn);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);
        root.add(form, BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        dialog.add(root);

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            try {
                stall.setStallNumber(numF.getText().trim());
                stall.setLocation(locF.getText().trim());
                stall.setCategory(catF.getText().trim());
                stall.setDailyRate(Double.parseDouble(rateF.getText().trim()));
                stall.setDescription(descF.getText().trim());
                stallService.updateStall(stall);
                refreshStallTable(model);
                dialog.dispose();
                statusBar.setText("Stall updated.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid daily rate.");
            }
        });
        dialog.setVisible(true);
    }

    private JPanel buildReservationsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        String[] cols = {"ID", "Farmer", "Stall", "Start Date", "End Date", "Status", "Amount", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshReservationTable(model);

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("All Reservations"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton approveBtn = UITheme.createPrimaryButton("Approve");
        JButton rejectBtn = UITheme.createDangerButton("Reject");
        JButton cancelBtn2 = UITheme.createWarningButton("Cancel");
        JButton completeBtn = UITheme.createSecondaryButton("Complete");
        JButton deleteBtn = UITheme.createDangerButton("Delete");
        toolbar.add(approveBtn); toolbar.add(rejectBtn); toolbar.add(cancelBtn2); toolbar.add(completeBtn); toolbar.add(deleteBtn);

        approveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int id = (int) model.getValueAt(row, 0);
            String result = reservationService.approveReservation(id);
            refreshReservationTable(model);
            statusBar.setText(result.equals("SUCCESS") ? "Reservation approved." : result);
        });
        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int id = (int) model.getValueAt(row, 0);
            String result = reservationService.rejectReservation(id);
            refreshReservationTable(model);
            statusBar.setText(result.equals("SUCCESS") ? "Reservation rejected." : result);
        });
        cancelBtn2.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int id = (int) model.getValueAt(row, 0);
            String result = reservationService.cancelReservation(id);
            refreshReservationTable(model);
            statusBar.setText(result.equals("SUCCESS") ? "Reservation cancelled." : result);
        });
        completeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int id = (int) model.getValueAt(row, 0);
            String result = reservationService.completeReservation(id);
            refreshReservationTable(model);
            statusBar.setText(result.equals("SUCCESS") ? "Reservation completed." : result);
        });
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int id = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete reservation #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dataStore.deleteReservation(id);
                refreshReservationTable(model);
                statusBar.setText("Reservation deleted.");
            }
        });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void refreshReservationTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Reservation r : dataStore.getReservations()) {
            model.addRow(new Object[]{r.getReservationId(), r.getFarmer().getFullName(),
                    r.getStall().getStallNumber(), r.getStartDate(), r.getEndDate(),
                    r.getStatus(), String.format("₱%.2f", r.getTotalAmount()), r.getNotes()});
        }
    }

    private void logout() {
        MarketEventManager.getInstance().unsubscribe(this);
        dispose();
        new LoginFrame().setVisible(true);
    }

    @Override
    public void onReservationUpdated(String message) {
        SwingUtilities.invokeLater(() -> statusBar.setText(message));
    }

    @Override
    public void onStallStatusChanged(String message) {
        SwingUtilities.invokeLater(() -> statusBar.setText(message));
    }

    @Override
    public void onUserUpdated(String message) {
        SwingUtilities.invokeLater(() -> statusBar.setText(message));
    }
}