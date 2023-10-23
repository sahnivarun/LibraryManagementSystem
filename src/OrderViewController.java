import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;

public class OrderViewController extends JFrame implements ActionListener {
    private JButton btnAdd = new JButton("Add a new item");
    private JButton btnPay = new JButton("Finish and pay");
    private JButton btnAddAddress = new JButton("Add Shipping Address");
    private JButton btnAddCard = new JButton("Add Credit Card");

    private DefaultTableModel items = new DefaultTableModel(); // store information for the table!

    private JTable tblItems = new JTable(items);
    private JLabel labTotal = new JLabel("Total: ");

    private Order order = null;

    private DataAdapter dataAdapter;
    private Product product; // Store the selected product
    private double quantity; // Store the selected quantity
    private Receipt receipt;

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
        panelButton.add(btnAddAddress);
        panelButton.add(btnAddCard);
        panelButton.add(btnPay);
        this.getContentPane().add(panelButton);

        btnAdd.addActionListener(this);
        btnPay.addActionListener(this);
        btnAddAddress.addActionListener(this);
        btnAddCard.addActionListener(this);

        order = new Order();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdd)
            addProduct();
        else if (e.getSource() == btnPay)
            makeOrder();
        else if (e.getSource() == btnAddAddress)
            addShippingAddress();
        else if (e.getSource() == btnAddCard)
            addCreditCard();
    }

    private void makeOrder() {

        if (order.getLines().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No items in the order.");
            return;
        }

        if (dataAdapter == null){
            dataAdapter = Application.getInstance().getDataAdapter();
        }

        // Capture Shipping Address and Credit Card information
        ShippingAddress shippingAddress = getShippingAddressFromUI();
        CreditCard creditCard = getCreditCardFromUI();

        // Save Shipping Address and Credit Card
        if (dataAdapter.saveShippingAddress(shippingAddress) && dataAdapter.saveCreditCard(creditCard)) {
            order.setShippingAddress(shippingAddress);
            order.setCreditCard(creditCard);

            // Continue with the existing code to save the order and update product quantities
        } else {
            JOptionPane.showMessageDialog(null, "Error saving shipping address or credit card.");
        }

        // Use the DataAdapter to save the order to the database
        if (dataAdapter.saveOrder(order)) {

            for (OrderLine line : order.getLines()) {
                int productID = line.getProductID();
                double lineQuantity = line.getQuantity();

                Product product = dataAdapter.loadProduct(productID);

                if (product != null) {
                    double updatedQuantity = product.getQuantity() - lineQuantity;
                    product.setQuantity(updatedQuantity);
                    dataAdapter.saveProduct(product); // Update the product's quantity
                }
            }

            // Create a new Receipt instance and set its properties
            Receipt receipt = new Receipt();
            receipt.setOrderId(order.getOrderID());
            receipt.setUserId(order.getBuyerID());
            receipt.setDateTime(System.currentTimeMillis());
            receipt.setTotalCost(order.getTotalCost());
            receipt.setShippingAddress(order.getShippingAddress().getFullAddress());
            receipt.setCreditCardNumber(String.valueOf(order.getCreditCard().getCardNumber()));

            // Use the DataAdapter to save the receipt to the database
            if (dataAdapter.saveReceipt(receipt)) {
                JOptionPane.showMessageDialog(null, "Order created and saved successfully!");

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

        Product product = Application.getInstance().getDataAdapter().loadProduct(Integer.parseInt(id));
        if (product == null) {
            JOptionPane.showMessageDialog(null, "This product does not exist!");
            return;
        }

        double quantity = Double.parseDouble(JOptionPane.showInputDialog(null,"Enter quantity: "));

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
        // Collect user input from your UI components
        String street = JOptionPane.showInputDialog("Enter Street: ");
        String apt = JOptionPane.showInputDialog("Enter Apt/Unit: ");
        String city = JOptionPane.showInputDialog("Enter City: ");
        String state = JOptionPane.showInputDialog("Enter State: ");
        String zipCodeStr = JOptionPane.showInputDialog("Enter Zip Code: ");

        // Convert zip code to an integer (you may need error handling here)
        int zipCode = Integer.parseInt(zipCodeStr);

        // Create a ShippingAddress object
        ShippingAddress address = new ShippingAddress();
        address.setStreetNumberAndName(street);
        address.setApartmentOrUnitNumber(apt);
        address.setCity(city);
        address.setState(state);
        address.setZipCode(zipCode);

        return address;
    }

    private CreditCard getCreditCardFromUI() {
        // Collect user input from your UI components
        String cardNumberStr = JOptionPane.showInputDialog("Enter Card Number: ");
        String name = JOptionPane.showInputDialog("Enter Cardholder Name: ");
        String expiryMonthStr = JOptionPane.showInputDialog("Enter Expiry Month: ");
        String expiryYearStr = JOptionPane.showInputDialog("Enter Expiry Year: ");
        String cvvStr = JOptionPane.showInputDialog("Enter CVV: ");
        String billingAddress = JOptionPane.showInputDialog("Enter Billing Address: ");

        // Convert input to appropriate data types (you may need error handling here)
        int cardNumber = Integer.parseInt(cardNumberStr);
        int expiryMonth = Integer.parseInt(expiryMonthStr);
        int expiryYear = Integer.parseInt(expiryYearStr);
        int cvv = Integer.parseInt(cvvStr);

        // Create a CreditCard object
        CreditCard card = new CreditCard();
        card.setCardNumber(cardNumber);
        card.setName(name);
        card.setExpiryMonth(expiryMonth);
        card.setExpiryYear(expiryYear);
        card.setCvv(cvv);
        card.setBillingAddress(billingAddress);

        return card;
    }


}
