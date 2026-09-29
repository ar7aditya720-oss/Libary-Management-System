package library.ui;

import library.service.LibraryService;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

public class MainFrame extends JFrame {
    private final LibraryService service = new LibraryService();
    private final DashboardPanel dashboardPanel;
    private final BooksPanel booksPanel;
    private final MembersPanel membersPanel;
    private final IssueReturnPanel issueReturnPanel;

    public MainFrame() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 620);
        setLocationRelativeTo(null);

        dashboardPanel = new DashboardPanel(service);
        booksPanel = new BooksPanel(service, this::refreshAll);
        membersPanel = new MembersPanel(service, this::refreshAll);
        issueReturnPanel = new IssueReturnPanel(service, this::refreshAll);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(new EmptyBorder(8, 8, 8, 8));
        tabs.addTab("Dashboard", dashboardPanel);
        tabs.addTab("Books", booksPanel);
        tabs.addTab("Members", membersPanel);
        tabs.addTab("Issue / Return", issueReturnPanel);
        tabs.addChangeListener(e -> refreshAll());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
        refreshAll();
    }

    private void refreshAll() {
        dashboardPanel.refresh();
        booksPanel.refresh();
        membersPanel.refresh();
        issueReturnPanel.refresh();
    }
}