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
    private static final String URLUSER = "http://localhost:5056";
    private static final String URLBOOK = "http://localhost:5057";
    private static final String URLORDER = "http://localhost:5058";

    private static final String BOOK = "/book";
    private static final String USER = "/user";
    private static final String STUDENT = "/student";

    public int orderCount;
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

    public Book updateBook(Book book) throws IOException {
        URL url = new URL("http://localhost:5057/book");

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

            if(book!=null){
                System.out.println("Response from GET Product request:");
                System.out.println("Receiving a Book object");
                System.out.println("BookID = " + book.getBookID());
                System.out.println("Book name = " + book.getBookName());
            }
            else{
                System.out.println("No such Book Found");
            }

        } else {
            System.out.println("GET Product request failed. Response code: " + responseCode);
        }
        return book;
    }

    public OrderBook updateOrderBook(OrderBook order) throws IOException {
        URL url = new URL("http://localhost:5058/order");
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
            orderCount = responseOrder.getOrderID();
        } else {
            System.out.println("POST Order request failed. Response code: " + responseCode);
        }
        return responseOrder;
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

    public Receipt updateReceipt(Receipt receipt) throws IOException {
        URL url = new URL("http://localhost:5058/receipt");
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
            System.out.println("Response from GET User request:");
            if(user!=null) {
                System.out.println("UserID: " + user.getUserID());
                System.out.println("UserName: " +  user.getUsername());
                System.out.println("Password: " + user.getPassword());
                System.out.println("Display Name: " + user.getFullName());
            }

            else{
                System.out.println("Authentication Failed");
            }

        } else {
            System.out.println("GET Product request failed. Response code: " + responseCode);
        }
        return user;
    }

}
