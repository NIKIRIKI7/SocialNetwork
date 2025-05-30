package org.mcniki.gui;

import org.mcniki.domain.DUser;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class UserTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Login", "Email", "Active"};
    private List<DUser> users;

    public UserTableModel(List<DUser> users) {
        this.users = new ArrayList<>(users);
    }

    public void setUsers(List<DUser> users) {
        this.users = new ArrayList<>(users);
        fireTableDataChanged();
    }

    public DUser getUserAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < users.size()) {
            return users.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return users.size();
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
            case 1: return String.class; // Login
            case 2: return String.class; // Email
            case 3: return Boolean.class; // Active
            default: return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DUser user = users.get(rowIndex);
        switch (columnIndex) {
            case 0: return user.getId();
            case 1: return user.getLogin();
            case 2: return user.getEmail();
            case 3: return user.isActive();
            default: return null;
        }
    }
}