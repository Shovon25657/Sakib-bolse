import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebSocketServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // WebSocket chat servlet
        ServletHolder wsHolder = new ServletHolder("ws-chat", ChatWebSocketServlet.class);
        context.addServlet(wsHolder, "/chat");

        // File serving servlet
        ServletHolder fileHolder = new ServletHolder("files", DefaultServlet.class);
        fileHolder.setInitParameter("resourceBase", System.getProperty("user.dir") + "/uploads"); // Absolute path
        fileHolder.setInitParameter("dirAllowed", "true"); // Allow directory browsing for debug purposes
        context.addServlet(fileHolder, "/download/*");

        server.start();
        System.out.println("Server started at http://localhost:8080");
        server.join();
    }
}
