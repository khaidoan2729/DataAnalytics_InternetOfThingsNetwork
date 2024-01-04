package cpen221.mp3.handler;


import cpen221.mp3.client.Client;
import cpen221.mp3.client.Request;
import java.net.*;
import java.io.*;

import cpen221.mp3.client.RequestCommand;
import cpen221.mp3.client.RequestType;
import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;
import cpen221.mp3.event.SensorEvent;
import cpen221.mp3.server.*;



// Purpose: when MessageHandler receives a Request
public class RequestHandler implements Runnable {
    private String msg;
    private Server server;
    public RequestHandler(String msg, Server server){
        this.msg = msg;
        this.server = server;
    }

    private Request getRequestFromMessage (String message) {
        String[] split = message.split(",");
        double timestamp = Double.valueOf(split[1]);
        StringBuilder requestdatabd = new StringBuilder();
        for (int i = 3; i < split.length; i++){
            requestdatabd.append(split[i]);
            requestdatabd.append(",");
        }
        requestdatabd.deleteCharAt(requestdatabd.length()-1);
        String requestData = requestdatabd.toString();
        int RCCode = Integer.valueOf(split[2]);
        switch (RCCode){
            case 0: return new Request(timestamp, RequestType.CONFIG, RequestCommand.CONFIG_UPDATE_MAX_WAIT_TIME, requestData);
            case 1: return new Request(timestamp, RequestType.CONTROL, RequestCommand.CONTROL_SET_ACTUATOR_STATE, requestData);
            case 2: return new Request(timestamp, RequestType.CONTROL, RequestCommand.CONTROL_TOGGLE_ACTUATOR_STATE, requestData);
            case 3: return new Request(timestamp, RequestType.CONTROL, RequestCommand.CONTROL_NOTIFY_IF, requestData);
            case 4: return new Request(timestamp, RequestType.CONTROL, RequestCommand.CONTROL_GET_ALL_LOGS, requestData);
            case 5: return new Request(timestamp, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_EVENTS_IN_WINDOW, requestData);
            case 6: return new Request(timestamp, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_ALL_ENTITIES, requestData);
            case 7: return new Request(timestamp, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_LATEST_EVENTS, requestData);
            case 8: return new Request(timestamp, RequestType.ANALYSIS, RequestCommand.ANALYSIS_GET_MOST_ACTIVE_ENTITY, requestData);
            case 9: return new Request(timestamp, RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_TIMESTAMPS, requestData);
            case 10: return new Request(timestamp, RequestType.PREDICT, RequestCommand.PREDICT_NEXT_N_VALUES, requestData);
            default:
                return null;
        }
    }

    @Override
    public void run() {
        this.server.processIncomingRequest(getRequestFromMessage(msg));
    }
}
