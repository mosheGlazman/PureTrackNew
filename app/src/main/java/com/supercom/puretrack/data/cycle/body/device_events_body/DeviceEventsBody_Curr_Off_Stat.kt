package com.supercom.puretrack.data.cycle.body.device_events_body

class DeviceEventsBody_Curr_Off_Stat {
    lateinit var Id: String
     var Off_Stat=0
    lateinit var Tag_Batt: String
    var Is_In=0
    var Is_In_Sched=0
    lateinit var Sync_Version: ArrayList<DeviceEventsBody_Sync_Version>
    lateinit var Tag_Status: ArrayList<DeviceEventsBody_Tag_Status>
}