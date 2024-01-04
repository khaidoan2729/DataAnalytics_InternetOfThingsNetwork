package cpen221.mp3.event;

public class ActuatorEvent implements Event {

    /**Fields for this class**/
    private double timeStamp;
    private int clientID;
    private int entityID;
    private String entityType;
    private boolean value;


    /**Constructor of ActuatorEvent class (implementing Event interface)
     *
     * @param TimeStamp time event is sent
     * @param ClientId ID of the client entity
     * @param EntityId ID of the IoT entity
     * @param EntityType type of IoT entity
     * @param Value value sensed from event
     * **/
    public ActuatorEvent(double TimeStamp, 
                        int ClientId,
                        int EntityId, 
                        String EntityType, 
                        boolean Value) {
        this.timeStamp = TimeStamp;
        this.clientID = ClientId;
        this.entityID = EntityId;
        this.entityType = EntityType;
        this.value = Value;
    }

    /**
     * Gets the time stamp of this event
     * @return time stamp of this event
     * **/
    public double getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * Gets the client ID of this event
     * @return client ID of this event
     * **/
    public int getClientId() {
        return this.clientID;
    }

    /**
     * Gets the entity ID of this event
     * @return entity ID of this event
     * **/
    public int getEntityId() {
        return this.entityID;
    }

    /**
     * Gets the entity type of this event
     * @return entity type of this event
     * **/
    public String getEntityType() {
        return this.entityType;
    }

    /**
     * Gets the boolean value of this event
     * @return boolean value of this event
     * **/
    public boolean getValueBoolean() {
        return this.value;
    }

    // Actuator events do not have a double value
    // no need to implement this method
    public double getValueDouble() {
        return -1;
    }

    @Override
    public String toString() {
        return "ActuatorEvent,"         // Entity family type
                + getTimeStamp() + ","  // Time stamp
                + getClientId() + ","   // ID of registered client
                + getEntityId() + ","   // Entity ID
                + getEntityType() + "," // Entity type
                + getValueBoolean()     // Value
                ;
    }
}
