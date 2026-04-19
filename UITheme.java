import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class UITheme {

    public static final Color PRIMARY = new Color(34, 139, 34);
    public static final Color PRIMARY_DARK = new Color(0, 100, 0);
    public static final Color PRIMARY_LIGHT = new Color(144, 238, 144);
    public static final Color ACCENT = new Color(255, 165, 0);
    public static final Color BACKGROUND = new Color(245, 250, 245);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(30, 30, 30);
    public static final Color TEXT_SECONDARY = new Color(90, 90, 90);
    public static final Color DANGER = new Color(200, 50, 50);
    public static final Color SUCCESS = new Color(34, 139, 34);
    public static final Color WARNING = new Color(220, 150, 0);
    public static final Color INFO = new Color(30, 130, 200);
    public static final Color TABLE_HEADER = new Color(34, 100, 34);
    public static final Color TABLE_ALT = new Color(240, 248, 240);
    public static final Color BORDER_COLOR = new Color(200, 220, 200);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_SUBHEADER);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_SUBHEADER);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(220, 220, 220));
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(FONT_SUBHEADER);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    public static JButton createWarningButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(WARNING);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_SUBHEADER);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    public static JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(PRIMARY_DARK);
        return lbl;
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        );
    }

    public static Border createTitledCardBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                new LineBorder(PRIMARY, 1, true), title, TitledBorder.LEFT, TitledBorder.TOP,
                FONT_SUBHEADER, PRIMARY_DARK);
        return BorderFactory.createCompoundBorder(tb, BorderFactory.createEmptyBorder(6, 8, 8, 8));
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_NORMAL);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(FONT_SUBHEADER);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setBackground(CARD_BG);
        table.setGridColor(BORDER_COLOR);
    }

    public static JPanel createStatCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color.darker(), 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(Color.WHITE);

        JLabel nameLbl = new JLabel(label, SwingConstants.CENTER);
        nameLbl.setFont(FONT_SUBHEADER);
        nameLbl.setForeground(new Color(255, 255, 255, 220));

        card.add(valLbl, BorderLayout.CENTER);
        card.add(nameLbl, BorderLayout.SOUTH);
        return card;
    }

    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }
}