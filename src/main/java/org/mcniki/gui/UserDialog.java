package org.mcniki.gui;

import io.ebean.DB;
import org.mcniki.domain.DUser;
import org.mcniki.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class UserDialog extends JDialog {
    private final MainApp parentApp;
    private final UserService userService;
    private final boolean isEditMode;
    private DUser userToEdit;

    private JTextField loginField;
    private JTextField emailField;
    private JCheckBox activeCheckBox;

    private boolean saved = false;

    public UserDialog(MainApp parent, boolean isEditMode, Long userIdToEdit, UserService userService) {
        super(parent, (isEditMode ? "Edit User" : "Add User"), true);
        this.parentApp = parent;
        this.userService = userService;
        this.isEditMode = isEditMode;

        if (isEditMode && userIdToEdit != null) {
            this.userToEdit = userService.findUserById(userIdToEdit);
            if (this.userToEdit == null) {
                JOptionPane.showMessageDialog(parent, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
        } else if (isEditMode) {
            JOptionPane.showMessageDialog(parent, "User ID for edit is null.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Login
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Login:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        loginField = new JTextField(20);
        formPanel.add(loginField, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;


        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Active
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Active:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        activeCheckBox = new JCheckBox();
        activeCheckBox.setSelected(true); // Default for new user
        formPanel.add(activeCheckBox, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(this::saveUserAction);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        if (isEditMode && userToEdit != null) {
            loginField.setText(userToEdit.getLogin());
            emailField.setText(userToEdit.getEmail());
            activeCheckBox.setSelected(userToEdit.isActive());
        }
    }

    private void saveUserAction(ActionEvent e) {
        String login = loginField.getText().trim();
        String email = emailField.getText().trim();
        boolean active = activeCheckBox.isSelected();

        if (login.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Login and Email cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            DUser user;
            if (isEditMode) {
                user = userToEdit;
                user.setLogin(login);
                user.setEmail(email);
                user.setActive(active);
                userService.updateUser(user);
            } else {
                user = new DUser(login, email);
                user.setActive(active);
                userService.saveUser(user);
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            // Catch potential unique constraint violations or other DB errors
            JOptionPane.showMessageDialog(this, "Error saving user: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            DB.getDefault().currentTransaction().rollback(); // Rollback on error
        }
    }

    public boolean isSaved() {
        return saved;
    }

    // The display() method is essentially showing the dialog, which happens on instantiation with modal=true.
    // Kept for consistency with your placeholder.
    public void display() {
        setVisible(true);
    }
}