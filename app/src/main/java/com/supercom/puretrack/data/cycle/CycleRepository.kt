package com.supercom.puretrack.data.cycle

import com.supercom.puretrack.data.cycle.body.DeviceEventsBody
import com.supercom.puretrack.data.cycle.interfaces.OffenderRequestType
import com.supercom.puretrack.data.cycle.resultObjects.authentication_token.TokenData
import com.supercom.puretrack.data.cycle.resultObjects.device_configuration.Configuration
import com.supercom.puretrack.data.cycle.resultObjects.offender_requests.OffenderRequest
import com.supercom.puretrack.data.cycle.resultObjects.offender_requests.RequestData
import com.supercom.puretrack.data.cycle.resultObjects.offender_requests.RequestDataItem
import com.supercom.puretrack.data.cycle.resultObjects.offender_schedule.ScheduleData
import com.supercom.puretrack.data.cycle.resultObjects.offender_zones.ZoneData
import com.supercom.puretrack.data.cycle.temp.Json
import com.supercom.puretrack.data.cycle.temp.LocationData
import com.supercom.puretrack.data.cycle.temp.RecordEventLog
import com.supercom.puretrack.data.cycle.temp.Utils

class CycleRepository {

    var token : String
    var deviceId : String=""
    var password : String=""
    var deviceConfigVersion : String
    private var deviceConfigOffenderVersion : String
    private var deviceConfiguration : Configuration?=null
    var syncRequest : OffenderRequest  ?=null
    var deviceRequest : OffenderRequest  ?=null
    var locations : ArrayList<LocationData> = ArrayList()
    var events : ArrayList<RecordEventLog> = ArrayList()
    var offenderId = 0

    lateinit var t : RequestData

    fun setAuthenticationTokenResult(data: TokenData) {
        token = data.Token
    }

    fun setDeviceConfigurationResult(data: Configuration) {
        deviceConfiguration= data
    }

    fun setOffenderRequestsResult(data: List<RequestData>) {
         for (r in data){
             offenderId = r.OffenderId
             if(r.OffenderRequestType == OffenderRequestType.SYNC) {
                 val varrr = Json.toObject(r.RequestData, arrayOf<RequestDataItem>().javaClass)

                 //val requestData = Json.toObject(r.RequestData, ArrayList<RequestDataItem>().javaClass)
                 if(varrr!=null && varrr.isNotEmpty()) {
                     addOffenderRequestsResult_sync(r, varrr!!)
                 }
             }
         }
    }

    private fun addOffenderRequestsResult_sync(requestData : RequestData,items: Array<RequestDataItem>) {

        for (d in items) {
            if (d.Type == 2) {
                if (!Utils.equals(d.Number, deviceConfigOffenderVersion)) {
                    syncRequest = OffenderRequest()
                    syncRequest!!.RequestId = requestData.RequestId
                    syncRequest!!.requiredVersion = d.Number
                }
            }

            if (d.Type == 3) {
                if (!Utils.equals(d.Number, deviceConfigVersion)) {
                    deviceRequest = OffenderRequest()
                    deviceRequest!!.RequestId = requestData.RequestId
                    deviceRequest!!.requiredVersion = d.Number
                }
            }
        }
    }

    fun setOffenderZonesResult(data: List<ZoneData>) {

    }

    fun setOffenderScheduleResult(data: List<ScheduleData>) {

    }

    fun getEventsToSend(): ArrayList<RecordEventLog> {

        return events
    }

    fun setEventsToSend(values : ArrayList<RecordEventLog>) {
        events = values
    }

    fun hasEventsToSend(): Boolean {
        return getEventsToSend().isNotEmpty()
    }

    fun getLocationsToSend(): ArrayList<LocationData> {
        // TODO sent balks
        return locations
    }

    fun setLocationsToSend(values : ArrayList<LocationData>) {
        locations = values
    }

    fun hasLocationsToSend(): Boolean {
        return getLocationsToSend().isNotEmpty()
    }

    fun getDeviceEventsBody(): DeviceEventsBody {
        var res = DeviceEventsBody (
            deviceId,
            token,
            "",
            "",
            "",
            "",
            events )

        return res
    }

    init{
        token=""
        deviceConfigVersion="1"
        deviceConfigOffenderVersion="1"
    }
}