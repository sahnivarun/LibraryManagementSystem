import redis.clients.jedis.Jedis;
import java.sql.*;
import java.util.Map;

    public class DataAdapter {

        private Jedis jedis;
        private Connection connection;

        public DataAdapter(Connection connection) {
            this.connection = connection;
            this.jedis = new Jedis("redis://default:varunsahni@redis-17090.c262.us-east-1-3.ec2.cloud.redislabs.com:17090");
        }

        //Redis function for loadProduct
        public Product loadProduct(int id) {
            try {
                // Construct the key based on the product ID
                String key = "Product:" + id;

                // Check if the key exists in Redis
                if (jedis.exists(key)) {
                    // Retrieve the values from Redis and create a Product object
                    Product product = new Product();
                    product.setProductID(id);
                    product.setName(jedis.hget(key, "Name"));
                    product.setPrice(Double.parseDouble(jedis.hget(key, "Price")));
                    product.setQuantity(Double.parseDouble(jedis.hget(key, "Quantity")));
                    // product.setSellerID(Integer.parseInt(jedis.hget(key, "SellerID")));

                    return product;
                }

            } catch (Exception e) {
                System.out.println("Error accessing Redis database!");
                e.printStackTrace();
            }
            return null;
        }

        //Redis function for saveProduct
        public boolean saveProduct(Product product) {
            try {
                // Construct the key based on the product ID
                String key = "Product:" + product.getProductID();

                // Check if the product already exists in Redis
                if (jedis.exists(key)) {
                    // Update the existing product's fields
                    jedis.hset(key, "Name", product.getName());
                    jedis.hset(key, "Price", String.valueOf(product.getPrice()));
                    jedis.hset(key, "Quantity", String.valueOf(product.getQuantity()));
                    //  jedis.hset(key, "sellerID", String.valueOf(product.getSellerID()));
                } else {
                    // Create a new product in Redis
                    jedis.hset(key, "Name", product.getName());
                    jedis.hset(key, "Price", String.valueOf(product.getPrice()));
                    jedis.hset(key, "Quantity", String.valueOf(product.getQuantity()));
                    //  jedis.hset(key, "sellerID", String.valueOf(product.getSellerID()));
                }

                return true; // saved successfully

            } catch (Exception e) {
                System.out.println("Error accessing Redis database!");
                e.printStackTrace();
                return false; // cannot save!
            }
        }

        public int getOrderCount() {
            int orderCount = 0;

            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM Orders");

                if (resultSet.next()) {
                    orderCount = resultSet.getInt(1);
                }

                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                System.out.println("Database access error!");
                e.printStackTrace();
            }

            return orderCount;

        }

        public boolean saveOrder(Order order) {

            try {

                int nextOrderID = getOrderCount() + 1;

                PreparedStatement statement = connection.prepareStatement("INSERT INTO Orders VALUES (?, ?, ?, ?, ?)");
                statement.setInt(1, nextOrderID);
                statement.setInt(3, nextOrderID);
                statement.setString(2, String.valueOf(new Date(System.currentTimeMillis())));
                statement.setDouble(4, order.getTotalCost());
                statement.setDouble(5, order.getTotalTax());

                statement.execute();    // commit to the database;
                statement.close();

                statement = connection.prepareStatement("INSERT INTO OrderLine VALUES (?, ?, ?, ?)");

                for (OrderLine line: order.getLines()) { // store for each order line!
                    statement.setInt(1,nextOrderID);
                    statement.setInt(2, line.getProductID());
                    statement.setDouble(3, line.getQuantity());
                    statement.setDouble(4, line.getCost());

                    statement.execute();    // commit to the database;
                }
                statement.close();
                return true; // save successfully!
            }
            catch (SQLException e) {
                System.out.println("Database access error!");
                e.printStackTrace();
                return false;
            }
        }

        //Redis function for loadUser
        public User loadUser(String username, String password) {
            try {
                // Construct the key based on the username
                String userKey = "User:" + username;

                // Check if the user exists in Redis
                if (jedis.exists(userKey)) {
                    // Retrieve stored username and password from Redis
                    String storedUsername = jedis.hget(userKey, "UserName");
                    String storedPassword = jedis.hget(userKey, "Password");

                    // Check if the provided username and password match the stored values
                    if (username.equals(storedUsername) && password.equals(storedPassword)) {
                        // Authentication successful, create and return the User object
                        User user = new User();
                        user.setUsername(storedUsername);
                        user.setPassword(storedPassword);
                        user.setFullName(jedis.hget(userKey, "DisplayName"));
                        return user;
                    } else {
                        // Username or password does not match
                        System.out.println("Incorrect username or password for user: " + username);
                    }
                } else {
                    // User does not exist in the database
                    System.out.println("User not found: " + username);
                }
            } catch (Exception e) {
                System.out.println("Error accessing Redis database!");
                e.printStackTrace();
            }
            return null;  // Authentication failed
        }

        //Redis function to save shipping address
        public boolean saveShippingAddress(ShippingAddress address) {
            try {
                // Construct the key based on the address details
                String key = "ShippingAddress:" + address.getStreetNumberAndName() + ":" +
                        address.getApartmentOrUnitNumber() + ":" +
                        address.getCity() + ":" +
                        address.getState() + ":" +
                        address.getZipCode();

                // Create a new hash for the shipping address or overwrite existing values
                jedis.hmset(key, Map.of(
                        "StreetNumberAndName", address.getStreetNumberAndName(),
                        "ApartmentOrUnitNumber", address.getApartmentOrUnitNumber(),
                        "City", address.getCity(),
                        "State", address.getState(),
                        "ZipCode", String.valueOf(address.getZipCode())
                ));

                return true; // save successfully

            } catch (Exception e) {
                System.out.println("Error accessing Redis database!");
                e.printStackTrace();
            }
            return false; // cannot save
        }

        //Redis function to save credit card
        public boolean saveCreditCard(CreditCard card) {
            try {
                // Construct the key based on the card number
                String key = "CreditCard:" + card.getCardNumber();

                // Create a new hash for the credit card
                jedis.hset(key, "CardNumber", String.valueOf(card.getCardNumber()));
                jedis.hset(key, "Name", card.getName());
                jedis.hset(key, "ExpiryMonth", String.valueOf(card.getExpiryMonth()));
                jedis.hset(key, "ExpiryYear", String.valueOf(card.getExpiryYear()));
                jedis.hset(key, "CVV", String.valueOf(card.getCvv()));
                jedis.hset(key, "BillingAddress", card.getBillingAddress());

                return true; // save successfully

            } catch (Exception e) {
                System.out.println("Error accessing Redis database!");
                e.printStackTrace();
            }
            return false; // cannot save
        }

        public boolean saveReceipt(Receipt receipt) {
            try {
                int receiptOrderID = getOrderCount();

                receipt.setOrderId(receiptOrderID);
                receipt.setUserId(receiptOrderID);

                PreparedStatement statement = connection.prepareStatement("INSERT INTO Receipt (OrderID, UserID, DateTime, TotalCost, ShippingAddress, CreditCardNumber) VALUES (?, ?, ?, ?, ?, ?)");
                statement.setInt(1, receiptOrderID);
                statement.setInt(2, receiptOrderID);
                statement.setString(3, receipt.getDateTime());
                statement.setDouble(4, receipt.getTotalCost());
                statement.setString(5, receipt.getShippingAddress());
                statement.setString(6, receipt.getCreditCardNumber());

                int rowsAffected = statement.executeUpdate();
                statement.close();

                return rowsAffected > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

//        public Product loadProduct(int id) {
//            try {
//                String query = "SELECT * FROM Products WHERE ProductID = " + id;
//
//                Statement statement = connection.createStatement();
//                ResultSet resultSet = statement.executeQuery(query);
//                if (resultSet.next()) {
//                    Product product = new Product();
//                    product.setProductID(resultSet.getInt(1));
//                    product.setName(resultSet.getString(2));
//                    product.setPrice(resultSet.getDouble(3));
//                    product.setQuantity(resultSet.getDouble(4));
//                    product.setSellerID(resultSet.getInt(5));
//
//                    resultSet.close();
//                    statement.close();
//
//                    return product;
//                }
//
//            } catch (SQLException e) {
//                System.out.println("Database access error!");
//                e.printStackTrace();
//            }
//            return null;
//        }

//        public boolean saveProduct(Product product) {
//            try {
//                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Products WHERE ProductID = ?");
//                statement.setInt(1, product.getProductID());
//
//                ResultSet resultSet = statement.executeQuery();
//
//                if (resultSet.next()) { // this product exists, update its fields
//                    statement = connection.prepareStatement("UPDATE Products SET Name = ?, Price = ?, Quantity = ?, SellerID = SellerID WHERE ProductID = ?");
//                    statement.setString(1, product.getName());
//                    statement.setDouble(2, product.getPrice());
//                    statement.setDouble(3, product.getQuantity());
//                    statement.setInt(4, product.getProductID());
//                    //statement.setNull(5, Types.INTEGER);
//                }
//                else { // this product does not exist, use insert into
//                    statement = connection.prepareStatement("INSERT INTO Products VALUES (?, ?, ?, ?, ?)");
//                    statement.setString(2, product.getName());
//                    statement.setDouble(3, product.getPrice());
//                    statement.setDouble(4, product.getQuantity());
//                    statement.setInt(1, product.getProductID());
//                    statement.setNull(5, Types.INTEGER);
//                }
//                statement.execute();
//                resultSet.close();
//                statement.close();
//                return true;        // save successfully
//
//            } catch (SQLException e) {
//                System.out.println("Database access error!");
//                e.printStackTrace();
//                return false; // cannot save!
//            }
//        }

//        public User loadUser(String username, String password) {
//            try {
//
//                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users WHERE UserName = ? AND Password = ?");
//                statement.setString(1, username);
//                statement.setString(2, password);
//                ResultSet resultSet = statement.executeQuery();
//                if (resultSet.next()) {
//                    User user = new User();
//                    user.setUserID(resultSet.getInt("UserID"));
//                    user.setUsername(resultSet.getString("UserName"));
//                    user.setPassword(resultSet.getString("Password"));
//                    user.setFullName(resultSet.getString("DisplayName"));
//                    resultSet.close();
//                    statement.close();
//
//                    return user;
//                }
//
//            } catch (SQLException e) {
//                System.out.println("Database access error!");
//                e.printStackTrace();
//            }
//            return null;
//        }

//        public boolean saveShippingAddress(ShippingAddress address) {
//            try {
//                PreparedStatement statement = connection.prepareStatement("INSERT INTO ShippingAddress (StreetNumberAndName, ApartmentOrUnitNumber, City, State, ZipCode) VALUES (?, ?, ?, ?, ?)");
//                statement.setString(1, address.getStreetNumberAndName());
//                statement.setString(2, address.getApartmentOrUnitNumber());
//                statement.setString(3, address.getCity());
//                statement.setString(4, address.getState());
//                statement.setInt(5, address.getZipCode());
//
//                int rowsAffected = statement.executeUpdate();
//                statement.close();
//
//                return rowsAffected > 0;
//            } catch (SQLException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }

//        public boolean saveCreditCard(CreditCard card) {
//            try {
//                PreparedStatement statement = connection.prepareStatement("INSERT INTO CreditCard (CardNumber, Name, ExpiryMonth, ExpiryYear, CVV, BillingAddress) VALUES (?, ?, ?, ?, ?, ?)");
//                statement.setInt(1, card.getCardNumber());
//                statement.setString(2, card.getName());
//                statement.setInt(3, card.getExpiryMonth());
//                statement.setInt(4, card.getExpiryYear());
//                statement.setInt(5, card.getCvv());
//                statement.setString(6, card.getBillingAddress());
//
//                int rowsAffected = statement.executeUpdate();
//                statement.close();
//
//                return rowsAffected > 0;
//            } catch (SQLException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
    }
