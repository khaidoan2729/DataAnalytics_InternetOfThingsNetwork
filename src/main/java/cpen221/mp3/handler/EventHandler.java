package cpen221.mp3.handler;


import cpen221.mp3.client.*;
import cpen221.mp3.event.*;
import cpen221.mp3.entity.*;

import java.net.*;
import java.io.*;
import cpen221.mp3.server.*;



// Purpose: when MessageHandler receives an Event
public class EventHandler implements Runnable {
    private Socket incomingSocket;
    private String msg;
    private Server server;
    private double timestamp;
    private int clientID;
    private int entityID;
    private String entityType;
    private boolean booleanVal;
    private double doubleVal;

    public EventHandler(String message, Server server){
        this.msg = message;
        this.server = server;
    }

    private Event getEventFromMessage (String message) {
        String[] split = message.split(",");
        double timestamp = Double.valueOf(split[1]);

        this.clientID = Integer.valueOf(split[2]);
        this.entityID = Integer.valueOf(split[3]);
        this.entityType = split[4];

        if (split[0].equals("ActuatorEvent")) {
            this.booleanVal = Boolean.valueOf(split[5]);
            return new ActuatorEvent(timestamp, this.clientID, this.entityID, split[4], this.booleanVal);
        } else {
            this.doubleVal = Double.valueOf(split[5]);
            return new SensorEvent(timestamp, this.clientID, this.entityID, split[4], this.doubleVal);
        }
    }

    @Override
    public void run() {
        this.server.processIncomingEvent(getEventFromMessage(msg));
    }

}
