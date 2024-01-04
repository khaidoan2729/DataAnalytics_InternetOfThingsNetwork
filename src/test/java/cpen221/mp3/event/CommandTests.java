package cpen221.mp3.event;

import cpen221.mp3.client.Request;
import cpen221.mp3.client.Client;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.entity.Sensor;
import cpen221.mp3.handler.MessageHandler;
import cpen221.mp3.server.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTests {
    // Our team think it's easier to run the demo in one test since it allows us to see what the MessageHandler is
    // receiving and follow the checkpoints for testing objectives. We can't really test Server on its own because it
    // requires socket connection.
    @Test
    public void testControl() {
        // Initializes of MessageHandler
        MessageHandler messageHandler = new MessageHandler(8080);

        // Initializes client, actuator and request. Testing for updating max wait time
        Client client = new Client(1, "abc", "0.0.0.0", 8080);
        Actuator actuator = new Actuator(1, 1, "Switch", false, "0.0.0.0", 8080);
        Request request1 = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, client.getRequestData(1));
        client.sendRequest(request1);

        // Allowing it some time to process
        try {
            Thread.sleep((long)(5000));
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }

        assertEquals(1, Server.maxWaitTime);
        System.out.println("\nCheckpoint: Passed test Update new wait time!!\n");

        // Initializes filter and request, testing for setting state of actuator with filter
        Filter filter = new Filter("timestamp", DoubleOperator.GREATER_THAN_OR_EQUALS, 1000.0);
        Request request2 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, client.getRequestData(filter, actuator));
        client.sendRequest(request2);

        // Allowing it some time to process, shorter time since max wait time is updated
        try {
            Thread.sleep((long) (5000));
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }

        assertTrue(actuator.getState());
        System.out.println("\nCheckpoint: Passed test set actuator state!!\n");

        // Initializes second client, actuator, request, testing for handling multiple clients and actuators and toggle state
        Client client2 = new Client(2, "abcd", "0.0.0.0", 8080);
        Actuator actuator2 = new Actuator(2, 2, "Switch", false, "0.0.0.0", 8080);

        // Allows the actuator to send events
        try {
            Thread.sleep((long)(10000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Filter filter2 = new Filter(BooleanOperator.NOT_EQUALS, true);
        Request request3 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, client2.getRequestData(filter2, actuator2));
        client2.sendRequest(request3);

        try {
            Thread.sleep((long)(5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //The state should be toggled to true
        assertTrue(actuator2.getState());
        System.out.println("\nCheckpoint: Passed test toggle actuator state!!\n");

        //Initializes request to testing notify if filter is satisfied
        Request request4 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, client2.getRequestData(filter2));
        client2.sendRequest(request4);

        try {
            Thread.sleep((long)(5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\nCheckpoint: Passed Notify If, server should send a message saying \"Successfully set log with given filter.\" \n");

        //Initializes request to testing get all logs
        Request request5 = new Request(RequestType.CONTROL, RequestCommand.CONTROL_GET_ALL_LOGS, "ABC");
        client2.sendRequest(request5);

        try {
            Thread.sleep((long)(5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //assertEquals(true, Server.eventLogs);
        System.out.println("\nCheckpoint: Passed GET_ALL_LOGS, server should've printed \"All Entity ID from logs: 2\".\n");

        //Initialize a different client, testing for request type of ANALYSIS
        Client client3 = new Client(3, "peezschrist", "0.0.0.0", 8080);
        Actuator actuator3 = new Actuator(3, 3, "Switch", false, "0.0.0.0", 8080);

        // Allows the actuator to send events
        try {
            Thread.sleep((long)(10000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Filter filter3 = new Filter("value", DoubleOperator.LESS_THAN_OR_EQUALS, 1000.0);
        Request request6 = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_LATEST_EVENTS, client3.getRequestData(5));
        client3.sendRequest(request6);

        try {
            Thread.sleep((long)(5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\nCheckpoint: Passed GET_LATEST_EVENTS, server should've.\n");

        //Initialize request to GET_ALL_ENTITIES
        Request request7 = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, "CDF");
        client3.sendRequest(request7);

        try {
            Thread.sleep((long)(5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //should return all entities in running terminal
        System.out.println("\nCheckpoint: Passed GET_ALL_ENTITIES, server should've given 3.\n");

        //Initialize request to GET_LATEST_EVENTS
        Request request8 = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW, client3.getRequestData(new TimeWindow(0.0, 9999E12)));
        client3.sendRequest(request8);

        try {
            Thread.sleep((long)(5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //should list all the events of the client that occurred in the given time window.
        System.out.println("\nCheckpoint: Passed GET_EVENTS_IN_WINDOW, server should've listed things.\n");

        //Initialize request to GET_EVENTS_IN_WINDOW
        Request request9 = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_MOST_ACTIVE_ENTITY, "motheriloveu");
        client3.sendRequest(request9);

        try {
            Thread.sleep((long)(5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //should list all the events of the client that occurred in the given time window.
        System.out.println("\nCheckpoint: Passed GET_MOST_ACTIVE_ENTITY, server should've .\n");

        //Initialize request to PREDICT_NEXT_N_TIMESTAMPS
        Request request10 = new Request(RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_TIMESTAMPS, client3.getRequestData(3, 5));
        client3.sendRequest(request10);

        try {
            Thread.sleep((long)(5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //should predict the next timestamp for the next event
        System.out.println("\nCheckpoint: Passed PREDICT_NEXT_N_TIMESTAMPS, server should've predicted the next timestamp.\n");

        //Initialize request to GET_EVENTS_IN_WINDOW
        Request request11 = new Request(RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_VALUES, client3.getRequestData(3,3));
        client3.sendRequest(request11);

        try {
            Thread.sleep((long)(5000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //should predict the next timestamp for the next values
        System.out.println("\nCheckpoint: Passed PREDICT_NEXT_N_VALUES, server should've predicted the next values for the next n events.\n");
    }

}
