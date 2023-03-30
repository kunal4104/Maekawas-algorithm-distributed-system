
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.*;
import java.sql.Timestamp;


public class SendMessageComplete extends Thread {

    private StateObj client;
    private Node server;


    public SendMessageComplete(Node server, StateObj client) {
        this.server = server;
        this.client = client;
    }

    private void startClient() {
        
        try (Socket socket = new Socket(server.ip, server.port)) {
            
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);

            String message = "COMPLETE";

            output.println(message);

            output.close();
            socket.close();
            
        } catch (Exception e) {
            System.out.println("Exception occured in client main: " + e);
        }
    }

    @Override
    public void run() {

        startClient();
        super.run();
    }

}