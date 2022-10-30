package com.supercom.puretrack.data.cycle.tasks

import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.CycleReportStatus
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.LocationTaskResult
import com.supercom.puretrack.data.cycle.JsonKotlin

class DeviceEventsTask(cycle : Cycle) : TaskBase(cycle) {

    override val type: E_TaskType?
        get() = E_TaskType.DeviceEvents

    override fun post(retrofitBuilder: APIInterface) {
        retrofitBuilder
            .insertDeviceEvents(
                cycle.repository.getDeviceEventsBody()
            )
            .enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var res = JsonKotlin.toObject(resultData, LocationTaskResult::class.java)
        //EventsManager.setEventStatus(repository.getEventsToSend(),CycleReportStatus.success)
        return BaseTaskResult.toSuccess()
    }

    override
    fun handledError(){
       // EventsManager.setEventStatus(repository.getEventsToSend(),CycleReportStatus.error)
    }

    override
    fun getMainPropertyName(): String{
        return "InsertDeviceEvents"
    }
}