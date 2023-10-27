import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Toolkit;

public class LoginScreenController extends JFrame implements ActionListener {
    private JTextField txtUserName = new JTextField(10);
    private JTextField txtPassword = new JTextField(10);
    private JButton    btnLogin    = new JButton("Login");

    public JButton getBtnLogin() {
        return btnLogin;
    }

    public JTextField getTxtPassword() {
        return txtPassword;
    }

    public JTextField getTxtUserName() {
        return txtUserName;
    }

    RemoteDataAdapter dao;

    public LoginScreenController(RemoteDataAdapter dao) {

        this.dao = dao;

        setTitle("Login Window");
        this.setSize(300, 150);

        // Center the application window on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        this.setLocation(x, y);


        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        this.getContentPane().add(new JLabel ("Library Management System"));

        JPanel main = new JPanel(new SpringLayout());
        main.add(new JLabel("Username:"));
        main.add(txtUserName);
        main.add(new JLabel("Password:"));
        main.add(txtPassword);

        SpringUtilities.makeCompactGrid(main,
                2, 2, //rows, cols
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad

        this.getContentPane().add(main);
        this.getContentPane().add(btnLogin);

        btnLogin.setAlignmentX(JButton.CENTER_ALIGNMENT);

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

