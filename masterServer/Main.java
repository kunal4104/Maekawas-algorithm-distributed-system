import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        //using serversocket as argument to automatically close the socket
        //the port number is unique for each server

        //list to add all the clients thread
        ArrayList<ServerThread> threadList = new ArrayList<>();


        StateObj serverState = new StateObj();

        try (ServerSocket serversocket = new ServerSocket(8000)){
            while(serverState.count < 5) {
                Socket socket = serversocket.accept();
                ServerThread serverThread = new ServerThread(socket, threadList, serverState);
                //starting the thread
                threadList.add(serverThread); 
                serverThread.start();
                System.out.println(serverState.count);

                //get all the list of currently running thread

            }
        } catch (Exception e) {
            System.out.println("Error occured in main: " + e);
        }
    }
}

class QueueObject {
    public long timeStamp;
    public int clientNum;
    public Socket socket;
    public QueueObject (long timestamp, int clientNum, Socket socket) {
        this.timeStamp = timeStamp;
        this.clientNum = clientNum;
        this.socket = socket;
    }
}

class StateObj {

    volatile static int count;


    public StateObj() {
        this.count = 0;
    }
}