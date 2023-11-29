import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import java.util.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DataAdapter2 implements DataAccess {
    private Connection connection;

    private Jedis jedis;

    private MongoClient mongoClient;
    private Document orderDocument;

    public DataAdapter2(Connection connection) {
        this.connection = connection;

        jedis = new Jedis("redis://default:library@redis-12961.c321.us-east-1-2.ec2.cloud.redislabs.com:12961");

        // Establish MongoDB connection
        establishMongoDBConnection();

    }

    public int getOrderCountMongoDB() {
        try {
            // Get the MongoDB collection
            MongoDatabase database = mongoClient.getDatabase("library");
            MongoCollection<Document> orderBookCollection = database.getCollection("OrderBook");

            // Count the number of documents in the Orders collection
            long orderCount = orderBookCollection.countDocuments();

            return (int) orderCount; // Convert to int and return

        } catch (Exception e) {
            System.out.println("Error getting order count from MongoDB!");
            e.printStackTrace();
            return -1; // Return -1 to indicate an error
        }
    }

    private void establishMongoDBConnection() {
        String connectionString = "mongodb+srv://varunsahni:varunsahni1@store.95blgys.mongodb.net/?retryWrites=true&w=majority";
        ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        try {
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("library");
            database.runCommand(new Document("ping", 1));
            System.out.println("Connected to MongoDB!");
        } catch (MongoException e) {
            System.out.println("Error connecting to MongoDB!");
            e.printStackTrace();
        }
    }

    public void checkMongoDBConnection() {
        if (mongoClient != null && mongoClient.getDatabase("library") != null) {
            System.out.println("MongoDB connection is established.");
        } else {
            System.out.println("MongoDB connection is not established.");
        }
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

//    public Book loadBook(int bookID) {
//        try {
//            String query = "SELECT * FROM Books WHERE BookID = " + bookID;
//
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery(query);
//            if (resultSet.next()) {
//                Book book = new Book();
//                book.setBookID(resultSet.getInt(1));
//                book.setBookName(resultSet.getString(2));
//                book.setAuthorName(resultSet.getString(3));
//                book.setQuantity(resultSet.getInt(4));
//                book.setStatus(resultSet.getString(5));
//
//                resultSet.close();
//                statement.close();
//
//                return book;
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
            // Construct the key based on the book ID
            System.out.println(bookID);
            String key = "Book" + bookID;

            // Check if the key exists in Redis
            if (jedis.exists(key)) {
                // Retrieve the values from Redis and create a Book object
                Book book = new Book();
                book.setBookID(bookID);
                book.setBookName(jedis.hget(key, "BookName"));
                book.setAuthorName(jedis.hget(key, "AuthorName"));
                book.setQuantity(Integer.parseInt(jedis.hget(key, "Quantity")));
                book.setStatus(jedis.hget(key, "Status"));

                return book;
            }

        } catch (Exception e) {
            System.out.println("Error accessing Redis database!");
            e.printStackTrace();
        }
        return null;
    }

    public Book updateBook(Book book) {
        return null;
    }

//    public boolean saveBook(Book book) {
//        try {
//            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Books WHERE BookID = ?");
//            statement.setInt(1, book.getBookID());
//
//            ResultSet resultSet = statement.executeQuery();
//
//            System.out.println("Book Name: "+book.getBookName());
//            System.out.println("Author Name: "+book.getAuthorName());
//            System.out.println("Book Quantity: "+book.getQuantity());
//            System.out.println("Book Status: "+book.getStatus());
//
//            if (resultSet.next()) { // this product exists, update its fields
//                statement = connection.prepareStatement("UPDATE Books SET BookName = ?, AuthorName = ?, Quantity = ?, Status = ? WHERE BookID = ?");
//                statement.setString(1, book.getBookName());
//                statement.setString(2, book.getAuthorName());
//                statement.setInt(3, (int)book.getQuantity());
//                statement.setString(4, book.getStatus());
//                statement.setInt(5, book.getBookID());
//                System.out.println("Inside Update command");
//
//            }
//            else { // this product does not exist, use insert into
//                statement = connection.prepareStatement("INSERT INTO Books(BookID, BookName, AuthorName, Quantity, Status) VALUES (?, ?, ?, ?, ?)");
//                statement.setString(2, book.getBookName());
//                statement.setString(3, book.getAuthorName());
//                statement.setDouble(4, book.getQuantity());
//                statement.setString(5, book.getStatus());
//                statement.setInt(1, book.getBookID());
//                System.out.println("Inside Insert command");
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

