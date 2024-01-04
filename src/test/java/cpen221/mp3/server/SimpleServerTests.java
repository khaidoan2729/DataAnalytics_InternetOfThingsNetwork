package cpen221.mp3.server;

import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.CSVEventReader;
import cpen221.mp3.server.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleServerTests {

    String csvFilePath = "data/tests/single_client_1000_events_in-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();

    Client client = new Client(0, "test@test.com", "1.1.1.1", 1);
    Actuator actuator1 = new Actuator(97, 0, "Switch", true);

    @Test
    public void testSetActuatorStateIf() {
        Server server = new Server(client);
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        Filter sensorValueFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 23);
        server.setActuatorStateIf(sensorValueFilter, actuator1);
        assertEquals(true, actuator1.getState());
    }

    @Test
    public void testToggleActuatorStateIf() {
        Server server = new Server(client);
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        Filter sensorValueFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 23);
        server.toggleActuatorStateIf(sensorValueFilter, actuator1);
        assertEquals(true, actuator1.getState());
    }

    @Test
    public void testEventsInTimeWindow() {
        Server server = new Server(client);
        TimeWindow tw = new TimeWindow(0.2, 1);
        for (int i = 0; i < 100; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        List<Event> result = server.eventsInTimeWindow(tw);
        for( Event event : result )
            System.out.println(event);
        assertEquals(9, result.size());
    }

    @Test
    public void testLastNEvents() {
        Server server = new Server(client);
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        System.out.println(eventList.get(0));
        List<Event> result = server.lastNEvents(2);

        assertEquals(2, result.size());
        System.out.println(result.get(1));
        assertEquals("PressureSensor", result.get(1).getEntityType());
        assertEquals(144, result.get(1).getEntityId());
    }

    @Test
    public void testPrediction() {
        Server server = new Server(client);
        for (int i = 0; i < 10; i++) {
            server.processIncomingEvent(eventList.get(i));
        }
        List<Double> result = server.predictNextNTimeStamps(97,4);

        assertEquals(4, result.size());
    }

    @Test
    public void testMostActiveEntity() {
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 5,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", true);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
        }
        int mostActiveEntity = server.mostActiveEntity();
        assertEquals(11, mostActiveEntity);
    }

    @Test
    public void testGetAllEntities() {
        Server server = new Server(client);
        Event event1 = new ActuatorEvent(0.00010015, 0, 11,"Switch", true);
        Event event2 = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        Event event3 = new ActuatorEvent(0.00015, 0, 5,"Switch", false);
        Event event4 = new SensorEvent(0.00022, 0, 1,"TempSensor", 11.0);
        Event event5 = new ActuatorEvent(0.00027, 0, 11,"Switch", true);
        Event event6 = new ActuatorEvent(0.00047, 0, 11,"Switch", true);
        List<Event> simulatedEvents = new ArrayList<>();
        simulatedEvents.add(event1);
        simulatedEvents.add(event2);
        simulatedEvents.add(event3);
        simulatedEvents.add(event4);
        simulatedEvents.add(event5);
        simulatedEvents.add(event6);
        Set<Integer> expected = new HashSet<>();
        for (int i = 0; i < simulatedEvents.size(); i++) {
            server.processIncomingEvent(simulatedEvents.get(i));
            int entityId = (simulatedEvents.get(i)).getEntityId();
            expected.add(entityId);
        }
        Set<Integer> result = new HashSet<>(server.getAllEntities());
        assertEquals(expected,result);
    }

    @Test
    public void testLogIf() {
        Server server = new Server(client);
        Event event1 = new SensorEvent(0.1, 0, 1, "TempSensor", 25.0);
        Event event2 = new SensorEvent(0.2, 0, 2, "PressureSensor", 100.0);
        Event event3 = new ActuatorEvent(0.3, 0, 3, "Switch", true);

        server.processIncomingEvent(event1);
        server.logIf(new Filter("value", DoubleOperator.GREATER_THAN, 20));
        server.processIncomingEvent(event2);
        List<Integer> logs = server.readLogs();

        assertEquals(1, logs.size());
    }

    @Test
    public void testReadLogsEmpty() {
        Server server = new Server(client);
        List<Integer> logs = server.readLogs();

        assertTrue(logs.isEmpty());
    }

    @Test
    public void testPredictNextNValuesAlternating() {
        Server server = new Server(client);
        Event e1 = new ActuatorEvent(1,1,1,"Switch", true);
        Event e2 = new ActuatorEvent(2,1,1,"Switch", false);
        Event e3 = new ActuatorEvent(3,1,1,"Switch", true);
        Event e4 = new ActuatorEvent(4,1,1,"Switch", false);
        Event e5 = new ActuatorEvent(5,1,1,"Switch", true);
        Event e6 = new ActuatorEvent(6,1,1,"Switch", false);
        server.processIncomingEvent(e1);
        server.processIncomingEvent(e2);
        server.processIncomingEvent(e3);
        server.processIncomingEvent(e4);
        server.processIncomingEvent(e5);
        server.processIncomingEvent(e6);
        List<Object> predictedValues = server.predictNextNValues(1, 5);
        List<Object> expectedValues = new ArrayList<>();
        expectedValues.add(true);
        expectedValues.add(false);
        expectedValues.add(true);
        expectedValues.add(false);
        expectedValues.add(true);
        assertEquals(expectedValues, predictedValues);
    }
    @Test
    public void testPredictNextNValuesSameSequence() {
        Server server = new Server(client);
        Event e1 = new ActuatorEvent(1,1,1,"Switch", true);
        Event e2 = new ActuatorEvent(2,1,1,"Switch", true);
        Event e3 = new ActuatorEvent(3,1,1,"Switch", true);
        Event e4 = new ActuatorEvent(4,1,1,"Switch", true);
        Event e5 = new ActuatorEvent(5,1,1,"Switch", true);
        Event e6 = new ActuatorEvent(6,1,1,"Switch", true);
        server.processIncomingEvent(e1);
        server.processIncomingEvent(e2);
        server.processIncomingEvent(e3);
        server.processIncomingEvent(e4);
        server.processIncomingEvent(e5);
        server.processIncomingEvent(e6);
        List<Object> predictedValues = server.predictNextNValues(1, 5);
        List<Object> expectedValues = new ArrayList<>();
        expectedValues.add(true);
        expectedValues.add(true);
        expectedValues.add(true);
        expectedValues.add(true);
        expectedValues.add(true);
        assertEquals(expectedValues, predictedValues);
    }

    @Test
    public void testPredictNextNValuesSameSequenceDouble() {
        Server server = new Server(client);
        Event e1 = new SensorEvent(1,1,1,"TempSensor", 21);
        Event e2 = new SensorEvent(2,1,1,"TempSensor", 22);
        Event e3 = new SensorEvent(3,1,1,"TempSensor", 21);
        Event e4 = new SensorEvent(4,1,1,"TempSensor", 22);
        Event e5 = new SensorEvent(5,1,1,"TempSensor", 21);
        Event e6 = new SensorEvent(6,1,1,"TempSensor", 22);
        server.processIncomingEvent(e1);
        server.processIncomingEvent(e2);
        server.processIncomingEvent(e3);
        server.processIncomingEvent(e4);
        server.processIncomingEvent(e5);
        server.processIncomingEvent(e6);
        List<Object> predictedValues = server.predictNextNValues(1, 5);
        List<Object> expectedValues = new ArrayList<>();
        expectedValues.add(21.0);
        expectedValues.add(22.0);
        expectedValues.add(21.0);
        expectedValues.add(22.0);
        expectedValues.add(21.0);
        assertEquals(expectedValues, predictedValues);
    }
    @Test
    public void testPredictNextNTimeStamps() {
        Server server = new Server(client);
        Event e1 = new ActuatorEvent(1,1,1,"Switch", true);
        Event e2 = new ActuatorEvent(2,1,1,"Switch", false);
        Event e3 = new ActuatorEvent(3,1,1,"Switch", true);
        Event e4 = new ActuatorEvent(4,1,1,"Switch", false);
        Event e5 = new ActuatorEvent(5,1,1,"Switch", true);
        Event e6 = new ActuatorEvent(6,1,1,"Switch", false);
        server.processIncomingEvent(e1);
        server.processIncomingEvent(e2);
        server.processIncomingEvent(e3);
        server.processIncomingEvent(e4);
        server.processIncomingEvent(e5);
        server.processIncomingEvent(e6);
        List<Double> predictedTimestamps = server.predictNextNTimeStamps(1, 5);
        List<Double> expectedValues = new ArrayList<>();
        expectedValues.add(7.0);
        expectedValues.add(8.0);
        expectedValues.add(9.0);
        expectedValues.add(10.0);
        expectedValues.add(11.0);
        assertEquals(expectedValues, predictedTimestamps);
    }

    @Test
    public void testPredictNextNTimeStampsNoEvents() {
        Server server = new Server(client);
        List<Double> predictedTimestamps = server.predictNextNTimeStamps(1, 5);

        assertTrue(predictedTimestamps.isEmpty());
    }

    @Test
    public void testUpdateMaxWaitTime() {
        Server server = new Server(client);
        server.updateMaxWaitTime(5.0);

        assertEquals(5.0, Server.maxWaitTime);
    }

    @Test
    public void testUpdateMaxWaitTimeWait() {
        // Test if the server waits for the old maxWaitTime before updating
        Server server = new Server(client);

        long startTime = System.currentTimeMillis();
        server.updateMaxWaitTime(5.0);
        long endTime = System.currentTimeMillis();

        assertTrue(endTime - startTime >= 2000);  // Waited for at least 2 seconds
    }

}