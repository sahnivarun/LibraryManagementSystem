import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;

public class UserServer {
    public static void main(String[] args) throws IOException {
        int port = 5057;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/user", new UserHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("User Server is running on port " + port);
    }

    static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Connection conn = null;

            DataAdapter2 dataAdapter = new DataAdapter2(conn);
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

    static String objectToJson(Object object) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

}