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
            while(serverState.count < 4) {
                Socket socket = serversocket.accept();
                ServerThread serverThread = new ServerThread(socket, threadList, serverState);
                //starting the thread
                threadList.add(serverThread); 
                serverThread.start();

            }

            for (int i = 0; i < threadList.size(); i++) {
                threadList.get(i).join();
            }
            serverState.done = true;

            Node[] servTree = { new Node(1, "10.176.69.32", 8000), new Node(2, "10.176.69.33", 8000), new Node(3, "10.176.69.34", 8000), new Node(4, "10.176.69.35", 8000), new Node(5, "10.176.69.36", 8000), new Node(6, "10.176.69.37", 8000), new Node(7, "10.176.69.38", 8000)}; 

            ArrayList<SendMessageComplete> servConn = new ArrayList<SendMessageComplete>(); 

            for (int i = 0; i < servTree.length; i++) {
                servConn.add(new SendMessageComplete(servTree[i], serverState));
            }                

            for (int i = 0; i < servConn.size(); i++) {
                servConn.get(i).start();
            }

            for (int i = 0; i < servConn.size(); i++) {
                servConn.get(i).join();
            }






        } catch (Exception e) {
            System.out.println("Error occured in main: " + e);
        }
    }
}

class StateObj {

    volatile static int count;
    volatile static boolean done;


    public StateObj() {
        this.count = 0;
        this.done = false;
    }
}

class Node {

    public int serverNum;
    public String ip;
    public int port;
    public boolean grant;

    Node(int serverNum, String ip, int port) {
        this.serverNum = serverNum;
        this.ip = ip;
        this. port = port;
        this.grant = false;
    }

}

