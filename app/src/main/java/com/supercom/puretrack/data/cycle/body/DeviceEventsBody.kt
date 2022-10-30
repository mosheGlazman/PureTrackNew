package com.supercom.puretrack.data.cycle.body

import com.supercom.puretrack.data.cycle.body.device_events_body.*
import com.supercom.puretrack.data.cycle.temp.RecordEventLog

class DeviceEventsBody (
    var deviceId : String,
    var token : String,
    dev_status_power : String,
    dev_status_cellularDataAvailable : String,
    Curr_off_Stat_Id:String,
    Curr_off_Stat_Tag_Batt:String,
    var Events: ArrayList<RecordEventLog>
) {

    var Dev_Status: ArrayList<DeviceEventsBody_Dev_Status>
    var Curr_Off_Stat: ArrayList<DeviceEventsBody_Curr_Off_Stat>

    init {
        Dev_Status = ArrayList()
        var status = DeviceEventsBody_Dev_Status()
        status.Power = dev_status_power
        status.cellularDataAvailable = dev_status_cellularDataAvailable
        Dev_Status.add(status)

        var curr= DeviceEventsBody_Curr_Off_Stat()
        curr.Id=Curr_off_Stat_Id
        curr.Off_Stat=2
        curr.Tag_Batt=Curr_off_Stat_Tag_Batt
        curr.Is_In=0
        curr.Is_In_Sched=1

        curr.Sync_Version=ArrayList()
        var version= DeviceEventsBody_Sync_Version()
        version.Sync_Type=1
        version.Version_Number=1
        curr.Sync_Version.add(version)

        curr.Tag_Status=ArrayList()
        var tag_status= DeviceEventsBody_Tag_Status()
        curr.Tag_Status.add(tag_status)

        Curr_Off_Stat=ArrayList()
        Curr_Off_Stat.add(curr)
    }
}