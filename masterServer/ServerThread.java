
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

    // public boolean removeItem(int clientNum) {
    //     // QueueObject head = pQueue.poll();
    //     if (serverState.topClient != null)
    //        System.out.println("head "+serverState.topClient.clientNum);
    //     boolean isHead = false;
    //     if (serverState.topClient != null && serverState.topClient.clientNum == clientNum) {
    //         isHead = true;
    //     }

    //     for (QueueObject obj : serverState.pQueue) {
    //         if (obj.clientNum == clientNum) {
    //             serverState.pQueue.remove(obj);
    //             break;
    //         }
    //     }

    //     return isHead;
    // }

    // public void sendGrant(Socket socket) {
    //     try {
    //         PrintWriter outputGrant = new PrintWriter(socket.getOutputStream(),true);
    //         outputGrant.println("GRANT");
    //         // outputGrant.close();
    //     } catch (Exception e) {
    //         System.out.println("Error occured send grant: " +e);
    //     }
    // }

    @Override
    public void run() {
        try {
            //Reading the input from Client
            BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            //returning the output to the client : true statement is to flush the buffer otherwise
            //we have to do it manuallyy
            // output = new PrintWriter(socket.getOutputStream(),true);
            String outputStr = input.readLine();
            System.out.println(outputStr);
            serverState.count++;

        } catch (Exception e) {
            System.out.println("Error occured run: " +e);
        }
    }
}

