package cpen221.mp3.server;

import cpen221.mp3.CSVEventReader;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import static cpen221.mp3.server.BooleanOperator.*;
import static cpen221.mp3.server.DoubleOperator.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class FilterTests{

    String csvFilePath = "data/tests/single_client_1000_events_in-order.csv";
    CSVEventReader eventReader = new CSVEventReader(csvFilePath);
    List<Event> eventList = eventReader.readEvents();

    @Test
    public void testFilterTimeStampSingleEvent1() {
        Event event1 = new SensorEvent(0.00011, 0,
                1,"TempSensor", 1.0);
        Event event2 = new ActuatorEvent(0.33080, 0,
                97,"Switch", false);
        Filter timeStampFilter = new Filter("timestamp", GREATER_THAN, 0.0);
        assertTrue(timeStampFilter.satisfies(event1));
        assertTrue(timeStampFilter.satisfies(event2));
    }

    @Test
    public void testFilterBooleanValueSingleEvent() {
        Event event1 = new SensorEvent(0.00011, 0,
                1,"TempSensor", 1.0);
        Event event2 = new ActuatorEvent(0.33080, 0,
                97,"Switch", true);
        Filter booleanFilter = new Filter(BooleanOperator.EQUALS, true);
        assertFalse(booleanFilter.satisfies(event1));
        assertTrue(booleanFilter.satisfies(event2));
    }

    @Test
    public void testBooleanFilter() {
        Event actuatorEvent = eventList.get(3);
        Filter sensorFilter = new Filter(BooleanOperator.EQUALS, false);
        assertEquals(true, sensorFilter.satisfies(actuatorEvent));
    }

    @Test
    public void testDoubleFilterTS() {
        Event sensorEvent = eventList.get(0);
        Filter sensorFilter = new Filter("timestamp", LESS_THAN, 1);
        assertEquals(true, sensorFilter.satisfies(sensorEvent));
    }


    @Test
    public void testDoubleFilterValue() {
        Event sensorEvent = eventList.get(0);
        Filter sensorFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        assertEquals(false, sensorFilter.satisfies(sensorEvent));
    }

    @Test
    public void testComplexFilter() {
        Event sensorEvent = eventList.get(1);
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        assertEquals(true, complexFilter.satisfies(sensorEvent));
    }

    @Test
    public void testMultiEventSatisfies() {
        List<Event> eventsList = new ArrayList<>();
        eventsList.add(eventList.get(0));
        eventsList.add(eventList.get(1));
        eventsList.add(eventList.get(2));
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        assertEquals(false, complexFilter.satisfies(eventsList));
    }

    @Test
    public void testTrueMultiEventSatisfies() {
        List<Event> eventsList = new ArrayList<>();
        eventsList.add(eventList.get(0));
        eventsList.add(eventList.get(1));
        eventsList.add(eventList.get(2));
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        assertEquals(true, sensorTSFilter.satisfies(eventsList));
    }

    @Test
    public void testSift() {
        Event sensorEvent = eventList.get(1);
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        assertEquals(sensorEvent, complexFilter.sift(sensorEvent));
    }

    @Test
    public void testMultiEventSift() {
        List<Event> eventsList = new ArrayList<>();
        eventsList.add(eventList.get(0));
        eventsList.add(eventList.get(1));
        eventsList.add(eventList.get(2));
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        List<Event> filteredEvents = new ArrayList<>();
        filteredEvents.add(eventList.get(1));
        filteredEvents.add(eventList.get(2));
        assertEquals(filteredEvents, complexFilter.sift(eventsList));
    }

    @Test
    public void testFilterDoubleEqualsWithTimestamp() {
        Event sensorEvent = eventList.get(0);
        Filter sensorFilter = new Filter("timestamp", DoubleOperator.EQUALS, 1.1181831359863281E-4);
        assertTrue(sensorFilter.satisfies(sensorEvent));
    }

    @Test
    public void testFilterDoubleGreaterThanWithTimestamp() {
        Event sensorEvent = eventList.get(0);
        Filter sensorFilter = new Filter("timestamp", GREATER_THAN, 0.0001);
        assertTrue(sensorFilter.satisfies(sensorEvent));
    }

    @Test
    public void testFilterDoubleLessThanOrEqualsWithTimestamp() {
        Event sensorEvent = eventList.get(0);
        Filter sensorFilter = new Filter("timestamp", LESS_THAN_OR_EQUALS, 0.00010);
        assertFalse(sensorFilter.satisfies(sensorEvent));
    }

    @Test
    public void testComplexFilterWithMixedTypes() {
        Event sensorEvent = eventList.get(1);
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 1);
        Filter booleanFilter = new Filter(BooleanOperator.EQUALS, false);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        filterList.add(booleanFilter);
        Filter complexFilter = new Filter(filterList);
        assertEquals(true, complexFilter.satisfies(sensorEvent));
    }

    @Test
    public void testMultiEventSatisfiesWithDifferentOperators() {
        List<Event> eventsList = new ArrayList<>();
        eventsList.add(eventList.get(1));
        eventsList.add(eventList.get(2));
        Filter sensorValueFilter = new Filter("value", LESS_THAN_OR_EQUALS, 449);
        Filter sensorTSFilter = new Filter("timestamp", GREATER_THAN, 0.1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(sensorValueFilter);
        filterList.add(sensorTSFilter);
        Filter complexFilter = new Filter(filterList);
        assertEquals(true, complexFilter.satisfies(eventsList));
    }

    @Test
    public void testSiftWithDifferentOperators() {
        Event sensorEvent = eventList.get(1);
        System.out.println(eventList.get(1));
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 0.5);
        Filter sensorTSFilter2 = new Filter("timestamp", GREATER_THAN, 0.1);
        List<Filter> filterList = new ArrayList<>();
        List<Filter> filterList2 = new ArrayList<>();
        // Create lower branch of Complex Filter
        filterList2.add(sensorTSFilter);
        filterList2.add(sensorTSFilter2);
        Filter complexFilter2 = new Filter(filterList2);
        // Add basic filter and complex filter to same Filter
        filterList.add(sensorValueFilter);
        filterList.add(complexFilter2);
        // Create top Filter
        Filter complexFilter = new Filter(filterList);

        assertEquals(eventList.get(1),complexFilter.sift(sensorEvent));
    }

    @Test
    public void testSiftWithDifferentOperatorsButNull() {
        Event sensorEvent = eventList.get(1);
        System.out.println(eventList.get(1));
        Filter sensorValueFilter = new Filter("value", GREATER_THAN_OR_EQUALS, 23);
        Filter sensorTSFilter = new Filter("timestamp", LESS_THAN, 0.5);
        Filter sensorTSFilter2 = new Filter("timestamp", GREATER_THAN, 0.2);
        List<Filter> filterList = new ArrayList<>();
        List<Filter> filterList2 = new ArrayList<>();

        filterList2.add(sensorTSFilter);
        filterList2.add(sensorTSFilter2);

        Filter complexFilter2 = new Filter(filterList2);

        filterList.add(sensorValueFilter);
        filterList.add(complexFilter2);

        Filter complexFilter = new Filter(filterList);
        assertNull(complexFilter.sift(sensorEvent));
    }
    
}

