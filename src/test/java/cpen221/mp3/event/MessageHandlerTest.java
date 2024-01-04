package cpen221.mp3.event;

import cpen221.mp3.handler.MessageHandler;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.*;
import cpen221.mp3.entity.*;
import cpen221.mp3.client.*;
import cpen221.mp3.server.*;


import static org.junit.jupiter.api.Assertions.*;

public class MessageHandlerTest {

    // KHAI"S NOTE FOR PEEZS: write tests with actual enities down like the one I wrote below, dont use Socket's
    @Test
    public void testClientConnectionSensor() {
        // Test if the client can connect to the MessageHandler with sensor
        try {
            MessageHandler messageHandler = new MessageHandler(8080);
            Client client = new Client(2, "abcsdasdadsa", "0.0.0.0", 8080);
            Sensor sensor = new Sensor(1, "TempSensor", "0.0.0.0", 8080);
            Request request = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, client.getRequestData(2));
            client.sendRequest(request);
            // Allow some time for the message to be processed
            Thread.sleep((long) (5000));
            assertEquals(2, Server.maxWaitTime);
        } catch (InterruptedException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testMessageProcessing() {
        // Test if the MessageHandler processes messages correctly
        try {

            MessageHandler messageHandler = new MessageHandler(8080);
            // Create a client socket and send a message
            Client client = new Client(1, "abc", "0.0.0.0", 8080);
            Actuator actuator = new Actuator(1, 1, "Switch", false, "0.0.0.0", 8080);
            Request request = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, client.getRequestData(4));
            client.sendRequest(request);
            // Allow some time for the message to be processed
            Thread.sleep((long) (10000));
            assertEquals(4, Server.maxWaitTime);

        } catch (InterruptedException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

}
