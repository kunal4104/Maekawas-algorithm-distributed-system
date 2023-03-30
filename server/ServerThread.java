
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

    public boolean removeItem(int clientNum) {
        // QueueObject head = pQueue.poll();
        if (serverState.topClient != null)
           System.out.println("head "+serverState.topClient.clientNum);
        boolean isHead = false;
        if (serverState.topClient != null && serverState.topClient.clientNum == clientNum) {
            isHead = true;
        }

        for (QueueObject obj : serverState.pQueue) {
            System.out.println(obj.clientNum+ " "+obj.timeStamp);
            if (obj.clientNum == clientNum) {
                serverState.pQueue.remove(obj);
            }
        }

        return isHead;
    }

    public boolean sendGrant(Socket socket) {
        try {
            PrintWriter outputGrant = new PrintWriter(socket.getOutputStream(),true);
            outputGrant.println("GRANT");
            // outputGrant.close();
        } catch (Exception e) {
            System.out.println("Error occured send grant: " +e);
        }
        return true;
    }

    @Override
    public void run() {
        try {
            //Reading the input from Client
            BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            //returning the output to the client : true statement is to flush the buffer otherwise
            //we have to do it manuallyy
            output = new PrintWriter(socket.getOutputStream(),true);
            // boolean grantSent = false;


            //inifite loop for server
            // while(!grantSent) {
            String outputString = input.readLine();
            //if user types exit command
            // if(outputString.equals("exit")) {
            //     break;
            // }

            String[] outputArr = outputString.split(" ");

            // printToALlClients(outputString);
            // output.println("server sends " + outputString);
            // output.println("Server says " + outputString);
            System.out.println("Server received " + outputString);

            if (outputArr[0].equals("RELEASE")) {
                //remove client from pq
                //if client at top of the pq
                //send GRANT to next client
                System.out.println("got release from "+ outputArr[2]);

                boolean isTop = removeItem(Integer.parseInt(outputArr[2]));

                if (isTop) {
                    serverState.topClient = serverState.pQueue.poll();
                    System.out.println("server was locked by "+outputArr[2]);
                    if (serverState.topClient != null) {
                        System.out.println("sending grant to "+serverState.topClient.clientNum);
                        sendGrant(serverState.topClient.socket);
                    } else {
                        serverState.isLocked = false;
                        System.out.println("server is unlocked!");
                    }
                } 
            }
            else {
                if (serverState.isLocked) {
                    //add to pq
                    System.out.println("server is locked by" + serverState.topClient.clientNum+" adding request to queue!");
                    serverState.pQueue.add(new QueueObject(Long.parseLong(outputArr[1]), Integer.parseInt(outputArr[2]), socket));
                    System.out.println("queue size " + serverState.pQueue.size());
                } else {
                    //add to pq
                    //locked is true
                    //send GRANT to the client 
                    System.out.println("server was not locked, empty queue size " + serverState.pQueue.size());
                    serverState.pQueue.add(new QueueObject(Long.parseLong(outputArr[1]), Integer.parseInt(outputArr[2]), socket));
                    serverState.isLocked = true;
                    System.out.println("locking server for client "+ outputArr[2] + ", queue size " + serverState.pQueue.size());
                    serverState.topClient = serverState.pQueue.poll();
                    System.out.println("set head "+ serverState.topClient.clientNum);
                    sendGrant(serverState.topClient.socket);
                }
            }

                // }
        }
        catch (Exception e) {
            System.out.println("Error occured run: " +e);
        }
    }
}
// }

// class QueueObject {
//     public long timeStamp;
//     public int clientNum;
//     public Socket socket;
//     public QueueObject (long timestamp, int clientNum, Socket socket) {
//         this.timeStamp = timeStamp;
//         this.clientNum = clientNum;
//         this.socket = socket;
//     }
// }
