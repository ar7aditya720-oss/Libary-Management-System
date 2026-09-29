package library.ui;

import library.model.Book;
import library.model.Member;
import library.model.Transaction;
import library.service.LibraryService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

public class IssueReturnPanel extends JPanel {
    private final LibraryService service;
    private final Runnable onChange;

    private final JComboBox<Book> bookCombo = new JComboBox<>();
    private final JComboBox<Member> memberCombo = new JComboBox<>();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Tx ID", "Book", "Member", "Issue Date", "Due Date", "Status", "Fine (Rs)"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public IssueReturnPanel(LibraryService service, Runnable onChange) {
        this.service = service;
        this.onChange = onChange;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(buildIssueForm(), BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton returnBtn = new JButton("Return Selected");
        returnBtn.addActionListener(e -> returnSelected());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        south.add(returnBtn);
        south.add(refreshBtn);
        south.add(new JLabel("Loan period: " + LibraryService.LOAN_DAYS
                + " days | Fine: Rs " + (int) LibraryService.FINE_PER_DAY + "/day overdue"));
        add(south, BorderLayout.SOUTH);
    }

    private JPanel buildIssueForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new TitledBorder("Issue Book"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Book:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(bookCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Member:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(memberCombo, gbc);

        JButton issueBtn = new JButton("Issue");
        issueBtn.addActionListener(e -> issueBook());
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0;
        form.add(issueBtn, gbc);

        return form;
    }

    public void refresh() {
        Book selectedBook = (Book) bookCombo.getSelectedItem();
        Member selectedMember = (Member) memberCombo.getSelectedItem();

        bookCombo.removeAllItems();
        for (Book b : service.getAllBooks()) {
            bookCombo.addItem(b);
        }
        memberCombo.removeAllItems();
        for (Member m : service.getAllMembers()) {
            memberCombo.addItem(m);
        }

        if (selectedBook != null) {
            selectBookById(selectedBook.getId());
        }
        if (selectedMember != null) {
            selectMemberById(selectedMember.getId());
        }

        tableModel.setRowCount(0);
        List<Transaction> transactions = service.getAllTransactions();
        for (int i = transactions.size() - 1; i >= 0; i--) {
            Transaction tx = transactions.get(i);
            Book book = service.getBookById(tx.getBookId());
            Member member = service.getMemberById(tx.getMemberId());
            String bookLabel = book != null ? book.getTitle() : ("#" + tx.getBookId());
            String memberLabel = member != null ? member.getName() : ("#" + tx.getMemberId());
            String status = tx.isActive()
                    ? (tx.getDueDate().isBefore(java.time.LocalDate.now()) ? "OVERDUE" : "ISSUED")
                    : "RETURNED";
            double fine = tx.isActive() ? service.calculateCurrentFine(tx) : tx.getFine();
            tableModel.addRow(new Object[]{
                    tx.getId(),
                    bookLabel,
                    memberLabel,
                    tx.getIssueDate().toString(),
                    tx.getDueDate().toString(),
                    status,
                    String.format("%.2f", fine)
            });
        }
    }

    private void selectBookById(int id) {
        for (int i = 0; i < bookCombo.getItemCount(); i++) {
            if (bookCombo.getItemAt(i).getId() == id) {
                bookCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectMemberById(int id) {
        for (int i = 0; i < memberCombo.getItemCount(); i++) {
            if (memberCombo.getItemAt(i).getId() == id) {
                memberCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void issueBook() {
        Book book = (Book) bookCombo.getSelectedItem();
        Member member = (Member) memberCombo.getSelectedItem();
        if (book == null || member == null) {
            showError("Select both a book and a member.");
            return;
        }
        try {
            Transaction tx = service.issueBook(book.getId(), member.getId());
            onChange.run();
            JOptionPane.showMessageDialog(this,
                    "Issued successfully.\nDue date: " + tx.getDueDate());
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void returnSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a transaction to return.");
            return;
        }
        int txId = (Integer) tableModel.getValueAt(row, 0);
        String status = String.valueOf(tableModel.getValueAt(row, 5));
        if ("RETURNED".equals(status)) {
            showError("This transaction is already returned.");
            return;
        }
        try {
            Transaction tx = service.returnBook(txId);
            onChange.run();
            String msg = "Book returned.";
            if (tx.getFine() > 0) {
                msg += "\nOverdue fine: Rs " + String.format("%.2f", tx.getFine());
            } else {
                msg += "\nNo fine.";
            }
            JOptionPane.showMessageDialog(this, msg);
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}