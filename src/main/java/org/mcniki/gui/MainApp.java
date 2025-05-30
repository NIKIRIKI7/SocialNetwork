package org.mcniki.gui;

import io.ebean.DB;
import io.ebean.Database;
import org.mcniki.domain.DPost;
import org.mcniki.domain.DUser;
import org.mcniki.service.PostService;
import org.mcniki.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.OffsetDateTime;
import java.util.Collections;

public class MainApp extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    private UserService userService;
    private PostService postService;

    private JTable userTable;
    private UserTableModel userTableModel;
    private JTable postTable;
    private PostTableModel postTableModel;

    private JButton editUserButton;
    private JButton deleteUserButton;
    private JButton addPostForUserButton;

    private JButton editPostButton;
    private JButton deletePostButton;

    public MainApp() {
        Database server = DB.getDefault();
        logger.info("Ebean server initialized: {}", server.name());
        this.userService = new UserService(server);
        this.postService = new PostService(server);

        setTitle("Ebean CRUD GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // Center window

        initComponents();
        loadInitialData();
        refreshUserTable();
    }

    private void initComponents() {
        JPanel userPanel = new JPanel(new BorderLayout(5,5));
        userPanel.setBorder(BorderFactory.createTitledBorder("Users"));

        userTableModel = new UserTableModel(Collections.emptyList());
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getSelectionModel().addListSelectionListener(this::userSelectionChanged);
        JScrollPane userScrollPane = new JScrollPane(userTable);
        userPanel.add(userScrollPane, BorderLayout.CENTER);

        JPanel userButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addUserButton = new JButton("Add User");
        addUserButton.addActionListener(this::addUserAction);
        editUserButton = new JButton("Edit User");
        editUserButton.addActionListener(this::editUserAction);
        editUserButton.setEnabled(false);
        deleteUserButton = new JButton("Delete User");
        deleteUserButton.addActionListener(this::deleteUserAction);
        deleteUserButton.setEnabled(false);
        addPostForUserButton = new JButton("Add Post for User");
        addPostForUserButton.addActionListener(this::addPostForSelectedUserAction);
        addPostForUserButton.setEnabled(false);
        JButton refreshUsersButton = new JButton("Refresh Users");
        refreshUsersButton.addActionListener(e -> refreshUserTable());


        userButtonPanel.add(addUserButton);
        userButtonPanel.add(editUserButton);
        userButtonPanel.add(deleteUserButton);
        userButtonPanel.add(addPostForUserButton);
        userButtonPanel.add(refreshUsersButton);
        userPanel.add(userButtonPanel, BorderLayout.SOUTH);

        JPanel postPanel = new JPanel(new BorderLayout(5,5));
        postPanel.setBorder(BorderFactory.createTitledBorder("Posts"));

        postTableModel = new PostTableModel(Collections.emptyList());
        postTable = new JTable(postTableModel);
        postTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        postTable.getSelectionModel().addListSelectionListener(this::postSelectionChanged);
        JScrollPane postScrollPane = new JScrollPane(postTable);
        postPanel.add(postScrollPane, BorderLayout.CENTER);

        JPanel postButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addGenericPostButton = new JButton("Add Any Post"); // Add post not tied to current user selection
        addGenericPostButton.addActionListener(this::addAnyPostAction);
        editPostButton = new JButton("Edit Post");
        editPostButton.addActionListener(this::editPostAction);
        editPostButton.setEnabled(false);
        deletePostButton = new JButton("Delete Post");
        deletePostButton.addActionListener(this::deletePostAction);
        deletePostButton.setEnabled(false);
        JButton showAllPostsButton = new JButton("Show All Posts");
        showAllPostsButton.addActionListener(e -> refreshPostTableAll());

        postButtonPanel.add(addGenericPostButton);
        postButtonPanel.add(editPostButton);
        postButtonPanel.add(deletePostButton);
        postButtonPanel.add(showAllPostsButton);
        postPanel.add(postButtonPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, userPanel, postPanel);
        splitPane.setResizeWeight(0.4); // Give users panel a bit less space initially
        add(splitPane);
    }

    private void loadInitialData() {
        if (userService.findAllUsers().isEmpty()) {
            logger.info("Database is empty. Seeding initial data...");
            DUser user1 = new DUser("john_doe", "john.doe@mcniki.com");
            userService.saveUser(user1);

            DUser user2 = new DUser("jane_smith", "jane.smith@mcniki.com");
            userService.saveUser(user2);

            DPost post1 = new DPost(DB.reference(DUser.class, user1.getId()), "John's first GUI post!", OffsetDateTime.now());
            post1.setPublished(true);
            postService.savePost(post1);

            DPost post2 = new DPost(DB.reference(DUser.class, user1.getId()), "Another one from John via GUI.", OffsetDateTime.now().plusHours(1));
            postService.savePost(post2);

            DPost post3 = new DPost(DB.reference(DUser.class, user2.getId()), "Jane's GUI contribution.", OffsetDateTime.now().plusMinutes(30));
            post3.setPublished(true);
            postService.savePost(post3);
            logger.info("Initial data seeded.");
        }
    }

    private void refreshUserTable() {
        try {
            userTableModel.setUsers(userService.findAllUsers());
        } catch (Exception e) {
            logger.error("Error refreshing user table", e);
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshPostTableForUser(DUser user) {
        if (user == null) {
            postTableModel.setPosts(Collections.emptyList());
            return;
        }
        try {
            postTableModel.setPosts(postService.findPostsByUser(user));
        } catch (Exception e) {
            logger.error("Error refreshing post table for user {}", user.getLogin(), e);
            JOptionPane.showMessageDialog(this, "Error loading posts for user: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshPostTableAll() {
        try {
            postTableModel.setPosts(postService.findAllPosts());
        } catch (Exception e) {
            logger.error("Error refreshing all posts table", e);
            JOptionPane.showMessageDialog(this, "Error loading all posts: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void userSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            DUser selectedUser = getSelectedUser();
            boolean userSelected = (selectedUser != null);
            editUserButton.setEnabled(userSelected);
            deleteUserButton.setEnabled(userSelected);
            addPostForUserButton.setEnabled(userSelected);

            if (userSelected) {
                refreshPostTableForUser(selectedUser);
            } else {
                postTableModel.setPosts(Collections.emptyList()); // Clear posts if no user selected
            }
            // Reset post button states as post selection might become invalid
            editPostButton.setEnabled(false);
            deletePostButton.setEnabled(false);
        }
    }

    private void postSelectionChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            boolean postSelected = (getSelectedPost() != null);
            editPostButton.setEnabled(postSelected);
            deletePostButton.setEnabled(postSelected);
        }
    }

    private DUser getSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            return userTableModel.getUserAt(userTable.convertRowIndexToModel(selectedRow));
        }
        return null;
    }

    private DPost getSelectedPost() {
        int selectedRow = postTable.getSelectedRow();
        if (selectedRow >= 0) {
            return postTableModel.getPostAt(postTable.convertRowIndexToModel(selectedRow));
        }
        return null;
    }

    // --- User Actions ---
    private void addUserAction(ActionEvent e) {
        UserDialog dialog = new UserDialog(this, false, null, userService);
        dialog.display(); // Or dialog.setVisible(true)
        if (dialog.isSaved()) {
            refreshUserTable();
        }
    }

    private void editUserAction(ActionEvent e) {
        DUser selectedUser = getSelectedUser();
        if (selectedUser != null) {
            UserDialog dialog = new UserDialog(this, true, selectedUser.getId(), userService);
            dialog.display();
            if (dialog.isSaved()) {
                refreshUserTable();
            }
        }
    }

    private void deleteUserAction(ActionEvent e) {
        DUser selectedUser = getSelectedUser();
        if (selectedUser != null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete user '" + selectedUser.getLogin() + "'?\nThis will also delete all their posts.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    userService.deleteUserById(selectedUser.getId());
                    refreshUserTable();
                    // Since user is deleted, their posts are gone too.
                    // If "Show All Posts" was active, refresh it. Otherwise, clear post table.
                    DUser stillSelectedUser = getSelectedUser(); // Check if another user got selected
                    if (stillSelectedUser != null) {
                        refreshPostTableForUser(stillSelectedUser);
                    } else {
                        refreshPostTableAll(); // Or clear postTableModel.setPosts(Collections.emptyList());
                    }
                } catch (Exception ex) {
                    logger.error("Error deleting user", ex);
                    JOptionPane.showMessageDialog(this, "Error deleting user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // --- Post Actions ---
    private void addPostForSelectedUserAction(ActionEvent e) {
        DUser selectedUser = getSelectedUser();
        if (selectedUser != null) {
            PostDialog dialog = new PostDialog(this, false, null, selectedUser.getId(), postService, userService);
            dialog.display();
            if (dialog.isSaved()) {
                refreshPostTableForUser(selectedUser); // Refresh posts for the current user
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user first to add a post for.", "No User Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addAnyPostAction(ActionEvent e) {
        // Opens post dialog allowing user selection from dropdown
        PostDialog dialog = new PostDialog(this, false, null, null, postService, userService);
        dialog.display();
        if (dialog.isSaved()) {
            // Determine how to refresh. If a user was selected in user table, refresh their posts.
            // Otherwise, refresh all posts or the posts of the user who just got a new post.
            DUser currentTableUser = getSelectedUser();
            if (currentTableUser != null && dialog.isSaved()) { // Check if the new post belongs to the currently selected user
                // A bit tricky to know which user the post was for without more info from dialog
                // Simplest is to refresh all or for current user.
                refreshPostTableForUser(currentTableUser);
            } else {
                refreshPostTableAll(); // Or refresh based on the user selected in the PostDialog if possible
            }
        }
    }


    private void editPostAction(ActionEvent e) {
        DPost selectedPost = getSelectedPost();
        if (selectedPost != null) {
            // The forUserId parameter isn't strictly needed for edit mode if PostDialog handles user from postToEdit
            PostDialog dialog = new PostDialog(this, true, selectedPost.getId(), selectedPost.getUser().getId(), postService, userService);
            dialog.display();
            if (dialog.isSaved()) {
                DUser userOfPost = selectedPost.getUser(); // Potentially stale if user was changed, but usually not
                // Or: DUser userOfPost = userService.findUserById(selectedPost.getUser().getId());
                if (getSelectedUser() != null && getSelectedUser().getId() == userOfPost.getId()) {
                    refreshPostTableForUser(getSelectedUser());
                } else {
                    refreshPostTableAll(); // If post was from a different user or "all posts" view
                }
            }
        }
    }

    private void deletePostAction(ActionEvent e) {
        DPost selectedPost = getSelectedPost();
        if (selectedPost != null) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this post?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    postService.deletePostById(selectedPost.getId());
                    DUser userOfPost = selectedPost.getUser();
                    if (getSelectedUser() != null && getSelectedUser().getId() == userOfPost.getId()) {
                        refreshPostTableForUser(getSelectedUser());
                    } else {
                        refreshPostTableAll(); // If post was from a different user or "all posts" view
                    }
                } catch (Exception ex) {
                    logger.error("Error deleting post", ex);
                    JOptionPane.showMessageDialog(this, "Error deleting post: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // The display() method for MainApp is essentially showing the frame.
    // This is typically done in main method.
    public void display() {
        setVisible(true);
    }

    public static void main(String[] args) {
        // Set Look and Feel to system default for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Could not set system look and feel", e);
        }

        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.display();
        });
    }
}