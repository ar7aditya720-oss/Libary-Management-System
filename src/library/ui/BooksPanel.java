package library.ui;

import library.model.Book;
import library.service.LibraryService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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

public class BooksPanel extends JPanel {
    private final LibraryService service;
    private final Runnable onChange;

    private final JTextField searchField = new JTextField(18);
    private final JTextField isbnField = new JTextField(16);
    private final JTextField titleField = new JTextField(16);
    private final JTextField authorField = new JTextField(16);
    private final JTextField copiesField = new JTextField(16);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "ISBN", "Title", "Author", "Total", "Available"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private Integer selectedId = null;

    public BooksPanel(LibraryService service, Runnable onChange) {
        this.service = service;
        this.onChange = onChange;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(searchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedIntoForm();
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new TitledBorder("Book Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        int row = 0;
        gbc.gridy = row++;
        form.add(new JLabel("ISBN"), gbc);
        gbc.gridy = row++;
        form.add(isbnField, gbc);
        gbc.gridy = row++;
        form.add(new JLabel("Title"), gbc);
        gbc.gridy = row++;
        form.add(titleField, gbc);
        gbc.gridy = row++;
        form.add(new JLabel("Author"), gbc);
        gbc.gridy = row++;
        form.add(authorField, gbc);
        gbc.gridy = row++;
        form.add(new JLabel("Total Copies"), gbc);
        gbc.gridy = row++;
        form.add(copiesField, gbc);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        addBtn.addActionListener(e -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        deleteBtn.addActionListener(e -> deleteBook());
        clearBtn.addActionListener(e -> clearForm());

        gbc.gridy = row++;
        form.add(addBtn, gbc);
        gbc.gridy = row++;
        form.add(updateBtn, gbc);
        gbc.gridy = row++;
        form.add(deleteBtn, gbc);
        gbc.gridy = row;
        form.add(clearBtn, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    public void refresh() {
        List<Book> books = service.searchBooks(searchField.getText());
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(),
                    b.getTotalCopies(), b.getAvailableCopies()
            });
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedId = (Integer) tableModel.getValueAt(row, 0);
        isbnField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        titleField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        authorField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        copiesField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
    }

    private void addBook() {
        try {
            int copies = Integer.parseInt(copiesField.getText().trim());
            service.addBook(isbnField.getText(), titleField.getText(), authorField.getText(), copies);
            clearForm();
            onChange.run();
            JOptionPane.showMessageDialog(this, "Book added.");
        } catch (NumberFormatException ex) {
            showError("Total copies must be a number.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateBook() {
        if (selectedId == null) {
            showError("Select a book to update.");
            return;
        }
        try {
            int copies = Integer.parseInt(copiesField.getText().trim());
            service.updateBook(selectedId, isbnField.getText(), titleField.getText(),
                    authorField.getText(), copies);
            onChange.run();
            JOptionPane.showMessageDialog(this, "Book updated.");
        } catch (NumberFormatException ex) {
            showError("Total copies must be a number.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteBook() {
        if (selectedId == null) {
            showError("Select a book to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected book?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.deleteBook(selectedId);
            clearForm();
            onChange.run();
            JOptionPane.showMessageDialog(this, "Book deleted.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void clearForm() {
        selectedId = null;
        table.clearSelection();
        isbnField.setText("");
        titleField.setText("");
        authorField.setText("");
        copiesField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}