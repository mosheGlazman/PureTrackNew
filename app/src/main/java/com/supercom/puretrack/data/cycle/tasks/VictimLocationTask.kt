package com.supercom.puretrack.data.cycle.tasks

import android.util.Log
import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.CycleReportStatus
import com.supercom.puretrack.data.cycle.body.LocationBody
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.LocationTaskResult
import com.supercom.puretrack.data.cycle.JsonKotlin
import com.supercom.puretrack.data.cycle.temp.Json
import com.supercom.puretrack.data.cycle.temp.LocationData

class VictimLocationTask(cycle : Cycle) : TaskBase(cycle) {
    lateinit var locations : ArrayList<LocationData>
    override val type: E_TaskType?
        get() = E_TaskType.Locations

    override fun post(retrofitBuilder: APIInterface) {
        locations=cycle.repository.getLocationsToSend()

        var body = LocationBody(
            cycle.repository.deviceId,
            cycle.repository.token,
            locations
        )

        Log.i(TAG,"body: " + Json.toString(body))

        //LocationManager.setLocationStatus(locations, CycleReportStatus.sending)

        retrofitBuilder
            .insertVictimLocations(body)
            .enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var res = JsonKotlin.toObject(resultData, LocationTaskResult::class.java)
        //LocationManager.setLocationStatus(locations,CycleReportStatus.success)
        return BaseTaskResult.toSuccess()
    }

    override
    fun handledError(){
        //LocationManager.setLocationStatus(repository.getLocationsToSend(),CycleReportStatus.error)
    }

    override
    fun getMainPropertyName(): String{
        return "InsertVictimLocationsResult"
    }
}