import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProductViewController extends JFrame implements ActionListener {
    private JTextField txtProductID  = new JTextField(10);
    private JTextField txtProductName  = new JTextField(30);
    private JTextField txtProductPrice  = new JTextField(10);
    private JTextField txtProductQuantity  = new JTextField(10);

    private JButton btnLoad = new JButton("Load Product");
    private JButton btnSave = new JButton("Save Product");

    public ProductViewController() {

        this.setTitle("Manage Products");
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
        this.setSize(500, 200);

        JPanel panelButton = new JPanel();
        panelButton.add(btnLoad);
        panelButton.add(btnSave);
        this.getContentPane().add(panelButton);

        JPanel panelProductID = new JPanel();
        panelProductID.add(new JLabel("Product ID: "));
        panelProductID.add(txtProductID);
        txtProductID.setHorizontalAlignment(JTextField.RIGHT);
        this.getContentPane().add(panelProductID);

        JPanel panelProductName = new JPanel();
        panelProductName.add(new JLabel("Product Name: "));
        panelProductName.add(txtProductName);
        this.getContentPane().add(panelProductName);

        JPanel panelProductInfo = new JPanel();
        panelProductInfo.add(new JLabel("Price: "));
        panelProductInfo.add(txtProductPrice);
        txtProductPrice.setHorizontalAlignment(JTextField.RIGHT);

        panelProductInfo.add(new JLabel("Quantity: "));
        panelProductInfo.add(txtProductQuantity);
        txtProductQuantity.setHorizontalAlignment(JTextField.RIGHT);

        this.getContentPane().add(panelProductInfo);

        btnLoad.addActionListener(this);
        btnSave.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLoad)
            loadProduct();
        else
        if (e.getSource() == btnSave)
            saveProduct();
    }

    private void saveProduct() {
        int productID;
        try {
            productID = Integer.parseInt(txtProductID.getText());
            if(productID <=0){
                JOptionPane.showMessageDialog(null, "Product ID must be a positive integer!" );
                return;
            }
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid product ID! Please provide a valid product ID!");
            return;
        }

        double productPrice;
        try {
            productPrice = Double.parseDouble(txtProductPrice.getText());
            if(productPrice <= 0){
                JOptionPane.showMessageDialog(null, "Product price must be a positive number!");
                return;
            }
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid product price! Please provide a valid product price!");
            return;
        }

        double productQuantity;
        try {
            productQuantity = Double.parseDouble(txtProductQuantity.getText());
            if(productQuantity <= 0){
                JOptionPane.showMessageDialog(null, "Product quantity must be a positive number!");
                return;
            }

        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid product quantity! Please provide a valid product quantity!");
            return;
        }

        String productName = txtProductName.getText().trim();

        if (productName.length() == 0) {
            JOptionPane.showMessageDialog(null, "Invalid product name! Please provide a non-empty product name!");
            return;
        }

        // Done all validations! Make an object for this product!

        Product product = new Product();
        product.setProductID(productID);
        product.setSellerID(Application.getInstance().getCurrentUser().getUserID());
        product.setName(productName);
        product.setPrice(productPrice);
        product.setQuantity(productQuantity);

//        // Check if the product with the same ID already exists in the database
//        Product existingProduct = Application.getInstance().getDataAdapter().loadProduct(productID);
//
//        if(existingProduct != null){
//            JOptionPane.showMessageDialog(null, "Product with this ID already exists. Please use a different product ID.");
//        }
//
//        else{
            // Store the product to the database and check if it was saved successfully
            if (Application.getInstance().getDataAdapter().saveProduct(product)) {
                JOptionPane.showMessageDialog(null, "Product Saved Successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to save the product. Please check the data and try again.");
            }
       // }

    }

    private void loadProduct() {
        int productID = 0;
        try {
            productID = Integer.parseInt(txtProductID.getText());
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid product ID! Please provide a valid product ID!");
            return;
        }

        Product product = Application.getInstance().getDataAdapter().loadProduct(productID);

        if (product == null) {
            JOptionPane.showMessageDialog(null, "This product ID does not exist in the database!");
            return;
        }

        txtProductName.setText(product.getName());
        txtProductPrice.setText(String.valueOf(product.getPrice()));
        txtProductQuantity.setText(String.valueOf(product.getQuantity()));
    }

}