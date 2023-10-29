import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DecimalFormat;
import java.util.Calendar;

public class OrderBookViewController extends JFrame implements ActionListener {
    private OrderBook orderBook = null;

    private JButton btnAddBook = new JButton("Add a new book");
    private JButton btnOrderBook = new JButton("Save book order");

    private DefaultTableModel items = new DefaultTableModel(); // store information for the table!

    private JTable tblItems = new JTable(items);

    // private DataAdapter dataAdapter;
    private RemoteDataAdapter dao;

    private Book book; // Store the selected book
    private double quantity; // Store the selected quantity
    private Receipt receipt;
    private Student student;

    public OrderBookViewController(RemoteDataAdapter dao) {

        this.dao = dao;

        this.setTitle("Book Order View");
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setSize(400, 600);

        items.addColumn("Book ID");
        items.addColumn("Book Name");
        items.addColumn("Book Author Name");
        items.addColumn("Order Date");
        items.addColumn("Return Date");

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
        panelButton.add(btnOrderBook);
        this.getContentPane().add(panelButton);

        btnAddBook.addActionListener(this);
        btnOrderBook.addActionListener(this);

        orderBook = new OrderBook();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAddBook)
            addBook();
        else if (e.getSource() == btnOrderBook)
            makeOrder();

    }

    private void makeOrder() {

        if (orderBook.getLines().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No books in the order.");
            return;
        }

        // Capture Student information
        Student student = getStudentFromUI();

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
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String formattedDate = dateFormat.format(date);

            // Create a new Receipt instance and set its properties
            Receipt receipt = new Receipt();
            receipt.setOrderId(orderBook.getOrderID());
            //  receipt.setStudentId(orderBook.getStudentID());
            receipt.setDateTime(formattedDate);
            receipt.setStudent(orderBook.getStudent().getFullStudent());

            // Build the book details string from the order lines
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

        //To assign current date & return date
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = dateFormat.format(calendar.getTime());

        // Add 60 days to the current date
        calendar.add(Calendar.DAY_OF_MONTH, 60);
        String futureDate = dateFormat.format(calendar.getTime());

        OrderLineBook line = new OrderLineBook();
        line.setOrderID(this.orderBook.getOrderID());
        line.setBookID(book.getBookID());
        line.setQuantity(quantity);
        line.setBookName(book.getBookName());
        orderBook.getLines().add(line);

        Object[] row = new Object[5];
        row[0] = line.getBookID();
        row[1] = book.getBookName();
        row[2] = book.getAuthorName();
        row[3] = formattedDate;
        row[4] = futureDate;

        addRow(row);
        invalidate();
    }

    public void addRow(Object[] row) {
        items.addRow(row);
    }

    private void addStudent() {
        Student student = getStudentFromUI();
        if (student != null) {
            orderBook.setStudent(student);
        }
    }

    private void loadStudent(JTextField txtStudentID, JTextField txtName, JTextField txtEmail, JTextField txtNum) {
        int studentID = 0;
        try {
            studentID = Integer.parseInt(txtStudentID.getText());
        }
        catch (NumberFormatException e) {
           // JOptionPane.showMessageDialog(null, "Invalid student ID! Please provide a valid book ID!");
            return;
        }

        if(studentID<=0) {
            JOptionPane.showMessageDialog(null, "Invalid student ID! Please provide a non negative student ID");
            return;
        }

        Student student = dao.loadStudent(studentID);

        if (student == null) {
           // JOptionPane.showMessageDialog(null, "This student ID does not exist in the database!");
            return;
        }

        txtName.setText(student.getStudentName());
        txtEmail.setText(student.getEmailID());
        txtNum.setText(student.getStudentNumber());

    }

