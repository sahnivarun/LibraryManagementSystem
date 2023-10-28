import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BookViewController extends JFrame implements ActionListener {
    private JTextField txtBookID  = new JTextField(10);
    private JTextField txtBookName  = new JTextField(30);
    private JTextField txtAuthorName  = new JTextField(30);
    private JTextField txtBookQuantity  = new JTextField(10);
    private JTextField txtStatus  = new JTextField(10);

    private JButton btnLoad = new JButton("Load Book");
    private JButton btnSave = new JButton("Save Book");

    RemoteDataAdapter dao;

    public BookViewController(RemoteDataAdapter dao) {

        this.dao = dao;

        this.setTitle("Manage Books");
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
        this.setSize(900, 300);

        JPanel panelButton = new JPanel();
        panelButton.add(btnLoad);
        panelButton.add(btnSave);
        this.getContentPane().add(panelButton);

        JLabel statusDescription1 = new JLabel("Set status as A/NA only");
        statusDescription1.setFont(new Font("Sans Serif", Font.BOLD, 12));
        JLabel statusDescription2 = new JLabel("A - Available, NA - Not Available");
        statusDescription2.setFont(new Font("Sans Serif", Font.BOLD, 12));

        JPanel panelStatusDescription1 = new JPanel();
        JPanel panelStatusDescription2 = new JPanel();
        panelStatusDescription1.add(statusDescription1);
        panelStatusDescription2.add(statusDescription2);
        this.getContentPane().add(panelStatusDescription1);
        this.getContentPane().add(panelStatusDescription2);

        JPanel panelBookID = new JPanel();
        panelBookID.add(new JLabel("Book ID: "));
        panelBookID.add(txtBookID);
        txtBookID.setHorizontalAlignment(JTextField.RIGHT);
        this.getContentPane().add(panelBookID);

        JPanel panelBookName = new JPanel();
        panelBookName.add(new JLabel("Book Name: "));
        panelBookName.add(txtBookName);
        this.getContentPane().add(panelBookName);

        JPanel panelBookInfo = new JPanel();
        panelBookInfo.add(new JLabel("Author Name: "));
        panelBookInfo.add(txtAuthorName);
        txtAuthorName.setHorizontalAlignment(JTextField.RIGHT);

        panelBookInfo.add(new JLabel("Quantity: "));
        panelBookInfo.add(txtBookQuantity);
        txtBookQuantity.setHorizontalAlignment(JTextField.RIGHT);

        panelBookInfo.add(new JLabel("Status: "));
        panelBookInfo.add(txtStatus);
        txtStatus.setHorizontalAlignment(JTextField.RIGHT);

        this.getContentPane().add(panelBookInfo);

        btnLoad.addActionListener(this);
        btnSave.addActionListener(this);
    }

    private JButton getBtnSaveBook() {
        return btnSave;
    }

    public JButton getBtnLoadBook() {
        return btnLoad;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == getBtnLoadBook())
            loadBook();
        else
        if (e.getSource() == getBtnSaveBook())
            saveBook();
    }

    public JTextField getTxtBookID() {
        return txtBookID;
    }

    public JTextField getTxtBookName() {
        return txtBookName;
    }

    public JTextField getTxtQuantity() {
        return txtBookQuantity;
    }

    public JTextField getTxtAuthorName() {
        return txtAuthorName;
    }

    public JTextField getTxtStatus() {
        return txtStatus;
    }

    private void saveBook() {
        int bookID;
        try {
            bookID = Integer.parseInt(txtBookID.getText());
            if(bookID <=0){
                JOptionPane.showMessageDialog(null, "Book ID must be a positive integer!" );
                return;
            }
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid book ID! Please provide a valid book ID!");
            return;
        }

        String bookName = txtBookName.getText().trim();

        if (bookName.length() == 0) {
            JOptionPane.showMessageDialog(null, "Invalid book name! Please provide a non-empty book name!");
            return;
        }

        String authorName = txtAuthorName.getText().trim();

        if (authorName.length() == 0) {
            JOptionPane.showMessageDialog(null, "Invalid author name! Please provide a non-empty author name!");
            return;
        }

        double bookQuantity;
        try {
            bookQuantity = Double.parseDouble(txtBookQuantity.getText());
            if(bookQuantity <= 0){
                JOptionPane.showMessageDialog(null, "Book quantity must be a positive number!");
                return;
            }

        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid book quantity! Please provide a valid book quantity!");
            return;
        }

        String status = getTxtStatus().getText().trim();

        // Done all validations! Make an object for this product!
        Book book = new Book();
        book.setBookID(bookID);
        book.setBookName(bookName);
        book.setAuthorName(authorName);
        book.setStatus(status);
        book.setQuantity(bookQuantity);

        // Store the book to the database and check if it was saved successfully
        if (dao.saveBook(book)) {
            JOptionPane.showMessageDialog(null, "Book Saved Successfully");
        } else {
            JOptionPane.showMessageDialog(null, "Failed to save the book. Please check the data and try again.");
        }

    }

    private void loadBook() {
        int bookID = 0;
        try {
            bookID = Integer.parseInt(txtBookID.getText());
        }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid book ID! Please provide a valid book ID!");
            return;
        }

        if(bookID<=0) {
            JOptionPane.showMessageDialog(null, "Invalid book ID! Please provide a non negative book ID");
            return;
        }

        Book book = dao.loadBook(bookID);

        if (book == null) {
            JOptionPane.showMessageDialog(null, "This book ID does not exist in the database!");
            return;
        }

        txtBookName.setText(book.getBookName());
        txtBookQuantity.setText(String.valueOf(book.getQuantity()));
        txtAuthorName.setText(book.getAuthorName());
        txtStatus.setText(book.getStatus());
    }

}