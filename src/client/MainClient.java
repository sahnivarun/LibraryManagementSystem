package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static util.PortAddresses.MAIN_SERVER_PORT;

public class MainClient {
    public static void main(String[] args) throws Exception {

        String serverHostname = new String("127.0.0.1");
        int portNumber = MAIN_SERVER_PORT;

        System.out.println("Attempting to connect to host " + serverHostname + " on port " + portNumber);

        try {

            Socket socket = new Socket(serverHostname, portNumber);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            while (true) {

                System.out.println("Choose an option:");
                System.out.println("1. Fetch product data");
                System.out.println("2. Update product data");
                System.out.println("3. Quit");

                System.out.print("Enter your choice (1/2/3): ");

                String userChoice = stdIn.readLine();

                if (userChoice.equals("1")) {
                    out.println("fetch");
                } else if (userChoice.equals("2")) {
                    out.println("update");
                } else if (userChoice.equals("3")) {
                    out.println("quit");
                    break; // Exit the loop if the user chooses to quit1
                } else {
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                    continue;
                }

                // Handle server responses based on the user's choice
                String serverAnswer = in.readLine();
                System.out.println("Server says: " + serverAnswer);

                while (true) {
                    String serverPrompt = in.readLine();
                    if (serverPrompt == null || serverPrompt.isEmpty()) {
                        break;
                    }
                    System.out.println("Server Says: " + serverPrompt);
                    String userInput = stdIn.readLine();
                    out.println(userInput);
                }

            }
            out.close();
            in.close();
            stdIn.close();
            socket.close();
        }catch (Exception e){
            System.out.println(e.toString());
        }


    }
}
