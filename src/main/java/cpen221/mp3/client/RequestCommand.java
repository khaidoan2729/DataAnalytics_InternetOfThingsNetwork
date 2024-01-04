package cpen221.mp3.client;

public enum RequestCommand {
    CONFIG_UPDATE_MAX_WAIT_TIME,            //0 => requestData: double. Example: "5"
    CONTROL_SET_ACTUATOR_STATE,             //1 => requestData: filter+actuatorID.
    CONTROL_TOGGLE_ACTUATOR_STATE,          //2 => requestData: filter+actuatorID.
    CONTROL_NOTIFY_IF,                      //3 => requestData: filter.
    CONTROL_GET_ALL_LOGS,                   //4
    ANALYSIS_GET_EVENTS_IN_WINDOW,          //5 => requestData: TimeWindow.
    ANALYSIS_GET_ALL_ENTITIES,              //6 => any String
    ANALYSIS_GET_LATEST_EVENTS,             //7 => requestData: int. Example: "5"
    ANALYSIS_GET_MOST_ACTIVE_ENTITY,        //8 => any String
    PREDICT_NEXT_N_TIMESTAMPS,              //9
    PREDICT_NEXT_N_VALUES,                  //10
}