package cpen221.mp3.client;

import cpen221.mp3.server.SeverCommandToActuator;

public class Request {
    private final double timeStamp;
    private final RequestType requestType;
    private final RequestCommand requestCommand;
    private final String requestData;
    public final boolean cmdToActuator;
    private final int SCTAtype;

    public Request(RequestType requestType, RequestCommand requestCommand, String requestData) {
        this.timeStamp = System.currentTimeMillis();
        this.requestType = requestType;
        this.requestCommand = requestCommand;
        this.requestData = requestData;
        this.cmdToActuator = false;
        this.SCTAtype = -1;
    }

    public Request(double timestamp, RequestType requestType, RequestCommand requestCommand, String requestData) {
        this.timeStamp = timestamp;
        this.requestType = requestType;
        this.requestCommand = requestCommand;
        this.requestData = requestData;
        this.cmdToActuator = false;
        this.SCTAtype = -1;
    }

    public Request(SeverCommandToActuator type) {
        this.timeStamp = System.currentTimeMillis();
        this.cmdToActuator = true;
        if(SeverCommandToActuator.SET_STATE.equals(type))
            this.SCTAtype = 0;
        else
            this.SCTAtype = 1;
        this.requestData = null; // We do not use these
        this.requestType = null;
        this.requestCommand = null;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public RequestCommand getRequestCommand() {
        return requestCommand;
    }

    public String getRequestData() {
        return requestData;
    }

    public int getSCTAtype() {return this.SCTAtype; }

    @Override
    public String toString() {
        if (this.cmdToActuator) {
            return "SCTA," + this.timeStamp + "," + this.SCTAtype;
        } else {
            String RCDecoded = null;

            switch (this.requestCommand) {
                case CONFIG_UPDATE_MAX_WAIT_TIME:
                    RCDecoded = "0";
                    break;
                case CONTROL_SET_ACTUATOR_STATE:
                    RCDecoded = "1";
                    break;
                case CONTROL_TOGGLE_ACTUATOR_STATE:
                    RCDecoded = "2";
                    break;
                case CONTROL_NOTIFY_IF:
                    RCDecoded = "3";
                    break;
                case CONTROL_GET_ALL_LOGS:
                    RCDecoded = "4";
                    break;
                case ANALYSIS_GET_EVENTS_IN_WINDOW:
                    RCDecoded = "5";
                    break;
                case ANALYSIS_GET_ALL_ENTITIES:
                    RCDecoded = "6";
                    break;
                case ANALYSIS_GET_LATEST_EVENTS:
                    RCDecoded = "7";
                    break;
                case ANALYSIS_GET_MOST_ACTIVE_ENTITY:
                    RCDecoded = "8";
                    break;
                case PREDICT_NEXT_N_TIMESTAMPS:
                    RCDecoded = "9";
                    break;
                case PREDICT_NEXT_N_VALUES:
                    RCDecoded = "10";
                    break;
                default:
                    RCDecoded = "ERR";
            }

            return "Request,"+ this.timeStamp + ","
                    + RCDecoded + ","
                    + this.requestData;
        }
    }

}