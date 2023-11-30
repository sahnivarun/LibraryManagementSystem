//import com.google.gson.Gson;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.net.HttpURLConnection;
//import java.net.Socket;
//import java.io.IOException;
//import java.net.URL;
//
//import org.codehaus.jackson.JsonNode;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.node.ObjectNode;
//
//import org.codehaus.jackson.map.DeserializationConfig;
//import org.codehaus.jackson.map.ObjectMapper;
//
//import java.io.*;
//
//import static util.PortAddresses.MAIN_SERVER_PORT;
//
//public class OrderBookRemoteDataAdapter {
//    private static final String URL = "http://localhost:5058";
//    private static final String BOOK = "/book";
//    private static final String USER = "/user";
//    private static final String STUDENT = "/student";
//    public int orderCount;
//    private Gson gson = new Gson();
//    private Socket s = null;
//    private DataInputStream dis = null;
//    private DataOutputStream dos = null;
//
//    public void connect() {
//
//        try {
//            s = new Socket("localhost", MAIN_SERVER_PORT);
//            dis = new DataInputStream(s.getInputStream());
//            dos = new DataOutputStream(s.getOutputStream());
//        } catch (Exception ex) {
//            System.out.println("connect Ex:" + ex.toString());
//            ex.printStackTrace();
//        }
//    }
//
//    public void disconnect() {
//        try {
//            if (dis != null) {
//                dis.close();
//            }
//            if (dos != null) {
//                dos.close();
//            }
//            if (s != null && !s.isClosed()) {
//                s.close();
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public OrderBook updateOrderBook(OrderBook order) throws IOException {
//        URL url = new URL("http://localhost:5058/order");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setDoOutput(true);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        ObjectNode orderNode = objectMapper.valueToTree(order);
//        try (OutputStream os = connection.getOutputStream()) {
//            byte[] input = objectMapper.writeValueAsBytes(orderNode);
//            os.write(input);
//        }
//        int responseCode = connection.getResponseCode();
//        OrderBook responseOrder = new OrderBook();
//        if (responseCode == 200) {
//            ObjectMapper responseMapper = new ObjectMapper();
//            responseOrder = responseMapper.readValue(connection.getInputStream(), OrderBook.class);
//            //validation for debugging
//            System.out.println("Order Id: " +  responseOrder.getOrderID());
//            orderCount = responseOrder.getOrderID();
//        } else {
//            System.out.println("POST Order request failed. Response code: " + responseCode);
//        }
//        return responseOrder;
//    }
//
//    public Student loadStudent(int studentID) {
//        //connect();
//        RequestModel req = new RequestModel();
//        req.code = RequestModel.LOAD_STUDENT_REQUEST;
//        req.body = String.valueOf(studentID);
//
//        String json = gson.toJson(req);
//        try {
//            dos.writeUTF(json);
//            String received = dis.readUTF();
//
//            System.out.println("Server response:" + received);
//
//            ResponseModel res = gson.fromJson(received, ResponseModel.class);
//
//            if (res.code == ResponseModel.UNKNOWN_REQUEST) {
//                System.out.println("The request is not recognized by the Server");
//                return null;
//            } else if (res.code == ResponseModel.DATA_NOT_FOUND) {
//                System.out.println("The Server could not find a student with this ID!");
//                return null;
//            } else {
//                Student model = gson.fromJson(res.body, Student.class);
//                System.out.println("Receiving a Student object");
//                System.out.println("StudentID = " + model.getStudentID());
//                System.out.println("Student name = " + model.getStudentName());
//                return model;
//            }
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//        return null;
//    }
//
//    static Student rdagetStudent(int id) throws IOException {
//        URL url = getUrlBook(STUDENT, id);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        Student student = new Student();
//        int responseCode = connection.getResponseCode();
//        if (responseCode == 200) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            JsonNode responseJson = objectMapper.readTree(connection.getInputStream());
//            student = objectMapper.readValue(responseJson, Student.class);
//            System.out.println("Response from GET Student request:");
//            System.out.println("Receiving a Student object");
//            System.out.println("StudentID = " + student.getStudentID());
//            System.out.println("Student name = " + student.getStudentName());
//        } else {
//            System.out.println("GET Student request failed. Response code: " + responseCode);
//        }
//        return student;
//    }
//
//    public boolean saveStudent(Student student, int id) {
//        // Connect to the server (establish communication with the server)
//
//        RequestModel req = new RequestModel();
//        req.code = RequestModel.SAVE_STUDENT_REQUEST;
//        req.body = gson.toJson(student);
//
//        String json = gson.toJson(req);
//        try {
//            dos.writeUTF(json);
//
//            String received = dis.readUTF();
//
//            ResponseModel res = gson.fromJson(received, ResponseModel.class);
//
//            if (res.code == ResponseModel.OK) {
//                System.out.println("Student details saved on the server successfully.");
//                return true;
//            } else {
//                System.out.println("Failed to save the student details on the server.");
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            System.out.println("Error while saving the student details on the server.");
//        }
//
//        return false;
//    }
//
//    public boolean saveReceipt(Receipt receipt) {
//        //connect();
//        RequestModel req = new RequestModel();
//        req.code = RequestModel.SAVE_RECEIPT_REQUEST;
//        req.body = gson.toJson(receipt);
//
//        String json = gson.toJson(req);
//        try {
//            dos.writeUTF(json);
//
//            String received = dis.readUTF();
//
//            ResponseModel res = gson.fromJson(received, ResponseModel.class);
//
//            if (res.code == ResponseModel.OK) {
//                System.out.println("Receipt saved on the server successfully.");
//                return true;
//            } else {
//                System.out.println("Failed to save the receipt on the server.");
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            System.out.println("Error while saving the receipt on the server.");
//        }
//
//        return false;
//    }
//
//    public Receipt updateReceipt(Receipt receipt) throws IOException {
//        URL url = new URL("http://localhost:5058/receipt");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("POST");
//        connection.setDoOutput(true);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        ObjectNode receiptNode = objectMapper.valueToTree(receipt);
//        try (OutputStream os = connection.getOutputStream()) {
//            byte[] input = objectMapper.writeValueAsBytes(receiptNode);
//            os.write(input);
//        }
//        int responseCode = connection.getResponseCode();
//        Receipt responseReceipt = new Receipt();
//        if (responseCode == 200) {
//            ObjectMapper responseMapper = new ObjectMapper();
//            responseReceipt = responseMapper.readValue(connection.getInputStream(), Receipt.class);
//        } else {
//            System.out.println("POST Receipt request failed. Response code: " + responseCode);
//        }
//        return responseReceipt;
//    }
//
//}
//
