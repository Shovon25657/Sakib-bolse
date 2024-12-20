import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatServer {
    private static final int PORT = 12345;
    static Set<PrintWriter> clientWriters = new CopyOnWriteArraySet<>();
    static List<String> chatHistory = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("Chat server starting...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                try {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    clientWriters.add(writer);
                    new ClientHandler(socket, writer).start();
                } catch (IOException e) {
                    System.out.println("Error setting up client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void broadcast(String message) {
        synchronized (clientWriters) {
            for (Iterator<PrintWriter> it = clientWriters.iterator(); it.hasNext(); ) {
                PrintWriter writer = it.next();
                try {
                    writer.println(message);
                } catch (Exception e) {
                    System.out.println("Error sending message. Removing client.");
                    it.remove();
                }
            }
        }
        addMessageToHistory(message);
        saveMessageToDatabase(message); // Save message to DB
    }

    static void saveMessageToDatabase(String message) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO messages (username, message) VALUES (?, ?)";
            try (var stmt = conn.prepareStatement(query)) {
                String username = message.split(":")[0]; // Assuming message format: "username: message"
                stmt.setString(1, username);
                stmt.setString(2, message);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    static void addMessageToHistory(String message) {
        if (chatHistory.size() > 50) {
            chatHistory.remove(0);
        }
        chatHistory.add(message);
    }

    static void sendChatHistory(PrintWriter writer) {
        synchronized (chatHistory) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Fetch the oldest messages first
                String query = "SELECT username, message, timestamp FROM messages ORDER BY timestamp ASC LIMIT 10";
                try (var stmt = conn.prepareStatement(query); var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String username = rs.getString("username");
                        String message = rs.getString("message");
                        String timestamp = rs.getString("timestamp");
                        writer.println(username + " [" + timestamp + "]: " + message);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


}
