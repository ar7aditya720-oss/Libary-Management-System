package library.ui;

import library.service.LibraryService;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import java.awt.GridLayout;

public class DashboardPanel extends JPanel {
    private final LibraryService service;
    private final JLabel booksLabel = new JLabel();
    private final JLabel membersLabel = new JLabel();
    private final JLabel issuedLabel = new JLabel();
    private final JLabel overdueLabel = new JLabel();

    public DashboardPanel(LibraryService service) {
        this.service = service;
        setLayout(new GridLayout(2, 2, 16, 16));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(card("Total Books", booksLabel));
        add(card("Total Members", membersLabel));
        add(card("Currently Issued", issuedLabel));
        add(card("Overdue", overdueLabel));
    }

    private JPanel card(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 14f));
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 28f));
        valueLabel.setHorizontalAlignment(JLabel.LEFT);
        panel.add(titleLabel);
        panel.add(valueLabel);
        return panel;
    }

    public void refresh() {
        booksLabel.setText(String.valueOf(service.getBookCount()));
        membersLabel.setText(String.valueOf(service.getMemberCount()));
        issuedLabel.setText(String.valueOf(service.getIssuedCount()));
        overdueLabel.setText(String.valueOf(service.getOverdueCount()));
    }
}