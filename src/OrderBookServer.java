import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;

public class OrderBookServer {
    public static void main(String[] args) throws IOException {
        int port = 5059;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/order", new OrderHandler());
        server.createContext("/student", new StudentHandler());
        server.createContext("/receipt", new ReceiptHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("OrderBook Server is running on port " + port);
    }

    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // Set CORS headers for all requests
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:20000");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1); // preflight request successful
                return;
            }

            Connection conn = null;

            DataAdapter2 dataAdapter = new DataAdapter2(conn);
            if ("POST".equals(exchange.getRequestMethod())) {
                ObjectMapper objectMapper = new ObjectMapper();
                OrderBook receivedOrder = objectMapper.readValue(exchange.getRequestBody(), OrderBook.class);
                System.out.println("received order is:" +receivedOrder);
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

    static class StudentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // Set CORS headers for all requests
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:20000");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1); // preflight request successful
                return;
            }

            Connection conn = null;

            DataAdapter2 dataAdapter = new DataAdapter2(conn);
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

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:20000");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.getResponseHeaders().add("Access-Control-Allow-Credentials", "true");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1); // preflight request successful
                return;
            }

            Connection conn = null;

            DataAdapter2 dataAdapter = new DataAdapter2(conn);
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