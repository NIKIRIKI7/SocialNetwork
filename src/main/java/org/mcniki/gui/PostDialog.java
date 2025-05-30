package org.mcniki.gui;

import io.ebean.DB;
import org.mcniki.domain.DPost;
import org.mcniki.domain.DUser;
import org.mcniki.service.PostService;
import org.mcniki.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Vector;

public class PostDialog extends JDialog {
    private final MainApp parentApp;
    private final PostService postService;
    private final UserService userService;
    private final boolean isEditMode;
    private DPost postToEdit;
    private Long forUserIdPreselected; // User ID if "Add post for this user"

    private JComboBox<DUserItem> userComboBox;
    private JTextArea postTextArea;
    private JCheckBox publishedCheckBox;
    private JTextField postDateField; // For simplicity, can be improved with a date picker

    private boolean saved = false;

    // Helper class for JComboBox items
    private static class DUserItem {
        DUser user;
        public DUserItem(DUser user) { this.user = user; }
        public DUser getUser() { return user; }
        @Override public String toString() { return user != null ? user.getLogin() + " (ID: " + user.getId() + ")" : "Select User"; }
    }

    public PostDialog(MainApp parent, boolean isEditMode, Long postIdToEdit, Long forUserId,
                      PostService postService, UserService userService) {
        super(parent, (isEditMode ? "Edit Post" : "Add Post"), true);
        this.parentApp = parent;
        this.postService = postService;
        this.userService = userService;
        this.isEditMode = isEditMode;
        this.forUserIdPreselected = forUserId;

        if (isEditMode && postIdToEdit != null) {
            this.postToEdit = postService.findPostById(postIdToEdit);
            if (this.postToEdit == null) {
                JOptionPane.showMessageDialog(parent, "Post not found.", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }
        } else if (isEditMode) {
            JOptionPane.showMessageDialog(parent, "Post ID for edit is null.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }


        initComponents();
        loadUsers();
        pack();
        setLocationRelativeTo(parent);
        setSize(450, 400); // একটু বড়
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // User
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("User:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        userComboBox = new JComboBox<>();
        formPanel.add(userComboBox, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        // Post Text
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Post Text:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        postTextArea = new JTextArea(5, 20);
        postTextArea.setLineWrap(true);
        postTextArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(postTextArea), gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0; gbc.anchor = GridBagConstraints.WEST;


        // Post Date
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Post Date (ISO):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        postDateField = new JTextField(20);
        postDateField.setToolTipText("YYYY-MM-DDTHH:mm:ssZ or YYYY-MM-DDTHH:mm:ss+HH:MM");
        formPanel.add(postDateField, gbc);
        gbc.fill = GridBagConstraints.NONE;

        // Published
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Published:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        publishedCheckBox = new JCheckBox();
        formPanel.add(publishedCheckBox, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(this::savePostAction);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        if (isEditMode && postToEdit != null) {
            // User selection will be handled by loadUsers
            postTextArea.setText(postToEdit.getPostText());
            publishedCheckBox.setSelected(postToEdit.isPublished());
            postDateField.setText(postToEdit.getPostDate() != null ? postToEdit.getPostDate().toString() : "");
            userComboBox.setEnabled(false); // Typically user isn't changed when editing a post
        } else {
            // For new post
            postDateField.setText(OffsetDateTime.now().toString()); // Default to now
            publishedCheckBox.setSelected(false); // Default for new post
        }
    }

    private void loadUsers() {
        List<DUser> users = userService.findAllUsers();
        Vector<DUserItem> userItems = new Vector<>();
        DUserItem selectedItem = null;

        for (DUser u : users) {
            DUserItem item = new DUserItem(u);
            userItems.add(item);
            if (isEditMode && postToEdit != null && postToEdit.getUser() != null && postToEdit.getUser().getId() == u.getId()) {
                selectedItem = item;
            } else if (!isEditMode && forUserIdPreselected != null && forUserIdPreselected.equals(u.getId())) {
                selectedItem = item;
            }
        }
        userComboBox.setModel(new DefaultComboBoxModel<>(userItems));
        if (selectedItem != null) {
            userComboBox.setSelectedItem(selectedItem);
        }
        if (forUserIdPreselected != null && !isEditMode) {
            userComboBox.setEnabled(false); // Preselected user for new post, don't allow change
        }
    }

    private void savePostAction(ActionEvent e) {
        DUserItem selectedUserItem = (DUserItem) userComboBox.getSelectedItem();
        if (selectedUserItem == null || selectedUserItem.getUser() == null) {
            JOptionPane.showMessageDialog(this, "Please select a user.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        DUser selectedUser = selectedUserItem.getUser();

        String postText = postTextArea.getText().trim();
        if (postText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Post text cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        OffsetDateTime postDate;
        try {
            postDate = OffsetDateTime.parse(postDateField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Post Date format. Use ISO OffsetDateTime (e.g., 2023-01-01T10:00:00+01:00 or ...Z).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean published = publishedCheckBox.isSelected();

        try {
            DPost post;
            if (isEditMode) {
                post = postToEdit;
                // User of a post usually doesn't change, but if needed:
                // post.setUser(DB.reference(DUser.class, selectedUser.getId()));
            } else {
                post = new DPost(DB.reference(DUser.class, selectedUser.getId()), postText, postDate);
            }
            post.setPostText(postText);
            post.setPostDate(postDate);
            post.setPublished(published);

            if (isEditMode) {
                postService.updatePost(post);
            } else {
                // If saving directly through postService:
                postService.savePost(post);
                // Or if managing via user (assuming user is fetched and not just a reference):
                // DUser owner = userService.findUserById(selectedUser.getId());
                // owner.addPost(post); // This sets post.setUserInternal(this)
                // userService.updateUser(owner); // This would cascade save the post
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving post: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            DB.getDefault().currentTransaction().rollback();
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public void display() {
        setVisible(true);
    }
}