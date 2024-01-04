package cpen221.mp3.handler;

import cpen221.mp3.client.Client;
import cpen221.mp3.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;
import java.util.HashMap;


public class MessageHandler {
    private static ServerSocket serverSocket;
    private int port;
    final private int queueCapacity = 2000;

    // you may need to add additional private fields and methods to this class

    public MessageHandler(int port) {
        this.port = port;
        start();
    }



    public void start() {
        // the following is just to get you started
        // you may need to change it to fit your implementation
        PriorityBlockingQueue<String> messageQueue = new PriorityBlockingQueue<>(queueCapacity, new CustomComparator());
        HashMap<Integer, Server> serverMap = new HashMap<>();

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server: " + serverSocket.getInetAddress().getHostAddress());
            System.out.println("Server started on port " + port);
            Thread getImcomingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        Socket incomingSocket = null;
                        BufferedReader bufferedReader = null;
                        String incomingMessage = null;
                        int clientID = 0;
                        try {
                            incomingSocket = serverSocket.accept();
                            System.out.println("Client/Entity connected: " + incomingSocket.getInetAddress().getHostAddress());
                            bufferedReader = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));
                            incomingMessage = bufferedReader.readLine();
                            System.out.println("Message: " + incomingMessage);
                            clientID = getClientID(incomingMessage);
                            // Add a server for the Client if not already available
                            if (!serverMap.containsKey(clientID)){
                                serverMap.put(clientID, new Server(new Client(clientID, "mock-email", // Email should not matter because compare Client based on ID
                                        serverSocket.getInetAddress().getHostAddress(),8080)));
                            }

                            // Insert message to the queue, it is sorted in real time
                            messageQueue.add(incomingMessage);
                            incomingSocket.close();
                            bufferedReader.close();
                        } catch (IOException e) {throw new RuntimeException(e);}

                        // Wait a maxWaitTime number seconds
                        try {
                            Thread.sleep((long)Server.maxWaitTime*1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        // Flushed out of the queue when it starts being processed
                        messageQueue.remove(incomingMessage);

                        // Create a new thread to handle the client request or entity event
                        Thread handlerThread = new Thread(new MessageHandlerThread(incomingMessage, serverMap.get(clientID)));
                        // Start Thread
                        handlerThread.start();
                    }
                }
            });
            // Start Thread
            getImcomingThread.start();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    /**
     * Gets the ID of the Client that sent that Request or registered with the Entity
     * that has sent the Event
     *
     * @param message incoming message received
     * @return the ID of the Client
     * **/
    private int getClientID(String message) {
        String[] split = message.split(",");
        if (split[0].equals("Request")) {
            return Integer.valueOf(split[split.length-1]);
        } else {
            return Integer.valueOf(split[2]);
        }
    }

    public static void main(String[] args) {
        // you would need to initialize the RequestHandler with the port number
        // and then start it here
        MessageHandler messageHandler = new MessageHandler(8080);


/*        try {
            ServerSocket s0 = new ServerSocket( 8080);
            ServerSocket s1 = new ServerSocket( 8081);
            System.out.println(s0.getInetAddress().getHostAddress());
            //System.out.println(s1.getInetAddress().getHostAddress());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        Socket incomingSocket = null;
        // Define 8080 to be the port where all Request and Event are sent to
        /*while (true) {
            try {
                incomingSocket = messageHandler.serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }*/

    }

    /**
     * Returns the timestamp of the Request / Entity Event **/
    private double returnTimeStampFromString (String message) {
        String[] elements = message.split(",");
        return Double.valueOf(elements[1]);
    }


    /**
     * Customized Comparator class to sort the command strings based on timestamp**/
    class CustomComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            double num1 = -returnTimeStampFromString(str1);
            double num2 = -returnTimeStampFromString(str2);
            return Double.compare(num1,num2);
        }
    }

    public void closeMessageHandler() {
        try {
            // Stop processing, used for testing mainly
            serverSocket.close();
            serverSocket = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
