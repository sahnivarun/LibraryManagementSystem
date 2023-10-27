import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.sql.Date;

public class SQLiteDataAdapter implements DataAccess {
    private Connection connection;

    public SQLiteDataAdapter(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void connect() {
        try {
            // db parameters
            String url = "jdbc:sqlite:store.db";

            // create a connection to the database
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection(url);

            if (connection == null)
                System.out.println("Cannot make the connection!!!");
            else
                System.out.println("The connection object is " + connection);

            System.out.println("Connection to SQLite has been established.");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

//    public Product loadProduct(int id) {
//        try {
//            String query = "SELECT * FROM Products WHERE ProductID = " + id;
//
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery(query);
//            if (resultSet.next()) {
//                Product product = new Product();
//                product.setProductID(resultSet.getInt(1));
//                product.setName(resultSet.getString(2));
//                product.setPrice(resultSet.getDouble(3));
//                product.setQuantity(resultSet.getDouble(4));
//                product.setSellerID(resultSet.getInt(5));
//
//                resultSet.close();
//                statement.close();
//
//                return product;
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Database access error!");
//            e.printStackTrace();
//        }
//        return null;
//    }

    public Book loadBook(int bookID) {
        try {
            String query = "SELECT * FROM Books WHERE BookID = " + bookID;

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                Book book = new Book();
                book.setBookID(resultSet.getInt(1));
                book.setBookName(resultSet.getString(2));
                book.setAuthorName(resultSet.getString(3));
                book.setQuantity(resultSet.getInt(4));
                book.setStatus(resultSet.getString(5));

                resultSet.close();
                statement.close();

                return book;
            }

        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
        }
        return null;
    }

//    public boolean saveProduct(Product product) {
//        try {
//            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Products WHERE ProductID = ?");
//            statement.setInt(1, product.getProductID());
//
//            ResultSet resultSet = statement.executeQuery();
//
//            if (resultSet.next()) { // this product exists, update its fields
//                statement = connection.prepareStatement("UPDATE Products SET Name = ?, Price = ?, Quantity = ?, SellerID = SellerID WHERE ProductID = ?");
//                statement.setString(1, product.getName());
//                statement.setDouble(2, product.getPrice());
//                statement.setDouble(3, product.getQuantity());
//                statement.setInt(4, product.getProductID());
//                //statement.setNull(5, Types.INTEGER);
//            } else { // this product does not exist, use insert into
//                statement = connection.prepareStatement("INSERT INTO Products VALUES (?, ?, ?, ?, ?)");
//                statement.setString(2, product.getName());
//                statement.setDouble(3, product.getPrice());
//                statement.setDouble(4, product.getQuantity());
//                statement.setInt(1, product.getProductID());
//                statement.setNull(5, Types.INTEGER);
//            }
//            statement.execute();
//            resultSet.close();
//            statement.close();
//            return true;        // save successfully
//
//        } catch (SQLException e) {
//            System.out.println("Database access error!");
//            e.printStackTrace();
//            return false; // cannot save!
//        }
//    }

    public boolean saveBook(Book book) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Books WHERE BookID = ?");
            statement.setInt(1, book.getBookID());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) { // this book exists, update its fields
                statement = connection.prepareStatement("UPDATE Books SET BookName = ?, AuthorName = ?, Quantity = ?, Status = ? WHERE BookID = ?");
                statement.setString(1, book.getBookName());
                statement.setString(2, book.getAuthorName());
                statement.setInt(3, (int)book.getQuantity());
                statement.setString(4, book.getStatus());

            } else { // this book does not exist, use insert into
                statement = connection.prepareStatement("INSERT INTO Books (BookID, BookName, AuthorName, Quantity, Status) VALUES (?, ?, ?, ?, ?)");
                statement.setInt(1, book.getBookID());
                statement.setString(2, book.getBookName());
                statement.setString(3, book.getAuthorName());
                statement.setInt(4, (int)book.getQuantity());
                statement.setString(5, book.getStatus());
            }
            statement.execute();
            resultSet.close();
            statement.close();
            return true; // save successfully

        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
            return false; // cannot save!
        }
    }

