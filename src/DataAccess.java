import java.io.IOException;

public interface DataAccess {

    void connect();

    Book loadBook(int id);

    Book updateBook(Book book) throws IOException;

    boolean saveOrderBook(OrderBook orderBook);

    Student loadStudent(int id);

    boolean saveStudent(Student student, int id);

    boolean saveReceipt(Receipt receipt);

    User loadUser(String username, String password);

    int getOrderCount();
}


