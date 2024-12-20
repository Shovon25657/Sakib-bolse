import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the chat server");

            // Get the username
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            output.println(username + " has joined the chat!");

            // Create a thread to listen for messages
            Thread listenerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = input.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            listenerThread.start();

            // Send messages to the server
            String userMessage;
            while (true) {
                System.out.print("You: ");
                userMessage = scanner.nextLine();
                if (userMessage.equalsIgnoreCase("exit")) {
                    output.println(username + " has left the chat.");
                    break;
                }
                output.println(username + ": " + userMessage);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
