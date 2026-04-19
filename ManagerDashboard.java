import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class ManagerDashboard extends JFrame implements MarketObserver {

    private User currentUser;
    private ReservationService reservationService = new ReservationService();
    private StallService stallService = new StallService();
    private MarketDataStore dataStore = MarketDataStore.getInstance();
    private JTabbedPane tabs;
    private JLabel statusBar;

    public ManagerDashboard(User user) {
        this.currentUser = user;
        MarketEventManager.getInstance().subscribe(this);
        setTitle("Market Manager Dashboard - " + user.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 700);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.PRIMARY_DARK);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JLabel title = new JLabel("\uD83C\uDF3E Market Manager Dashboard");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel userInfo = new JLabel("Manager: " + currentUser.getFullName());
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

        tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_SUBHEADER);
        tabs.addTab("Dashboard", buildDashboardTab());
        tabs.addTab("Stall Availability", buildStallAvailabilityTab());
        tabs.addTab("Manage Reservations", buildReservationsTab());
        tabs.addTab("Reports", new ReportPanel());

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

        long totalStalls = dataStore.getStalls().size();
        long available = dataStore.getStalls().stream().filter(s -> s.getStatus() == Stall.StallStatus.AVAILABLE).count();
        long reserved = dataStore.getStalls().stream().filter(s -> s.getStatus() == Stall.StallStatus.RESERVED).count();
        long pending = dataStore.getReservations().stream().filter(r -> r.getStatus() == Reservation.ReservationStatus.PENDING).count();
        long maintenance = dataStore.getStalls().stream().filter(s -> s.getStatus() == Stall.StallStatus.UNDER_MAINTENANCE).count();

        JPanel statsRow = new JPanel(new GridLayout(1, 5, 12, 0));
        statsRow.setOpaque(false);
        statsRow.add(UITheme.createStatCard("Total Stalls", String.valueOf(totalStalls), new Color(50, 100, 180)));
        statsRow.add(UITheme.createStatCard("Available", String.valueOf(available), new Color(0, 140, 100)));
        statsRow.add(UITheme.createStatCard("Reserved", String.valueOf(reserved), new Color(190, 80, 0)));
        statsRow.add(UITheme.createStatCard("Pending Requests", String.valueOf(pending), new Color(190, 140, 0)));
        statsRow.add(UITheme.createStatCard("Under Maintenance", String.valueOf(maintenance), new Color(120, 60, 140)));

        // Pending reservations list
        String[] cols = {"ID", "Farmer", "Stall", "Start", "End", "Amount", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Reservation r : reservationService.getPendingReservations()) {
            model.addRow(new Object[]{r.getReservationId(), r.getFarmer().getFullName(),
                    r.getStall().getStallNumber(), r.getStartDate(), r.getEndDate(),
                    String.format("₱%.2f", r.getTotalAmount()), r.getNotes()});
        }
        JTable table = new JTable(model);
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("Pending Reservation Requests"));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btnRow.setOpaque(false);
        JButton approveBtn = UITheme.createPrimaryButton("Approve Selected");
        JButton rejectBtn = UITheme.createDangerButton("Reject Selected");
        btnRow.add(approveBtn); btnRow.add(rejectBtn);

        approveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int id = (int) model.getValueAt(row, 0);
            String result = reservationService.approveReservation(id);
            model.removeRow(row);
            statusBar.setText(result.equals("SUCCESS") ? "Reservation approved." : result);
        });
        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int id = (int) model.getValueAt(row, 0);
            String result = reservationService.rejectReservation(id);
            model.removeRow(row);
            statusBar.setText(result.equals("SUCCESS") ? "Reservation rejected." : result);
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(btnRow, BorderLayout.NORTH);
        bottom.add(scroll, BorderLayout.CENTER);

        panel.add(statsRow, BorderLayout.NORTH);
        panel.add(bottom, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStallAvailabilityTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        String[] cols = {"ID", "Stall No.", "Location", "Category", "Daily Rate", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Stall s : dataStore.getStalls()) {
            model.addRow(new Object[]{s.getStallId(), s.getStallNumber(), s.getLocation(),
                    s.getCategory(), String.format("₱%.2f", s.getDailyRate()), s.getStatus()});
        }

        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                Object status = getValueAt(row, 5);
                if (!isRowSelected(row)) {
                    if (Stall.StallStatus.AVAILABLE.name().equals(status.toString())) {
                        c.setBackground(new Color(220, 255, 220));
                    } else if (Stall.StallStatus.RESERVED.name().equals(status.toString())) {
                        c.setBackground(new Color(255, 230, 200));
                    } else {
                        c.setBackground(new Color(240, 240, 240));
                    }
                }
                return c;
            }
        };
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("Stall Availability (Green=Available, Orange=Reserved, Gray=Maintenance)"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        JButton maintBtn = UITheme.createWarningButton("Toggle Maintenance");
        toolbar.add(refreshBtn); toolbar.add(maintBtn);

        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            for (Stall s : dataStore.getStalls()) {
                model.addRow(new Object[]{s.getStallId(), s.getStallNumber(), s.getLocation(),
                        s.getCategory(), String.format("₱%.2f", s.getDailyRate()), s.getStatus()});
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
                model.setValueAt(s.getStatus(), row, 5);
                statusBar.setText("Stall status updated.");
            }
        });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReservationsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        String[] cols = {"ID", "Farmer", "Stall", "Start", "End", "Status", "Amount", "Notes"};
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
        JButton cancelBtn = UITheme.createWarningButton("Cancel");
        JButton completeBtn = UITheme.createSecondaryButton("Complete");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        toolbar.add(approveBtn); toolbar.add(rejectBtn); toolbar.add(cancelBtn); toolbar.add(completeBtn); toolbar.add(refreshBtn);

        approveBtn.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row < 0) return;
            String r = reservationService.approveReservation((int) model.getValueAt(row, 0));
            refreshReservationTable(model);
            statusBar.setText(r.equals("SUCCESS") ? "Approved." : r);
        });
        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row < 0) return;
            String r = reservationService.rejectReservation((int) model.getValueAt(row, 0));
            refreshReservationTable(model);
            statusBar.setText(r.equals("SUCCESS") ? "Rejected." : r);
        });
        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row < 0) return;
            String r = reservationService.cancelReservation((int) model.getValueAt(row, 0));
            refreshReservationTable(model);
            statusBar.setText(r.equals("SUCCESS") ? "Cancelled." : r);
        });
        completeBtn.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row < 0) return;
            String r = reservationService.completeReservation((int) model.getValueAt(row, 0));
            refreshReservationTable(model);
            statusBar.setText(r.equals("SUCCESS") ? "Completed." : r);
        });
        refreshBtn.addActionListener(e -> refreshReservationTable(model));

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

    @Override public void onReservationUpdated(String message) { SwingUtilities.invokeLater(() -> statusBar.setText(message)); }
    @Override public void onStallStatusChanged(String message) { SwingUtilities.invokeLater(() -> statusBar.setText(message)); }
    @Override public void onUserUpdated(String message) { SwingUtilities.invokeLater(() -> statusBar.setText(message)); }
}