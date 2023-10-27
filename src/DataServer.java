
import java.io.*;
import java.sql.*;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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


                System.out.println("A new client is connected : " + s);

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
                    case RequestModel.SAVE_ORDER_BOOK_REQUEST:
                        handleSaveOrderBookRequest(req, res, dao, connection);
                        break;
                    case RequestModel.LOAD_USER_REQUEST:
                        System.out.println("Switch Load User ");
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
                    case RequestModel.SAVE_SHIPPING_ADDRESS_REQUEST:
                        handleSaveShippingAddressRequest(req, res, dao, connection);
                        break;
                    case RequestModel.UPDATE_SHIPPING_ADDRESS_REQUEST:
                        handleUpdateShippingAddressRequest(req, res, dao, connection);
                        break;
                    case RequestModel.DELETE_SHIPPING_ADDRESS_REQUEST:
                        handleDeleteShippingAddressRequest(req, res, dao, connection);
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

//    private static void handleLoadProductRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
//        // Create a PreparedStatement for loading a product from the database
//        try {
//            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Products WHERE productID = ?");
//            int id = Integer.parseInt(req.body);
//            stmt.setInt(1, id);
//            Gson gson = new Gson();
//            ResultSet resultSet = stmt.executeQuery();
//            if (resultSet.next()) {
//                // Build a Product object from the result and set it in the response
//                Product product = new Product(id,resultSet.getInt(1), resultSet.getString(2), resultSet.getDouble(3), resultSet.getInt(4));
//                res.code = ResponseModel.OK;
//                res.body = gson.toJson(product);
//            } else {
//                // Product not found
//                res.code = ResponseModel.DATA_NOT_FOUND;
//                res.body = "";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            res.code = ResponseModel.ERROR;
//            res.body = "Error loading the product from the database.";
//        }
//    }
//
//    private static void handleSaveProductRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
//        try {
//            // Parse the JSON data from the request into a Product object
//            Gson gson = new Gson();
//            Product product = gson.fromJson(req.body, Product.class);
//
//            // Check if a product with the same ProductID already exists
//            PreparedStatement checkStmt = connection.prepareStatement("SELECT COUNT(*) FROM Products WHERE ProductID = ?");
//            checkStmt.setInt(1, product.getProductID());
//            ResultSet checkResult = checkStmt.executeQuery();
//            checkResult.next();
//            int existingProductCount = checkResult.getInt(1);
//
//            if (existingProductCount > 0) {
//                // Update the existing product
//                PreparedStatement updateStmt = connection.prepareStatement("UPDATE Products SET Name = ?, Price = ?, Quantity = ? WHERE ProductID = ?");
//                updateStmt.setString(1, product.getName());
//                updateStmt.setDouble(2, product.getPrice());
//                updateStmt.setInt(3, (int) product.getQuantity());
//                updateStmt.setInt(4, product.getProductID());
//
//                int rowsUpdated = updateStmt.executeUpdate();
//
//                if (rowsUpdated > 0) {
//                    // Product was successfully updated
//                    res.code = ResponseModel.OK;
//                    res.body = "Product updated successfully.";
//                } else {
//                    // Product update failed
//                    res.code = ResponseModel.ERROR;
//                    res.body = "Error updating the product in the database.";
//                }
//            } else {
//                // Insert a new product
//                PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO Products (ProductID, Name, Price, Quantity) VALUES (?, ?, ?, ?)");
//                insertStmt.setInt(1, product.getProductID());
//                insertStmt.setString(2, product.getName());
//                insertStmt.setDouble(3, product.getPrice());
//                insertStmt.setInt(4, (int) product.getQuantity());
//
//                int rowsInserted = insertStmt.executeUpdate();
//
//                if (rowsInserted > 0) {
//                    // New product was successfully saved
//                    res.code = ResponseModel.OK;
//                    res.body = "New product saved successfully.";
//                } else {
//                    // Product insertion failed
//                    res.code = ResponseModel.ERROR;
//                    res.body = "Error saving the new product to the database.";
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            res.code = ResponseModel.ERROR;
//            res.body = "Error saving or updating the product in the database.";
//        }
//    }
//
//    private static void handleLoadProductRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
//        // Create a PreparedStatement for loading a product from the database
//        try {
//            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Products WHERE productID = ?");
//            int id = Integer.parseInt(req.body);
//            stmt.setInt(1, id);
//            Gson gson = new Gson();
//            ResultSet resultSet = stmt.executeQuery();
//            if (resultSet.next()) {
//                // Build a Product object from the result and set it in the response
//                Product product = new Product(id,resultSet.getInt(1), resultSet.getString(2), resultSet.getDouble(3), resultSet.getInt(4));
//                res.code = ResponseModel.OK;
//                res.body = gson.toJson(product);
//            } else {
//                // Product not found
//                res.code = ResponseModel.DATA_NOT_FOUND;
//                res.body = "";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            res.code = ResponseModel.ERROR;
//            res.body = "Error loading the product from the database.";
//        }
//    }

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

