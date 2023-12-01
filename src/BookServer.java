import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;

public class BookServer {
    public static void main(String[] args) throws IOException {
        int port = 5058;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/book", new BookHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Book Server is running on port " + port);
    }

    static class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // Set CORS headers for all requests
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1); // preflight request successful
                return;
            }

            Connection conn = null;

            DataAdapter2 dataAdapter = new DataAdapter2(conn);
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
                // Log the received book details
                System.out.println("Received Book: " + receivedBook.toString());

                System.out.println("Before saveBook");
                dataAdapter.saveBook(receivedBook);
                System.out.println("After saveBook");

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

    static String objectToJson(Object object) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

}

