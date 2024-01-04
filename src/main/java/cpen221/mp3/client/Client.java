package cpen221.mp3.client;

import cpen221.mp3.entity.Entity;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.handler.MessageHandler;
import cpen221.mp3.entity.*;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.server.*;


import java.io.*;
import java.util.*;
import java.net.*;

public class Client {

    /** Private fields in this class **/
    private final int clientId;
    private String email;
    private String serverIP;
    private int serverPort;
    private List<Entity> registeredEntityList;
    private static ServerSocket listenSocket;

    public static String clientServerAddress;
    private static List<Client> allClients = new ArrayList<>();



    /**
     * Constructor for Client class
     *
     * @param clientId ID of this client
     * @param email email address of this client
     * @param serverIP IP address of server
     * @param serverPort ports of server
     * **/
    public Client(int clientId, String email, String serverIP, int serverPort) {
        this.clientId = clientId;
        this.email = email;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.registeredEntityList = new ArrayList<>();
        if (allClients.contains(this))
            try {
                throw new Exception();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        else
            allClients.add(this);
    }

    /**
     * Gets ID of this client
     * @return ID of this client
     * **/
    public int getClientId() {
        return clientId;
    }

    /**
     * Gets server IP where events are sent to
     * @return server IP
     * **/
    private String getServerIP() {return this.serverIP; }


    /**
     * Gets server port where events are sent to
     * @return server port
     * **/
    private int getServerPort() {return this.serverPort; }

    /**
     * Registers an entity for the client
     * @return true if the entity is new and gets successfully registered, false if the Entity
     * is already registered
     */
    public boolean addEntity(Entity entity) {
        if (entity.getClientId() != -1)    // Entity has already been registered (for this or other client)
            return false;
        this.registeredEntityList.add(entity);      // Add to registered entity
        entity.registerForClient(this.clientId);    // Register the entity for this client
        return true;
    }

    /**
     * Sends request to the server specified for this client. If not defined then do nothing.
     * Then waits for the response from the server.
     *
     * @param request the request to send to the server
     **/
    public void sendRequest(Request request) {
        // implement this method
        // note that Event is a complex object that you need to serialize before sending
        int ID = this.clientId;
        if (this.serverPort > 0 && this.serverIP != null) { // if endpoint exists
            Thread requestSender = new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket socket = null;
                    PrintWriter printWriterOut = null;
                    try {
                        // Create socket to connect
                        socket = new Socket(getServerIP(), getServerPort());
                        printWriterOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                        String requestString = request.toString() +","+ ID;
                        printWriterOut.println(requestString);
                        printWriterOut.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (printWriterOut != null)
                            printWriterOut.close();
                    }
                }
            });

            requestSender.start();

            startListening();

        }
    }

    private void displayResponse (String response)  {
        System.out.println("Response from server: "+ response.split("//")[1]);
    }



    public static void startListening () {
        try {
            if (listenSocket == null) {
                listenSocket = new ServerSocket(1234);
                clientServerAddress = listenSocket.getInetAddress().getHostAddress();
            }


            // Thread for handling commands
            Thread commandHandler = new Thread(new Runnable() {

                @Override
                public void run() {
                    Socket responseSocket = null;
                    try {
                        responseSocket = listenSocket.accept();
                        // Establish buffer to write into
                        BufferedReader bufferIn = new BufferedReader(new InputStreamReader(
                                responseSocket.getInputStream()));
                        // Establish print writer to get
                        String response = bufferIn.readLine();
                        int clientID = Integer.valueOf(response.split("//")[0]);
                        for (Client client : allClients)
                            if (client.getClientId() == clientID) {
                                client.displayResponse(response);
                                break;
                            }
                        bufferIn.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            responseSocket.close();
                            listenSocket.close();
                            listenSocket = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            // Start thread and start listening
            commandHandler.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        Client client1 = new Client(1, "blah", "0.0.0.0", 8080);
        //Client client2 = new Client(2, "blah", "0.0.0.0", 8080);
        Filter f = new Filter("timestamp", DoubleOperator.GREATER_THAN_OR_EQUALS, 1000.0);
        Actuator ac2 = new Actuator(2, 1, "Switch", false, "0.0.0.0", 8080);
        //Actuator ac1 = new Actuator(1, 1, "Switch", true, "0.0.0.0", 8080);

        String[] a = f.toString().split("&");
        //System.out.println(a[1]);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Entity actuator1 = new Actuator(1, "Switch", true);
        Request newRequest = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, client1.getRequestData(f, ac2));
        //Request newRequest = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, client1.getRequestData(4));

        client1.sendRequest(newRequest);
        //Sensor sen1 = new Sensor(1,"TempSensor", "0.0.0.0", 8080);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(ac2.getState());
    }


    public String getRequestData(double newMaxWaitTime) {return newMaxWaitTime+"";}
    public String getRequestData(Filter filter, Actuator actuator) {
        return filter.toString()+ "/" + actuator.toString();
    }

    public String getRequestData(Filter filter) {
        return filter.toString();
    }

    public String getRequestData(TimeWindow timeWindow) {return timeWindow.toString();}

    public String getRequestData(int entityID, int n) {return entityID+ ","+n;}

    public boolean equals (Client client1) {
        if (this.clientId == client1.clientId)
            return true;
        else
            return false;
    }


}
