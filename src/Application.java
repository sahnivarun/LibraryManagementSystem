public class Application {

    private static Application instance;   // Singleton pattern

    public static Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }
        return instance;
    }

    private static RemoteDataAdapter dao;

    private User currentUser = null;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private Application() {
            dao = new RemoteDataAdapter();
            dao.connect();
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
