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
//
//import java.io.*;
//
//import static util.PortAddresses.MAIN_SERVER_PORT;
//
//public class UserRemoteDataAdapter {
//    private static final String URL = "http://localhost:5056";
//    private static final String USER = "/user";
//
//    private Gson gson = new Gson();
//    private Socket s = null;
//    private DataInputStream dis = null;
//    private DataOutputStream dos = null;
//
//    public void connect() {
//
//        try {
//            s = new Socket("localhost", 5056);
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
//    public User loadUser(String username, String password) {
//        //connect();
//        RequestModel req = new RequestModel();
//        req.code = RequestModel.LOAD_USER_REQUEST;
//
//        // Create a User object with the provided username and password
//        User user = new User(username, password);
//
//        req.body = gson.toJson(user);
//
//        String json = gson.toJson(req);
//        System.out.println("json: " + json);
//        try {
//            dos.writeUTF(json);
//            String received = dis.readUTF();  // No data received
//            System.out.println("Received: " + received);
//
//            ResponseModel res = gson.fromJson(received, ResponseModel.class);
//
//            System.out.println("res: " + res.toString() + "  code: " + res.code + " body: "+ res.body);
//
//            if (res.code == ResponseModel.UNKNOWN_REQUEST) {
//                System.out.println("The request is not recognized by the Server");
//                return null;
//            } else if (res.code == ResponseModel.DATA_NOT_FOUND) {
//                System.out.println("The Server could not find a user with that username and password!");
//                return null;
//            } else {
//                User loadedUser = gson.fromJson(res.body, User.class);
//                System.out.println("Receiving a User object");
//                System.out.println("Username = " + loadedUser.getUsername());
//                return loadedUser;
//            }
//
//        } catch (Exception ex) {
//            System.out.println("exception: " + ex.toString() );
//            ex.printStackTrace();
//        }
//
//        return null;
//    }
//
//    private static URL getUrlUser(String item, String username, String password) throws IOException{
//        String url = URL + item +"/" + username + "/" + password;
//        return new URL(url);
//    }
//
//    public User getUser(String username, String password) throws IOException {
//        URL url = getUrlUser(USER, username, password);
//        System.out.println(url);
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        User user = new User();
//        int responseCode = connection.getResponseCode();
//        if (responseCode == 200) {
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode responseJson = objectMapper.readTree(connection.getInputStream());
//            user = objectMapper.readValue(responseJson, User.class);
//            //testing for proper working
//            System.out.println("Response from GET User request:");
//            if(user!=null) {
//                System.out.println("UserID: " + user.getUserID());
//                System.out.println("UserName: " +  user.getUsername());
//                System.out.println("Password: " + user.getPassword());
//                System.out.println("Display Name: " + user.getFullName());
//            }
//
//            else{
//                System.out.println("Authentication Failed");
//            }
//
//        } else {
//            System.out.println("GET Product request failed. Response code: " + responseCode);
//        }
//        return user;
//    }
//
//}
