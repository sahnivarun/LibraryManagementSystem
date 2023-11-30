import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;

public class DataServer2 {
    public static void main(String[] args) throws IOException {
        int port = 5056;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/book", new BookHandler());
        server.createContext("/order", new OrderHandler());
        server.createContext("/user", new UserHandler());
        server.createContext("/student", new StudentHandler());
        server.createContext("/receipt", new ReceiptHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Data Server is running on port " + port);
    }

    static class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Connection sqlConn = null;

            DataAdapter2 dataAdapter = new DataAdapter2(sqlConn);
            if ("GET".equals(exchange.getRequestMethod())) {
                String requestPath = exchange.getRequestURI().getPath();
                String[] pathSegments = requestPath.split("/");
                String id = pathSegments[pathSegments.length-1];
                Book book = dataAdapter.loadBook(Integer.valueOf(id));
                String jsonResponse = objectToJson(book);

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } else if ("POST".equals(exchange.getRequestMethod())) {
                ObjectMapper objectMapper = new ObjectMapper();
                Book receivedBook = objectMapper.readValue(exchange.getRequestBody(), Book.class);

                dataAdapter.saveBook(receivedBook);

                String jsonResponse = objectToJson(receivedBook);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0); // Method not allowed for non-POST requests
            }
        }
    }

    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Connection sqlConn = null;

            DataAdapter2 dataAdapter = new DataAdapter2(sqlConn);
            if ("POST".equals(exchange.getRequestMethod())) {
                ObjectMapper objectMapper = new ObjectMapper();
                OrderBook receivedOrder = objectMapper.readValue(exchange.getRequestBody(), OrderBook.class);
                dataAdapter.saveOrderBook(receivedOrder);

                String jsonResponse = objectToJson(receivedOrder);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0); // Method not allowed for non-POST requests
            }
        }
    }

    static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Connection sqlConn = null;

            DataAdapter2 dataAdapter = new DataAdapter2(sqlConn);
            if ("GET".equals(exchange.getRequestMethod())) {
                String requestPath = exchange.getRequestURI().getPath();
                String[] pathSegments = requestPath.split("/");
                String username = pathSegments[pathSegments.length-2];
                String password = pathSegments[pathSegments.length-1];
                User user = dataAdapter.loadUser(username, password);
                String jsonResponse = objectToJson(user);

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            }
        }
    }

    static class StudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Connection sqlConn = null;

            DataAdapter2 dataAdapter = new DataAdapter2(sqlConn);
            if ("GET".equals(exchange.getRequestMethod())) {
                String requestPath = exchange.getRequestURI().getPath();
                String[] pathSegments = requestPath.split("/");
                String id = pathSegments[pathSegments.length-1];  //this id is CustomerID
                Student student = dataAdapter.loadStudent(Integer.valueOf(id));
                String jsonResponse = objectToJson(student);

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            }

            else {
                exchange.sendResponseHeaders(405, 0); // Method not allowed for non-POST requests
            }
        }
    }

    static class ReceiptHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Connection sqlConn = null;

            DataAdapter2 dataAdapter = new DataAdapter2(sqlConn);
            if ("POST".equals(exchange.getRequestMethod())) {
                ObjectMapper objectMapper = new ObjectMapper();
                Receipt receivedReceipt = objectMapper.readValue(exchange.getRequestBody(), Receipt.class);
                dataAdapter.saveReceipt(receivedReceipt);

                String jsonResponse = objectToJson(receivedReceipt);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0);
            }
        }
    }

    static String objectToJson(Object object) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

}