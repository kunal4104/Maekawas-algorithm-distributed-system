import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        //list to add all the clients thread
        ArrayList<ServerThread> threadList = new ArrayList<>();



        try (ServerSocket serversocket = new ServerSocket(8000)){
            StateObj serverState = new StateObj(serversocket);
            while(true) {
                Socket socket = serversocket.accept();
                ServerThread serverThread = new ServerThread(socket, threadList, serverState);
                threadList.add(serverThread); 
                //starting the thread
                serverThread.start();

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
        this.timeStamp = timestamp;
        this.clientNum = clientNum;
        this.socket = socket;
    }
}

class StateObj {

    static volatile PriorityQueue<QueueObject> pQueue = new PriorityQueue<QueueObject>(10, new Comparator<QueueObject>() {
        public int compare(QueueObject n1, QueueObject n2) {
            return Long.compare(n1.timeStamp, n2.timeStamp);
        }
    });
    static volatile boolean isLocked = false;
    static volatile ServerSocket serverSocket;
    static volatile QueueObject topClient = null;


    public StateObj(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
}