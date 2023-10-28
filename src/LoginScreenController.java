import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Toolkit;

public class LoginScreenController extends JFrame implements ActionListener {
    private JTextField txtUserName = new JTextField(20);
    private JPasswordField txtPassword = new JPasswordField(20);
    private JButton    btnLogin    = new JButton("Login");

    RemoteDataAdapter dao;

    public LoginScreenController(RemoteDataAdapter dao) {

        this.dao = dao;

        setTitle("Login Window");
        this.setSize(500, 300);
        getContentPane().setBackground(Color.GRAY);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Center the application window on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        this.setLocation(x, y);

//        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
//
//        this.getContentPane().add(new JLabel ("Library Management System"));
//
//        JPanel main = new JPanel(new SpringLayout());
//
//        main.add(new JLabel("Username:"));
//        main.add(txtUserName);
//        main.add(new JLabel("Password:"));
//        main.add(txtPassword);
//
//        SpringUtilities.makeCompactGrid(main, 2,2,6,6,6,6);
//
//        this.getContentPane().add(main);
//        this.getContentPane().add(btnLogin);
//
//        btnLogin.setAlignmentX(JButton.CENTER_ALIGNMENT);
//
//        btnLogin.addActionListener(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Set the background color here
        mainPanel.setBackground(Color.GRAY);

        JPanel titlePanel = new JPanel();
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 25));
        titlePanel.add(titleLabel);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 2, 20, 20)); // 2 rows, 2 columns, with spacing

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        inputPanel.add(usernameLabel);
        inputPanel.add(txtUserName);
        inputPanel.add(passwordLabel);
        inputPanel.add(txtPassword);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(btnLogin);
        btnLogin.setPreferredSize(new Dimension(100, 60));

        mainPanel.add(titlePanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);

        add(mainPanel);

        btnLogin.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnLogin) {
            String username = txtUserName.getText().trim();
            String password = txtPassword.getText().trim();

            System.out.println("Login with username = " + username + " and password = " + password);
            User user = dao.loadUser(username, password);

            if (user == null) {
                JOptionPane.showMessageDialog(null, "This user does not exist!");
            }
            else {
                Application.getInstance().setCurrentUser(user);

                // Set user information in the MainScreen
                MainScreen main = new MainScreen(dao);
                main.setUserInfo(user);;

                this.setVisible(false);
                main.setVisible(true);

            }
        }
    }
}