//    public Order loadOrder(int id) {
//        try {
//            Order order = null;
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery("SELECT * FROM Orders WHERE OrderID = " + id);
//
//            if (resultSet.next()) {
//                order = new Order();
//                order.setOrderID(resultSet.getInt("OrderID"));
//                order.setBuyerID(resultSet.getInt("CustomerID"));
//                order.setTotalCost(resultSet.getDouble("TotalCost"));
//                order.setDate(resultSet.getString("OrderDate"));
//                resultSet.close();
//                statement.close();
//            }
//
//            // loading the order lines for this order
//            resultSet = statement.executeQuery("SELECT * FROM OrderLine WHERE OrderID = " + id);
//
//            while (resultSet.next()) {
//                OrderLine line = new OrderLine();
//                line.setOrderID(resultSet.getInt(1));
//                line.setProductID(resultSet.getInt(2));
//                line.setQuantity(resultSet.getDouble(3));
//                line.setCost(resultSet.getDouble(4));
//                order.addLine(line);
//            }
//
//            return order;
//
//        } catch (SQLException e) {
//            System.out.println("Database access error!");
//            e.printStackTrace();
//            return null;
//        }
//    }

    public int getOrderCount() {
        int orderCount = 0;

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM OrderBook");

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

//    public boolean saveOrder(Order order) {
//
//        try {
//
//            int nextOrderID = getOrderCount() + 1;
//
//            PreparedStatement statement = connection.prepareStatement("INSERT INTO Orders VALUES (?, ?, ?, ?, ?)");
//            statement.setInt(1, nextOrderID);
//            statement.setInt(3, nextOrderID);
//            statement.setString(2, String.valueOf(new Date(System.currentTimeMillis())));
//            statement.setDouble(4, order.getTotalCost());
//            statement.setDouble(5, order.getTotalTax());
//
//            statement.execute();    // commit to the database;
//            statement.close();
//
//            statement = connection.prepareStatement("INSERT INTO OrderLine VALUES (?, ?, ?, ?)");
//
//            for (OrderLine line : order.getLines()) { // store for each order line!
//                statement.setInt(1, nextOrderID);
//                statement.setInt(2, line.getProductID());
//                statement.setDouble(3, line.getQuantity());
//                statement.setDouble(4, line.getCost());
//
//                statement.execute();    // commit to the database;
//            }
//            statement.close();
//            return true; // save successfully!
//        } catch (SQLException e) {
//            System.out.println("Database access error!");
//            e.printStackTrace();
//            return false;
//        }
//    }

    public boolean saveOrderBook(OrderBook orderBook) {
        try {
            int nextOrderID = getOrderCount() + 1;

            // Calculate the return date, which is 60 days from the order date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Define your date format
            Date orderDate = new Date(System.currentTimeMillis());
            Calendar calendar = Calendar.getInstance();
            //calendar.setTime(orderDate);
            calendar.add(Calendar.DATE, 60);
            Date returnDate = (Date) calendar.getTime();

            // Format the return date as a string
            String returnDateString = dateFormat.format(returnDate);

            PreparedStatement statement = connection.prepareStatement("INSERT INTO OrderBook (OrderID, StudentID, OrderDate, ReturnDate) VALUES (?, ?, ?, ?)");
            statement.setInt(1, nextOrderID);
            statement.setInt(2, nextOrderID);
            statement.setString(3, String.valueOf(orderDate));
            statement.setString(4, returnDateString);

            statement.execute();
            statement.close();

            statement = connection.prepareStatement("INSERT INTO OrderLineBook (OrderID, BookID, Quantity, BookName) VALUES (?, ?, ?, ?)");

            for (OrderLineBook line : orderBook.getLines()) {
                statement.setInt(1, nextOrderID);
                statement.setInt(2, line.getBookID());
                statement.setInt(3, (int)line.getQuantity());
                statement.setString(4, line.getBookName());

                statement.execute();
            }
            statement.close();
            return true; // save successfully!
        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
            return false;
        }
    }

    public User loadUser(String username, String password) {
        try {

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users WHERE UserName = ? AND Password = ?");
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUserID(resultSet.getInt("UserID"));
                user.setUsername(resultSet.getString("UserName"));
                user.setPassword(resultSet.getString("Password"));
                user.setFullName(resultSet.getString("DisplayName"));
                resultSet.close();
                statement.close();

                return user;
            }

        } catch (SQLException e) {
            System.out.println("Database access error!");
            e.printStackTrace();
        }
        return null;
    }

