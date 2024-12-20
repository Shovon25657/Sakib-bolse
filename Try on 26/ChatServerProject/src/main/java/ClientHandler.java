import java.io.*;
import java.net.*;

class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter output;

    public ClientHandler(Socket socket, PrintWriter writer) {
        this.socket = socket;
        this.output = writer;
    }

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            ChatServer.sendChatHistory(output); // Send chat history to the new client
            while ((message = input.readLine()) != null) {
                System.out.println("Received: " + message);
                ChatServer.broadcast(message);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        } finally {
            ChatServer.clientWriters.remove(output);
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket.");
            }
        }
    }
}
