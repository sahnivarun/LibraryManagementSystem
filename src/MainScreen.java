import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainScreen extends JFrame {

    private JButton btnBuy = new JButton("Order Book");
    private JButton btnSell = new JButton("Load/Save Book");
    RemoteDataAdapter dao;

    // Label to display user information
    private JLabel lblUserInfo = new JLabel();

    public MainScreen(RemoteDataAdapter dao) {

        this.dao = dao;
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Main Screen");
        this.setSize(500, 300);

        // Center the application window on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        this.setLocation(x, y);

        btnSell.setPreferredSize(new Dimension(120, 50));
        btnBuy.setPreferredSize(new Dimension(120, 50));

        JLabel title = new JLabel("Library Management System");
        title.setFont(new Font("Sans Serif", Font.BOLD, 25));
        JPanel panelTitle = new JPanel();
        panelTitle.add(title);
        this.getContentPane().add(panelTitle);

        JPanel panelButton = new JPanel();
        panelButton.add(btnBuy);
        panelButton.add(btnSell);

        this.getContentPane().add(panelButton);

        btnBuy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                OrderBookViewController orderBookView = new OrderBookViewController(dao);
                orderBookView.setVisible(true);
            }
        });


        btnSell.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                BookViewController bookView = new BookViewController(dao);
                bookView.setVisible(true);
            }
        });

        // Add user info label and make it visible
        JPanel panelUserInfo = new JPanel();
        panelUserInfo.add(lblUserInfo);
        lblUserInfo.setVisible(true);

        this.getContentPane().add(panelUserInfo);

    }

    public void setUserInfo(User user) {
        // Display userID, username, and fullName in the label
        lblUserInfo.setText("User ID: " + user.getUserID() + "    |    Username: " + user.getUsername() + "    |    Full Name: " + user.getFullName());
    }

}
