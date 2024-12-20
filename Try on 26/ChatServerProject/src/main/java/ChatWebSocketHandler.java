import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@WebSocket
public class ChatWebSocketHandler {
    private static final Map<Session, String> sessionUserMap = Collections.synchronizedMap(new HashMap<>());
    private static final String UPLOAD_DIR = "uploads/";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    static {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessionUserMap.put(session, null); // Assign a null username initially
        System.out.println("Client connected: " + session.getRemoteAddress());

        // Load the chat history from the database
        try {
            // Use ChatDatabase to load messages in JSON format
            String chatHistoryJson = ChatDatabase.loadMessagesAsJson(); // Convert messages to JSON
            session.getRemote().sendString(chatHistoryJson); // Send the JSON to the client
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String username = sessionUserMap.remove(session);
        if (username != null) {
            broadcast("Server: " + username + " has left the chat.");
        }
        System.out.println("Client disconnected: " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        String username = sessionUserMap.get(session);
        if (username == null) {
            // First message is treated as the username
            sessionUserMap.put(session, message);
            broadcast("Server: " + message + " has joined the chat.");
        } else {
            // Save message to database
            ChatDatabase.saveMessage(username, message);

            // Broadcast to other users
            broadcast(username + ": " + message);
        }
    }


    @OnWebSocketMessage
    public void onMessage(Session session, byte[] payload, int offset, int length) {
        try {
            System.out.println("Received file size: " + length + " bytes");

            if (length > MAX_FILE_SIZE) {
                session.getRemote().sendString("Server: File size exceeds the 10 MB limit.");
                return;
            }

            // Log file signature for debugging
            System.out.print("File signature: ");
            for (int i = 0; i < Math.min(12, payload.length); i++) {
                System.out.printf("%02X ", payload[i]);
            }
            System.out.println();

            String username = sessionUserMap.get(session);
            String fileName;
            File file;

            // Check if the file is a PNG or MP4
            if (isPng(payload)) {
                fileName = "file_" + System.currentTimeMillis() + ".png";
            } else if (isMp4(payload)) {
                fileName = "file_" + System.currentTimeMillis() + ".mp4";
            } else {
                session.getRemote().sendString("Server: Only PNG and MP4 files are allowed.");
                return;
            }

            file = new File(UPLOAD_DIR + fileName);

            // Save the file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(payload, offset, length);
            }

            System.out.println("File received from " + username + " and saved as " + fileName);

            // Embed the file in the chat
            String fileHtml;

            if (fileName.endsWith(".png")) {
                String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
                fileHtml = "<img src='data:image/png;base64," + base64Image + "' style='max-width: 200px;' />";
            } else {
                String base64Video = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
                fileHtml = "<video controls style='max-width: 200px;'><source src='data:video/mp4;base64," + base64Video + "' type='video/mp4'></video>";
            }

            // Broadcast the embedded file
            broadcast(username + " shared a file:<br>" + fileHtml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPng(byte[] data) {
        // PNG files start with an 8-byte signature: 89 50 4E 47 0D 0A 1A 0A
        if (data.length < 8) return false;
        return data[0] == (byte) 0x89 &&
                data[1] == (byte) 0x50 &&
                data[2] == (byte) 0x4E &&
                data[3] == (byte) 0x47 &&
                data[4] == (byte) 0x0D &&
                data[5] == (byte) 0x0A &&
                data[6] == (byte) 0x1A &&
                data[7] == (byte) 0x0A;
    }

    private boolean isMp4(byte[] data) {
        // MP4 files start with a 'ftyp' box, which begins at byte 4
        if (data.length < 12) return false;
        for (int i = 0; i < data.length - 8; i++) {
            if (data[i + 4] == (byte) 0x66 && // 'f'
                    data[i + 5] == (byte) 0x74 && // 't'
                    data[i + 6] == (byte) 0x79 && // 'y'
                    data[i + 7] == (byte) 0x70) { // 'p'
                return true;
            }
        }
        return false;
    }

    private void broadcast(String message) {
        synchronized (sessionUserMap) {
            for (Session session : sessionUserMap.keySet()) {
                try {
                    session.getRemote().sendString(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
