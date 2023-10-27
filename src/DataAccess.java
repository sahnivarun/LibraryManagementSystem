public interface DataAccess {

    void connect();

    Book loadBook(int id);

    boolean saveBook(Book book);

    boolean saveOrderBook(OrderBook orderBook);

    boolean saveShippingAddress(ShippingAddress address);

    //boolean saveCreditCard(CreditCard card);

    boolean saveReceipt(Receipt receipt);

    User loadUser(String username, String password);

    int getOrderCount();
}


