package org.mcniki.gui;

import org.mcniki.domain.DPost;
import org.mcniki.domain.DUser;

import javax.swing.table.AbstractTableModel;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PostTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "User", "Post Text (Preview)", "Post Date", "Published"};
    private List<DPost> posts;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public PostTableModel(List<DPost> posts) {
        this.posts = new ArrayList<>(posts);
    }

    public void setPosts(List<DPost> posts) {
        this.posts = new ArrayList<>(posts);
        fireTableDataChanged();
    }

    public DPost getPostAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < posts.size()) {
            return posts.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return posts.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Long.class; // ID
            case 1: return String.class; // User Login
            case 2: return String.class; // Post Text
            case 3: return String.class; // Post Date (formatted)
            case 4: return Boolean.class; // Published
            default: return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DPost post = posts.get(rowIndex);
        switch (columnIndex) {
            case 0: return post.getId();
            case 1:
                DUser user = post.getUser();
                return (user != null ? user.getLogin() : "N/A");
            case 2:
                String text = post.getPostText();
                return text != null ? text.substring(0, Math.min(text.length(), 50)) + (text.length() > 50 ? "..." : "") : "";
            case 3:
                OffsetDateTime postDate = post.getPostDate();
                return postDate != null ? postDate.format(DATE_FORMATTER) : "";
            case 4: return post.isPublished();
            default: return null;
        }
    }
}