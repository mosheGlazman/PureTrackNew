package com.supercom.puretrack.data.cycle.tasks

import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.OffenderRequestsTaskResult
import com.supercom.puretrack.data.cycle.JsonKotlin

class OffenderRequestsTask(cycle : Cycle) : TaskBase(cycle) {

    override val type: E_TaskType?
        get() {
            return E_TaskType.OffenderRequests
        }

    override fun post(retrofitBuilder: APIInterface) {
        retrofitBuilder
            .getOffenderRequests(
                cycle.repository.deviceId,
                cycle.repository.token
            )
            .enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var res = JsonKotlin.toObject(resultData, OffenderRequestsTaskResult::class.java)
        if (!res!!.GetOffenderRequestsResult.isSuccess()) {
            return res!!.GetOffenderRequestsResult
        }

        cycle.repository.setOffenderRequestsResult(res!!.GetOffenderRequestsResult!!.data!!)
        //DataManager.setOffenderRequestsResult(res!!.GetOffenderRequestsResult!!.data!!)

        return res!!.GetOffenderRequestsResult
    }

    override
    fun handledError(){

    }

    override
    fun getMainPropertyName(): String{
        return "GetOffenderRequestsResult"
    }
}