//    public boolean saveShippingAddress(ShippingAddress address) {
//        try {
//            PreparedStatement statement = connection.prepareStatement("INSERT INTO ShippingAddress (StreetNumberAndName, ApartmentOrUnitNumber, City, State, ZipCode) VALUES (?, ?, ?, ?, ?)");
//            statement.setString(1, address.getStreetNumberAndName());
//            statement.setString(2, address.getApartmentOrUnitNumber());
//            statement.setString(3, address.getCity());
//            statement.setString(4, address.getState());
//            statement.setInt(5, address.getZipCode());
//
//            int rowsAffected = statement.executeUpdate();
//            statement.close();
//
//            return rowsAffected > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    public boolean saveStudentDetails(Student student) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Student (StudentID, StudentName, EmailID, StudentNumber) VALUES (?, ?, ?, ?)");
            statement.setInt(1, student.getStudentID());
            statement.setString(2, student.getStudentName());
            statement.setString(3, student.getEmailID());
            statement.setString(4, student.getStudentNumber());

            int rowsAffected = statement.executeUpdate();
            statement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

//    public ShippingAddress loadShippingAddress(int id) {
//        try {
//            PreparedStatement statement = connection.prepareStatement("SELECT * FROM ShippingAddress WHERE AddressID = ?");
//            statement.setInt(1, id);
//
//            ResultSet result = statement.executeQuery();
//            if (result.next()) {
//                ShippingAddress address = new ShippingAddress();
//                address.setAddressID(result.getInt("AddressID"));
//                address.setStreetNumberAndName(result.getString("StreetNumberAndName"));
//                address.setApartmentOrUnitNumber(result.getString("ApartmentOrUnitNumber"));
//                address.setCity(result.getString("City"));
//                address.setState(result.getString("State"));
//                address.setZipCode(result.getInt("ZipCode"));
//
//                result.close();
//                statement.close();
//                return address;
//            } else {
//                result.close();
//                statement.close();
//                return null;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    public Student loadStudentDetails(int studentID) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Student WHERE StudentID = ?");
            statement.setInt(1, studentID);

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                Student student = new Student(
                        result.getInt("StudentID"),
                        result.getString("StudentName"),
                        result.getString("EmailID"),
                        result.getString("StudentNumber")
                );

                result.close();
                statement.close();
                return student;
            } else {
                result.close();
                statement.close();
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveReceipt(Receipt receipt) {
        try {
            int receiptID = getOrderCount();

            receipt.setOrderId(receiptID);
            receipt.setStudentId(receiptID);
            receipt.setReceiptNumber(receiptID);

            PreparedStatement statement = connection.prepareStatement("INSERT INTO Receipt (OrderID, StudentID, DateTime, StudentDetails, Books) VALUES (?, ?, ?, ?, ?)");
            statement.setInt(1, receipt.getOrderId());
            statement.setInt(2, receipt.getStudentId());
            statement.setString(3, receipt.getDateTime());
            statement.setString(4, receipt.getStudentDetails());
            statement.setString(5, receipt.getBooks());

            int rowsAffected = statement.executeUpdate();
            statement.close();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}