//    public boolean saveBook(Book book) {
//        try {
//            // Construct the key based on the book ID
//            String key = "Book:" + book.getBookID();
//
//            // Check if the book already exists in Redis
//            if (jedis.exists(key)) {
//                // Update the existing book's fields
//                jedis.hset(key, "BookName", book.getBookName());
//                jedis.hset(key, "AuthorName", book.getAuthorName());
//                jedis.hset(key, "Quantity", String.valueOf(book.getQuantity()));
//                jedis.hset(key, "Status", book.getStatus());
//            } else {
//                // Create a new book in Redis
//                jedis.hset(key, "BookName", book.getBookName());
//                jedis.hset(key, "AuthorName", book.getAuthorName());
//                jedis.hset(key, "Quantity", String.valueOf(book.getQuantity()));
//                jedis.hset(key, "Status", book.getStatus());
//            }
//
//            return true; // saved successfully
//
//        } catch (Exception e) {
//            System.out.println("Error accessing Redis database!");
//            e.printStackTrace();
//            return false; // cannot save!
//        }
//    }

    public boolean saveBook(Book book) {
        try {
            // Construct the key based on the book ID
            String key = "Book" + book.getBookID();

            // Update the existing book's fields or create a new book in Redis
            jedis.hset(key, "BookName", book.getBookName());
            jedis.hset(key, "AuthorName", book.getAuthorName());
            jedis.hset(key, "Quantity", String.valueOf(book.getQuantity()));
            jedis.hset(key, "Status", book.getStatus());

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

//    public boolean saveOrderBook(OrderBook orderBook) {
//        try {
//            int nextOrderID = getOrderCount() + 1;
//            Calendar calendar = Calendar.getInstance();
//
//            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
//
//            calendar.add(Calendar.DAY_OF_MONTH, 60);
//
//            String futureDate = dateFormat.format(calendar.getTime());
//
//            orderBook.setReturnDate(futureDate);
//
//
//            PreparedStatement orderStmt = connection.prepareStatement("INSERT INTO OrderBook (OrderDate, StudentID, ReturnDate) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
//            orderStmt.setString(1, String.valueOf(new java.util.Date(System.currentTimeMillis())));
//            orderStmt.setInt(2, orderBook.getStudentID());
//            orderStmt.setString(3, futureDate);
//
//            int rowsInserted = orderStmt.executeUpdate();
//
//            if (rowsInserted > 0) {
//                // Retrieve the ID of the inserted order
//                ResultSet rs = orderStmt.getGeneratedKeys();
//                if (rs.next()) {
//                    int lastOrderID = rs.getInt(1);
//
//                    // Insert order lines
//                    PreparedStatement lineStmt = connection.prepareStatement("INSERT INTO OrderLineBook (OrderID, BookID, Quantity, BookName) VALUES (?, ?, ?, ?)");
//
//                    for (OrderLineBook line : orderBook.getLines()) {
//                        lineStmt.setInt(1, lastOrderID);
//                        lineStmt.setInt(2, line.getBookID());
//                        lineStmt.setDouble(3, line.getQuantity());
//                        lineStmt.setString(4, line.getBookName());
//                        lineStmt.addBatch();
//                    }
//                    lineStmt.executeBatch();
//                    lineStmt.close();
//                    System.out.println("Order and order lines saved successfully.");
//                    return true;
//
//                } else {
//                    System.out.println("Error retrieving the order ID.");
//                    return false;
//                }
//            } else {
//                System.out.println("Error saving the order to the database.");
//                return false;
//            }
//        } catch (SQLException e) {
//            System.out.println("Database access error!");
//            e.printStackTrace();
//            return false;
//        }
//    }

    public boolean saveOrderBook(OrderBook orderBook) {
        try {
            // Get the MongoDB database
            MongoDatabase database = mongoClient.getDatabase("YourDatabaseName");

            // Save OrderBook to the OrderBook collection
            MongoCollection<Document> orderBookCollection = database.getCollection("OrderBook");

            // Create a document for OrderBook
            Document orderBookDocument = new Document()
                    .append("OrderID", getNextOrderIDMongoDB()) // Implement getNextOrderIDMongoDB() accordingly
                    .append("OrderDate", new Date())
                    .append("StudentID", orderBook.getStudentID())
                    .append("ReturnDate", getFutureDate());

            // Insert the OrderBook document into the OrderBook collection
            orderBookCollection.insertOne(orderBookDocument);

            // Save OrderLineBook to the OrderLineBook collection
            MongoCollection<Document> orderLineBookCollection = database.getCollection("OrderLineBook");

            // Insert OrderLineBooks into the OrderLineBook collection
            for (OrderLineBook line : orderBook.getLines()) {
                Document orderLineBookDocument = new Document()
                        .append("OrderID", orderBookDocument.get("OrderID"))
                        .append("BookID", line.getBookID())
                        .append("Quantity", line.getQuantity())
                        .append("BookName", line.getBookName());

                orderLineBookCollection.insertOne(orderLineBookDocument);
            }

            System.out.println("OrderBook and OrderLineBook saved successfully to MongoDB!");
            return true;
        } catch (Exception e) {
            System.out.println("Error saving OrderBook to MongoDB!");
            e.printStackTrace();
            return false;
        }
    }

    // Helper function to get a future date
    private String getFutureDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        calendar.add(Calendar.DAY_OF_MONTH, 60);
        return dateFormat.format(calendar.getTime());
    }

    private int getNextOrderIDMongoDB() {
        try {
            // Get the MongoDB database
            MongoDatabase database = mongoClient.getDatabase("YourDatabaseName");

            // Get the OrderBook collection
            MongoCollection<Document> orderBookCollection = database.getCollection("OrderBook");

            // Find the document with the highest OrderID
            Document highestOrder = orderBookCollection.find()
                    .sort(Sorts.descending("OrderID"))
                    .limit(1)
                    .first();

            // If no orders exist, start with OrderID 1
            if (highestOrder == null) {
                return 1;
            }

            // Increment the highest OrderID for the next order
            return highestOrder.getInteger("OrderID") + 1;
        } catch (Exception e) {
            System.out.println("Error getting next OrderID from MongoDB!");
            e.printStackTrace();
            return -1; // Return -1 to indicate an error
        }
    }

    public boolean saveStudent(Student student, int id) {

        System.out.println("calling savestudent");
        try {
            if (isStudentIDExists(student.getStudentID())) {

                System.out.println(student.getStudentID());
                // If the student ID already exists, update the existing entry
                PreparedStatement updateStatement = connection.prepareStatement("UPDATE Student SET StudentName = ?, EmailID = ?, StudentNumber = ? WHERE StudentID = ?");
                updateStatement.setString(1, student.getStudentName());
                updateStatement.setString(2, student.getEmailID());
                updateStatement.setString(3, student.getStudentNumber());
                updateStatement.setInt(4, student.getStudentID());

                int rowsAffected = updateStatement.executeUpdate();
                updateStatement.close();

                return true;

            } else {
                // If the student ID doesn't exist, insert a new entry
                PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO Student (StudentID, StudentName, EmailID, StudentNumber) VALUES (?, ?, ?, ?)");
                insertStatement.setInt(1, student.getStudentID());
                insertStatement.setString(2, student.getStudentName());
                insertStatement.setString(3, student.getEmailID());
                insertStatement.setString(4, student.getStudentNumber());

                int rowsAffected = insertStatement.executeUpdate();
                insertStatement.close();

                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isStudentIDExists(int studentID) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM Student WHERE StudentID = ?");
            statement.setInt(1, studentID);

            ResultSet resultSet = statement.executeQuery();
            boolean exists = resultSet.next(); // Check if any rows were returned

            resultSet.close();
            statement.close();

            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

//    public boolean saveReceipt(Receipt receipt) {
//        try {
//            int receiptID = getOrderCount();
//
//            receipt.setOrderId(receiptID);
//            receipt.setReceiptNumber(receiptID);
//
//            PreparedStatement statement = connection.prepareStatement("INSERT INTO Receipt (OrderID, DateTime, StudentDetails, Books) VALUES (?, ?, ?, ?)");
//            statement.setInt(1, receipt.getOrderId());
//            //   statement.setInt(2, receipt.getStudentId());
//            statement.setString(2, receipt.getDateTime());
//            statement.setString(3, receipt.getStudent());
//            statement.setString(4, receipt.getBooks());
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

    public boolean saveReceipt(Receipt receipt) {
        try {
            int receiptOrderID = getNextReceiptOrderIDMongoDB();

            receipt.setOrderId(receiptOrderID);
            receipt.setReceiptNumber(receiptOrderID);

            // Get the MongoDB database
            MongoDatabase database = mongoClient.getDatabase("YourDatabaseName");

            // Get the Receipts collection
            MongoCollection<Document> receiptsCollection = database.getCollection("Receipts");

            // Create a document for the receipt
            Document receiptDocument = new Document()
                    .append("orderID", receiptOrderID)
                    .append("userID", receiptOrderID)
                    .append("dateTime", receipt.getDateTime())
                    .append("studentDetails", receipt.getStudent()) // Assuming studentDetails is equivalent to StudentDetails in SQL
                    .append("books", receipt.getBooks());

            // Insert the receipt document into the Receipts collection
            receiptsCollection.insertOne(receiptDocument);

            System.out.println("Receipt saved successfully to MongoDB!");
            return true;
        } catch (Exception e) {
            System.out.println("Error saving receipt to MongoDB!");
            e.printStackTrace();
            return false;
        }
    }

    // Helper function to get the next receipt OrderID from MongoDB
    private int getNextReceiptOrderIDMongoDB() {
        try {
            // Get the MongoDB database
            MongoDatabase database = mongoClient.getDatabase("YourDatabaseName");

            // Get the Receipts collection
            MongoCollection<Document> receiptsCollection = database.getCollection("Receipts");

            // Find the document with the highest orderID
            Document highestReceipt = receiptsCollection.find()
                    .sort(Sorts.descending("orderID"))
                    .limit(1)
                    .first();

            // If no receipts exist, start with orderID 1
            if (highestReceipt == null) {
                return 1;
            }

            // Increment the highest orderID for the next receipt
            return highestReceipt.getInteger("orderID") + 1;
        } catch (Exception e) {
            System.out.println("Error getting next receipt OrderID from MongoDB!");
            e.printStackTrace();
            return -1; // Return -1 to indicate an error
        }
    }


//    public User loadUser(String username, String password) {
//        try {
//
//            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Users WHERE UserName = ? AND Password = ?");
//            statement.setString(1, username);
//            statement.setString(2, password);
//            ResultSet resultSet = statement.executeQuery();
//            if (resultSet.next()) {
//                User user = new User();
//                user.setUserID(resultSet.getInt("UserID"));
//                user.setUsername(resultSet.getString("UserName"));
//                user.setPassword(resultSet.getString("Password"));
//                user.setFullName(resultSet.getString("DisplayName"));
//                resultSet.close();
//                statement.close();
//
//                return user;
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Database access error!");
//            e.printStackTrace();
//        }
//        return null;
//    }

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
        System.out.println("Authentication Failed");
        return null;  // Authentication failed
    }

    public Student loadStudent(int studentID) {
        try {
            // Construct the key based on the student ID
            String key = "Student" + studentID;

            // Check if the key exists in Redis
            if (jedis.exists(key)) {
                // Retrieve the values from Redis and create a Student object
                Student student = new Student();
                student.setStudentID(studentID);
                student.setStudentName(jedis.hget(key, "StudentName"));
                student.setEmailID(jedis.hget(key, "EmailID"));
                student.setStudentNumber(jedis.hget(key, "StudentNumber"));

                return student;
            }

        } catch (Exception e) {
            System.out.println("Error accessing Redis database!");
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return null;
    }

//    public Student loadStudent(int studentID) {
//        try {
//            String query = "SELECT * FROM Student WHERE StudentID = " + studentID;
//
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery(query);
//            if (resultSet.next()) {
//                Student student = new Student();
//                student.setStudentID(resultSet.getInt(1));
//                student.setStudentName(resultSet.getString(2));
//                student.setEmailID(resultSet.getString(3));
//                student.setStudentNumber(resultSet.getString(4));
//
//                resultSet.close();
//                statement.close();
//
//                return student;
//            }
//
//        } catch (SQLException e) {
//            System.out.println("Database access error!");
//            e.printStackTrace();
//        }
//        return null;
//    }

//    public boolean saveStudent(Student student) {
//        try {
//            if (isStudentIDExists(student.getStudentID())) {
//                return true;
//            }
//            else {
//
//                PreparedStatement statement = connection.prepareStatement("INSERT INTO Student (StudentID, StudentName, EmailID, StudentNumber) VALUES (?, ?, ?, ?)");
//                statement.setInt(1, student.getStudentID());
//                statement.setString(2, student.getStudentName());
//                statement.setString(3, student.getEmailID());
//                statement.setString(4, student.getStudentNumber());
//
//                int rowsAffected = statement.executeUpdate();
//                statement.close();
//
//                return rowsAffected > 0;
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public boolean isStudentIDExists(int studentID) {
//        try {
//            PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM Student WHERE StudentID = ?");
//            statement.setInt(1, studentID);
//
//            ResultSet resultSet = statement.executeQuery();
//            boolean exists = resultSet.next(); // Check if any rows were returned
//
//            resultSet.close();
//            statement.close();
//
//            return exists;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

}