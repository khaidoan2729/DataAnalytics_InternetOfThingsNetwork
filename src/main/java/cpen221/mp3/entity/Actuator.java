package cpen221.mp3.entity;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.server.SeverCommandToActuator;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;

/**
 * Rep Invariant:
 *  - id is unique to this enity
 *
 * **/

public class Actuator implements Entity {

    /** Private fields of this class **/
    private final int id;
    private int clientId;
    private final String type;
    private boolean state;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)
    // the following specifies the http endpoint that the actuator should send events to
    private String serverIP = null;
    final private String[] TYPES = {"TempSensor","PressureSensor", "CO2Sensor", "Switch"};
    private int serverPort = 0;
    // the following specifies the http endpoint that the actuator should be able to receive commands on from server
    private String host = null;
    private int port = 1111;
    final private int maxIncomingCmd = 50;

    // Socket to listen for commands from server
    private static ServerSocket listenSocket;  // to listen for commands from server

    public static List<Actuator> allActuators = new ArrayList<>();
    private static boolean listeningThreadOn = false;

    private final List<ActuatorEvent> receivedEvents = new ArrayList<>(); // used for getLastEventTimeStamp


    /**
     * Constructor for Actuator class
     *
     * @param id ID of the actuator entity
     * @param type type of actuator, should always be "Switch".
     * @param init_state initial state of actuator.
     * **/
    public Actuator(int id, String type, boolean init_state) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;

        if (listenSocket != null)
            this.host = listenSocket.getInetAddress().getHostAddress();

        this.port = 1111;                   // Set 1111 to be port where commands are received
        // TODO: need to establish a server socket to listen for commands from server
        allActuators.add(this);
        if (!listeningThreadOn) {
            listeningThreadOn = true;
            startListening();
        }
        startSendingEvent();
    }

    /**
     * Constructor for Actuator class
     *
     * @param id ID of the actuator entity
     * @param clientId ID of client actuator is registered to
     * @param type type of actuator, should always be "Switch".
     * @param init_state initial state of actuator.
     * **/
    public Actuator(int id, int clientId, String type, boolean init_state) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.port = 1111;                   // Set 1111 to be port where commands are received
        // TODO: need to establish a server socket to listen for commands from server
        allActuators.add(this);
        if (listenSocket != null)
            this.host = listenSocket.getInetAddress().getHostAddress();
        if (!listeningThreadOn) {
            listeningThreadOn = true;
            startListening();
        }
        startSendingEvent();
    }


    /**
     * Constructor for Actuator class
     *
     * @param id ID of the actuator entity
     * @param type type of actuator, should always be "Switch".
     * @param init_state initial state of actuator.
     * @param serverIP IP address of server actuator is connected to
     * @param serverPort port number of said server
     * **/
    public Actuator(int id, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.port = 1111;                   // Set 1111 to be port where commands are received
        if (listenSocket != null)
            this.host = listenSocket.getInetAddress().getHostAddress();
        // TODO: need to establish a server socket to listen for commands from server
        allActuators.add(this);
        if (!listeningThreadOn) {
            listeningThreadOn = true;
            startListening();
        }
        startSendingEvent();
    }


    /**
     * Constructor for Actuator class
     *
     * @param id ID of the actuator entity
     * @param clientId ID of client actuator is registered to
     * @param type type of actuator, should always be "Switch".
     * @param init_state initial state of actuator.
     * @param serverIP IP address of server actuator is connected to
     * @param serverPort port number of said server
     * **/
    public Actuator(int id, int clientId, String type, boolean init_state, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        if (listenSocket != null)
            this.host = listenSocket.getInetAddress().getHostAddress();
        this.serverPort = serverPort;
        this.port = 1111;                   // Set 1111 to be port where commands are received

        // TODO: need to establish a server socket to listen for commands from server
        allActuators.add(this);
        if (!listeningThreadOn) {
            listeningThreadOn = true;
            startListening();
        }
        startSendingEvent();
    }




    public Actuator(int id, int clientId, String type, boolean init_state, String serverIP, int serverPort, boolean isAtServer) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.state = init_state;
        this.serverIP = serverIP;
        if (listenSocket != null)
            this.host = listenSocket.getInetAddress().getHostAddress();
        this.serverPort = serverPort;
        this.port = 1111;                   // Set 1111 to be port where commands are received
    }


    /**
     * Gets ID of actuator
     * @return ID of actuator
     * **/
    public int getId() {
        return id;
    }
    /**
     * Gets ID of the client actuator is registered to
     * @return ID of the client actuator is registered to
     * **/
    public int getClientId() {
        return clientId;
    }
    /**
     * Gets type of actuator
     * @return should always be "Switch".
     * **/
    public String getType() {
        return type;
    }

    /**
     * @return if this actuator is an actuator, should always be true
     * **/
    public boolean isActuator() {
        return true;
    }

    /**
     * Gets state of this actuator
     * @return state (on or off/ true or false) of this actuator
     * **/
    public boolean getState() {
        return state;
    }
    /**
     * Gets IP address of host server where commands are sent from
     * @return IP address of host server of this actuator
     * **/
    public String getIP() {
        if (listenSocket != null)
            this.host = listenSocket.getInetAddress().getHostAddress();
        return host;
    }
    /**
     * Gets port number of this actuator where commands are sent from
     * @return port number of this actuator to host server
     * **/
    public int getPort() {
        return port;
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
     * Updates state of this actuator
     * **/
    public void updateState(boolean new_state) {
        this.state = new_state;
    }

    /**
     * Registers the actuator for the given client
     *
     * @return true if the actuator is new (clientID is -1 already) and gets successfully registered
     * or if it is already registered for clientId, else false
     */
    public boolean registerForClient(int clientId) {
        if (this.clientId == -1) {
            this.clientId = clientId;
            return true;
        } else if (this.clientId == clientId)
            return true;
        else
            return false;
    }

    /**
     * Sets or updates the http endpoint that
     * the actuator should send events to
     *
     * @param serverIP the IP address of the endpoint
     * @param serverPort the port number of the endpoint
     */
    public void setEndpoint(String serverIP, int serverPort){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     */
    public void setEventGenerationFrequency(double frequency){
        this.eventGenerationFrequency = frequency;
    }

    /**
     * Sends event to the server specified in this actuator. If not defined then do nothing
     *
     * @param event the event to send to the server
     **/
    public void sendEvent(Event event) {
        // implement this method
        // note that Event is a complex object that you need to serialize before sending
        if (event.getEntityType().equals(this.TYPES[3]) && this.serverPort > 0 && this.serverIP != null) { // if correct type and endpoint exists
            Thread eventSender = new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket actuatorSocket = null;
                    PrintWriter printWriterOut = null;
                    try {
                        // Create socket to connect
                        actuatorSocket = new Socket(getServerIP(), getServerPort());
                        printWriterOut = new PrintWriter(new OutputStreamWriter(actuatorSocket.getOutputStream()));
                        String requestString = event.toString();
                        printWriterOut.println(requestString);
                        printWriterOut.flush();

                        actuatorSocket.close();
                        printWriterOut.close();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } /*finally {
                        if (actuatorSocket != null) {
                            try {
                                actuatorSocket.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (printWriterOut != null)
                            printWriterOut.close();
                    }*/

                }
            });

            eventSender.start();
        }
    }

    public void processServerMessage(Request command) throws IllegalArgumentException{
        if (command.getSCTAtype() == 0){
            this.state = true;              // set state
        } else if (command.getSCTAtype() == 1) {
            this.state = !this.state;       // toggle state
        }

    }

    @Override
    public String toString() {
        return "Actuator," +
                getId() + ","
                + getClientId() + ","
                + getType() + ","
                + this.state + ","
                + getIP() + ","
                + getPort();
    }

    public static Actuator toActuator(String actuatorStr) {
        String[] actuatorElements = actuatorStr.split(",");
            return new Actuator( Integer.valueOf(actuatorElements[1]),
                                Integer.valueOf(actuatorElements[2]),
                                actuatorElements[3],
                                Boolean.valueOf(actuatorElements[4]),
                                actuatorElements[5],
                                Integer.valueOf(actuatorElements[6]), true);
    }

    // you will most likely need additional helper methods for this class
    /**
     * Initializes threads to listen for commands from server
     *
     * **/
    public static void startListening () {
        try {
            listenSocket = new ServerSocket(1111);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            // Thread for handling commands
            Thread commandHandler = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        Socket commandSocket = null;
                        try {
                            commandSocket = listenSocket.accept();
                            // Establish buffer to write into
                            BufferedReader bufferIn = new BufferedReader(new InputStreamReader(
                                    commandSocket.getInputStream()));
                            // Establish print writer to get
                            String command = bufferIn.readLine();
                            Request cmdRequest = stringToRequestDecoder(command);
                            for (Actuator actuator : allActuators)
                                if (actuator.getId() == getIDFromCommand(command)) {
                                    actuator.processServerMessage(cmdRequest);
                                    break;
                                }
                            bufferIn.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                commandSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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


    /**
     * Decodes a command string into a Request object for the Actuator to process
     *
     * @param cmd the encoded string representing the command
     * **/
    public static Request stringToRequestDecoder(String cmd) {
        String[] elements = cmd.split(",");
        if (elements[2].equals(SeverCommandToActuator.SET_STATE))
            return new Request(SeverCommandToActuator.SET_STATE);
        else
            return new Request(SeverCommandToActuator.TOGGLE_STATE);
    }

    /**
     * Gets the entity ID from the command
     *
     * @param cmd the encoded string representing the command
     * **/
    public static int getIDFromCommand(String cmd) {
        String[] elements = cmd.split(",");
        return Integer.valueOf(elements[3]);
    }


    /**
     * Initializes threads to send events to server
     * **/
    private void startSendingEvent () {
        Thread startSendingEventThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    double waitTime = 1000 / eventGenerationFrequency;
                    Event sentEvent = new ActuatorEvent(System.currentTimeMillis(),
                            getClientId(), getId(), getType(), getState());
                    try {
                        Thread.sleep((long)(waitTime));
                        sendEvent(sentEvent);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        startSendingEventThread.start();
    }

    //IMPLEMENTATIONS FOR SERVER (JASON)
    public double getLastEventTimestamp() {
        if (!receivedEvents.isEmpty()) {
            ActuatorEvent lastEvent = receivedEvents.get(receivedEvents.size() - 1);
            return lastEvent.getTimeStamp();
        } else {
            // Return a default value or throw an exception based on your requirements
            return -1; // Default value, replace it with what makes sense for your application
        }
    }

    public static void main(String[] args) {
        //Client client1 = new Client(1, "blah", "0.0.0.0", 8080);
        //Request newRequest = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "abc");
        //client1.sendRequest(newRequest);
        //client1.sendRequest(newRequest);
        Actuator ac1 = new Actuator(1, "Switch", true, "0.0.0.0", 8080);
        //Actuator ac2 = new Actuator(2, "Switch", true, "0.0.0.0", 8080);
    }
}



