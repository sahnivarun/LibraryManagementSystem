import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Application {

    private String url = "jdbc:sqlite:store.db";
    private static Application instance;   // Singleton pattern

    public static Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }
        return instance;
    }

    // Main components of this application
    private Connection connection;

    public Connection getDBConnection() {
        return connection;
    }

    private static RemoteDataAdapter dao;

    private User currentUser = null;

//    public User getCurrentUser() {
//        return currentUser;
//    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

//    public LoginScreenController loginScreenController = new LoginScreenController(dao);

//    public LoginScreenController getLoginScreenController() {
//        return loginScreenController;
//    }
//
//    public RemoteDataAdapter getDao() {
//        return dao;
//    }
//
//    public void setDao(RemoteDataAdapter dao) {
//        this.dao = dao;
//    }
//
//    private Connection setConnection() {
//        if (connection == null) {
//            try {
//                Class.forName("org.sqlite.JDBC");
//                connection = DriverManager.getConnection(url);
//
//                return connection;
//            } catch (Exception e) {
//                return null;
//            }
//        } else {
//            return connection;
//        }
//
//    }

    private Application() {
        // create SQLite database connection here!
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(url);

            dao = new RemoteDataAdapter();
            dao.connect();

        } catch (ClassNotFoundException ex) {
            System.out.println("SQLite is not installed. System exits with error!");
            ex.printStackTrace();
            System.exit(1);
        } catch (SQLException ex) {
            System.out.println("SQLite database is not ready. System exits with error!" + ex.getMessage());
            System.exit(2);
        }
    }

    public static void main(String[] args) {
        try {
            Application application = new Application();

            LoginScreenController login = new LoginScreenController(dao);
            login.setVisible(true);

        } catch (Exception e) {
            System.out.println("e2: "+e.toString());
            e.printStackTrace();
        }

    }

}
