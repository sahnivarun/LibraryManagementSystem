// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
            {
                // socket object to receive incoming client requests
//                if (nClients < 1){
                    s = ss.accept();
                    nClients++;

//                }else {
//                    return;
//                }

                System.out.println("A new client is connected : " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                Gson gson = new Gson();
                DataAccess dao = new SQLiteDataAdapter(Application.getInstance().getDBConnection());

                Thread t = new ClientHandler(s, dis, dos, gson, dao, connection);
                t.start();

            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
}


class ClientHandler extends Thread {
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Socket clientSocket;
    private final Gson gson;
    private final DataAccess dao;
    private final Connection connection;

    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, Gson gson, DataAccess dao, Connection connection) {
        this.clientSocket = s;
        this.dis = dis;
        this.dos = dos;
        this.gson = gson;
        this.dao = dao;
        this.connection = connection;
    }

    @Override
    public void run() {
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
                    case RequestModel.LOAD_PRODUCT_REQUEST:
                        handleLoadProductRequest(req, res, dao, connection);
                        break;
                    case RequestModel.SAVE_PRODUCT_REQUEST:
                        handleSaveProductRequest(req, res, dao, connection);
                        break;
//                    case RequestModel.DELETE_PRODUCT_REQUEST:
//                        handleDeleteProductRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.SAVE_ORDER_REQUEST:
//                        handleSaveOrderRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.LOAD_ORDER_REQUEST:
//                        handleLoadOrderRequest(req,res,dao,connection);
//                        break;
//                    case RequestModel.UPDATE_ORDER_REQUEST:
//                        handleUpdateOrderRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.DELETE_ORDER_REQUEST:
//                        handleDeleteOrderRequest(req, res, dao, connection);
//                        break;
                    case RequestModel.LOAD_USER_REQUEST:
                        System.out.println("Switch Load User ");
                        handleLoadUserRequest(req, res, connection);
                        break;
//                    case RequestModel.SAVE_USER_REQUEST:
//                        handleSaveUserRequest();
//                        break;
//                    case RequestModel.UPDATE_USER_REQUEST:
//                        handleUpdateUserRequest();
//                        break;
//                    case RequestModel.DELETE_USER_REQUEST:
//                        handleDeleteUserRequest();
//                        break;
//                    case RequestModel.SAVE_SHIPPING_ADDRESS_REQUEST:
//                        handleSaveShippingAddressRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.UPDATE_SHIPPING_ADDRESS_REQUEST:
//                        handleUpdateShippingAddressRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.LOAD_SHIPPING_ADDRESS_REQUEST:
//                        handleLoadShippingAddressRequest();
//                        break;
//                    case RequestModel.DELETE_SHIPPING_ADDRESS_REQUEST:
//                        handleDeleteShippingAddressRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.SAVE_CREDIT_CARD_REQUEST:
//                    handleSaveCreditCardRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.UPDATE_CREDIT_CARD_REQUEST:
//                        handleUpdateCreditCardRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.LOAD_CREDIT_CARD_REQUEST:
//                        handleLoadCreditCardRequest();
//                        break;
//                    case RequestModel.DELETE_CREDIT_CARD_REQUEST:
//                        handleDeleteCreditCardRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.SAVE_RECEIPT_REQUEST:
//                    handleSaveReceiptRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.UPDATE_RECEIPT_REQUEST:
//                        handleUpdateReceiptRequest(req, res, dao, connection);
//                        break;
//                    case RequestModel.LOAD_RECEIPT_REQUEST:
//                        handleLoadReceiptRequest();
//                        break;
//                    case RequestModel.DELETE_RECEIPT_REQUEST:
//                        handleDeleteReceiptRequest(req, res, dao, connection);
//                        break;

                    default:
                        handleUnknownRequest(req);
                }

                String json = gson.toJson(res);
                System.out.println("JSON object of ResponseModel: " + json);
                dos.writeUTF(json);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleLoadProductRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        // Create a PreparedStatement for loading a product from the database
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Products WHERE productID = ?");
            int id = Integer.parseInt(req.body);
            stmt.setInt(1, id);
            Gson gson = new Gson();
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                // Build a Product object from the result and set it in the response
                Product product = new Product(id,resultSet.getInt(1), resultSet.getString(2), resultSet.getDouble(3), resultSet.getInt(4));
                res.code = ResponseModel.OK;
                res.body = gson.toJson(product);
            } else {
                // Product not found
                res.code = ResponseModel.DATA_NOT_FOUND;
                res.body = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error loading the product from the database.";
        }
    }

    private static void handleSaveProductRequest(RequestModel req, ResponseModel res, DataAccess dao, Connection connection) {
        // Create a PreparedStatement for saving a product to the database
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO Products (name, price, quantity) VALUES (?, ?, ?)");
            Gson gson = new Gson();
            Product product = gson.fromJson(req.body, Product.class);
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, (int)product.getQuantity());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                res.code = ResponseModel.OK;
                res.body = "Product saved successfully.";
            } else {
                res.code = ResponseModel.ERROR;
                res.body = "Failed to save the product.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res.code = ResponseModel.ERROR;
            res.body = "Error saving the product to the database.";
        }
    }

//    private static void handleLoadUserRequest(RequestModel req, ResponseModel res, Connection connection) {
//        // Create a PreparedStatement for loading a user from the database
//        System.out.println("Handle Load UserRequst()");
//        try {
//            System.out.println("Inside try");
//            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Users WHERE UserName = ?");
//            String username = req.body;
//            System.out.println("Body: " + username);
//            stmt.setString(1, username);
//            Gson gson = new Gson();
//            ResultSet resultSet = stmt.executeQuery();
//            System.out.println("result");
//            if (resultSet.next()) {
//                // Build a User object from the result and set it in the response
//                User user = new User();
//                user.setUserID(resultSet.getInt("UserID"));
//                user.setUsername(resultSet.getString("UserName"));
//                user.setFullName(resultSet.getString("DisplayName"));
//                user.setPassword(resultSet.getString("Password"));
//
//                res.code = ResponseModel.OK;
//                res.body = gson.toJson(user);
//                System.out.println("User Created");
//            } else {
//                // User not found
//                System.out.println("User not found");
//                res.code = ResponseModel.DATA_NOT_FOUND;
//                res.body = "";
//            }
//        } catch (Exception e) {
//            System.out.println("Exception: " + e.toString());
//            e.printStackTrace();
//            res.code = ResponseModel.ERROR;
//            res.body = "Error loading the user from the database.";
//        }
//    }

    private static void handleLoadUserRequest(RequestModel req, ResponseModel res, Connection connection) {
        System.out.println("Handle Load UserRequst()");
        try {
            System.out.println("Inside try");

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


    //TODO: All below functions to be implemented
    private static void handleExitRequest(){

    }

    private static void handleUnknownRequest(RequestModel res){

    }


}