//    private static void handleSaveProductRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
//        try {
//            // Parse the JSON data from the request into a Product object
//            Gson gson = new Gson();
//            Product product = gson.fromJson(req.body, Product.class);
//
//            // Check if a product with the same ProductID already exists
//            PreparedStatement checkStmt = connection.prepareStatement("SELECT COUNT(*) FROM Products WHERE ProductID = ?");
//            checkStmt.setInt(1, product.getProductID());
//            ResultSet checkResult = checkStmt.executeQuery();
//            checkResult.next();
//            int existingProductCount = checkResult.getInt(1);
//
//            if (existingProductCount > 0) {
//                // Update the existing product
//                PreparedStatement updateStmt = connection.prepareStatement("UPDATE Products SET Name = ?, Price = ?, Quantity = ? WHERE ProductID = ?");
//                updateStmt.setString(1, product.getName());
//                updateStmt.setDouble(2, product.getPrice());
//                updateStmt.setInt(3, (int) product.getQuantity());
//                updateStmt.setInt(4, product.getProductID());
//
//                int rowsUpdated = updateStmt.executeUpdate();
//
//                if (rowsUpdated > 0) {
//                    // Product was successfully updated
//                    res.code = ResponseModel.OK;
//                    res.body = "Product updated successfully.";
//                } else {
//                    // Product update failed
//                    res.code = ResponseModel.ERROR;
//                    res.body = "Error updating the product in the database.";
//                }
//            } else {
//                // Insert a new product
//                PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO Products (ProductID, Name, Price, Quantity) VALUES (?, ?, ?, ?)");
//                insertStmt.setInt(1, product.getProductID());
//                insertStmt.setString(2, product.getName());
//                insertStmt.setDouble(3, product.getPrice());
//                insertStmt.setInt(4, (int) product.getQuantity());
//
//                int rowsInserted = insertStmt.executeUpdate();
//
//                if (rowsInserted > 0) {
//                    // New product was successfully saved
//                    res.code = ResponseModel.OK;
//                    res.body = "New product saved successfully.";
//                } else {
//                    // Product insertion failed
//                    res.code = ResponseModel.ERROR;
//                    res.body = "Error saving the new product to the database.";
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            res.code = ResponseModel.ERROR;
//            res.body = "Error saving or updating the product in the database.";
//        }
//    }

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
                PreparedStatement insertStmt = connection.prepareStatement("INSERT INTO Books (BookName, AuthorName, Quantity, Status) VALUES (?, ?, ?, ?)");
                insertStmt.setString(1, book.getBookName());
                insertStmt.setString(2, book.getAuthorName());
                insertStmt.setInt(3, (int)book.getQuantity());
                insertStmt.setString(4, book.getStatus());

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




    private static void handleLoadUserRequest(RequestModel req, ResponseModel res, Connection connection) {
        System.out.println("Handle Load UserRequest()");
        try {

            // Parse the body JSON to extract the username value
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(req.body, JsonObject.class);
            if (jsonObject.has("username")) {
                String username = jsonObject.get("username").getAsString();
                System.out.println("Parsed username: " + username);

                PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Users WHERE UserName = ?");
                stmt.setString(1, username);

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
                    System.out.println("User Created");
                } else {
                    System.out.println("User not found");
                    res.code = ResponseModel.DATA_NOT_FOUND;
                    res.body = "";
                }
            } else {
                System.out.println("Username not found in request body");
                res.code = ResponseModel.ERROR;
                res.body = "Username not provided.";
            }

        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error loading the user from the database.";
        }
    }

