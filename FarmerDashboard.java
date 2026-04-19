

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class FarmerDashboard extends JFrame implements MarketObserver {

    private User currentUser;
    private ReservationService reservationService = new ReservationService();
    private StallService stallService = new StallService();
    private MarketDataStore dataStore = MarketDataStore.getInstance();

    private JTabbedPane tabs;
    private JLabel statusBar;

    public FarmerDashboard(User user) {
        this.currentUser = user;
        MarketEventManager.getInstance().subscribe(this);
        setTitle("Farmer Dashboard - " + user.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BACKGROUND);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0, 110, 50));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JLabel title = new JLabel("\uD83C\uDF3E Farmer Portal");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel userInfo = new JLabel("Welcome, " + currentUser.getFullName());
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
        tabs.addTab("My Reservations", buildMyReservationsTab());
        tabs.addTab("Stall Availability", buildStallAvailabilityTab());
        tabs.addTab("Make Reservation", buildReservationFormTab());
        tabs.addTab("Sales Tracker", buildSalesTab());

        statusBar = new JLabel("Ready");
        statusBar.setFont(UITheme.FONT_SMALL);
        statusBar.setForeground(UITheme.TEXT_SECONDARY);
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        root.add(topBar, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);
        add(root);
    }

    private JPanel buildMyReservationsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        String[] cols = {"ID", "Stall", "Location", "Start Date", "End Date", "Status", "Total Amount", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshMyReservations(model);

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("My Stall Reservations"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton cancelBtn = UITheme.createDangerButton("Cancel Reservation");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        toolbar.add(cancelBtn); toolbar.add(refreshBtn);

        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a reservation first."); return; }
            int id = (int) model.getValueAt(row, 0);
            String status = model.getValueAt(row, 5).toString();
            if (status.equals(Reservation.ReservationStatus.COMPLETED.name()) || status.equals(Reservation.ReservationStatus.CANCELLED.name())) {
                JOptionPane.showMessageDialog(this, "Cannot cancel a " + status.toLowerCase() + " reservation.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Cancel reservation #" + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String result = reservationService.cancelReservation(id);
                refreshMyReservations(model);
                statusBar.setText(result.equals("SUCCESS") ? "Reservation cancelled." : result);
            }
        });
        refreshBtn.addActionListener(e -> refreshMyReservations(model));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void refreshMyReservations(DefaultTableModel model) {
        model.setRowCount(0);
        for (Reservation r : reservationService.getReservationsByFarmer(currentUser.getUserId())) {
            model.addRow(new Object[]{r.getReservationId(), r.getStall().getStallNumber(),
                    r.getStall().getLocation(), r.getStartDate(), r.getEndDate(),
                    r.getStatus(), String.format("₱%.2f", r.getTotalAmount()), r.getNotes()});
        }
    }

    private JPanel buildStallAvailabilityTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        String[] cols = {"ID", "Stall No.", "Location", "Category", "Daily Rate", "Status", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshStallTable(model);

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
        scroll.setBorder(UITheme.createTitledCardBorder("Available Stalls (Green = Available)"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        JButton reserveBtn = UITheme.createPrimaryButton("Reserve This Stall");
        toolbar.add(refreshBtn); toolbar.add(reserveBtn);

        refreshBtn.addActionListener(e -> refreshStallTable(model));
        reserveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a stall first."); return; }
            int stallId = (int) model.getValueAt(row, 0);
            String status = model.getValueAt(row, 5).toString();
            if (!status.equals(Stall.StallStatus.AVAILABLE.name())) {
                JOptionPane.showMessageDialog(this, "This stall is not available for reservation.");
                return;
            }
            Stall stall = stallService.getStallById(stallId);
            if (stall != null) {
                tabs.setSelectedIndex(2);
                showReservationFormForStall(stall);
            }
        });

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JComboBox<String> stallComboBox;
    private JTextField startDateField, endDateField, notesField;
    private JLabel totalAmountLabel;

    private void refreshStallTable(DefaultTableModel model) {
        model.setRowCount(0);
        for (Stall s : dataStore.getStalls()) {
            model.addRow(new Object[]{s.getStallId(), s.getStallNumber(), s.getLocation(),
                    s.getCategory(), String.format("₱%.2f", s.getDailyRate()), s.getStatus(), s.getDescription()});
        }
    }

    private JPanel buildReservationFormTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD_BG);
        form.setBorder(UITheme.createTitledCardBorder("New Stall Reservation"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1;

        // Stall selection
        JLabel stallLbl = new JLabel("Select Stall:");
        stallLbl.setFont(UITheme.FONT_SUBHEADER);
        stallComboBox = new JComboBox<>();
        refreshStallCombo();
        stallComboBox.setFont(UITheme.FONT_NORMAL);

        // Dates
        JLabel startLbl = new JLabel("Start Date (yyyy-MM-dd):");
        startLbl.setFont(UITheme.FONT_SUBHEADER);
        startDateField = new JTextField(LocalDate.now().plusDays(1).toString());
        startDateField.setFont(UITheme.FONT_NORMAL);

        JLabel endLbl = new JLabel("End Date (yyyy-MM-dd):");
        endLbl.setFont(UITheme.FONT_SUBHEADER);
        endDateField = new JTextField(LocalDate.now().plusDays(7).toString());
        endDateField.setFont(UITheme.FONT_NORMAL);

        JLabel notesLbl = new JLabel("Notes / Products to Sell:");
        notesLbl.setFont(UITheme.FONT_SUBHEADER);
        notesField = new JTextField();
        notesField.setFont(UITheme.FONT_NORMAL);

        JLabel totalLbl = new JLabel("Estimated Total:");
        totalLbl.setFont(UITheme.FONT_SUBHEADER);
        totalAmountLabel = new JLabel("₱0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalAmountLabel.setForeground(UITheme.PRIMARY_DARK);

        JButton calcBtn = UITheme.createSecondaryButton("Calculate Amount");
        JButton submitBtn = UITheme.createPrimaryButton("Submit Reservation");
        JButton clearBtn = UITheme.createSecondaryButton("Clear");

        gbc.gridx = 0; gbc.gridy = 0; form.add(stallLbl, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(stallComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 2; form.add(startLbl, gbc);
        gbc.gridx = 0; gbc.gridy = 3; form.add(startDateField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; form.add(endLbl, gbc);
        gbc.gridx = 0; gbc.gridy = 5; form.add(endDateField, gbc);
        gbc.gridx = 0; gbc.gridy = 6; form.add(notesLbl, gbc);
        gbc.gridx = 0; gbc.gridy = 7; form.add(notesField, gbc);
        gbc.gridx = 0; gbc.gridy = 8; form.add(totalLbl, gbc);
        gbc.gridx = 0; gbc.gridy = 9; form.add(totalAmountLabel, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnRow.setOpaque(false);
        btnRow.add(calcBtn); btnRow.add(submitBtn); btnRow.add(clearBtn);
        gbc.gridx = 0; gbc.gridy = 10; form.add(btnRow, gbc);

        calcBtn.addActionListener(e -> calculateTotal());
        submitBtn.addActionListener(e -> submitReservation());
        clearBtn.addActionListener(e -> {
            stallComboBox.setSelectedIndex(0);
            startDateField.setText(LocalDate.now().plusDays(1).toString());
            endDateField.setText(LocalDate.now().plusDays(7).toString());
            notesField.setText("");
            totalAmountLabel.setText("₱0.00");
        });

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.BACKGROUND);
        GridBagConstraints wgbc = new GridBagConstraints();
        wgbc.fill = GridBagConstraints.BOTH;
        wgbc.weightx = 1; wgbc.weighty = 1;
        wrapper.add(form, wgbc);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void refreshStallCombo() {
        stallComboBox.removeAllItems();
        for (Stall s : dataStore.getStalls()) {
            if (s.getStatus() == Stall.StallStatus.AVAILABLE) {
                stallComboBox.addItem(s.getStallId() + " - " + s.getStallNumber() + " (" + s.getCategory() + ") ₱" + s.getDailyRate() + "/day");
            }
        }
    }

    private void showReservationFormForStall(Stall stall) {
        refreshStallCombo();
        for (int i = 0; i < stallComboBox.getItemCount(); i++) {
            if (stallComboBox.getItemAt(i).toString().startsWith(stall.getStallId() + " - ")) {
                stallComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void calculateTotal() {
        try {
            if (stallComboBox.getSelectedItem() == null) return;
            String item = stallComboBox.getSelectedItem().toString();
            int stallId = Integer.parseInt(item.split(" - ")[0]);
            Stall stall = stallService.getStallById(stallId);
            if (stall == null) return;

            LocalDate start = LocalDate.parse(startDateField.getText().trim());
            LocalDate end = LocalDate.parse(endDateField.getText().trim());
            long days = end.toEpochDay() - start.toEpochDay() + 1;
            if (days <= 0) { totalAmountLabel.setText("Invalid dates"); return; }
            double total = days * stall.getDailyRate();
            totalAmountLabel.setText(String.format("₱%.2f  (%d days × ₱%.2f)", total, days, stall.getDailyRate()));
        } catch (DateTimeParseException ex) {
            totalAmountLabel.setText("Invalid date format");
        }
    }

    private void submitReservation() {
        try {
            if (stallComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "No available stall selected.");
                return;
            }
            String item = stallComboBox.getSelectedItem().toString();
            int stallId = Integer.parseInt(item.split(" - ")[0]);
            Stall stall = stallService.getStallById(stallId);

            LocalDate start = LocalDate.parse(startDateField.getText().trim());
            LocalDate end = LocalDate.parse(endDateField.getText().trim());
            String notes = notesField.getText().trim();

            String result = reservationService.createReservation(currentUser, stall, start, end, notes);
            if ("SUCCESS".equals(result)) {
                JOptionPane.showMessageDialog(this, "Reservation submitted! Awaiting manager approval.");
                refreshStallCombo();
                totalAmountLabel.setText("₱0.00");
                notesField.setText("");
                tabs.setSelectedIndex(0);
                DefaultTableModel model = (DefaultTableModel) ((JTable) ((JScrollPane)
                        ((JPanel) tabs.getComponentAt(0)).getComponent(1)).getViewport().getView()).getModel();
                refreshMyReservations(model);
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + result);
            }
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-MM-dd.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred. Please check your inputs.");
        }
    }

    private JPanel buildSalesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        String[] cols = {"Sale ID", "Stall", "Product", "Category", "Qty", "Unit Price", "Total", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        double grandTotal = 0;
        for (ProductSale sale : dataStore.getProductSales()) {
            if (sale.getReservation().getFarmer().getUserId() == currentUser.getUserId()) {
                model.addRow(new Object[]{sale.getSaleId(),
                        sale.getReservation().getStall().getStallNumber(),
                        sale.getProductName(), sale.getCategory(),
                        sale.getQuantity(), String.format("₱%.2f", sale.getUnitPrice()),
                        String.format("₱%.2f", sale.getTotalSale()), sale.getSaleDate()});
                grandTotal += sale.getTotalSale();
            }
        }

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("My Product Sales"));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(UITheme.BACKGROUND);
        JLabel totalLbl = new JLabel("Total Sales: " + String.format("₱%.2f", grandTotal));
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalLbl.setForeground(UITheme.PRIMARY_DARK);
        footerPanel.add(totalLbl);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton addSaleBtn = UITheme.createPrimaryButton("+ Record Sale");
        toolbar.add(addSaleBtn);

        addSaleBtn.addActionListener(e -> showAddSaleDialog(model, footerPanel));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void showAddSaleDialog(DefaultTableModel model, JPanel footerPanel) {
        List<Reservation> myReservations = reservationService.getReservationsByFarmer(currentUser.getUserId());
        List<Reservation> active = new java.util.ArrayList<>();
        for (Reservation r : myReservations) {
            if (r.getStatus() == Reservation.ReservationStatus.APPROVED || r.getStatus() == Reservation.ReservationStatus.COMPLETED) {
                active.add(r);
            }
        }
        if (active.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No active reservations. You need an approved reservation to record sales.");
            return;
        }

        JDialog dialog = new JDialog(this, "Record Product Sale", true);
        dialog.setSize(400, 380);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BACKGROUND);
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1; gbc.insets = new Insets(6, 4, 6, 4);

        JComboBox<String> resBox = new JComboBox<>();
        for (Reservation r : active) resBox.addItem(r.getReservationId() + " - " + r.getStall().getStallNumber());
        resBox.setFont(UITheme.FONT_NORMAL);

        JTextField productF = new JTextField(); productF.setFont(UITheme.FONT_NORMAL);
        JTextField catF = new JTextField(); catF.setFont(UITheme.FONT_NORMAL);
        JTextField qtyF = new JTextField(); qtyF.setFont(UITheme.FONT_NORMAL);
        JTextField priceF = new JTextField(); priceF.setFont(UITheme.FONT_NORMAL);

        String[] labels = {"Reservation:", "Product Name:", "Category:", "Quantity:", "Unit Price (₱):"};
        JComponent[] comps = {resBox, productF, catF, qtyF, priceF};
        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i]); lbl.setFont(UITheme.FONT_SUBHEADER);
            gbc.gridx = 0; gbc.gridy = i * 2; form.add(lbl, gbc);
            gbc.gridx = 0; gbc.gridy = i * 2 + 1; form.add(comps[i], gbc);
        }

        JButton saveBtn = UITheme.createPrimaryButton("Record Sale");
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
                String resStr = resBox.getSelectedItem().toString();
                int resId = Integer.parseInt(resStr.split(" - ")[0]);
                Reservation res = active.stream().filter(r -> r.getReservationId() == resId).findFirst().orElse(null);
                int qty = Integer.parseInt(qtyF.getText().trim());
                double price = Double.parseDouble(priceF.getText().trim());
                ProductSale sale = new ProductSale(0, res, productF.getText().trim(),
                        catF.getText().trim(), qty, price, LocalDate.now());
                dataStore.addProductSale(sale);
                model.addRow(new Object[]{sale.getSaleId(), res.getStall().getStallNumber(),
                        sale.getProductName(), sale.getCategory(), sale.getQuantity(),
                        String.format("₱%.2f", sale.getUnitPrice()),
                        String.format("₱%.2f", sale.getTotalSale()), sale.getSaleDate()});
                dialog.dispose();
                statusBar.setText("Sale recorded successfully.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid quantity or price.");
            }
        });
        dialog.setVisible(true);
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