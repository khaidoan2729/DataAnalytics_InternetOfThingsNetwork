package cpen221.mp3.event;

import cpen221.mp3.client.Request;
import cpen221.mp3.client.Client;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.server.Server;
import cpen221.mp3.server.SeverCommandToActuator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
public class ActuatorTest {
    //Can only run one test at a time
    //Must run the main function (scroll down) first to have server running in the background, otherwise there is no server to connect to
    //startSending is initialized by default when an Actuator is created

    //run this main function to enable server first before testing
    public static void main(String[] args) {
        // Define the port on which the server will listen
        int portNumber = 8080;

        try {
            // Create a ServerSocket instance
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println(serverSocket.getInetAddress().getHostAddress());

            System.out.println("Server is listening on port " + portNumber);

            while (true) {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Perform operations with the client socket as needed

                // Close the client socket when done
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //test if it can run multiple actuators at one time
    @Test
    public void testMultipleActuators() {
        Actuator actuator = new Actuator(1, "Switch", true, "0.0.0.0", 8080);
        Actuator actuator2 = new Actuator(2, "Switch", true, "0.0.0.0", 8080);
        Actuator actuator3 = new Actuator(3, "Switch", true, "0.0.0.0", 8080);
        Actuator actuator4 = new Actuator(4, "Switch", true, "0.0.0.0", 8080);
    }

    @Test
    public void testSendEvent() throws IOException {
        Actuator actuator = new Actuator(1, "Switch", true, "0.0.0.0", 8080);

        Socket socket = new Socket("0.0.0.0", 8080);
        assertTrue(socket.isConnected());

        //try sending the event to port 8080
        ActuatorEvent event = new ActuatorEvent(123.45, 1, 2, "Switch", true);
        actuator.sendEvent(event);

        socket.close();
    }

    //tests startListening as well as processServerMessage is used within startListening, actuator enables startListen ing by default
    @Test
    public void testProcessServerMessage() throws IOException{
        Actuator actuator = new Actuator(2, "Switch", false, "0.0.0.0", 8080);

        Socket socket = new Socket("0.0.0.0", 8080);
        assertTrue(socket.isConnected());

        //process a message from the server
        Request request = new Request(SeverCommandToActuator.SET_STATE);
        //should be false as nothing is processed yet
        assertFalse(actuator.getState());

        actuator.processServerMessage(request);
        //should be true after request is processed
        assertTrue(actuator.getState());

        //test toggle
        Request request2 = new Request(SeverCommandToActuator.TOGGLE_STATE);
        actuator.processServerMessage(request2);
        //should be false again after toggling
        assertFalse(actuator.getState());
    }

    //this tests startListening, startSendingEvent and stringToRequestDecoder
    @Test
    public void testStartListening() throws IOException, InterruptedException {
        // Create an actuator with a server IP and port
        Actuator actuator = new Actuator(3, "Switch", true, "0.0.0.0", 8080);

        // should be true initially
        assertTrue(actuator.getState());

        // Create a client socket to simulate sending a command to the actuator
        Socket clientSocket = new Socket("0.0.0.0", 1111);
        PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);

        // Enable listening by starting the listening thread
        Thread listeningThread = new Thread(() -> actuator.startListening());
        listeningThread.start();

        // Allow some time for the listening thread to start
        Thread.sleep(500);

        // Send a fake command to the actuator
        //the format of toString for request is "SCTA," + this.timeStamp + "," + this.SCTAtype; thus we need to have 2 commas in our string
        clientWriter.println("Fake,Command,1");
        clientWriter.flush();

        // Allow some time for the actuator to process the command
        Thread.sleep(1000);

        // Close the client socket
        clientSocket.close();
    }

}