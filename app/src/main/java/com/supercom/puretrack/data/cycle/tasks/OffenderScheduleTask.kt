package com.supercom.puretrack.data.cycle.tasks

import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.OffenderScheduleTaskResult
import com.supercom.puretrack.data.cycle.JsonKotlin

class OffenderScheduleTask(cycle : Cycle,var zone_id:Int) : TaskBase(cycle) {
    var offenderId = 2
    var version = "100"

    override val type: E_TaskType?
        get() = E_TaskType.OffenderSchedule

    override fun post(retrofitBuilder: APIInterface) {
        retrofitBuilder
            .getOffenderScheduleOfZone(
                zone_id,
                cycle.repository.deviceId,
                cycle.repository.token,
                offenderId,
                version
            )
            .enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var res = JsonKotlin.toObject(resultData, OffenderScheduleTaskResult::class.java)
        if (!res!!.GetOffenderScheduleOfZoneResult.isSuccess()) {
            return res!!.GetOffenderScheduleOfZoneResult
        }

        cycle.repository.setOffenderScheduleResult(res!!.GetOffenderScheduleOfZoneResult!!.data!!)
        return res!!.GetOffenderScheduleOfZoneResult
    }

    override
    fun handledError(){

    }

    override
    fun getMainPropertyName(): String{
        return "GetOffenderScheduleOfZoneResult"
    }
}