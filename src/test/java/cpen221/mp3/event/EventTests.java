package cpen221.mp3.event;

import java.util.List;

import cpen221.mp3.CSVEventReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventTests{

    String csvFilePath = "data/tests/single_client_1000_events_in-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();

    @Test
    public void testCreateSingleEvent() {
        Event event = new SensorEvent(0.000111818, 0, 1,"TempSensor", 1.0);
        assertEquals(0.000111818, event.getTimeStamp());
        assertEquals(0, event.getClientId());
        assertEquals(1, event.getEntityId());
        assertEquals("TempSensor", event.getEntityType());
        assertEquals(1.0, event.getValueDouble());
    }

    @Test
    public void testSensorEvent() {
        Event sensorEvent = eventList.get(0);
        assertEquals(0.00011181831359863281, sensorEvent.getTimeStamp());
        assertEquals(0, sensorEvent.getClientId());
        assertEquals(0, sensorEvent.getEntityId());
        assertEquals("TempSensor", sensorEvent.getEntityType());
        assertEquals(22.21892397393261, sensorEvent.getValueDouble());
    }

    @Test
    public void testActuatorEvent() {
        Event actuatorEvent = eventList.get(3);
        assertEquals(0.33080601692199707, actuatorEvent.getTimeStamp());
        assertEquals(0, actuatorEvent.getClientId());
        assertEquals(97, actuatorEvent.getEntityId());
        assertEquals("Switch", actuatorEvent.getEntityType());
        assertEquals(false, actuatorEvent.getValueBoolean());
    }

    //testing basic functionality of sensorEvent's get methods
    @Test
    public void testSensorEventCreation() {
        // Test creating a SensorEvent
        double timestamp = 123.45;
        int clientID = 123;
        int entityID = 345;
        String entityType = "TempSensor";
        double value = 22.5;

        SensorEvent sensorEvent = new SensorEvent(timestamp, clientID, entityID, entityType, value);

        assertEquals(timestamp, sensorEvent.getTimeStamp(), 0.001);
        assertEquals(clientID, sensorEvent.getClientId());
        assertEquals(entityID, sensorEvent.getEntityId());
        assertEquals(entityType, sensorEvent.getEntityType());
        assertEquals(value, sensorEvent.getValueDouble(), 0.001);
    }

    //testing basic functionality of actuatorEvent's get methods
    @Test
    public void testActuatorEventCreation() {
        // Test creating an ActuatorEvent
        double timestamp = 650.12;
        int clientID = 867;
        int entityID = 34234;
        String entityType = "Switch";
        boolean value = true;

        ActuatorEvent actuatorEvent = new ActuatorEvent(timestamp, clientID, entityID, entityType, value);

        assertEquals(timestamp, actuatorEvent.getTimeStamp(), 0.001);
        assertEquals(clientID, actuatorEvent.getClientId());
        assertEquals(entityID, actuatorEvent.getEntityId());
        assertEquals(entityType, actuatorEvent.getEntityType());
        assertEquals(value, actuatorEvent.getValueBoolean());
    }
    @Test
    public void testActuatorEventToString() {
        // Test toString method of ActuatorEvent
        double timeStamp = 423.45;
        int clientID = 956;
        int entityID = 1229;
        String entityType = "Switch";
        boolean value = true;

        ActuatorEvent actuatorEvent = new ActuatorEvent(timeStamp, clientID, entityID, entityType, value);

        // Check if the toString method returns the expected string
        String expectedString = "ActuatorEvent,423.45,956,1229,Switch,true";
        assertEquals(expectedString, actuatorEvent.toString());
    }
}
