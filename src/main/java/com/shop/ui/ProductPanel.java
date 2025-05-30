package com.shop.ui;

import com.shop.model.Category;
import com.shop.model.Product;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductPanel extends JPanel {
    
    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchTypeComboBox;
    private JComboBox<Category> categoryFilterComboBox;
    
    public ProductPanel() {
        setLayout(new BorderLayout());
        
        // Создание верхней панели с поиском и фильтрами
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        searchField = new JTextField(20);
        searchTypeComboBox = new JComboBox<>(new String[]{"По названию", "По артикулу"});
        JButton searchButton = new JButton("Поиск");
        
        categoryFilterComboBox = new JComboBox<>();
        categoryFilterComboBox.addItem(null); // Пустой элемент для "Все категории"
        List<Category> categories = categoryService.findAll();
        for (Category category : categories) {
            categoryFilterComboBox.addItem(category);
        }
        categoryFilterComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    value = "Все категории";
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        
        JButton refreshButton = new JButton("Обновить");
        JButton addButton = new JButton("Добавить товар");
        JButton editButton = new JButton("Редактировать");
        JButton deleteButton = new JButton("Удалить");
        
        topPanel.add(new JLabel("Поиск:"));
        topPanel.add(searchField);
        topPanel.add(searchTypeComboBox);
        topPanel.add(searchButton);
        topPanel.add(new JLabel("Категория:"));
        topPanel.add(categoryFilterComboBox);
        topPanel.add(refreshButton);
        topPanel.add(addButton);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Создание таблицы товаров
        String[] columnNames = {"ID", "Артикул", "Название", "Цена", "Количество", "Категория"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Загрузка данных
        loadProducts();
        
        // Добавление обработчиков событий
        searchButton.addActionListener(e -> searchProducts());
        refreshButton.addActionListener(e -> loadProducts());
        categoryFilterComboBox.addActionListener(e -> filterByCategory());
        
        addButton.addActionListener(e -> showProductDialog(null));
        
        editButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow >= 0) {
                Long productId = (Long) tableModel.getValueAt(selectedRow, 0);
                Product product = productService.findById(productId);
                showProductDialog(product);
            } else {
                JOptionPane.showMessageDialog(this, "Выберите товар для редактирования.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow >= 0) {
                Long productId = (Long) tableModel.getValueAt(selectedRow, 0);
                productService.delete(productId);
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Выберите товар для удаления.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void loadProducts() {
        List<Product> products = productService.findAll();
        updateTable(products);
    }
    
    private void searchProducts() {
        String query = searchField.getText();
        String searchType = (String) searchTypeComboBox.getSelectedItem();
        List<Product> products = productService.search(query, searchType);
        updateTable(products);
    }
    
    private void filterByCategory() {
        Category category = (Category) categoryFilterComboBox.getSelectedItem();
        List<Product> products = productService.findByCategory(category);
        updateTable(products);
    }
    
    private void updateTable(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product product : products) {
            Object[] row = {
                product.getId(),
                product.getArticle(),
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory().getName()
            };
            tableModel.addRow(row);
        }
    }
    
    private void showProductDialog(Product product) {
        ProductDialog dialog = new ProductDialog(product);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadProducts();
        }
    }
}
