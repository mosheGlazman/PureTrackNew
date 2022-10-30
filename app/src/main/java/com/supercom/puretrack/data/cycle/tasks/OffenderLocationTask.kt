package com.supercom.puretrack.data.cycle.tasks

import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.CycleReportStatus
import com.supercom.puretrack.data.cycle.body.LocationBody
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.LocationTaskResult
import com.supercom.puretrack.data.cycle.JsonKotlin

class OffenderLocationTask(cycle : Cycle) : TaskBase(cycle) {

    override val type: E_TaskType?
        get() = E_TaskType.Locations

    override fun post(retrofitBuilder: APIInterface) {
        //LocationManager.setLocationStatus(repository.getLocationsToSend(),CycleReportStatus.sending)
        retrofitBuilder
            .insertOffenderLocations(
                LocationBody(
                    cycle.repository.deviceId,
                    cycle.repository.token,
                    cycle.repository.getLocationsToSend()
                )
            )
            .enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var res = JsonKotlin.toObject(resultData, LocationTaskResult::class.java)
        //LocationManager.setLocationStatus(repository.getLocationsToSend(),CycleReportStatus.success)
         return BaseTaskResult.toSuccess()
    }

    override
    fun handledError(){
        //LocationManager.setLocationStatus(repository.getLocationsToSend(),CycleReportStatus.error)
    }

    override
    fun getMainPropertyName(): String{
        return "InsertOffenderLocationsResult"
    }
}