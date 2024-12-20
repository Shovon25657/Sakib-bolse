import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatDatabase {
    public static void saveMessage(String username, String message) {
        String query = "INSERT INTO messages (username, message) VALUES (?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public static String loadMessagesAsJson() {
        JSONArray messagesArray = new JSONArray();
        String query = "SELECT username, message, timestamp FROM messages ORDER BY timestamp ASC"; // Fetch in ascending order

        try (Connection conn = DBConfig.getConnection();
             var stmt = conn.prepareStatement(query);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                JSONObject messageObject = new JSONObject();
                messageObject.put("username", rs.getString("username"));
                messageObject.put("text", rs.getString("message"));
                messageObject.put("timestamp", rs.getString("timestamp"));

                messagesArray.put(messageObject); // Add each message to the JSON array
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messagesArray.toString(); // Convert the JSON array to a string
    }




    public static String loadMessages() {
        StringBuilder chatHistory = new StringBuilder();
        String query = "SELECT username, message, timestamp FROM messages ORDER BY timestamp ASC"; // Fetch in ascending order

        try (Connection conn = DBConfig.getConnection();
             var stmt = conn.prepareStatement(query);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String message = rs.getString("message");
                String timestamp = rs.getString("timestamp");

                // Format message
                chatHistory.append("[").append(timestamp).append("] ")
                        .append(username).append(": ")
                        .append(message).append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chatHistory.toString(); // Return the formatted chat history
    }


}
