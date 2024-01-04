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
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTests {

    //need more test cases for Sensor, Actuator, and related functionalities

    //temp sensor
    @Test
    public void testSensorCreation() {
        // Test creating a Sensor
        int id = 123;
        String type = "TempSensor";

        Sensor sensor = new Sensor(id, type);

        // Check if the values match the expected values
        assertEquals(id, sensor.getId());
        assertEquals(-1, sensor.getClientId()); // Unregistered client ID should be -1
        assertEquals(type, sensor.getType());
    }

    //pressure sensor
    @Test
    public void testSensorRegistration() {
        // Test registering a Sensor for a client
        int id = 123;
        String type = "PressureSensor";
        int clientId = 456;

        Sensor sensor = new Sensor(id, type);
        boolean isRegistered = sensor.registerForClient(clientId);

        // Check if the sensor is registered for the client
        assertTrue(isRegistered);

        // check if a non registered client is false
        boolean isRegistered2 = sensor.registerForClient(1234);
        assertFalse(isRegistered2);
        assertEquals(clientId, sensor.getClientId());
    }


    //not sure how to test server IP and port
    //CO2 Sensor
    @Test
    public void testSensorEndpoint() {
        // Test setting the endpoint for a Sensor
        int id = 123;
        String type = "CO2Sensor";
        String serverIP = "192.168.0.1";
        int serverPort = 8080;

        Sensor sensor = new Sensor(id, type);
        sensor.setEndpoint(serverIP, serverPort);

        // Check if the endpoint values are set correctly
        //assertEquals(serverIP, sensor.serverIP);
        //assertEquals(serverPort, sensor.serverPort);
    }

    @Test
    public void testSensorEventGenerationFrequency() {
        // Test setting the event generation frequency for a Sensor
        int id = 123;
        String type = "TempSensor";
        double frequency = 0.5;

        Sensor sensor = new Sensor(id, type);
        sensor.setEventGenerationFrequency(frequency);

        // Check if the frequency value is set correctly
       //assertEquals(frequency, sensor.eventGenerationFrequency, 0.001);
    }

    @Test
    public void testSensorSendEvent() {
        // Test sending an event from a Sensor
        int id = 123;
        String type = "PressureSensor";
        String serverIP = "192.168.0.1";
        int serverPort = 8080;

        Sensor sensor = new Sensor(id, type, serverIP, serverPort);

        // Create a sample ActuatorEvent
        Event event = new ActuatorEvent(123.45, 456, 789, "Switch", true);

        // Since the event type is "Switch", the event should not be sent
        // Check if the sendEvent method does not throw an exceptions
        assertDoesNotThrow(() -> sensor.sendEvent(event));
    }

    //HAVE NOT CONFIRMED IF REQUESTS R GOING THRU YET
    @Test
    public void testAll() {
        /*Entity Tests:
        Create instances of Sensor and Actuator with different parameters, ensuring that their properties are correctly set.
        Test entity registration with a client and ensure that an entity cannot be re-registered to another client.
        Test setting and updating the endpoint for entities.*/
        Sensor tempSensor = new Sensor(1, "TempSensor");
        Actuator switchActuator = new Actuator(2, "Switch", false);

        // Test entity registration
        assert tempSensor.registerForClient(1);
        assert !tempSensor.registerForClient(2); // Cannot re-register to another client

        // Test setting and updating endpoint
        tempSensor.setEndpoint("localhostJas", 8080);
        switchActuator.setEndpoint("localhostKhai", 9090);

        // Client Tests
        Client client1 = new Client(1, "jason@gmail.com", "123123123213", 3232);
        Client client2 = new Client(2, "khaiphan@gmail.com", "1212312313123123213", 6652);

        // Test sending CONFIG request
        Request configRequest = new Request(RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, "5.2");
        client1.sendRequest(configRequest);

        // Test sending CONTROL request
        Request controlRequest = new Request(RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, "Switch,1");
        client1.sendRequest(controlRequest);

        // Test sending ANALYSIS request
        Request analysisRequest = new Request(RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW, "2,5");
        client2.sendRequest(analysisRequest);

        // Actuator Communication Tests
        switchActuator.processServerMessage(new Request(SeverCommandToActuator.SET_STATE));
        switchActuator.processServerMessage(new Request(SeverCommandToActuator.TOGGLE_STATE));

        // Edge Cases
        tempSensor.setEndpoint(null, 0); // Invalid endpoint
        //tempSensor.sendEvent(tempSensor.generateEvent()); // Should do nothing

        // Simulate 5 consecutive failed event sends
        for (int i = 0; i < 5; i++) {
            //switchActuator.sendEvent(switchActuator.generateEvent());
        }

        // Ensure waiting behavior
        //switchActuator.sendEvent(switchActuator.generateEvent()); // Should wait 10 seconds before sending

    }

    private Actuator actuator;
    public void setUp() {
        // Set up the Actuator object before each test
        this.actuator = new Actuator(1, "Switch", false, "127.0.0.1", 8080);
    }

    @Test
    public void testSendEventSuccess() {
        // Test sending an event when everything is set up correctly
        setUp();
        ActuatorEvent event = new ActuatorEvent(1.0, 1, 1, "Switch", true);
        actuator.sendEvent(event); //ensures actuator is not null and sending
    }

    @Test
    public void testSendEventIncorrectEntityType() {
        // Test sending an event with an incorrect entity type, temperatureSensor is not supported "tempSensor" is
        // In this case the program still sends the Event, but nothing changes, no errors will occur
        setUp();
        ActuatorEvent event = new ActuatorEvent(1.0, 1, 1, "TemperatureSensor", true);
        actuator.sendEvent(event);
    }

    @Test
    public void testSendEventIOException() {
        setUp();
        // Test sending an event when there's an IOException (simulate a failure to connect)
        ActuatorEvent event = new ActuatorEvent(1.0, 1, 1, "Switch", true);

        // Set an invalid server IP to simulate an IOException
        actuator.setEndpoint("invalidIP", 6960);

        actuator.sendEvent(event);
    }

}


