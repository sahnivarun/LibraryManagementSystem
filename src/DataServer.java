
import java.io.*;
import java.sql.*;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static util.PortAddresses.MAIN_SERVER_PORT;

// Server class
public class DataServer
{
    public static void main(String[] args) throws Exception
    {
        // server is listening on port MAIN_SERVER_PORT
        ServerSocket ss = new ServerSocket(MAIN_SERVER_PORT);

        // client request
        System.out.println("Starting server program!!!");

        int nClients = 0;

        // Establish a database connection
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:store.db");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        while (true)
        {
            Socket s = null;
            try
            {       s = ss.accept();
                nClients++;

                System.out.println("A client is connected : " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                Gson gson = new Gson();
                DataAccess dao = new SQLiteDataAdapter(Application.getInstance().getDBConnection());

                ClientHandler clientHandler = new ClientHandler(s, dis, dos, gson, dao, connection,ss);
                clientHandler.run();
                break;

            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
}

class ClientHandler extends Thread  {
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private Socket clientSocket;
    private final Gson gson;
    private final DataAccess dao;
    private final Connection connection;
    ServerSocket ss;

    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, Gson gson, DataAccess dao, Connection connection, ServerSocket ss) throws Exception{
        this.clientSocket = s;
        this.dis = dis;
        this.dos = dos;
        this.gson = gson;
        this.dao = dao;
        this.connection = connection;
        this.ss = ss;
    }

    @Override
    public void run(){
        String received;

        while (true) {
            try {

                received = dis.readUTF();
                System.out.println("Message from client: " + received);

                RequestModel req = gson.fromJson(received, RequestModel.class);
                ResponseModel res = new ResponseModel();

                switch (req.code) {
                    case RequestModel.EXIT_REQUEST:
                        handleExitRequest();
                        break;
                    case RequestModel.LOAD_BOOK_REQUEST:
                        handleLoadBookRequest(req, res, dao, connection);
                        break;
                    case RequestModel.SAVE_BOOK_REQUEST:
                        handleSaveBookRequest(req, res, dao, connection);
                        break;
                    case RequestModel.DELETE_BOOK_REQUEST:
                        handleDeleteBookRequest(req, res, dao, connection);
                        break;
                    case RequestModel.SAVE_ORDERBOOK_REQUEST:
                        handleSaveOrderBookRequest(req, res, dao, connection);
                        break;
                    case RequestModel.LOAD_USER_REQUEST:
                        handleLoadUserRequest(req, res, connection);
                        break;
                    case RequestModel.SAVE_USER_REQUEST:
                        handleSaveUserRequest(req, res, dao, connection);
                        break;
                    case RequestModel.UPDATE_USER_REQUEST:
                        handleUpdateUserRequest(req, res, dao, connection);
                        break;
                    case RequestModel.DELETE_USER_REQUEST:
                        handleDeleteUserRequest(req, res, dao, connection);
                        break;
                    case RequestModel.LOAD_STUDENT_REQUEST:
                        handleLoadStudentRequest(req, res, dao, connection);
                        break;
                    case RequestModel.SAVE_STUDENT_REQUEST:
                        handleSaveStudentRequest(req, res, dao, connection);
                        break;
                    case RequestModel.DELETE_STUDENT_REQUEST:
                        handleDeleteStudentRequest(req, res, dao, connection);
                        break;
                    case RequestModel.SAVE_RECEIPT_REQUEST:
                        handleSaveReceiptRequest(req, res, dao, connection);
                        break;
                    case RequestModel.GET_ORDER_COUNT_REQUEST:
                        handleGetOrderCountRequest(req, res, dao, connection);
                        break;
                    case RequestModel.UPDATE_RECEIPT_REQUEST:
                        handleUpdateReceiptRequest(req, res, dao, connection);
                        break;
                    case RequestModel.LOAD_RECEIPT_REQUEST:
                        handleLoadReceiptRequest(req,res,dao,connection);
                        break;
                    case RequestModel.DELETE_RECEIPT_REQUEST:
                        handleDeleteReceiptRequest(req, res, dao, connection);
                        break;
                    default:
                        handleUnknownRequest(req);
                }

                String json = gson.toJson(res);
                System.out.println("JSON object of ResponseModel: " + json);
                dos.writeUTF(json);
                dos.flush();
            } catch (EOFException eofe) {
                System.err.println("EOFException: " + eofe.getMessage());
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private static void handleLoadBookRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Create a PreparedStatement for loading a book from the database
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Books WHERE BookID = ?");
            int bookID = Integer.parseInt(req.body);
            stmt.setInt(1, bookID);
            Gson gson = new Gson();
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                // Build a Book object from the result and set it in the response
                int bookId = resultSet.getInt("BookID");
                String bookName = resultSet.getString("BookName");
                String authorName = resultSet.getString("AuthorName");
                int quantity = resultSet.getInt("Quantity");
                String status = resultSet.getString("Status");

                Book book = new Book(bookId, bookName, authorName, quantity, status);

                res.code = ResponseModel.OK;
                res.body = gson.toJson(book);
            } else {
                // Book not found
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error loading the book from the database.";
        }
    }

    private static void handleSaveBookRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Parse the JSON data from the request into a Book object
            Gson gson = new Gson();
            Book book = gson.fromJson(req.body, Book.class);

            // Check if a book with the same BookID already exists
            PreparedStatement checkStmt = connection.prepareStatement("SELECT COUNT(*) FROM Books WHERE BookID = ?");
            checkStmt.setInt(1, book.getBookID());
            ResultSet checkResult = checkStmt.executeQuery();
            checkResult.next();
            int existingBookCount = checkResult.getInt(1);

            if (existingBookCount > 0) {
                // Update the existing book
                PreparedStatement updateStmt = connection.prepareStatement("UPDATE Books SET BookName = ?, AuthorName = ?, Quantity = ?, Status = ? WHERE BookID = ?");
                updateStmt.setString(1, book.getBookName());
                updateStmt.setString(2, book.getAuthorName());
                updateStmt.setInt(3, (int)book.getQuantity());
                updateStmt.setString(4, book.getStatus());
                updateStmt.setInt(5, book.getBookID());

                int rowsUpdated = updateStmt.executeUpdate();

                if (rowsUpdated > 0) {
                    // Book was successfully updated
                    res.code = ResponseModel.OK;
                    res.body = "Book updated successfully.";
                } else {
                    // Book update failed
                    res.code = ResponseModel.ERROR;
                    res.body = "Error updating the book in the database.";
                }
            } else {
                // Insert a new book with a starting BookID of 1
                PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO Books (BookID, BookName, AuthorName, Quantity, Status) VALUES (?, ?, ?, ?, ?)");
                insertStmt.setInt(1, book.getBookID());
                insertStmt.setString(2, book.getBookName());
                insertStmt.setString(3, book.getAuthorName());
                insertStmt.setInt(4, (int)book.getQuantity());
                insertStmt.setString(5, book.getStatus());

                int rowsInserted = insertStmt.executeUpdate();

                if (rowsInserted > 0) {
                    // New book was successfully saved
                    res.code = ResponseModel.OK;
                    res.body = "New book saved successfully.";
                } else {
                    // Book insertion failed
                    res.code = ResponseModel.ERROR;
                    res.body = "Error saving the new book to the database.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error saving or updating the book in the database.";
        }
    }

    private static void handleSaveOrderBookRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Parse the JSON data from the request into an OrderBook object
            Gson gson = new Gson();
            OrderBook orderBook = gson.fromJson(req.body, OrderBook.class);

            Calendar calendar = Calendar.getInstance();

            // Print the current date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

            //  String formattedDate = dateFormat.format(calendar.getTime());

            // Add 30 days to the current date
            calendar.add(Calendar.DAY_OF_MONTH, 60);

            // Print the date after 30 days
            String futureDate = dateFormat.format(calendar.getTime());

            orderBook.setReturnDate(futureDate);

            // Insert order
            PreparedStatement orderStmt = connection.prepareStatement("INSERT INTO OrderBook (OrderDate, StudentID, ReturnDate) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            orderStmt.setString(1, String.valueOf(new Date(System.currentTimeMillis())));
            orderStmt.setInt(2, orderBook.getStudentID());
            orderStmt.setString(3, futureDate);

            int rowsInserted = orderStmt.executeUpdate();

            if (rowsInserted > 0) {
                // Retrieve the ID of the inserted order
                ResultSet rs = orderStmt.getGeneratedKeys();
                if (rs.next()) {
                    int lastOrderID = rs.getInt(1);

                    // Insert order lines
                    PreparedStatement lineStmt = connection.prepareStatement("INSERT INTO OrderLineBook (OrderID, BookID, Quantity, BookName) VALUES (?, ?, ?, ?)");

                    for (OrderLineBook line : orderBook.getLines()) {
                        lineStmt.setInt(1, lastOrderID);
                        lineStmt.setInt(2, line.getBookID());
                        lineStmt.setDouble(3, line.getQuantity());
                        lineStmt.setString(4, line.getBookName());
                        lineStmt.addBatch();
                    }
                    lineStmt.executeBatch();
                    lineStmt.close();

                    res.code = ResponseModel.OK;
                    res.body = "Order and order lines saved successfully.";
                } else {
                    // Handle not being able to get the inserted order's ID
                    res.code = ResponseModel.ERROR;
                    res.body = "Error retrieving the order ID.";
                }

                rs.close();
                orderStmt.close();
            } else {
                // Handle not being able to save the order
                res.code = ResponseModel.ERROR;
                res.body = "Error saving the order to the database.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error saving the order to the database.";
        }
    }

    private static void handleSaveStudentRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {

            Gson gson = new Gson();
            Student student = gson.fromJson(req.body, Student.class);

            res.code = ResponseModel.OK;
            res.body = "Student record saved successfully.";
            // Parse the JSON data from the request into a Student object

//            // Create a PreparedStatement for inserting a student into the database
//            PreparedStatement stmt = connection.prepareStatement("INSERT INTO Student (StudentID, StudentName, EmailID, StudentNumber) VALUES (?, ?, ?, ?)");
//            stmt.setInt(1, student.getStudentID());
//            stmt.setString(2, student.getStudentName());
//            stmt.setString(3, student.getEmailID());
//            stmt.setString(4, student.getStudentNumber());
//
//            int rowsInserted = stmt.executeUpdate();
//
//            if (rowsInserted > 0) {
//                // Student record was successfully saved
//                res.code = ResponseModel.OK;
//                res.body = "Student record saved successfully.";
//            } else {
//                // Student record could not be saved
//                res.code = ResponseModel.ERROR;
//                res.body = "Error saving the student record to the database.";
//            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error saving the student record to the database.";
        }
    }

    private static void handleSaveReceiptRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Parse the JSON data from the request into a Receipt object
            Gson gson = new Gson();
            Receipt receipt = gson.fromJson(req.body, Receipt.class);

            // Get the next available receipt number
            int receiptNumber = dao.getOrderCount();

            receipt.setReceiptNumber(receiptNumber);
           // receipt.setStudentId(receiptNumber);
            receipt.setOrderId(receiptNumber);

            // Create a PreparedStatement for inserting receipt information into the database
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO Receipt (OrderID, DateTime, StudentDetails, Books) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, receipt.getOrderId());
         //   stmt.setInt(2, receipt.getStudentId());
            stmt.setString(2, receipt.getDateTime());
            stmt.setString(3, receipt.getStudent());
            stmt.setString(4, receipt.getBooks());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                // Receipt information was successfully saved
                res.code = ResponseModel.OK;
                res.body = "Receipt information saved successfully.";
            } else {
                // Receipt information could not be saved
                res.code = ResponseModel.ERROR;
                res.body = "Error saving the receipt information to the database.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error saving the receipt information to the database.";
        }
    }

//    private static void handleLoadUserRequest(RequestModel req, ResponseModel res, Connection connection) {
//        System.out.println("Handle Load UserRequest()");
//        try {
//
//            // Parse the body JSON to extract the username value
//            Gson gson = new Gson();
//            JsonObject jsonObject = gson.fromJson(req.body, JsonObject.class);
//            if (jsonObject.has("username")) {
//                String username = jsonObject.get("username").getAsString();
//                if (jsonObject.has("password")){
//                    String password = jsonObject.get("password").getAsString();
//                }
//                System.out.println("Parsed username: " + username);
//
//                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Users WHERE UserName = ?");
//                stmt.setString(1, username);
//
//                ResultSet resultSet = stmt.executeQuery();
//                System.out.println("result");
//                if (resultSet.next()) {
//                    User user = new User();
//                    user.setUserID(resultSet.getInt("UserID"));
//                    user.setUsername(resultSet.getString("UserName"));
//                    user.setFullName(resultSet.getString("DisplayName"));
//                    user.setPassword(resultSet.getString("Password"));
//
//                    res.code = ResponseModel.OK;
//                    res.body = gson.toJson(user);
//                    System.out.println("User Created");
//                } else {
//                    System.out.println("User not found");
//                    res.code = ResponseModel.DATA_NOT_FOUND;
//                    res.body = "";
//                }
//            } else {
//                System.out.println("Username not found in request body");
//                res.code = ResponseModel.ERROR;
//                res.body = "Username not provided.";
//            }
//
//        } catch (Exception e) {
//            System.out.println("Exception: " + e.toString());
//            e.printStackTrace();
//            res.code = ResponseModel.ERROR;
//            res.body = "Error loading the user from the database.";
//        }
//    }
    private static void handleLoadUserRequest(RequestModel req, ResponseModel res, Connection connection) {
        System.out.println("Handle Load UserRequest()");
        try {
            // Parse the body JSON to extract the username and password values
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(req.body, JsonObject.class);
            if (jsonObject.has("username") && jsonObject.has("password")) {
                String username = jsonObject.get("username").getAsString();
                String password = jsonObject.get("password").getAsString(); // Get password from the request

                System.out.println("Parsed username: " + username);

                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Users WHERE UserName = ? AND Password = ?");
                stmt.setString(1, username);
                stmt.setString(2, password);

                ResultSet resultSet = stmt.executeQuery();
                System.out.println("result");
                if (resultSet.next()) {
                    User user = new User();
                    user.setUserID(resultSet.getInt("UserID"));
                    user.setUsername(resultSet.getString("UserName"));
                    user.setFullName(resultSet.getString("DisplayName"));
                    user.setPassword(resultSet.getString("Password"));

                    res.code = ResponseModel.OK;
                    res.body = gson.toJson(user);
                    System.out.println("User Found");
                } else {
                    System.out.println("Username or Password incorrect");
                    res.code = ResponseModel.DATA_NOT_FOUND; // This code may need to be changed to reflect an authentication failure
                    res.body = ""; // You might want to send a specific message here, e.g., "Invalid credentials."
                }
            } else {
                System.out.println("Username or Password not found in request body");
                res.code = ResponseModel.ERROR;
                res.body = "Username or Password not provided.";
            }
        } catch (SQLException e) {
            // Handle SQLException
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "An error occurred while processing the request.";
        }
    }