//    private static void handleSaveOrderRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
//        System.out.println("handleSaveOrderRequest");
//        try {
//            // Parse the JSON data from the request into an Order object
//            Gson gson = new Gson();
//            Order order = gson.fromJson(req.body, Order.class);
//
//            // Insert order
//            PreparedStatement orderStmt = connection.prepareStatement("INSERT INTO Orders (OrderDate, CustomerID, TotalCost, TotalTax) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
//            orderStmt.setString(1, String.valueOf(new Date(System.currentTimeMillis())));
//            orderStmt.setInt(2, order.getBuyerID());
//            orderStmt.setDouble(3, order.getTotalCost());
//            orderStmt.setDouble(4, order.getTotalTax());
//
//            int rowsInserted = orderStmt.executeUpdate();
//
//            if (rowsInserted > 0) {
//                // Retrieve the ID of the inserted order
//                ResultSet rs = orderStmt.getGeneratedKeys();
//                if (rs.next()) {
//                    int lastOrderId = rs.getInt(1);
//
//                    // Insert order lines
//                    PreparedStatement lineStmt = connection.prepareStatement("INSERT INTO OrderLine VALUES (?, ?, ?, ?)");
//
//                    for (OrderLine line : order.getLines()) {
//                        lineStmt.setInt(1, lastOrderId);
//                        lineStmt.setInt(2, line.getProductID());
//                        lineStmt.setDouble(3, line.getQuantity());
//                        lineStmt.setDouble(4, line.getCost());
//                        lineStmt.addBatch();
//                    }
//                    lineStmt.executeBatch();
//                    lineStmt.close();
//
//                    res.code = ResponseModel.OK;
//                    res.body = "Order and order lines saved successfully.";
//                } else {
//                    // Handle not being able to get the inserted order's ID
//                    res.code = ResponseModel.ERROR;
//                    res.body = "Error retrieving the order ID.";
//                }
//
//                rs.close();
//                orderStmt.close();
//
//            } else {
//                // Handle not being able to save the order
//                res.code = ResponseModel.ERROR;
//                res.body = "Error saving the order to the database.";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            res.code = ResponseModel.ERROR;
//            res.body = "Error saving the order to the database.";
//        }
//    }

    private static void handleSaveOrderBookRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Parse the JSON data from the request into an OrderBook object
            Gson gson = new Gson();
            OrderBook orderBook = gson.fromJson(req.body, OrderBook.class);

            // Insert order
            PreparedStatement orderStmt = connection.prepareStatement("INSERT INTO OrderBook (OrderDate, StudentID, ReturnDate) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            orderStmt.setString(1, String.valueOf(new Date(System.currentTimeMillis())));
            orderStmt.setInt(2, orderBook.getStudentID());
            orderStmt.setString(3, orderBook.getReturnDate());

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
                        lineStmt.setInt(3, (int)line.getQuantity());
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


    private static void handleSaveShippingAddressRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            // Parse the JSON data from the request into a ShippingAddress object
            Gson gson = new Gson();
            ShippingAddress shippingAddress = gson.fromJson(req.body, ShippingAddress.class);

            // Create a PreparedStatement for inserting a shipping address into the database
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO ShippingAddress (StreetNumberAndName, ApartmentOrUnitNumber, City, State, ZipCode) VALUES (?, ?, ?, ?, ?)");
            stmt.setString(1, shippingAddress.getStreetNumberAndName());
            stmt.setString(2, shippingAddress.getApartmentOrUnitNumber());
            stmt.setString(3, shippingAddress.getCity());
            stmt.setString(4, shippingAddress.getState());
            stmt.setInt(5, shippingAddress.getZipCode());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                // Shipping address was successfully saved
                res.code = ResponseModel.OK;
                res.body = "Shipping address saved successfully.";
            } else {
                // Shipping address could not be saved
                res.code = ResponseModel.ERROR;
                res.body = "Error saving the shipping address to the database.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error saving the shipping address to the database.";
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
            receipt.setUserId(receiptNumber);
            receipt.setOrderId(receiptNumber);

            // Create a PreparedStatement for inserting receipt information into the database
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO Receipt (OrderID, UserID, DateTime, TotalCost, ShippingAddress, Books) VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setInt(1, receipt.getOrderId());
            stmt.setInt(2, receipt.getUserId());
            stmt.setString(3, receipt.getDateTime());
            stmt.setDouble(4, receipt.getTotalCost());
            stmt.setString(5, receipt.getShippingAddress());
            stmt.setString(6, receipt.getBooks());

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

    // Handle Save User Request
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

    // Handle Update User Request
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

    // Handle Delete User Request
    private static void handleDeleteUserRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            int id = Integer.parseInt(req.body);
            // Create a PreparedStatement for deleting a user by ID
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM Users WHERE UserID = ?");
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // User deleted successfully
                res.code = ResponseModel.OK;
                res.body = "User deleted successfully.";
            } else {
                // User not found
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "User not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error deleting the user from the database.";
        }
    }

    // Handle Update Shipping Address Request
    private static void handleUpdateShippingAddressRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            Gson gson = new Gson();
            // Parse the JSON data from the request into a ShippingAddress object
            ShippingAddress shippingAddress = gson.fromJson(req.body, ShippingAddress.class);

            // Create a PreparedStatement for updating a shipping address by ID
            PreparedStatement stmt = connection.prepareStatement("UPDATE ShippingAddresses SET UserID = ?, Address = ? WHERE AddressID = ?");
            stmt.setInt(1, shippingAddress.getAddressID());
            stmt.setString(2, shippingAddress.getStreetNumberAndName());
            stmt.setString(3, shippingAddress.getApartmentOrUnitNumber());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Shipping address updated successfully
                res.code = ResponseModel.OK;
                res.body = "Shipping address updated successfully.";
            } else {
                // Shipping address not found
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "Shipping address not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error updating the shipping address in the database.";
        }
    }

    // Handle Delete Shipping Address Request
    private static void handleDeleteShippingAddressRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            int id = Integer.parseInt(req.body);
            // Create a PreparedStatement for deleting a shipping address by ID
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM ShippingAddresses WHERE AddressID = ?");
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Shipping address deleted successfully
                res.code = ResponseModel.OK;
                res.body = "Shipping address deleted successfully.";
            } else {
                // Shipping address not found
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "Shipping address not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error deleting the shipping address from the database.";
        }
    }

    // Handle Update Receipt Request
    private static void handleUpdateReceiptRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        try {
            Gson gson = new Gson();
            // Parse the JSON data from the request into a Receipt object
            Receipt receipt = gson.fromJson(req.body, Receipt.class);

            // PreparedStatement for updating a receipt by ID
            PreparedStatement stmt = connection.prepareStatement("UPDATE Receipt SET OrderID = ?, UserID = ?, DateTime = ?, TotalCost = ?, ShippingAddress = ?, Products = ? WHERE ReceiptNumber = ?");
            stmt.setInt(1, receipt.getOrderId());
            stmt.setInt(2, receipt.getUserId());
            stmt.setString(3, receipt.getDateTime());
            stmt.setDouble(4, receipt.getTotalCost());
            stmt.setString(5, receipt.getShippingAddress());
            stmt.setString(6, receipt.getBooks());
            stmt.setInt(7, receipt.getReceiptNumber());

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

    // Handle Load Receipt Request
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
                        resultSet.getInt("UserID"),
                        resultSet.getString("DateTime"),
                        resultSet.getDouble("TotalCost"),
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

    // Handle Delete Receipt Request
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
