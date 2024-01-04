package cpen221.mp3.entity;

import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;

import java.io.*;
import java.net.Socket;
import java.util.Random;


public class Sensor implements Entity {
    final private String[] TYPES = {"TempSensor", "PressureSensor", "CO2Sensor", "Switch"};
    private final int id;                   // ID of the sensor entity
    private int clientId;                   // ID of the client
    private final String type;              // type of entity
    private String serverIP = null;
    private int serverPort = 0;
    private double eventGenerationFrequency = 0.2; // default value in Hz (1/s)

    /**
     * Constructor for Sensor class
     *
     * @param id   ID of the sensor entity
     * @param type type of sensor, one of three types: "TempSensor","PressureSensor", "CO2Sensor".
     **/
    public Sensor(int id, String type) {
        this.id = id;
        this.clientId = -1;         // remains unregistered
        this.type = type;
    }

    /**
     * Constructor for Sensor class
     *
     * @param id       ID of the sensor entity.
     * @param clientId ID of client sensor is registered to
     * @param type     type of sensor, one of three types: "TempSensor","PressureSensor", "CO2Sensor".
     **/
    public Sensor(int id, int clientId, String type) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
    }

    /**
     * Constructor for Sensor class
     *
     * @param id         ID of the sensor entity.
     * @param type       type of sensor, one of three types: "TempSensor","PressureSensor", "CO2Sensor".
     * @param serverIP   IP address of server that sensor is connected to
     * @param serverPort port number of server
     **/
    public Sensor(int id, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = -1;   // remains unregistered
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.startSendingEvent(this.type);
    }

    /**
     * Constructor for Sensor class
     *
     * @param id         ID of the sensor entity.
     * @param clientId   ID of client sensor is registered to
     * @param type       type of sensor, one of three types: "TempSensor","PressureSensor", "CO2Sensor".
     * @param serverIP   IP address of server that sensor is connected to
     * @param serverPort port number of server
     **/
    public Sensor(int id, int clientId, String type, String serverIP, int serverPort) {
        this.id = id;
        this.clientId = clientId;   // registered for the client
        this.type = type;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        startSendingEvent(this.type);
    }

    /**
     * Gets ID of sensor
     *
     * @return ID of sensor
     **/
    public int getId() {
        return id;
    }

    /**
     * Gets ID of the client sensor is registered to
     *
     * @return ID of the client sensor is registered to
     **/
    public int getClientId() {
        return clientId;
    }

    /**
     * Gets type of sensor
     *
     * @return one of the three types: "TempSensor","PressureSensor", "CO2Sensor".
     **/
    public String getType() {
        return type;
    }

    /**
     * Gets server IP where events are sent to
     * @return server IP
     * **/
    public String getServerIP() {return this.serverIP; }


    /**
     * Gets server port where events are sent to
     * @return server port
     * **/
    public int getServerPort() {return this.serverPort; }

    /**
     * @return if this sensor is an actuator, should always be false
     **/
    public boolean isActuator() {
        return false;
    }

    /**
     * Registers the sensor for the given client
     *
     * @return true if the sensor is new (clientID is -1 already) and gets successfully
     * registered or if it is already registered for clientId, else false
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
     * the sensor should send events to
     *
     * @param serverIP   the IP address of the endpoint
     * @param serverPort the port number of the endpoint
     */
    public void setEndpoint(String serverIP, int serverPort) {
        if(this.serverIP == null && this.serverPort == 0) {
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            startSendingEvent(this.type);
        }
    }

    /**
     * Sets the frequency of event generation
     *
     * @param frequency the frequency of event generation in Hz (1/s)
     */
    public void setEventGenerationFrequency(double frequency) {
        this.eventGenerationFrequency = frequency;
    }

    /**
     * Sends event to the server specified in this sensor. If not defined then do nothing
     *
     * @param event the event to send to the server
     **/
    public void sendEvent(Event event) {
        // implement this method
        // note that Event is a complex object that you need to serialize before sending
        if (!event.getEntityType().equals(this.TYPES[3]) && this.serverPort > 0 && this.serverIP != null) { // if correct type and endpoint exists
            Thread eventSender = new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket sensorSocket = null;
                    PrintWriter printWriterOut = null;
                    try {
                        // Create socket to connect
                        sensorSocket = new Socket(getServerIP(), getServerPort());
                        printWriterOut = new PrintWriter(new OutputStreamWriter(sensorSocket.getOutputStream()));
                        String requestString = event.toString();
                        printWriterOut.println(requestString);
                        printWriterOut.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        if (sensorSocket != null) {
                            try {
                                sensorSocket.close();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (printWriterOut != null)
                            printWriterOut.close();
                    }

                }
            });

            eventSender.start();
        }
    }

    /**
     * Initializes threads to send random data based on the type of sensor to server
     * **/
    private void startSendingEvent (String type) {
        Thread startSendingEventThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    double value = getRandomData(type);
                    double waitTime = 1000 / eventGenerationFrequency;

                    Event sentEvent = new SensorEvent(System.currentTimeMillis(),
                            getClientId(), getId(), getType(), value);
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


    private double getRandomData(String type) throws IllegalArgumentException {
        Random random = new Random();
        if (type.equals(TYPES[0])) {
            return 20 + (4 * random.nextDouble());   // Temp in Celsius
        } else if (type.equals(TYPES[1])) {
            return 1020 + (4 * random.nextDouble());  // Pressure in millibars
        } else if (type.equals(TYPES[2])) {
            return 400 + (50 * random.nextDouble());  // CO2 level in ppm
        } else {
            throw new IllegalArgumentException();
        }
    }


}