    private static void handleLoadStudentRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Create a PreparedStatement for loading a student from the database
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Student WHERE StudentID = ?");
            int studentID = Integer.parseInt(req.body);
            stmt.setInt(1, studentID);
            Gson gson = new Gson();
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                // Build a Student object from the result and set it in the response
                int studentId = resultSet.getInt("StudentID");
                String studentName = resultSet.getString("StudentName");
                String emailId = resultSet.getString("EmailID");
                String studentNumber = resultSet.getString("StudentNumber");

                Student student = new Student(studentId, studentName, emailId, studentNumber);

                res.code = ResponseModel.OK;
                res.body = gson.toJson(student);
            } else {
                // Book not found
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error loading the student from the database.";
        }
    }

    private static void handleDeleteStudentRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Parse the JSON data from the request into a Student object
            Gson gson = new Gson();
            Student student = gson.fromJson(req.body, Student.class);

            // Create a PreparedStatement for deleting a student from the database based on StudentID
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM Student WHERE StudentID = ?");
            stmt.setInt(1, student.getStudentID());

            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                // Student record was successfully deleted
                res.code = ResponseModel.OK;
                res.body = "Student record deleted successfully.";
            } else {
                // Student record could not be deleted (Student with the given ID not found)
                res.code = ResponseModel.ERROR;
                res.body = "Error deleting the student record from the database. Student not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error deleting the student record from the database.";
        }
    }

    private static void handleDeleteUserRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Parse the JSON data from the request into a User object
            Gson gson = new Gson();
            User user = gson.fromJson(req.body, User.class);

            // Create a PreparedStatement for deleting a user from the database based on UserID
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM User WHERE UserID = ?");
            stmt.setInt(1, user.getUserID());

            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                // User record was successfully deleted
                res.code = ResponseModel.OK;
                res.body = "User record deleted successfully.";
            } else {
                // User record could not be deleted (User with the given ID not found)
                res.code = ResponseModel.ERROR;
                res.body = "Error deleting the user record from the database. User not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error deleting the user record from the database.";
        }
    }

    private static void handleGetOrderCountRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            int orderCount = dao.getOrderCount();

            // Set the response code to OK
            res.code = ResponseModel.OK;

            // Convert the orderCount to a string and set it as the response body
            res.body = String.valueOf(orderCount);
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error getting the order count from the database.";
        }
    }

    private static void handleDeleteBookRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Parse the request body to get the BookID to delete
            int bookIDToDelete = Integer.parseInt(req.body);

            // Check if a book with the specified BookID exists
            PreparedStatement checkStmt = connection.prepareStatement("SELECT COUNT(*) FROM Books WHERE BookID = ?");
            checkStmt.setInt(1, bookIDToDelete);
            ResultSet checkResult = checkStmt.executeQuery();
            checkResult.next();
            int existingBookCount = checkResult.getInt(1);

            if (existingBookCount > 0) {
                // Delete the book
                PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM Books WHERE BookID = ?");
                deleteStmt.setInt(1, bookIDToDelete);

                int rowsDeleted = deleteStmt.executeUpdate();

                if (rowsDeleted > 0) {
                    // Book was successfully deleted
                    res.code = ResponseModel.OK;
                    res.body = "Book deleted successfully.";
                } else {
                    // Book deletion failed
                    res.code = ResponseModel.ERROR;
                    res.body = "Error deleting the book from the database.";
                }
            } else {
                // Book not found, cannot delete
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "Book with the specified BookID not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error deleting the book from the database.";
        }
    }

    private static void handleSaveUserRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            Gson gson = new Gson();
            // Parse the JSON data from the request into a User object
            User user = gson.fromJson(req.body, User.class);

            // Create a PreparedStatement for inserting user information into the database
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO Users (Username, Password, Email) VALUES (?, ?, ?)");
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                // User information was successfully saved
                res.code = ResponseModel.OK;
                res.body = "User information saved successfully.";
            } else {
                // User information could not be saved
                res.code = ResponseModel.ERROR;
                res.body = "Error saving the user information to the database.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error saving the user information to the database.";
        }
    }

    private static void handleUpdateUserRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            Gson gson = new Gson();
            // Parse the JSON data from the request into a User object
            User user = gson.fromJson(req.body, User.class);

            // Create a PreparedStatement for updating user information in the database
            PreparedStatement stmt = connection.prepareStatement("UPDATE Users SET Username = ?, Password = ?, Email = ? WHERE UserID = ?");
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setInt(4, user.getUserID());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // User information updated successfully
                res.code = ResponseModel.OK;
                res.body = "User information updated successfully.";
            } else {
                // User not found
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "User not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error updating the user information in the database.";
        }
    }

    private static void handleUpdateStudentRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Parse the JSON data from the request into a Student object
            Gson gson = new Gson();
            Student updatedStudent = gson.fromJson(req.body, Student.class);

            // Create a PreparedStatement for updating a Student record in the database
            PreparedStatement stmt = connection.prepareStatement("UPDATE Student SET StudentName = ?, EmailID = ?, Number = ? WHERE StudentID = ?");
            stmt.setString(1, updatedStudent.getStudentName());
            stmt.setString(2, updatedStudent.getEmailID());
            stmt.setString(3, updatedStudent.getStudentNumber());
            stmt.setInt(4, updatedStudent.getStudentID());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                // Student record was successfully updated
                res.code = ResponseModel.OK;
                res.body = "Student record updated successfully.";
            } else {
                // Student record could not be updated (Student with the given ID not found)
                res.code = ResponseModel.ERROR;
                res.body = "Error updating the student record in the database. Student not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error updating the student record in the database.";
        }
    }

    private static void handleUpdateReceiptRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            Gson gson = new Gson();
            // Parse the JSON data from the request into a Receipt object
            Receipt receipt = gson.fromJson(req.body, Receipt.class);

            // PreparedStatement for updating a receipt by ID
            PreparedStatement stmt = connection.prepareStatement("UPDATE Receipt SET OrderID = ?, DateTime = ?, StudentDetails = ?, Books = ? WHERE ReceiptNumber = ?");
            stmt.setInt(1, receipt.getOrderId());
         //   stmt.setInt(2, receipt.getStudentId());
            stmt.setString(2, receipt.getDateTime());
            stmt.setString(3, receipt.getStudent());
            stmt.setString(4, receipt.getBooks());
            stmt.setInt(5, receipt.getReceiptNumber());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Receipt updated successfully
                res.code = ResponseModel.OK;
                res.body = "Receipt updated successfully.";
            } else {
                // Receipt not found
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "Receipt not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error updating the receipt in the database.";
        }
    }

    private static void handleLoadReceiptRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            int id = Integer.parseInt(req.body);
            // PreparedStatement for loading a receipt by ID
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Receipt WHERE ReceiptNumber = ?");
            stmt.setInt(1, id);
            Gson gson = new Gson();
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                // Receipt object from the result and set it in the response
                Receipt receipt = new Receipt(
                        id,
                        resultSet.getInt("OrderID"),
                        resultSet.getInt("StudentID"),
                        resultSet.getString("DateTime"),
                        resultSet.getString("ShippingAddress"),
                        resultSet.getString("Books")
                );
                res.code = ResponseModel.OK;
                res.body = gson.toJson(receipt);
            } else {
                // Receipt not found
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "Receipt not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error loading the receipt from the database.";
        }
    }

    private static void handleDeleteReceiptRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            int id = Integer.parseInt(req.body);
            // PreparedStatement for deleting a receipt by ID
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM Receipt WHERE ReceiptNumber = ?");
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Receipt deleted successfully
                res.code = ResponseModel.OK;
                res.body = "Receipt deleted successfully.";
            } else {
                // Receipt not found
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "Receipt not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error deleting the receipt from the database.";
        }
    }

    // Handle Exit Request
    private static void handleExitRequest() {
        System.out.println("Server is exiting.");
        System.exit(0);
    }

    // Handle Unknown Request
    private static void handleUnknownRequest(RequestModel res) {
        // This function is called when the server receives an unknown request code.
        System.out.println("Received an unknown request.");
        res.code = ResponseModel.UNKNOWN_REQUEST;
        res.body = "Request not recognized by the server.";
    }



}
