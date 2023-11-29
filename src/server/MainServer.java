//package server;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//
//import static util.PortAddresses.MAIN_SERVER_PORT;
//
//public class MainServer {
//    public static void main(String[] args) throws Exception {
//        Class.forName("org.sqlite.JDBC");  // connect to its local database
//        Connection connection = DriverManager.getConnection("jdbc:sqlite:store.db");
//        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Products WHERE productID = ?");
//        PreparedStatement updateStatement =
//                connection.prepareStatement("UPDATE Products SET name = ?, price = ?, quantity = ? WHERE productID = ?");
//
//        System.out.println("Waiting for connection at port "+ MAIN_SERVER_PORT + ".....");
//        ServerSocket serverSocket = new ServerSocket(MAIN_SERVER_PORT);
//        Socket clientSocket = serverSocket.accept();
//
//        System.out.println("Connection successful");
//        System.out.println("Waiting for input.....");
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//
//        while (true) {
//            out.println("Processing");
//            String userChoice = in.readLine();
//
//            if (userChoice == null || userChoice.equalsIgnoreCase("quit")) {
//                break;
//            } else if (userChoice.equalsIgnoreCase("fetch")) {
//                out.println("Enter product ID to fetch:");
//                String fetchInput = in.readLine();
//                int id = Integer.parseInt(fetchInput);
//
//                stmt.setInt(1, id);
//                ResultSet res = stmt.executeQuery();
//
//                if (!res.next()) {
//                    out.println(-1); // No product with that id
//                } else {
//                    out.print("Name: " + res.getString(2)); // Product name
//                    out.print(" Price: " + res.getDouble(3)); // Price
//                    out.print(" Qty: " + res.getInt(4)); // Quantity
//                    out.println(); // Newline
//                }
//            } else if (userChoice.equalsIgnoreCase("update")) {
//                out.println("Enter product ID to update:");
//                String updateInput = in.readLine();
//                int id = Integer.parseInt(updateInput);
//
//
//                out.println("Enter updated product information (name, price, quantity):");
//                String updateData = in.readLine();
//
//                // Parse and validate the update data
//                String[] updateValues = updateData.split(",");
//                if (updateValues.length == 3) {
//                    String newName = updateValues[0].trim();
//                    double newPrice = Double.parseDouble(updateValues[1].trim());
//                    int newQuantity = Integer.parseInt(updateValues[2].trim());
//
//                    // Update the product in the database
//                    updateStatement.setString(1, newName);
//                    updateStatement.setDouble(2, newPrice);
//                    updateStatement.setInt(3, newQuantity);
//                    updateStatement.setInt(4, id);
//
//                    int rowsUpdated = updateStatement.executeUpdate();
//
//                    if (rowsUpdated > 0) {
//                        out.println("Product updated successfully.");
//                    } else {
//                        out.println("Failed to update product.");
//                    }
//                } else {
//                    out.println("Invalid input format. Please provide name, price, and quantity separated by commas.");
//                }
//            } else {
//                out.println("Invalid choice. Enter 'fetch' to fetch data or 'update' to update data, or 'quit' to exit.");
//            }
//        }
//        out.close();
//        in.close();
//        clientSocket.close();
//        serverSocket.close();
//    }
//}
