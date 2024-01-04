package cpen221.mp3.server;

import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.entity.Actuator;
import cpen221.mp3.client.Client;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.client.Request;
import cpen221.mp3.server.*;

import javax.management.MBeanServerConnection;
import java.io.*;
import java.net.Socket;
import java.sql.Time;
import java.util.*;

import static java.util.Collections.reverse;


// Specific to Client, each Client can have multiple Entity's registered

public class Server {
    private Client client;
    public static double maxWaitTime = 2; // in seconds
    private List<Event> eventLogs = new ArrayList<>();
    private List<Double> loggedEventTimestamps = new ArrayList<>();
    private List<Actuator> actuators = new ArrayList<>();
    private List<Event> receivedEvents = new ArrayList<>();
    private boolean turnLogIfOn;
    private Filter filterLogIf;
    private Event curentEvent;
    private boolean responseToActuator;
    private String messageToActuator;
    private String responseMessage;

    public Server(Client client) {
        this.client = client;
    }

    /**
     * Update the max wait time for the client.
     * The max wait time is the maximum amount of time
     * that the server can wait for before starting to process each event of the client:
     * It is the difference between the time the message was received on the server
     * (not the event timeStamp from above) and the time it started to be processed.
     *
     * @param newMaxWaitTime the new max wait time
     */
    public void updateMaxWaitTime(double newMaxWaitTime) {
        // Sleep for an old maxWaitTime number of seconds to finish any in-process message
        try {
            Thread.sleep((long) (maxWaitTime * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Update new maxWaitTime
        maxWaitTime = newMaxWaitTime;
        this.responseMessage = this.client.getClientId()+"//Successfully set max wait time to "+ newMaxWaitTime +".";

    }

    /**
     * Set the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event 
     * that was received by the server the latest.
     *
     * If the actuator is not registered for the client, then this method should do nothing.
     * 
     * @param filter the filter to check
     * @param actuator the actuator to set the state of as true
     */
    public void setActuatorStateIf(Filter filter, Actuator actuator) {
        // Check if there has been any events
        if (this.receivedEvents.isEmpty()){
            this.responseMessage = this.client.getClientId()+"//No events detected yet. Please wait...";
            return;
        }
        // Check if the filter is satisfied by the latest event
        // If satisfied, send the appropriate ServerCommandToActuator as a Request to the actuator
        if (filter.satisfies(this.receivedEvents.get(0))) {
            Request rq = new Request(SeverCommandToActuator.SET_STATE);
            this.messageToActuator = rq.toString()+","+actuator.getId();
            this.responseMessage = this.client.getClientId()+"//Successfully set state to true.";


            /*try {
                Socket responseSocket = new Socket(Client.clientServerAddress, 1234);
                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(responseSocket.getOutputStream()));
                printWriter.println(this.client.getClientId()+"//Successfully set state to true.");

                printWriter.flush();
                responseSocket.close();
                printWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }*/
        } else {
            // Response to client if filter is not satisfied
            this.responseMessage = this.client.getClientId()+"//Does not satisfy filter";
        }

    }
    
    /**
     * Toggle the actuator state if the given filter is satisfied by the latest event.
     * Here the latest event is the event with the latest timestamp not the event 
     * that was received by the server the latest.
     * 
     * If the actuator has never sent an event to the server, then this method should do nothing.
     * If the actuator is not registered for the client, then this method should do nothing.
     *
     * @param filter the filter to check
     * @param actuator the non-null actuator to toggle the state of (true -> false, false -> true)
     */
    public void toggleActuatorStateIf(Filter filter, Actuator actuator) {

        // Check if there has been any events
        if (this.receivedEvents.isEmpty()){
            this.responseMessage = this.client.getClientId()+"//No events detected yet. Please wait...";
            return;
        }
        // Check if the filter is satisfied by the latest event
        // If satisfied, send the appropriate ServerCommandToActuator as a Request to the actuator

        if (filter.satisfies(this.receivedEvents.get(0))) {
                Request rq = new Request(SeverCommandToActuator.TOGGLE_STATE);
                this.messageToActuator = rq.toString() + "," + actuator.getId();
                this.responseMessage = this.client.getClientId() + "//Successfully toggle state.";
        } else {
            this.responseMessage = this.client.getClientId() + "//Does not satisfy filter";
        }
    }

    /**
     * Log the entity (not event - Piazza) ID for which a given filter was satisfied.
     * Orer of log is based on timestamp of the event (index 0 is the lastest).
     * This method is checked for every event received by the server.
     *
     * @param filter the filter to check
     */
    public void logIf(Filter filter) {
        // Add and sort current events with other events based on timestamps
        if (filter.satisfies(this.curentEvent)) {
            if (this.curentEvent != null) {
                this.eventLogs.add(this.curentEvent);
            }
            this.eventLogs.sort((e1, e2) -> Double.compare(e2.getTimeStamp(), e1.getTimeStamp()));
        }
    }

    /**
     * Return all the logs made by the "logIf" method so far.
     * If no logs have been made, then this method should return an empty list.
     * The list should be sorted in the order of event timestamps.
     * After the logs are read, they should be cleared from the server.
     *
     * @return list of *event* entity (Piazza) IDs
     */
    public List<Integer> readLogs() {

        List<Integer> entityIDLogs = new ArrayList<>();
        for (int i =0 ; i < this.eventLogs.size(); i++) {
            entityIDLogs.add(this.eventLogs.get(i).getEntityId());
        }
        this.eventLogs.clear();

        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append(this.client.getClientId()+"//All Entity ID from logs: ");
        for (Integer i : entityIDLogs) {
            msgBuilder.append(i);
            if (i != entityIDLogs.get(entityIDLogs.size() -1))
                msgBuilder.append(", ");
        }

        this.responseMessage = msgBuilder.toString();
        return entityIDLogs;
    }


    /**
     * List all the events of the client that occurred in the given time window.
     * Here the timestamp of an event is the time at which the event occurred, not 
     * the time at which the event was received by the server.
     * If no events occurred in the given time window, then this method should return an empty list.
     *
     * @param timeWindow the time window of events, inclusive of the start and end times
     * @return list of the events for the client in the given time window
     */
    public List<Event> eventsInTimeWindow(TimeWindow timeWindow) {
        List<Event> eventsInWindow = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.client.getClientId()+ "//{EventType, Timestamp, ClientID, EntityID, EntityType, Value}: ");
        for (int i = 0; i < this.receivedEvents.size(); i++) {
            if (this.receivedEvents.get(i).getTimeStamp() >= timeWindow.getStartTime() && this.receivedEvents.get(i).getTimeStamp() <= timeWindow.getEndTime()) {
                eventsInWindow.add(this.receivedEvents.get(i));
                stringBuilder.append("{"+this.receivedEvents.get(i).toString()+"}");
            }
        }
        eventsInWindow.sort((e1,e2) -> Double.compare(e1.getTimeStamp(), e2.getTimeStamp()));
        this.responseMessage = stringBuilder.toString();

        return eventsInWindow;
    }

     /**
     * Returns a set of IDs for all the entities of the client for which 
     * we have received events so far.
     * Returns an empty list if no events have been received for the client.
     * 
     * @return list of all the entities of the client for which we have received events so far
     */
    public List<Integer> getAllEntities() {
        List<Integer> entities = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.client.getClientId() + "//[");
        for (Event event : receivedEvents) {
            if (!entities.contains(event.getEntityId())) { //ensures same id is not repeated
                entities.add(event.getEntityId());
                stringBuilder.append(event.getEntityId()+ ",");
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("]");

        this.responseMessage = stringBuilder.toString();

        return entities;
    }

    /**
     * List the latest n events of the client.
     * Here the order is based on the original timestamp of the events, not the time at which the events were received by the server.
     * If the client has fewer than n events, then this method should return all the events of the client.
     * If no events exist for the client, then this method should return an empty list.
     * If there are multiple events with the same timestamp in the boundary,
     * the ones with largest EntityId should be included in the list.
     *
     * @param n the max number of events to list
     * @return list of the latest n events of the client
     */
    public List<Event> lastNEvents(int n) {
        List<Event> events = new ArrayList<>(this.receivedEvents);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.client.getClientId()+ "// {EventType, Timestamp, ClientID, EntityID, EntityType, Value}: ");
        events.sort((e1, e2) -> {
            int timestampComparison = Double.compare(e2.getTimeStamp(), e1.getTimeStamp());
            if (timestampComparison == 0) {
                // If timestamps are equal, sort by EntityId in descending order
                return Integer.compare(e2.getEntityId(), e1.getEntityId());
            }
            return timestampComparison;
        });


        List<Event> result = new ArrayList<>();
        for (int i = 0 ; i < Math.min(n,this.receivedEvents.size()); i++) {
            result.add(0, this.receivedEvents.get(i));
            stringBuilder.append("{"+ events.get(i).toString() + "}");
        }


        this.responseMessage = stringBuilder.toString();

        return result;
    }

    /**
     * returns the ID corresponding to the most active entity of the client
     * in terms of the number of events it has generated.
     *
     * If there was a tie, then this method should return the largest ID.
     * 
     * @return the most active entity ID of the client
     */
    public int mostActiveEntity() {
        int mostActiveEntityId = -1;
        int maxEventCount = 0;

        for (Integer entityId : getAllEntities()) {
            int eventCount = getEventCountForEntity(entityId);
            if ((eventCount > maxEventCount) || (eventCount == maxEventCount && mostActiveEntityId < entityId)) {
                maxEventCount = eventCount;
                mostActiveEntityId = entityId;
            }
        }

        this.responseMessage = this.client.getClientId()+ "//Most active entity has ID: "+ mostActiveEntityId;

        return mostActiveEntityId;
    }


    /**
     * Gets the number of events that have been sent by a specific entity
     *
     * @param entityId the ID of the entity that has sent events
     * @return the number of events that entity with ID {@param entityId} has sent
     * **/
    private int getEventCountForEntity(int entityId) {
        int count = 0;
        for (Event event : receivedEvents) {
            if (event.getEntityId() == entityId) {
                count++;
            }
        }
        return count;
    }

    /**
     * the client can ask the server to predict what will be 
     * the next n timestamps for the next n events 
     * of the given entity of the client (the entity is identified by its ID).
     * 
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     * 
     * @param entityId the ID of the entity
     * @param n the number of timestamps to predict
     * @return list of the predicted timestamps
     */
    public List<Double> predictNextNTimeStamps(int entityId, int n) {
        List <Double> timestampList = new ArrayList<>();
        List <Double> predictedTimestampList = new ArrayList<>();
        if(this.receivedEvents.isEmpty()){
            return predictedTimestampList;
        }
        for (int i = 0; i < this.receivedEvents.size(); i++){
            if (this.receivedEvents.get(i).getEntityId() == entityId) {
                timestampList.add(this.receivedEvents.get(i).getTimeStamp());
            }
        }


        Predictor predictorTimestamp = new Predictor(timestampList,n,true);
        predictedTimestampList.addAll(predictorTimestamp.makeNValuePredictionsTimestamp());
        // Building message to send to client
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.client.getClientId()+"//The next "+ n + " timestamps are predicted to be: {");
        for (int i = predictedTimestampList.size() -1 ; i>= 0 ; i--){
            stringBuilder.append(predictedTimestampList.get(i));
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append("}");

        this.responseMessage = stringBuilder.toString();
        reverse(predictedTimestampList);
        return predictedTimestampList;
    }

    /**
     * the client can ask the server to predict what will be 
     * the next n values of the timestamps for the next n events
     * of the given entity of the client (the entity is identified by its ID).
     * The values correspond to Event.getValueDouble() or Event.getValueBoolean() 
     * based on the type of the entity. That is why the return type is List<Object>.
     * 
     * If the server has not received any events for an entity with that ID,
     * or if that Entity is not registered for the client, then this method should return an empty list.
     * 
     * @param entityId the ID of the entity
     * @param n the number of double value to predict
     * @return list of the predicted timestamps
     */
    public List<Object> predictNextNValues(int entityId, int n) {
        List <Event> valueList = new ArrayList<>();
        List <Object> predictedValuesList = new ArrayList<>();
        if(this.receivedEvents.isEmpty()){
            return predictedValuesList;
        }
        for (Event event : this.receivedEvents){
            if (event.getEntityId() == entityId) {
                valueList.add(event);
            }
        }

        valueList.sort((e1,e2) -> Double.compare(e2.getTimeStamp(), e1.getTimeStamp()));

        Predictor predictorValues = new Predictor(valueList,n);
        if (predictorValues.isActuatorEvents)
            predictedValuesList.addAll(predictorValues.makeNValuePredictionsBoolean());
        else
            predictedValuesList.addAll(predictorValues.makeNValuePredictionsDouble());

        // Building message to send to client
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.client.getClientId()+"//The next "+ n + " values are predicted to be: {");
        for (Object num : predictedValuesList){
            stringBuilder.append(num + ",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append("}");

        this.responseMessage = stringBuilder.toString();

        return predictedValuesList;
    }

    /**
     * Processes the incoming events received through MessageHandler/MessageHandlerThread by
     * storing them and check logIf if it is turned on
     * **/
    public void processIncomingEvent(Event event) {
        this.receivedEvents.add(event);
        this.receivedEvents.sort((e1, e2) -> Double.compare(e2.getTimeStamp(), e1.getTimeStamp()));
        if (this.turnLogIfOn) {
            logIf(this.filterLogIf);
        }
        this.curentEvent = event;
    }

    /**
     * Processes the incoming request received through MessageHandler/MessageHandlerThread and perform tasks
     * based on the request type, request command and the required data for the task
     * **/
    public void processIncomingRequest(Request request) {
        switch (request.getRequestCommand()) {
            case CONFIG_UPDATE_MAX_WAIT_TIME:
                String [] split = request.getRequestData().split(",");
                updateMaxWaitTime(Double.valueOf(split[0]));    // direct to updateMacWaitTime with new wait time
                notifyClient();
                break;
            case CONTROL_SET_ACTUATOR_STATE:
                String[] elements_set_state = request.getRequestData().split("/");
                Filter filter = new Filter(Filter.toFilters(elements_set_state[0]));
                Actuator actuator = toActuator(elements_set_state[1]);
                setActuatorStateIf(filter, actuator);
                notifyActuator(actuator.getIP());
                notifyClient();
                break;
            case CONTROL_TOGGLE_ACTUATOR_STATE:
                String[] elements_toggle_state = request.getRequestData().split("/");
                Filter filter1 = new Filter(Filter.toFilters(elements_toggle_state[0]));
                Actuator actuator1 = toActuator(elements_toggle_state[1]);
                toggleActuatorStateIf(filter1, actuator1);
                notifyActuator(actuator1.getIP());
                notifyClient();
                break;
            case CONTROL_NOTIFY_IF:
                this.filterLogIf = new Filter(Filter.toFilters(request.getRequestData()));
                this.responseMessage = this.client.getClientId()+"//Successfully set log with given filter.";
                notifyClient();
                this.turnLogIfOn = true;
                break;
            case CONTROL_GET_ALL_LOGS:
                readLogs();
                notifyClient();
                break;
            case ANALYSIS_GET_EVENTS_IN_WINDOW:
                eventsInTimeWindow(TimeWindow.toTimeWindow(request.getRequestData()));
                notifyClient();
                break;
            case ANALYSIS_GET_ALL_ENTITIES:
                getAllEntities();
                notifyClient();
                break;
            case ANALYSIS_GET_LATEST_EVENTS:
                lastNEvents((int) Math.round(Double.valueOf(request.getRequestData().split(",")[0])));
                notifyClient();
                break;
            case ANALYSIS_GET_MOST_ACTIVE_ENTITY:
                mostActiveEntity();
                notifyClient();
                break;
            case PREDICT_NEXT_N_VALUES:
                String[] elements_val = request.getRequestData().split(",");
                int numberOfPredictions_val = Double.valueOf(elements_val[1]).intValue();
                int entityID_val = Double.valueOf(elements_val[0]).intValue();
                predictNextNValues(entityID_val, numberOfPredictions_val);
                notifyClient();
                break;
            case PREDICT_NEXT_N_TIMESTAMPS:
                String[] elements_ts = request.getRequestData().split(",");
                int numberOfPredictions_ts = Double.valueOf(elements_ts[1]).intValue();
                int entityID_ts = Double.valueOf(elements_ts[0]).intValue();
                predictNextNTimeStamps(entityID_ts, numberOfPredictions_ts);
                notifyClient();
                break;
            default:

        }
    }

    /**
     * Generates an actuator based on the encoded message given by the
     * MessageHandler.
     *
     * @param actuatorStr the encoded String sent by the client
     * @returns the decoded Actuator that does not trigger the sending event function
     * to differ from the ones that actually in the network.
     * **/
    public Actuator toActuator(String actuatorStr) {
        String[] actuatorElements = actuatorStr.split(",");
        return new Actuator( Integer.valueOf(actuatorElements[1]),
                Integer.valueOf(actuatorElements[2]),
                actuatorElements[3],
                Boolean.valueOf(actuatorElements[4]),
                actuatorElements[5],
                Integer.valueOf(actuatorElements[6]), true);
    }

    /**
     * Sends notifications to the Client based on what request they sent. Coudld be a
     * value return or just confirmation of task.
     *
     * **/
    private void notifyClient () {
        try {
            Socket responseSocket = new Socket(Client.clientServerAddress, 1234);
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(responseSocket.getOutputStream()));
            printWriter.println(this.responseMessage);

            printWriter.flush();
            responseSocket.close();
            printWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends command to actuators based on the request of client. Can be toggle or set state.
     *
     * @param address the IP address of the actuator.
     *
     * **/
    private void notifyActuator (String address) {
        try {
            Socket responseSocket = new Socket(address, 1111);
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(responseSocket.getOutputStream()));
            printWriter.println(this.messageToActuator);

            printWriter.flush();
            responseSocket.close();
            printWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