//changed UI
//    private Student getStudentFromUI() {
//
//        JDialog studentDialog = new JDialog(this, "Student Information", true);
//        studentDialog.setLayout(new BorderLayout());
//
//        JPanel studentPanel = new JPanel();
//        studentPanel.setLayout(new BoxLayout(studentPanel, BoxLayout.Y_AXIS));
//
//        // Student ID Row
//        JLabel lblStudentID = new JLabel("Student ID:");
//        JTextField txtStudentID = new JTextField(20);
//        JPanel studentIDRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        studentIDRow.add(lblStudentID);
//        studentIDRow.add(txtStudentID);
//        studentPanel.add(studentIDRow);
//
//        // Load Student Button Row
//        JButton btnloadStudent = new JButton("Load Student Details");
//        JPanel btnLoadRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        btnLoadRow.add(btnloadStudent);
//        studentPanel.add(btnLoadRow);
//
////        JButton btnloadStudent = new JButton("Load Student Details");
////        btnloadStudent.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnloadStudent.getPreferredSize().height));
////        studentPanel.add(btnloadStudent);
//
//        // Student Name Row
//        JLabel lblName = new JLabel("Student Name:");
//        JTextField txtName = new JTextField(20);
//        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        nameRow.add(lblName);
//        nameRow.add(txtName);
//        studentPanel.add(nameRow);
//
//        // Email ID Row
//        JLabel lblEmail = new JLabel("Email ID:");
//        JTextField txtEmail = new JTextField(20);
//        JPanel emailRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        emailRow.add(lblEmail);
//        emailRow.add(txtEmail);
//        studentPanel.add(emailRow);
//
//        // Mobile Number Row
//        JLabel lblNum = new JLabel("Mobile Number:");
//        JTextField txtNum = new JTextField(20);
//        JPanel numRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        numRow.add(lblNum);
//        numRow.add(txtNum);
//        studentPanel.add(numRow);
//
//        // OK Button
//        JButton btnOK = new JButton("OK");
//
//        // ActionListener for the "Load Student Details" button
//        btnloadStudent.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // ... [Keep this part unchanged]
//            }
//        });
//
//        btnOK.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // ... [Keep this part unchanged]
//            }
//        });
//
//        studentDialog.add(studentPanel, BorderLayout.CENTER);
//        studentDialog.add(btnOK, BorderLayout.SOUTH);
//
//        studentDialog.pack();
//        studentDialog.setLocationRelativeTo(this);
//        studentDialog.setVisible(true);
//
//        return student;
//    }

    private Student getStudentFromUI() {

        JDialog studentDialog = new JDialog(this, "Student Information", true);
        studentDialog.setLayout(new BorderLayout());

        JPanel studentPanel = new JPanel();
        studentPanel.setLayout(new GridLayout(0, 2));

        // Create labels and input fields for student information
        JLabel lblStudentID = new JLabel("Student ID:");
        JTextField txtStudentID = new JTextField(20);
        studentPanel.add(lblStudentID);
        studentPanel.add(txtStudentID);

        JLabel lblSpace = new JLabel("Click here to load student details! -->");
        JButton btnloadStudent = new JButton("Load Student Details");
        btnloadStudent.setAlignmentX(Component.CENTER_ALIGNMENT);
        studentPanel.add(lblSpace);
        studentPanel.add(btnloadStudent);


        JLabel lblName = new JLabel("Student Name:");
        JTextField txtName = new JTextField(20);
        studentPanel.add(lblName);
        studentPanel.add(txtName);

        JLabel lblEmail = new JLabel("Email ID:");
        JTextField txtEmail = new JTextField(20);
        studentPanel.add(lblEmail);
        studentPanel.add(txtEmail);

        JLabel lblNum = new JLabel("Mobile Number:");
        JTextField txtNum = new JTextField(20);
        studentPanel.add(lblNum);
        studentPanel.add(txtNum);

        // Create a single OK button to save student information and close the dialog
        JButton btnOK = new JButton("OK");

        // ActionListener for the "Load Student Details" button
        btnloadStudent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int studentID = Integer.parseInt(txtStudentID.getText());

                // Attempt to load student details
                Student student = dao.loadStudent(studentID);

                if (student != null) {
                    // Student exists, populate the fields
                    txtName.setText(student.getStudentName());
                    txtEmail.setText(student.getEmailID());
                    txtNum.setText(student.getStudentNumber());
                } else {
                    // Student doesn't exist, allow the user to fill in the details
                    txtName.setText("");
                    txtEmail.setText("");
                    txtNum.setText("");
                    JOptionPane.showMessageDialog(null, "This student ID does not exist in our records. Please fill in the details.");
                }
            }
        });

        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Retrieve and process the input values for student
                int studentID = Integer.parseInt(txtStudentID.getText());
                String name = txtName.getText();
                String email = txtEmail.getText();
                String num = txtNum.getText();

                try {
                    // Add validations

                    if (studentID<=0 || name.isEmpty() || email.isEmpty() || num.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Please fill in all required fields.");
                    }  else {
                        student = new Student(studentID, name, email, num);
                        student.setStudentID(studentID);
                        student.setStudentName(name);
                        student.setEmailID(email);
                        student.setStudentNumber(num);
                        orderBook.setStudentID(studentID);

                        // Save student using DataAdapter
                        if (dao.saveStudent(student)) {
                            orderBook.setStudent(student);

                            // After processing, close the dialog
                            studentDialog.dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, "Error saving student details.");
                        }
                    }
                } catch (Exception ex) {
                }
            }
        });

        studentDialog.add(studentPanel, BorderLayout.CENTER);
        studentDialog.add(btnOK, BorderLayout.SOUTH);

        studentDialog.pack();
        studentDialog.setLocationRelativeTo(this);
        studentDialog.setVisible(true);

        return student;
    }

    private void showReceiptDialog(Receipt receipt) {
        // Create a dialog to display the receipt information
        JDialog receiptDialog = new JDialog(this, "Receipt", true);
        receiptDialog.setLayout(new BorderLayout());

        JPanel receiptPanel = new JPanel();
        receiptPanel.setLayout(new GridLayout(0, 1));

        int orderCount = dao.getOrderCount();
        receipt.setReceiptNumber(orderCount);
       // System.out.println("OrderCount"+orderCount);

        // Add receipt information labels
        JLabel lblReceiptNumber = new JLabel("Receipt Number: " + orderCount);
        JLabel lblOrderID = new JLabel("Order ID: " + orderCount);

        JLabel lblStudent = new JLabel("Student Details: " + receipt.getStudent());

        JLabel lblBooks = new JLabel("Books: " + receipt.getBooks());
        receiptPanel.add(lblBooks);

        JLabel lblDateTime = new JLabel("Date and Time: " + receipt.getDateTime());

        receiptPanel.add(lblReceiptNumber);
        receiptPanel.add(lblOrderID);
        receiptPanel.add(lblStudent);
        receiptPanel.add(lblBooks);
        receiptPanel.add(lblDateTime);

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