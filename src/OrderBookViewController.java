import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DecimalFormat;

public class OrderBookViewController extends JFrame implements ActionListener {
    private OrderBook orderBook = null;

    private JButton btnAddBook = new JButton("Add a new book");
    private JButton btnSaveBook = new JButton("Save book order");

    private DefaultTableModel items = new DefaultTableModel(); // store information for the table!

    private JTable tblItems = new JTable(items);

    // private DataAdapter dataAdapter;
    private RemoteDataAdapter dao;

    private Book book; // Store the selected book
    private double quantity; // Store the selected quantity
    private Receipt receipt;
    private ShippingAddress address;
    private CreditCard card;

    public OrderBookViewController(RemoteDataAdapter dao) {

        this.dao = dao;

        this.setTitle("Book Order View");
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setSize(400, 600);

        items.addColumn("Book ID");
        items.addColumn("Book Name");
        items.addColumn("Book Author Name");
        items.addColumn("Borrow Date Time");
        items.addColumn("Return Date Time");

        JPanel panelOrder = new JPanel();
        panelOrder.setPreferredSize(new Dimension(400, 450));
        panelOrder.setLayout(new BoxLayout(panelOrder, BoxLayout.PAGE_AXIS));
        tblItems.setBounds(0, 0, 400, 350);
        panelOrder.add(tblItems.getTableHeader());
        panelOrder.add(tblItems);
        tblItems.setFillsViewportHeight(true);
        this.getContentPane().add(panelOrder);

        JPanel panelButton = new JPanel();
        panelButton.setPreferredSize(new Dimension(400, 100));
        panelButton.add(btnAddBook);
        panelButton.add(btnSaveBook);
        this.getContentPane().add(panelButton);

        btnAddBook.addActionListener(this);
        btnSaveBook.addActionListener(this);

        orderBook = new OrderBook();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddBook)
            addBook();
        else if (e.getSource() == btnSaveBook)
            makeOrder();
    }

    private void makeOrder() {

        if (orderBook.getLines().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No books in the order.");
            return;
        }

        // Capture Shipping Address and Credit Card information
        ShippingAddress shippingAddress = getShippingAddressFromUI();
        CreditCard creditCard = getCreditCardFromUI();

        // Use the DataAdapter to save the order to the database
        if (dao.saveOrderBook(orderBook)) {

            for (OrderLineBook line : orderBook.getLines()) {
                int bookID = line.getBookID();
                double lineQuantity = line.getQuantity();

                Book book = dao.loadBook(bookID);

                if (book != null) {
                    double updatedQuantity = book.getQuantity() - lineQuantity;
                    book.setQuantity(updatedQuantity);
                    dao.saveBook(book); // Update the book's quantity
                }
            }

            long currentTimeMillis = System.currentTimeMillis();
            Date date = new Date(currentTimeMillis);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            String formattedDate = dateFormat.format(date);

            // Create a new Receipt instance and set its properties
            Receipt receipt = new Receipt();
            receipt.setOrderId(orderBook.getOrderID());
            receipt.setUserId(orderBook.getStudentID());
            receipt.setDateTime(formattedDate);
           // receipt.setTotalCost(orderBook.getTotalCost());
            receipt.setShippingAddress(orderBook.getShippingAddress().getFullAddress());
            receipt.setCreditCardNumber(String.valueOf(orderBook.getCreditCard().getCardNumber()));

            // Build the product details string from the order lines
            StringBuilder bookDetails = new StringBuilder();
            for (OrderLineBook line : orderBook.getLines()) {
                Book orderedBook = dao.loadBook(line.getBookID());
                if (orderedBook != null) {
                    String bookDetail = orderedBook.getBookName() + " x " + line.getQuantity();
                    if (bookDetails.length() > 0) {
                        bookDetails.append(", ");
                    }
                    bookDetails.append(bookDetail);
                }
            }

            receipt.setBooks(bookDetails.toString()); // Set the book details in the receipt

            // Use the DataAdapter to save the receipt to the database
            if (dao.saveReceipt(receipt)) {
                JOptionPane.showMessageDialog(null, "Order created and saved successfully!");

                showReceiptDialog(receipt);

                // Now, you can reset the order and clear the table
                orderBook = new OrderBook();
                items.setRowCount(0);
            } else {
                JOptionPane.showMessageDialog(null, "Error saving the receipt.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Error creating and saving the order.");
        }

    }

//    private void addProduct() {
//        String id = JOptionPane.showInputDialog("Enter ProductID: ");
//
//        if (id == null || id.isEmpty() || !id.matches("\\d+")) {
//            JOptionPane.showMessageDialog(null, "Invalid product ID. Please enter a valid ProductID.");
//            return;
//        }
//
//        Product product = dao.loadProduct(Integer.parseInt(id));
//        if (product == null) {
//            JOptionPane.showMessageDialog(null, "This product does not exist!");
//            return;
//        }
//
//        double quantity = Double.parseDouble(JOptionPane.showInputDialog(null, "Enter quantity: "));
//
//        if (quantity <= 0 || quantity > product.getQuantity()) {
//            JOptionPane.showMessageDialog(null, "This quantity is not valid!");
//            return;
//        }
//
//        OrderLineBook line = new OrderLineBook();
//        line.setOrderID(this.orderBook.getOrderID());
//        line.setBookID(product.getProductID());
//        line.setQuantity(quantity);
//        line.setCost(quantity * product.getPrice());
//        orderBook.getLines().add(line);
//        orderBook.setTotalCost(orderBook.getTotalCost() + line.getCost());
//        orderBook.setTotalTax(orderBook.getTotalCost() * 0.08);
//
//        Object[] row = new Object[5];
//        row[0] = line.getBookID();
//        row[1] = product.getName();
//        row[2] = product.getPrice();
//        row[3] = line.getQuantity();
//        row[4] = line.getCost();
//
//        addRow(row);
//        labTotal.setText("Total: $" + order.getTotalCost());
//        invalidate();
//    }

    private void addBook() {
        String id = JOptionPane.showInputDialog("Enter BookID: ");

        if (id == null || id.isEmpty() || !id.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Invalid book ID. Please enter a valid BookID.");
            return;
        }

        Book book = dao.loadBook(Integer.parseInt(id));
        if (book == null) {
            JOptionPane.showMessageDialog(null, "This book does not exist!");
            return;
        }

        double quantity = Double.parseDouble(JOptionPane.showInputDialog(null, "Enter quantity: "));

        if (quantity <= 0 || quantity > book.getQuantity()) {
            JOptionPane.showMessageDialog(null, "This quantity is not valid!");
            return;
        }

        OrderLineBook line = new OrderLineBook();
        line.setOrderID(this.orderBook.getOrderID());
        line.setBookID(book.getBookID());
        line.setBookName(book.getBookName());
        orderBook.getLines().add(line);

        Object[] row = new Object[5];
        row[0] = line.getBookID();
        row[1] = line.getBookName();
        row[2] = book.getAuthorName();
        row[3] = orderBook.getOrderDate();
        row[4] = orderBook.getReturnDate();

        addRow(row);
        invalidate();
    }

    public void addRow(Object[] row) {
        items.addRow(row);
    }

    private void addShippingAddress() {
        ShippingAddress shippingAddress = getShippingAddressFromUI();
        if (shippingAddress != null) {
            orderBook.setShippingAddress(shippingAddress);
        }
    }

    private void addCreditCard() {
        CreditCard creditCard = getCreditCardFromUI();
        if (creditCard != null) {
            orderBook.setCreditCard(creditCard);
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
                            orderBook.setShippingAddress(address);

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
                            orderBook.setCreditCard(card);

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

        int orderCount = dao.getOrderCount();
        receipt.setReceiptNumber(orderCount);
        System.out.println("OrderCount"+orderCount);

        // Add receipt information labels
        JLabel lblReceiptNumber = new JLabel("Receipt Number: " + orderCount);
        JLabel lblOrderID = new JLabel("Order ID: " + orderCount);
        JLabel lblDateTime = new JLabel("Date and Time: " + receipt.getDateTime());

        JLabel lblProducts = new JLabel("Products: " + receipt.getBooks());
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