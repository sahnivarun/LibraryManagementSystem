public interface DataAccess {

    void connect();

    Product loadProduct(int id);

    boolean saveProduct(Product product);

    boolean saveOrder(Order order);

    boolean saveShippingAddress(ShippingAddress address);

    boolean saveCreditCard(CreditCard card);

    boolean saveReceipt(Receipt receipt);

    User loadUser(String username, String password);

}


