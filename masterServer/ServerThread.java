
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.*;
import java.sql.Timestamp;

public class ServerThread extends Thread {
    private Socket socket;
    private ArrayList<ServerThread> threadList;
    private PrintWriter output;
    private StateObj serverState;    


    public ServerThread(Socket socket, ArrayList<ServerThread> threads, StateObj ServerState) {
        this.socket = socket;
        this.threadList = threads;
        this.serverState = ServerState;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            String outputStr = input.readLine();
            System.out.println(outputStr);
            serverState.count++;

        } catch (Exception e) {
            System.out.println("Error occured run: " +e);
        }
    }
}

