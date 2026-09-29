package library.ui;

import library.model.Member;
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

public class MembersPanel extends JPanel {
    private final LibraryService service;
    private final Runnable onChange;

    private final JTextField searchField = new JTextField(18);
    private final JTextField nameField = new JTextField(16);
    private final JTextField emailField = new JTextField(16);
    private final JTextField phoneField = new JTextField(16);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Email", "Phone"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private Integer selectedId = null;

    public MembersPanel(LibraryService service, Runnable onChange) {
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
        form.setBorder(new TitledBorder("Member Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        int row = 0;
        gbc.gridy = row++;
        form.add(new JLabel("Name"), gbc);
        gbc.gridy = row++;
        form.add(nameField, gbc);
        gbc.gridy = row++;
        form.add(new JLabel("Email"), gbc);
        gbc.gridy = row++;
        form.add(emailField, gbc);
        gbc.gridy = row++;
        form.add(new JLabel("Phone"), gbc);
        gbc.gridy = row++;
        form.add(phoneField, gbc);

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");

        addBtn.addActionListener(e -> addMember());
        updateBtn.addActionListener(e -> updateMember());
        deleteBtn.addActionListener(e -> deleteMember());
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
        List<Member> members = service.searchMembers(searchField.getText());
        tableModel.setRowCount(0);
        for (Member m : members) {
            tableModel.addRow(new Object[]{m.getId(), m.getName(), m.getEmail(), m.getPhone()});
        }
    }

    private void loadSelectedIntoForm() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedId = (Integer) tableModel.getValueAt(row, 0);
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        emailField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        phoneField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
    }

    private void addMember() {
        try {
            service.addMember(nameField.getText(), emailField.getText(), phoneField.getText());
            clearForm();
            onChange.run();
            JOptionPane.showMessageDialog(this, "Member added.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateMember() {
        if (selectedId == null) {
            showError("Select a member to update.");
            return;
        }
        try {
            service.updateMember(selectedId, nameField.getText(), emailField.getText(), phoneField.getText());
            onChange.run();
            JOptionPane.showMessageDialog(this, "Member updated.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteMember() {
        if (selectedId == null) {
            showError("Select a member to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected member?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.deleteMember(selectedId);
            clearForm();
            onChange.run();
            JOptionPane.showMessageDialog(this, "Member deleted.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void clearForm() {
        selectedId = null;
        table.clearSelection();
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}