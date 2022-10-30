package com.supercom.puretrack.model.business_logic_models.network.network_repository;

public interface NetworkStateType {
    int IDLE_STAGE = 0;
    int SEND_GET_AUTHENTICATION = 1;
    int WAIT_FOR_AUTHENTICATION = 2;
    int RECEIVE_AUTHENTICATION = 3;
    int AUTHENTICATION_TOKEN_IS_OK = 4;
    int SEND_LOCATION_START = 5;
    int SEND_LOCATION_FINISH = 6;
    int SEND_EVENTS_START = 7;
    int SEND_EVENTS_FINISH = 8;
    int GET_OFFENDER_REQUEST_SEND = 9;
    int GET_OFFENDER_REQUEST_RECEIVE = 10;
    int NEW_MESSAGE_START = 11;
    int NEW_MESSAGE_FINISH = 12;
    int GET_NEW_SCHEDULE_START = 13;
    int GET_NEW_SCHEDULE_FINISH = 14;
    int GET_NEW_CONFIGURATION_START = 15;
    int GET_NEW_CONFIGURATION_FINISH = 16;
    int GET_NEW_ZONES_START = 17;
    int GET_NEW_ZONES_FINISH = 18;
}