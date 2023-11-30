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

public class BookRemoteDataAdapter {
    private static final String URL = "http://localhost:5057";
    private static final String BOOK = "/book";
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



}
