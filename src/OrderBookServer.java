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
        int port = 5058;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/order", new OrderHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("OrderBook Server is running on port " + port);
    }

    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Connection sqlConn = null;
            try {
                sqlConn = DriverManager.getConnection("jdbc:sqlite:store.db");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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

    static String objectToJson(Object object) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

}
