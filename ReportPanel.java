import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ReportPanel extends JPanel {

    private MarketDataStore dataStore = MarketDataStore.getInstance();
    private JTabbedPane reportTabs;

    public ReportPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        buildUI();
    }

    private void buildUI() {
        JLabel header = UITheme.createSectionTitle("Report Generation");
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        reportTabs = new JTabbedPane();
        reportTabs.setFont(UITheme.FONT_NORMAL);
        reportTabs.addTab("Stall Reservation Report", buildStallReservationReport());
        reportTabs.addTab("Most Reserved Stalls", buildMostReservedReport());
        reportTabs.addTab("Farmer Reservation History", buildFarmerHistoryReport());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Reports");
        refreshBtn.addActionListener(e -> {
            int idx = reportTabs.getSelectedIndex();
            reportTabs.removeAll();
            reportTabs.addTab("Stall Reservation Report", buildStallReservationReport());
            reportTabs.addTab("Most Reserved Stalls", buildMostReservedReport());
            reportTabs.addTab("Farmer Reservation History", buildFarmerHistoryReport());
            reportTabs.setSelectedIndex(idx);
        });

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(header, BorderLayout.WEST);
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(refreshBtn);
        topRow.add(btnWrap, BorderLayout.EAST);

        add(topRow, BorderLayout.NORTH);
        add(reportTabs, BorderLayout.CENTER);
    }

    private JPanel buildStallReservationReport() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));

        // Summary stats
        List<Reservation> all = dataStore.getReservations();
        long total = all.size();
        long approved = all.stream().filter(r -> r.getStatus() == Reservation.ReservationStatus.APPROVED).count();
        long pending = all.stream().filter(r -> r.getStatus() == Reservation.ReservationStatus.PENDING).count();
        long completed = all.stream().filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED).count();
        long cancelled = all.stream().filter(r -> r.getStatus() == Reservation.ReservationStatus.CANCELLED).count();
        long rejected = all.stream().filter(r -> r.getStatus() == Reservation.ReservationStatus.REJECTED).count();
        double totalRevenue = all.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED || r.getStatus() == Reservation.ReservationStatus.APPROVED)
                .mapToDouble(Reservation::getTotalAmount).sum();

        JPanel statsRow = new JPanel(new GridLayout(1, 6, 8, 0));
        statsRow.setOpaque(false);
        statsRow.add(UITheme.createStatCard("Total", String.valueOf(total), new Color(60, 80, 160)));
        statsRow.add(UITheme.createStatCard("Approved", String.valueOf(approved), new Color(0, 140, 80)));
        statsRow.add(UITheme.createStatCard("Pending", String.valueOf(pending), new Color(180, 140, 0)));
        statsRow.add(UITheme.createStatCard("Completed", String.valueOf(completed), new Color(34, 100, 34)));
        statsRow.add(UITheme.createStatCard("Cancelled", String.valueOf(cancelled), new Color(160, 80, 0)));
        statsRow.add(UITheme.createStatCard("Rejected", String.valueOf(rejected), new Color(160, 40, 40)));

        // Detail table
        String[] cols = {"Reservation ID", "Farmer", "Stall No.", "Start Date", "End Date", "Days", "Status", "Amount"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Reservation r : all) {
            long days = r.getEndDate().toEpochDay() - r.getStartDate().toEpochDay() + 1;
            model.addRow(new Object[]{
                    r.getReservationId(),
                    r.getFarmer().getFullName(),
                    r.getStall().getStallNumber(),
                    r.getStartDate(),
                    r.getEndDate(),
                    days,
                    r.getStatus(),
                    String.format("₱%.2f", r.getTotalAmount())
            });
        }

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("Stall Reservation Detail Report"));

        JLabel revLbl = new JLabel("Total Expected Revenue: " + String.format("₱%.2f", totalRevenue));
        revLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        revLbl.setForeground(UITheme.PRIMARY_DARK);
        revLbl.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        panel.add(statsRow, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(revLbl, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildMostReservedReport() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));

        // Count reservations per stall
        Map<Integer, Long> countMap = dataStore.getReservations().stream()
                .collect(Collectors.groupingBy(r -> r.getStall().getStallId(), Collectors.counting()));

        Map<Integer, Double> revenueMap = dataStore.getReservations().stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED || r.getStatus() == Reservation.ReservationStatus.APPROVED)
                .collect(Collectors.groupingBy(r -> r.getStall().getStallId(),
                        Collectors.summingDouble(Reservation::getTotalAmount)));

        List<Stall> sortedStalls = new ArrayList<>(dataStore.getStalls());
        sortedStalls.sort((a, b) -> Long.compare(
                countMap.getOrDefault(b.getStallId(), 0L),
                countMap.getOrDefault(a.getStallId(), 0L)));

        String[] cols = {"Rank", "Stall No.", "Location", "Category", "Daily Rate", "Reservations", "Revenue"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        int rank = 1;
        for (Stall s : sortedStalls) {
            long cnt = countMap.getOrDefault(s.getStallId(), 0L);
            double rev = revenueMap.getOrDefault(s.getStallId(), 0.0);
            model.addRow(new Object[]{
                    rank++, s.getStallNumber(), s.getLocation(), s.getCategory(),
                    String.format("₱%.2f", s.getDailyRate()), cnt, String.format("₱%.2f", rev)
            });
        }

        JTable table = new JTable(model);
        UITheme.styleTable(table);

        // Highlight top 3
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) {
                    int r = (int) t.getValueAt(row, 0);
                    if (r == 1) c.setBackground(new Color(255, 215, 0, 120));
                    else if (r == 2) c.setBackground(new Color(192, 192, 192, 120));
                    else if (r == 3) c.setBackground(new Color(205, 127, 50, 100));
                    else c.setBackground(row % 2 == 0 ? Color.WHITE : UITheme.TABLE_ALT);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("Most Reserved Stalls (Ranked - Gold=1st, Silver=2nd, Bronze=3rd)"));

        // Bar chart visual
        JPanel barPanel = createBarChart(sortedStalls, countMap);
        barPanel.setPreferredSize(new Dimension(0, 160));

        panel.add(barPanel, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBarChart(List<Stall> stalls, Map<Integer, Long> countMap) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int margin = 40;
                int chartH = getHeight() - 50;
                int chartW = getWidth() - 2 * margin;
                int n = Math.min(stalls.size(), 10);
                if (n == 0) return;

                long maxVal = stalls.stream().mapToLong(s -> countMap.getOrDefault(s.getStallId(), 0L)).max().orElse(1);
                if (maxVal == 0) maxVal = 1;
                int barW = (chartW / n) - 8;

                g2.setColor(new Color(240, 248, 240));
                g2.fillRect(margin, 0, chartW, chartH);

                Color[] colors = {new Color(34, 139, 34), new Color(0, 100, 200), new Color(200, 100, 0),
                        new Color(120, 60, 160), new Color(0, 140, 140)};

                for (int i = 0; i < n; i++) {
                    Stall s = stalls.get(i);
                    long val = countMap.getOrDefault(s.getStallId(), 0L);
                    int barH = (int) ((val * (chartH - 20)) / maxVal);
                    int x = margin + i * (barW + 8);
                    int y = chartH - barH;

                    g2.setColor(colors[i % colors.length]);
                    g2.fillRoundRect(x, y, barW, barH, 6, 6);

                    g2.setColor(UITheme.TEXT_PRIMARY);
                    g2.setFont(UITheme.FONT_SMALL);
                    String label = s.getStallNumber();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(label, x + (barW - fm.stringWidth(label)) / 2, chartH + 14);
                    g2.drawString(String.valueOf(val), x + (barW - fm.stringWidth(String.valueOf(val))) / 2, y - 2);
                }

                g2.setColor(UITheme.TEXT_SECONDARY);
                g2.drawString("Reservations per Stall", margin, getHeight() - 2);
            }
        };
    }

    private JPanel buildFarmerHistoryReport() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 8, 8, 8));

        // Farmer selector
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topRow.setOpaque(false);
        JLabel selectLbl = new JLabel("Select Farmer:");
        selectLbl.setFont(UITheme.FONT_SUBHEADER);

        JComboBox<String> farmerBox = new JComboBox<>();
        farmerBox.addItem("All Farmers");
        List<User> farmers = dataStore.getUsers().stream()
                .filter(u -> u.getRole() == User.UserRole.FARMER).collect(Collectors.toList());
        for (User f : farmers) farmerBox.addItem(f.getUserId() + " - " + f.getFullName());
        farmerBox.setFont(UITheme.FONT_NORMAL);

        JButton filterBtn = UITheme.createPrimaryButton("Filter");
        topRow.add(selectLbl); topRow.add(farmerBox); topRow.add(filterBtn);

        String[] cols = {"Reservation ID", "Farmer", "Stall No.", "Location", "Start Date", "End Date", "Status", "Amount", "Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        loadFarmerHistory(model, null);

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createTitledCardBorder("Farmer Reservation History"));

        JLabel summaryLbl = new JLabel("Total Reservations: " + dataStore.getReservations().size());
        summaryLbl.setFont(UITheme.FONT_SUBHEADER);
        summaryLbl.setForeground(UITheme.PRIMARY_DARK);
        summaryLbl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        filterBtn.addActionListener(e -> {
            String selected = (String) farmerBox.getSelectedItem();
            Integer farmerId = null;
            if (selected != null && !selected.equals("All Farmers")) {
                farmerId = Integer.parseInt(selected.split(" - ")[0]);
            }
            loadFarmerHistory(model, farmerId);
            int count = model.getRowCount();
            String farmerName = farmerId == null ? "All Farmers" :
                    farmers.stream().filter(f -> f.getUserId() == Integer.parseInt(selected.split(" - ")[0]))
                            .map(User::getFullName).findFirst().orElse("");
            summaryLbl.setText("Reservations found: " + count + (farmerId != null ? " for " + farmerName : " (all farmers)"));
        });

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.add(summaryLbl, BorderLayout.WEST);

        panel.add(topRow, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottomRow, BorderLayout.SOUTH);
        return panel;
    }

    private void loadFarmerHistory(DefaultTableModel model, Integer farmerId) {
        model.setRowCount(0);
        List<Reservation> list = dataStore.getReservations().stream()
                .filter(r -> farmerId == null || r.getFarmer().getUserId() == farmerId)
                .collect(Collectors.toList());
        for (Reservation r : list) {
            model.addRow(new Object[]{
                    r.getReservationId(),
                    r.getFarmer().getFullName(),
                    r.getStall().getStallNumber(),
                    r.getStall().getLocation(),
                    r.getStartDate(),
                    r.getEndDate(),
                    r.getStatus(),
                    String.format("₱%.2f", r.getTotalAmount()),
                    r.getNotes()
            });
        }
    }
}