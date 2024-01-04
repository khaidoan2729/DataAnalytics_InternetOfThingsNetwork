package cpen221.mp3.event;

import cpen221.mp3.entity.Actuator;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.server.SeverCommandToActuator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SensorTest {

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

    @Test
    public void testMultipleSensors() {
        Sensor sensor = new Sensor(1, "TempSensor", "0.0.0.0", 8080);
        Sensor sensor2 = new Sensor(2, "PressureSensor", "0.0.0.0", 8080);
        Sensor sensor3 = new Sensor(3, "CO2Sensor", "0.0.0.0", 8080);
        Sensor sensor4 = new Sensor(4, "TempSensor", "0.0.0.0", 8080);
    }

    @Test
    public void testSensorSendEvent() throws IOException {
        Sensor sensor = new Sensor(1, "TempSensor", "0.0.0.0", 8080);

        Socket socket = new Socket("0.0.0.0", 8080);
        assertTrue(socket.isConnected());

        //try sending the event to port 8080
        SensorEvent event = new SensorEvent(123.45, 1, 2, "TempSensor", 21.1);
        sensor.sendEvent(event);

        socket.close();
    }
}
