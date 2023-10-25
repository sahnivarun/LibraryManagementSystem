import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DecimalFormat;

public class OrderViewController extends JFrame implements ActionListener {
    private JButton btnAdd = new JButton("Add a new item");
    private JButton btnPay = new JButton("Finish and pay");

    private DefaultTableModel items = new DefaultTableModel(); // store information for the table!

    private JTable tblItems = new JTable(items);
    private JLabel labTotal = new JLabel("Total: ");

    private Order order = null;

   // private DataAdapter dataAdapter;
    private RemoteDataAdapter dao;

    private Product product; // Store the selected product
    private double quantity; // Store the selected quantity
    private Receipt receipt;
    private ShippingAddress address;
    private CreditCard card;

    public OrderViewController(Connection connection) {
        this.setTitle("Order View");
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setSize(400, 600);

        items.addColumn("Product ID");
        items.addColumn("Name");
        items.addColumn("Price");
        items.addColumn("Quantity");
        items.addColumn("Cost");

        JPanel panelOrder = new JPanel();
        panelOrder.setPreferredSize(new Dimension(400, 450));
        panelOrder.setLayout(new BoxLayout(panelOrder, BoxLayout.PAGE_AXIS));
        tblItems.setBounds(0, 0, 400, 350);
        panelOrder.add(tblItems.getTableHeader());
        panelOrder.add(tblItems);
        panelOrder.add(labTotal);
        tblItems.setFillsViewportHeight(true);
        this.getContentPane().add(panelOrder);

        JPanel panelButton = new JPanel();
        panelButton.setPreferredSize(new Dimension(400, 100));
        panelButton.add(btnAdd);
        panelButton.add(btnPay);
        this.getContentPane().add(panelButton);

        btnAdd.addActionListener(this);
        btnPay.addActionListener(this);

        order = new Order();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd)
            addProduct();
        else if (e.getSource() == btnPay)
            makeOrder();
    }

    private void makeOrder() {

        if (order.getLines().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No items in the order.");
            return;
        }

        if (dao == null) {
            dao = Application.getInstance().getDao();
        }

        // Capture Shipping Address and Credit Card information
        ShippingAddress shippingAddress = getShippingAddressFromUI();
        CreditCard creditCard = getCreditCardFromUI();

        // Use the DataAdapter to save the order to the database
        if (dao.saveOrder(order)) {

            for (OrderLine line : order.getLines()) {
                int productID = line.getProductID();
                double lineQuantity = line.getQuantity();

                Product product = dao.loadProduct(productID);

                if (product != null) {
                    double updatedQuantity = product.getQuantity() - lineQuantity;
                    product.setQuantity(updatedQuantity);
                    dao.saveProduct(product); // Update the product's quantity
                }
            }

            long currentTimeMillis = System.currentTimeMillis();
            Date date = new Date(currentTimeMillis);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String formattedDate = dateFormat.format(date);

            // Create a new Receipt instance and set its properties
            Receipt receipt = new Receipt();
            receipt.setOrderId(order.getOrderID());
            receipt.setUserId(order.getBuyerID());
            receipt.setDateTime(formattedDate);
            receipt.setTotalCost(order.getTotalCost());
            receipt.setShippingAddress(order.getShippingAddress().getFullAddress());
            receipt.setCreditCardNumber(String.valueOf(order.getCreditCard().getCardNumber()));

            // Build the product details string from the order lines
            StringBuilder productDetails = new StringBuilder();
            for (OrderLine line : order.getLines()) {
                Product orderedProduct = dao.loadProduct(line.getProductID());
                if (orderedProduct != null) {
                    String productDetail = orderedProduct.getName() + " x " + line.getQuantity();
                    if (productDetails.length() > 0) {
                        productDetails.append(", ");
                    }
                    productDetails.append(productDetail);
                }
            }

            receipt.setProducts(productDetails.toString()); // Set the product details in the receipt


            // Use the DataAdapter to save the receipt to the database
            if (dao.saveReceipt(receipt)) {
                JOptionPane.showMessageDialog(null, "Order created and saved successfully!");

                showReceiptDialog(receipt);

                // Now, you can reset the order and clear the table
                order = new Order();
                items.setRowCount(0);
                labTotal.setText("Total: $0.0");
            } else {
                JOptionPane.showMessageDialog(null, "Error saving the receipt.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Error creating and saving the order.");
        }

    }

    private void addProduct() {
        String id = JOptionPane.showInputDialog("Enter ProductID: ");

        if (id == null || id.isEmpty() || !id.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Invalid product ID. Please enter a valid ProductID.");
            return;
        }

        Product product = Application.getInstance().getDao().loadProduct(Integer.parseInt(id));
        if (product == null) {
            JOptionPane.showMessageDialog(null, "This product does not exist!");
            return;
        }

        double quantity = Double.parseDouble(JOptionPane.showInputDialog(null, "Enter quantity: "));

        if (quantity <= 0 || quantity > product.getQuantity()) {
            JOptionPane.showMessageDialog(null, "This quantity is not valid!");
            return;
        }

        OrderLine line = new OrderLine();
        line.setOrderID(this.order.getOrderID());
        line.setProductID(product.getProductID());
        line.setQuantity(quantity);
        line.setCost(quantity * product.getPrice());
        order.getLines().add(line);
        order.setTotalCost(order.getTotalCost() + line.getCost());
        order.setTotalTax(order.getTotalCost() * 0.08);

        Object[] row = new Object[5];
        row[0] = line.getProductID();
        row[1] = product.getName();
        row[2] = product.getPrice();
        row[3] = line.getQuantity();
        row[4] = line.getCost();

        addRow(row);
        labTotal.setText("Total: $" + order.getTotalCost());
        invalidate();
    }

    public void addRow(Object[] row) {
        items.addRow(row);
    }

    private void addShippingAddress() {
        ShippingAddress shippingAddress = getShippingAddressFromUI();
        if (shippingAddress != null) {
            order.setShippingAddress(shippingAddress);
        }
    }

    private void addCreditCard() {
        CreditCard creditCard = getCreditCardFromUI();
        if (creditCard != null) {
            order.setCreditCard(creditCard);
        }
    }

    private ShippingAddress getShippingAddressFromUI() {
        // Create a new dialog to enter payment information
        JDialog addressDialog = new JDialog(this, "Shipping Address Information", true);
        addressDialog.setLayout(new BorderLayout());

        JPanel addressPanel = new JPanel();
        addressPanel.setLayout(new GridLayout(0, 2));

        // Create labels and input fields for shipping address
        JLabel lblStreet = new JLabel("Street:");
        JTextField txtStreet = new JTextField(20);

        JLabel lblApt = new JLabel("Apt/Unit:");
        JTextField txtApt = new JTextField(20);

        JLabel lblCity = new JLabel("City:");
        JTextField txtCity = new JTextField(20);

        JLabel lblState = new JLabel("State:");
        JTextField txtState = new JTextField(20);

        JLabel lblZipCode = new JLabel("Zip Code:");
        JTextField txtZipCode = new JTextField(20);

        addressPanel.add(lblStreet);
        addressPanel.add(txtStreet);
        addressPanel.add(lblApt);
        addressPanel.add(txtApt);
        addressPanel.add(lblCity);
        addressPanel.add(txtCity);
        addressPanel.add(lblState);
        addressPanel.add(txtState);
        addressPanel.add(lblZipCode);
        addressPanel.add(txtZipCode);

        // Create a single OK button to save payment information and close the dialog
        JButton btnOK = new JButton("OK");

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Retrieve and process the input values for shipping address
                String street = txtStreet.getText();
                String apt = txtApt.getText();
                String city = txtCity.getText();
                String state = txtState.getText();
                String zipCodeText = txtZipCode.getText();

                try {
                    // Add validations
                    int zipCode = Integer.parseInt(zipCodeText);

                    if (street.isEmpty() || city.isEmpty() || state.isEmpty() || zipCodeText.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Please fill in all required fields.");
                    } else if (zipCode < 0) {
                        JOptionPane.showMessageDialog(null, "Zip code should not be negative.");
                    } else {
                        address = new ShippingAddress();
                        address.setStreetNumberAndName(street);
                        address.setApartmentOrUnitNumber(apt);
                        address.setCity(city);
                        address.setState(state);
                        address.setZipCode(zipCode);

                        // Save shipping address using DataAdapter
                        if (dao.saveShippingAddress(address)) {
                            order.setShippingAddress(address);

                            // After processing, close the dialog
                            addressDialog.dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, "Error saving shipping address.");
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid zip code format. Please enter a valid number.");
                }
            }
        });

        addressDialog.add(addressPanel, BorderLayout.CENTER);
        addressDialog.add(btnOK, BorderLayout.SOUTH);

        addressDialog.pack();
        addressDialog.setLocationRelativeTo(this);
        addressDialog.setVisible(true);

        return address;
    }

    private CreditCard getCreditCardFromUI() {

        JDialog paymentDialog = new JDialog(this, "Payment Information", true);
        paymentDialog.setLayout(new BorderLayout());

        JPanel paymentPanel = new JPanel();
        paymentPanel.setLayout(new GridLayout(0, 2));

        JLabel lblCardNumber = new JLabel("Card Number:");
        JTextField txtCardNumber = new JTextField(20);

        JLabel lblCardName = new JLabel("Cardholder Name:");
        JTextField txtCardName = new JTextField(20);

        JLabel lblExpiryMonth = new JLabel("Expiry Month:");
        JTextField txtExpiryMonth = new JTextField(20);

        JLabel lblExpiryYear = new JLabel("Expiry Year:");
        JTextField txtExpiryYear = new JTextField(20);

        JLabel lblCvv = new JLabel("CVV:");
        JTextField txtCvv = new JTextField(20);

        JLabel lblBillingAddress = new JLabel("Billing Address:");
        JTextField txtBillingAddress = new JTextField(20);

        paymentPanel.add(lblCardNumber);
        paymentPanel.add(txtCardNumber);
        paymentPanel.add(lblCardName);
        paymentPanel.add(txtCardName);
        paymentPanel.add(lblExpiryMonth);
        paymentPanel.add(txtExpiryMonth);
        paymentPanel.add(lblExpiryYear);
        paymentPanel.add(txtExpiryYear);
        paymentPanel.add(lblCvv);
        paymentPanel.add(txtCvv);
        paymentPanel.add(lblBillingAddress);
        paymentPanel.add(txtBillingAddress);

        // Create a single OK button to save payment information and close the dialog
        JButton btnOK = new JButton("OK");

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Retrieve and process the input values for credit card
                    int cardNumber = Integer.parseInt(txtCardNumber.getText());
                    String name = txtCardName.getText();
                    int expiryMonth = Integer.parseInt(txtExpiryMonth.getText());
                    int expiryYear = Integer.parseInt(txtExpiryYear.getText());
                    int cvv = Integer.parseInt(txtCvv.getText());
                    String billingAddress = txtBillingAddress.getText();

                    // Add validations
                    if (cardNumber < 0 || name.isEmpty() || name.matches(".*\\d.*") ||
                            expiryMonth < 1 || expiryMonth > 12 || expiryYear < 2022 ||
                            cvv < 0 || billingAddress.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Invalid details received. Please check the input.");
                    } else {
                        card = new CreditCard();
                        card.setCardNumber(cardNumber);
                        card.setName(name);
                        card.setExpiryMonth(expiryMonth);
                        card.setExpiryYear(expiryYear);
                        card.setCvv(cvv);
                        card.setBillingAddress(billingAddress);

                        // Save credit card using DataAdapter
                        if (dao.saveCreditCard(card)) {
                            order.setCreditCard(card);

                            // After processing, close the dialog
                            paymentDialog.dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, "Error saving credit card.");
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid input format. Please enter valid numbers.");
                }
            }
        });

        paymentDialog.add(paymentPanel, BorderLayout.CENTER);
        paymentDialog.add(btnOK, BorderLayout.SOUTH);

        paymentDialog.pack();
        paymentDialog.setLocationRelativeTo(this);
        paymentDialog.setVisible(true);

        return card;
    }

    private void showReceiptDialog(Receipt receipt) {
        // Create a dialog to display the receipt information
        JDialog receiptDialog = new JDialog(this, "Receipt", true);
        receiptDialog.setLayout(new BorderLayout());

        JPanel receiptPanel = new JPanel();
        receiptPanel.setLayout(new GridLayout(0, 1));

        double totalCostWithTax = receipt.getTotalCost() * 1.08;
        DecimalFormat df = new DecimalFormat("#.##");
        String formattedTotalCost = df.format(totalCostWithTax);

        // Add receipt information labels
        JLabel lblReceiptNumber = new JLabel("Receipt Number: " + receipt.getOrderId());
        JLabel lblOrderID = new JLabel("Order ID: " + receipt.getOrderId());
        JLabel lblDateTime = new JLabel("Date and Time: " + receipt.getDateTime());

        JLabel lblProducts = new JLabel("Products: " + receipt.getProducts());
        receiptPanel.add(lblProducts);

        JLabel lblShippingAddress = new JLabel("Shipping Address: " + receipt.getShippingAddress());
        JLabel lblTotalCost = new JLabel("Total Cost(with 8% Tax): $" + formattedTotalCost);
        JLabel lblCreditCardNumber = new JLabel("Credit Card Number: " + receipt.getCreditCardNumber());

        receiptPanel.add(lblReceiptNumber);
        receiptPanel.add(lblOrderID);
        receiptPanel.add(lblDateTime);
        receiptPanel.add(lblProducts);
        receiptPanel.add(lblShippingAddress);
        receiptPanel.add(lblTotalCost);
        receiptPanel.add(lblCreditCardNumber);

        receiptDialog.add(receiptPanel, BorderLayout.CENTER);

        // Create a Close button to close the dialog
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receiptDialog.dispose();
            }
        });
        receiptDialog.add(btnClose, BorderLayout.SOUTH);

        receiptDialog.pack();
        receiptDialog.setLocationRelativeTo(this);
        receiptDialog.setVisible(true);
    }

}