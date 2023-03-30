import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class Main {

    // public static String clientNum;

    private static boolean checkQuorum(int index, StateObj clientState) {
        // System.out.println("idx "+index);
        if (index*2 > clientState.servTree.length || index*2+1 > clientState.servTree.length)
                return clientState.servTree[index - 1].grant;

        if (clientState.servTree[index - 1].grant){
            return checkQuorum(index*2, clientState) || checkQuorum(index*2+1, clientState);

        } else {
            return checkQuorum(index*2, clientState) && checkQuorum(index*2+1, clientState);
        }
    }

    private static int executeCriticalSec(StateObj clientState, long diffLat, int msgEx) {
        try {

            Calendar calendar = Calendar.getInstance();
            long timeMilli = calendar.getTimeInMillis();
            String timeStmp = String.valueOf(timeMilli);

            System.out.println("Entering: "+clientState.clientNum+" timestamp:"+timeStmp + " exec num:"+clientState.criticalExecs +" latency: "+diffLat+" message exchanges: "+ msgEx);
            Thread.sleep(5000);
            System.out.println("Exiting Critical section!");
        } catch (Exception e) {
            System.out.println("Exception occured in client main: " + e);
        }

        return clientState.criticalExecs + 1;
    }

    public static void main(String[] args) {

        StateObj clientState = new StateObj(Integer.parseInt(args[0]));
        Random r = new Random();
        
        try {

            while (clientState.criticalExecs < 5) {

                ArrayList<SendMessageRG> servConn = new ArrayList<SendMessageRG>(); 
                int currRequest = clientState.totalRequest;
                int currRelease = clientState.totalRelease;
                int currGrant = clientState.totalGrant;

                int randWait= (r.nextInt(5) + 5)*1000;
                System.out.println("wait for "+ randWait/1000);
                Thread.currentThread().sleep(randWait);

                Calendar calendar = Calendar.getInstance();
                long timeMilliReq = calendar.getTimeInMillis();



                for (int i = 0; i < clientState.servTree.length; i++) {
                    servConn.add(new SendMessageRG(clientState.servTree[i], clientState));
                    clientState.totalRequest++;
                }

                for (int i = 0; i < servConn.size(); i++) {
                    servConn.get(i).start();
                }

                boolean quorumAvail = false;

                do {
                    quorumAvail = checkQuorum(1, clientState);

                    if (quorumAvail) {
                        if (!clientState.inCriticalSec) {
                            Calendar calendarEnter = Calendar.getInstance();
                            long timeMilliEnter = calendarEnter.getTimeInMillis();

                            clientState.inCriticalSec = true;
                            clientState.criticalExecs = executeCriticalSec(clientState, timeMilliEnter - timeMilliReq, (clientState.totalRequest - currRequest) + (clientState.totalGrant - currGrant));
                        }
                    }
                } while (!quorumAvail && clientState.criticalExecs < 20 && !clientState.inCriticalSec);

                ArrayList<SendMessageRelease> servConnRel = new ArrayList<SendMessageRelease>(); 

                for (int i = 0; i < clientState.servTree.length; i++) {
                    servConnRel.add(new SendMessageRelease(clientState.servTree[i], clientState));
                }

                for (int i = 0; i < servConnRel.size(); i++) {
                    servConnRel.get(i).start();
                }

                for (int i = 0; i < servConnRel.size(); i++) {
                    try {
                        servConnRel.get(i).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < clientState.servTree.length; i++) {
                    clientState.servTree[i].grant = false;
                }

                clientState.inCriticalSec = false;

            }


            SendMessageComplete sc = new SendMessageComplete(new Node(0, "10.176.69.46", 8000), clientState); 
            sc.start();
            sc.join();

        } catch (Exception e) {
                System.out.println("Exception occured in client main: " + e);
        }


    }
}

class SendMessageComplete extends Thread {

    private StateObj client;
    private Node server;


    public SendMessageComplete(Node server, StateObj client) {
        this.server = server;
        this.client = client;
    }

    private void startClient() {
        
        try (Socket socket = new Socket(server.ip, server.port)) {
            
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);

            Calendar calendar = Calendar.getInstance();
            long timeMilli = calendar.getTimeInMillis();
            String timeStmp = String.valueOf(timeMilli);

            String message = "Complete"+ " " + timeStmp + " client number: " + client.clientNum + " total requests: "+String.valueOf(client.totalRequest)+" total grants: "+String.valueOf(client.totalGrant) + " total release: "+ String.valueOf(client.totalRelease);

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


class SendMessageRelease extends Thread {

    private StateObj client;
    private Node server;


    public SendMessageRelease(Node server, StateObj client) {
        this.server = server;
        this.client = client;
    }

    private void startClient() {
        
        try (Socket socket = new Socket(server.ip, server.port)) {
            
            BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);

            Calendar calendar = Calendar.getInstance();
            long timeMilli = calendar.getTimeInMillis();
            String timeStmp = String.valueOf(timeMilli);

            String message = "RELEASE"+ " " + timeStmp + " " + client.clientNum;

            output.println(message);
            client.totalRelease++;

            server.grant = false;

            input.close();
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

class SendMessageRG extends Thread {

    private Node server;
    // private Socket socketObj;
    private StateObj client;


    // public ServerConnect(String serverIP, int serverPort, Node serverDetail) {
    // public SendMessageRG(Socket socketObj, Node server, StateObj client) {
    public SendMessageRG(Node server, StateObj client) {
        this.server = server;
        // this.socketObj = socketObj;
        this.client = client;
    }

    private void startClient() {
        
        try (Socket socket = new Socket(server.ip, server.port)){
            BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);

            Calendar calendar = Calendar.getInstance();
            long timeMilli = calendar.getTimeInMillis();
            String timeStmp = String.valueOf(timeMilli);
            boolean reqSent = false;

       

            String message = "REQUEST" + " " + timeStmp + " " + client.clientNum;

            String inputStr;
            
            do {
                
                if (!reqSent && !client.inCriticalSec && client.criticalExecs < 20) {
                    output.println(message);
                    reqSent = true;
                }
                inputStr = input.readLine();
                if (!client.inCriticalSec)
                    System.out.println(inputStr + " from server " + server.serverNum);

            } while (!inputStr.equals("GRANT") && !client.inCriticalSec && client.criticalExecs < 20);

            if (!client.inCriticalSec) {
                server.grant = true;
                client.totalGrant++;
            }

            input.close();
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

class StateObj {

    static volatile boolean inCriticalSec;
    static volatile int clientNum;
    static volatile int criticalExecs; 
    static volatile int totalRequest;
    static volatile int totalGrant;
    static volatile int totalRelease;

    // volatile static Node[] servTree = { new Node(1, "localhost", 8000), new Node(2, "localhost", 8001), new Node(3, "localhost", 8002), new Node(4, "localhost", 8003), new Node(5, "localhost", 8004), new Node(6, "localhost", 8005), new Node(7, "localhost", 8006)}; 
    static volatile Node[] servTree = { new Node(1, "10.176.69.32", 8000), new Node(2, "10.176.69.33", 8000), new Node(3, "10.176.69.34", 8000), new Node(4, "10.176.69.35", 8000), new Node(5, "10.176.69.36", 8000), new Node(6, "10.176.69.37", 8000), new Node(7, "10.176.69.38", 8000)}; 
    // volatile static Node[] servTree = { new Node(1, "localhost", 8000), new Node(2, "localhost", 8001), new Node(3, "localhost", 8002)}; 
    // volatile static Node[] servTree = { new Node(1, "localhost", 8000)}; 

    public StateObj(int clientNum) {
        this.clientNum = clientNum;
        this.inCriticalSec = false; 
        this.criticalExecs = 0;
        this.totalRequest = 0;
        this.totalGrant = 0;
        this.totalRelease = 0;
    }
}
