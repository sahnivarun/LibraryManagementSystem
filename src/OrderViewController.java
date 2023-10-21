import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrderViewController extends JFrame implements ActionListener {
    private JButton btnAdd = new JButton("Add a new item");
    private JButton btnPay = new JButton("Finish and pay");

    private DefaultTableModel items = new DefaultTableModel(); // store information for the table!

    private JTable tblItems = new JTable(items);
    private JLabel labTotal = new JLabel("Total: ");

    private Order order = null;

    public OrderViewController() {
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
        else
        if (e.getSource() == btnPay)
            makeOrder();
    }

    private void makeOrder() {
        JOptionPane.showMessageDialog(null, "This function is being implemented!");

        /* Remember to update new quantity of products!
        product.setQuantity(product.getQuantity() - quantity); // update new quantity!!
        dataAdapter.saveProduct(product); // and save this product back
        */

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

}
