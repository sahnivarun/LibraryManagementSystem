import com.google.gson.Gson;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.io.IOException;
import java.net.URL;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;

import static util.PortAddresses.MAIN_SERVER_PORT;

public class RemoteDataAdapter {
    private static final String URL = "http://localhost:5056";
    private static final String BOOK = "/book";
    private static final String USER = "/user";
    private static final String STUDENT = "/student";

    private int orderCount;
    private Gson gson = new Gson();
    private Socket s = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;

    public void connect() {

        try {
            s = new Socket("localhost", MAIN_SERVER_PORT);
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
        } catch (Exception ex) {
            System.out.println("connect Ex:" + ex.toString());
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (dis != null) {
                dis.close();
            }
            if (dos != null) {
                dos.close();
            }
            if (s != null && !s.isClosed()) {
                s.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void test() throws IOException {
        Book book = getBook(1);
        book.setBookName("Arshnoor");
        Book newBook = updateBook(book);
    }

    public Book updateBook(Book book) throws IOException {
        URL url = new URL("http://localhost:5056/book");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode bookNode = objectMapper.valueToTree(book);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = objectMapper.writeValueAsBytes(bookNode);
            os.write(input);
        }
        int responseCode = connection.getResponseCode();
        Book responseBook = new Book();
        if (responseCode == 200) {
            ObjectMapper responseMapper = new ObjectMapper();
            responseBook = responseMapper.readValue(connection.getInputStream(), Book.class);
            //for debugging
            System.out.println("BookName: " +  responseBook.getBookName());
        } else {
            System.out.println("GET Book request failed. Response code: " + responseCode);
        }
        return responseBook;
    }

    public Book rdaloadBook(int bookID) {
        //connect();
        RequestModel req = new RequestModel();
        req.code = RequestModel.LOAD_BOOK_REQUEST;
        req.body = String.valueOf(bookID);

        String json = gson.toJson(req);
        try {
            dos.writeUTF(json);
            String received = dis.readUTF();

            System.out.println("Server response:" + received);

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.UNKNOWN_REQUEST) {
                System.out.println("The request is not recognized by the Server");
                return null;
            } else if (res.code == ResponseModel.DATA_NOT_FOUND) {
                System.out.println("The Server could not find a book with this ID!");
                return null;
            } else {
                Book model = gson.fromJson(res.body, Book.class);
                System.out.println("Receiving a Book object");
                System.out.println("BookID = " + model.getBookID());
                System.out.println("Book name = " + model.getBookName());
                return model;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static URL getUrlBook(String item, int id) throws IOException{
        String url = URL + item +"/" + id;
        return new URL(url);
    }
    static Book getBook(int id) throws IOException {
        URL url = getUrlBook(BOOK, id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        Book book = new Book();
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseJson = objectMapper.readTree(connection.getInputStream());
            book = objectMapper.readValue(responseJson, Book.class);

            System.out.println("Response from GET Product request:");
            System.out.println("Receiving a Book object");
            System.out.println("BookID = " + book.getBookID());
            System.out.println("Book name = " + book.getBookName());
        } else {
            System.out.println("GET Product request failed. Response code: " + responseCode);
        }
        return book;
    }

    public OrderBook updateOrderBook(OrderBook order) throws IOException {
        URL url = new URL("http://localhost:5056/order");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode orderNode = objectMapper.valueToTree(order);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = objectMapper.writeValueAsBytes(orderNode);
            os.write(input);
        }
        int responseCode = connection.getResponseCode();
        OrderBook responseOrder = new OrderBook();
        if (responseCode == 200) {
            ObjectMapper responseMapper = new ObjectMapper();
            responseOrder = responseMapper.readValue(connection.getInputStream(), OrderBook.class);
            //validation for debugging
            System.out.println("Order Id: " +  responseOrder.getOrderID());
        } else {
            System.out.println("POST Order request failed. Response code: " + responseCode);
        }
        return responseOrder;
    }

    public Student loadStudent(int studentID) {
        //connect();
        RequestModel req = new RequestModel();
        req.code = RequestModel.LOAD_STUDENT_REQUEST;
        req.body = String.valueOf(studentID);

        String json = gson.toJson(req);
        try {
            dos.writeUTF(json);
            String received = dis.readUTF();

            System.out.println("Server response:" + received);

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.UNKNOWN_REQUEST) {
                System.out.println("The request is not recognized by the Server");
                return null;
            } else if (res.code == ResponseModel.DATA_NOT_FOUND) {
                System.out.println("The Server could not find a student with this ID!");
                return null;
            } else {
                Student model = gson.fromJson(res.body, Student.class);
                System.out.println("Receiving a Student object");
                System.out.println("StudentID = " + model.getStudentID());
                System.out.println("Student name = " + model.getStudentName());
                return model;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    static Student rdagetStudent(int id) throws IOException {
        URL url = getUrlBook(STUDENT, id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        Student student = new Student();
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JsonNode responseJson = objectMapper.readTree(connection.getInputStream());
            student = objectMapper.readValue(responseJson, Student.class);
            System.out.println("Response from GET Student request:");
            System.out.println("Receiving a Student object");
            System.out.println("StudentID = " + student.getStudentID());
            System.out.println("Student name = " + student.getStudentName());
        } else {
            System.out.println("GET Student request failed. Response code: " + responseCode);
        }
        return student;
    }

    public boolean saveStudent(Student student, int id) {
        // Connect to the server (establish communication with the server)

        RequestModel req = new RequestModel();
        req.code = RequestModel.SAVE_STUDENT_REQUEST;
        req.body = gson.toJson(student);

        String json = gson.toJson(req);
        try {
            dos.writeUTF(json);

            String received = dis.readUTF();

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.OK) {
                System.out.println("Student details saved on the server successfully.");
                return true;
            } else {
                System.out.println("Failed to save the student details on the server.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error while saving the student details on the server.");
        }

        return false;
    }

    public boolean saveReceipt(Receipt receipt) {
        //connect();
        RequestModel req = new RequestModel();
        req.code = RequestModel.SAVE_RECEIPT_REQUEST;
        req.body = gson.toJson(receipt);

        String json = gson.toJson(req);
        try {
            dos.writeUTF(json);

            String received = dis.readUTF();

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.OK) {
                System.out.println("Receipt saved on the server successfully.");
                return true;
            } else {
                System.out.println("Failed to save the receipt on the server.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Error while saving the receipt on the server.");
        }

        return false;
    }

    public Receipt updateReceipt(Receipt receipt) throws IOException {
        URL url = new URL("http://localhost:5056/receipt");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode receiptNode = objectMapper.valueToTree(receipt);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = objectMapper.writeValueAsBytes(receiptNode);
            os.write(input);
        }
        int responseCode = connection.getResponseCode();
        Receipt responseReceipt = new Receipt();
        if (responseCode == 200) {
            ObjectMapper responseMapper = new ObjectMapper();
            responseReceipt = responseMapper.readValue(connection.getInputStream(), Receipt.class);
        } else {
            System.out.println("POST Receipt request failed. Response code: " + responseCode);
        }
        return responseReceipt;
    }

    public User loadUser(String username, String password) {
        //connect();
        RequestModel req = new RequestModel();
        req.code = RequestModel.LOAD_USER_REQUEST;

        // Create a User object with the provided username and password
        User user = new User(username, password);

        req.body = gson.toJson(user);

        String json = gson.toJson(req);
        System.out.println("json: " + json);
        try {
            dos.writeUTF(json);
            String received = dis.readUTF();  // No data received
            System.out.println("Received: " + received);

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            System.out.println("res: " + res.toString() + "  code: " + res.code + " body: "+ res.body);

            if (res.code == ResponseModel.UNKNOWN_REQUEST) {
                System.out.println("The request is not recognized by the Server");
                return null;
            } else if (res.code == ResponseModel.DATA_NOT_FOUND) {
                System.out.println("The Server could not find a user with that username and password!");
                return null;
            } else {
                User loadedUser = gson.fromJson(res.body, User.class);
                System.out.println("Receiving a User object");
                System.out.println("Username = " + loadedUser.getUsername());
                return loadedUser;
            }

        } catch (Exception ex) {
            System.out.println("exception: " + ex.toString() );
            ex.printStackTrace();
        }

        return null;
    }

    private static URL getUrlUser(String item, String username, String password) throws IOException{
        String url = URL + item +"/" + username + "/" + password;
        return new URL(url);
    }

    public User getUser(String username, String password) throws IOException {
        URL url = getUrlUser(USER, username, password);
        System.out.println(url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        User user = new User();
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseJson = objectMapper.readTree(connection.getInputStream());
            user = objectMapper.readValue(responseJson, User.class);
            //testing for proper working
            System.out.println("Response from GET Product request:");
            System.out.println("UserID: " + user.getUserID());
            System.out.println("UserName: " +  user.getUsername());
            System.out.println("Password: " + user.getPassword());
            System.out.println("Display Name: " + user.getFullName());
        } else {
            System.out.println("GET Product request failed. Response code: " + responseCode);
        }
        return user;
    }

    public int getOrderCount() {
        //connect();

        // Create a request to fetch the order count
        RequestModel req = new RequestModel();
        req.code = RequestModel.GET_ORDER_COUNT_REQUEST;

        try {
            String json = gson.toJson(req);
            dos.writeUTF(json);

            //Receive the response from the server
            String received = dis.readUTF();
            System.out.println("Server response: " + received);

            ResponseModel res = gson.fromJson(received, ResponseModel.class);

            if (res.code == ResponseModel.UNKNOWN_REQUEST) {
                System.out.println("The request is not recognized by the Server");
                return -1; // Return an error value
            } else {
                int orderCount = Integer.parseInt(res.body); // Fetch and return the order count
                return orderCount;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return -1; // Return an error value if there's an issue
    }

}
