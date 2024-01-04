package cpen221.mp3.event;

public class SensorEvent implements Event {

    /**Fields for this class**/
    private double timeStamp;
    private int clientID;
    private int entityID;
    private String entityType;
    private double value;

    /**Constructor of SensorEvent class (implementing Event interface)
     *
     * @param TimeStamp time event is sent
     * @param ClientId ID of the client entity
     * @param EntityId ID of the IoT entity
     * @param EntityType type of IoT entity
     * @param Value value sensed from event
     * **/
    public SensorEvent(double TimeStamp,
                        int ClientId,
                        int EntityId, 
                        String EntityType, 
                        double Value) {
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
     * Gets the double value of this event
     * @return double value of this event
     * **/
    public double getValueDouble() {
        return this.value;
    }

    // Sensor events do not have a boolean value
    // no need to implement this method
    public boolean getValueBoolean() {
        return false;
    }

    @Override
    public String toString() {
        return "SensorEvent,"           // Entity family type
                + getTimeStamp() + ","  // Time stamp
                + getClientId() + ","   // ID of registered client
                + getEntityId() + ","   // Entity ID
                + getEntityType() + "," // Entity type
                + getValueDouble()      // Value
                ;
    }
}
