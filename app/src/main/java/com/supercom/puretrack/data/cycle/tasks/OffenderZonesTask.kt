package com.supercom.puretrack.data.cycle.tasks

import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.OffenderZonesTaskResult
import com.supercom.puretrack.data.cycle.JsonKotlin

class OffenderZonesTask(cycle : Cycle) : TaskBase(cycle) {
    override val type: E_TaskType?
        get() = E_TaskType.OffenderZones

    override fun post(retrofitBuilder: APIInterface) {
        retrofitBuilder
            .getOffenderZones(
                cycle.repository.deviceId,
                cycle.repository.token,
                1,
                "123"
            )
            .enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var res = JsonKotlin.toObject(resultData, OffenderZonesTaskResult::class.java)
        if (!res!!.GetOffenderZonesResult.isSuccess()) {
            return res!!.GetOffenderZonesResult
        }

        cycle.repository.setOffenderZonesResult(res!!.GetOffenderZonesResult!!.data!!)
        return res!!.GetOffenderZonesResult
    }

    override
    fun handledError(){

    }

    override
    fun getMainPropertyName(): String{
        return "GetOffenderZonesResult"
    }
}