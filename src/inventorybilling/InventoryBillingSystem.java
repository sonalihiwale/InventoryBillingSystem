
package inventorybilling;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class InventoryBillingSystem {

    // Collections to store inventory data
    static Map<Integer, String> itemNames = new HashMap<>();
    static Map<Integer, Double> itemPrices = new HashMap<>();
    static Map<Integer, Integer> itemStocks = new HashMap<>();

    // File to store inventory permanently
    static final Path FILE_PATH = Path.of("inventory.txt");

    public static void main(String[] args) {

        // Load saved inventory when application starts
        loadInventory();

        JFrame frame = new JFrame("Inventory Billing System");

        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JLabel title = new JLabel(
            "INVENTORY BILLING SYSTEM",
            SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 24));

        JButton addButton = new JButton("Add Items");
        JButton updateButton = new JButton("Update Stock");
        JButton billButton = new JButton("Generate Bills");

        addButton.addActionListener(
            e -> openAddItemWindow(frame)
        );

        updateButton.addActionListener(
            e -> openUpdateStockWindow(frame)
        );

        billButton.addActionListener(
            e -> openGenerateBillWindow(frame)
        );

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10));

        panel.add(title);
        panel.add(addButton);
        panel.add(updateButton);
        panel.add(billButton);

        frame.add(panel);
        frame.setVisible(true);
    }

    // Save inventory to file
    static boolean saveInventory() {

        try (BufferedWriter writer = Files.newBufferedWriter(
                FILE_PATH, StandardCharsets.UTF_8)) {

            for (Integer id : itemNames.keySet()) {

                String name = itemNames.get(id);

                // Encode name so special characters do not
                // interfere with the file format
                String encodedName = Base64.getEncoder()
                    .encodeToString(
                        name.getBytes(StandardCharsets.UTF_8)
                    );

                double price = itemPrices.get(id);
                int stock = itemStocks.get(id);

                writer.write(
                    id + "|" + encodedName + "|" + price + "|" + stock
                );

                writer.newLine();
            }

            return true;

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                null,
                "Could not save inventory file!\n" + ex.getMessage()
            );

            return false;
        }
    }

    // Load inventory from file
    static void loadInventory() {

        if (!Files.exists(FILE_PATH)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(
                FILE_PATH, StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split("\\|", -1);

                if (data.length != 4) {
                    throw new IllegalArgumentException(
                        "Invalid inventory data."
                    );
                }

                int id = Integer.parseInt(data[0]);

                String name = new String(
                    Base64.getDecoder().decode(data[1]),
                    StandardCharsets.UTF_8
                );

                double price = Double.parseDouble(data[2]);
                int stock = Integer.parseInt(data[3]);

                if (id <= 0 || name.isEmpty()
                        || price <= 0 || stock < 0) {

                    throw new IllegalArgumentException(
                        "Invalid item details in inventory file."
                    );
                }

                itemNames.put(id, name);
                itemPrices.put(id, price);
                itemStocks.put(id, stock);
            }

        } catch (IOException | IllegalArgumentException ex) {

            itemNames.clear();
            itemPrices.clear();
            itemStocks.clear();

            JOptionPane.showMessageDialog(
                null,
                "Could not load inventory data.\n"
                    + "Check inventory.txt.\n"
                    + ex.getMessage()
            );
        }
    }

    // Feature 1: Add Items
    static void openAddItemWindow(JFrame parent) {

        JDialog dialog = new JDialog(parent, "Add Items", true);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();

        dialog.add(new JLabel("Item ID:"));
        dialog.add(idField);

        dialog.add(new JLabel("Item Name:"));
        dialog.add(nameField);

        dialog.add(new JLabel("Price:"));
        dialog.add(priceField);

        dialog.add(new JLabel("Stock Quantity:"));
        dialog.add(stockField);

        JButton saveButton = new JButton("Add Item");

        dialog.add(new JLabel(""));
        dialog.add(saveButton);

        saveButton.addActionListener(e -> {

            try {
                int id = Integer.parseInt(
                    idField.getText().trim()
                );

                String name = nameField.getText().trim();

                double price = Double.parseDouble(
                    priceField.getText().trim()
                );

                int stock = Integer.parseInt(
                    stockField.getText().trim()
                );

                if (id <= 0 || name.isEmpty()
                        || price <= 0 || stock < 0) {

                    JOptionPane.showMessageDialog(
                        dialog,
                        "Enter valid details. Price must be positive and stock cannot be negative."
                    );
                    return;
                }

                if (itemNames.containsKey(id)) {

                    JOptionPane.showMessageDialog(
                        dialog,
                        "Item ID already exists!"
                    );
                    return;
                }

                itemNames.put(id, name);
                itemPrices.put(id, price);
                itemStocks.put(id, stock);

                // Save newly added item
                if (!saveInventory()) {
                    itemNames.remove(id);
                    itemPrices.remove(id);
                    itemStocks.remove(id);
                    return;
                }

                JOptionPane.showMessageDialog(
                    dialog,
                    "Item added successfully!"
                );

                dialog.dispose();

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                    dialog,
                    "Enter valid numeric values for ID, price and stock."
                );
            }
        });

        dialog.setVisible(true);
    }

    // Feature 2: Update Stock
    static void openUpdateStockWindow(JFrame parent) {

        JDialog dialog = new JDialog(
            parent, "Update Stock", true
        );

        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new GridLayout(3, 2, 10, 10));

        JTextField idField = new JTextField();
        JTextField stockField = new JTextField();

        dialog.add(new JLabel("Item ID:"));
        dialog.add(idField);

        dialog.add(new JLabel("New Stock Quantity:"));
        dialog.add(stockField);

        JButton updateButton = new JButton("Update Stock");

        dialog.add(new JLabel(""));
        dialog.add(updateButton);

        updateButton.addActionListener(e -> {

            try {
                int id = Integer.parseInt(
                    idField.getText().trim()
                );

                int newStock = Integer.parseInt(
                    stockField.getText().trim()
                );

                if (!itemNames.containsKey(id)) {

                    JOptionPane.showMessageDialog(
                        dialog,
                        "Item ID not found!"
                    );
                    return;
                }

                if (newStock < 0) {

                    JOptionPane.showMessageDialog(
                        dialog,
                        "Stock quantity cannot be negative!"
                    );
                    return;
                }

                int oldStock = itemStocks.get(id);

                itemStocks.put(id, newStock);

                // Save updated stock
                if (!saveInventory()) {
                    itemStocks.put(id, oldStock);
                    return;
                }

                JOptionPane.showMessageDialog(
                    dialog,
                    "Stock updated successfully!\n"
                        + "Item ID: " + id + "\n"
                        + "Item Name: " + itemNames.get(id) + "\n"
                        + "Updated Stock: " + itemStocks.get(id)
                );

                dialog.dispose();

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                    dialog,
                    "Enter valid numeric values!"
                );
            }
        });

        dialog.setVisible(true);
    }

    // Feature 3: Generate Bill
    static void openGenerateBillWindow(JFrame parent) {

        JDialog dialog = new JDialog(
            parent, "Generate Bill", true
        );

        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));

        JTextField idField = new JTextField();
        JTextField quantityField = new JTextField();

        dialog.add(new JLabel("Item ID:"));
        dialog.add(idField);

        dialog.add(new JLabel("Quantity:"));
        dialog.add(quantityField);

        JButton generateButton = new JButton("Generate Bill");

        dialog.add(new JLabel(""));
        dialog.add(generateButton);

        generateButton.addActionListener(e -> {

            try {
                int id = Integer.parseInt(
                    idField.getText().trim()
                );

                int quantity = Integer.parseInt(
                    quantityField.getText().trim()
                );

                if (!itemNames.containsKey(id)) {

                    JOptionPane.showMessageDialog(
                        dialog,
                        "Item ID not found!"
                    );
                    return;
                }

                if (quantity <= 0) {

                    JOptionPane.showMessageDialog(
                        dialog,
                        "Quantity must be greater than zero!"
                    );
                    return;
                }

                int availableStock = itemStocks.get(id);

                if (quantity > availableStock) {

                    JOptionPane.showMessageDialog(
                        dialog,
                        "Insufficient stock! Available stock: "
                            + availableStock
                    );
                    return;
                }

                String name = itemNames.get(id);
                double price = itemPrices.get(id);
                double total = price * quantity;

                // Reduce stock after successful billing
                itemStocks.put(id, availableStock - quantity);

                // Save stock after billing
                if (!saveInventory()) {
                    itemStocks.put(id, availableStock);
                    return;
                }

                int remainingStock = itemStocks.get(id);

                String bill = "========== BILL ==========\n"
                        + "Item ID: " + id + "\n"
                        + "Item Name: " + name + "\n"
                        + "Price: Rs. "
                        + String.format("%.2f", price) + "\n"
                        + "Quantity: " + quantity + "\n"
                        + "--------------------------\n"
                        + "Total: Rs. "
                        + String.format("%.2f", total) + "\n"
                        + "Remaining Stock: " + remainingStock + "\n"
                        + "==========================";

                JOptionPane.showMessageDialog(
                    dialog,
                    bill,
                    "Generated Bill",
                    JOptionPane.INFORMATION_MESSAGE
                );

                dialog.dispose();

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                    dialog,
                    "Enter valid numeric Item ID and Quantity!"
                );
            }
        });

        dialog.setVisible(true);
    }
}
