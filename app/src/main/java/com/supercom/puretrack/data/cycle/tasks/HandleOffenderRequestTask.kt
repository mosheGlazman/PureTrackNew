package com.supercom.puretrack.data.cycle.tasks

import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.body.HandleOffenderBody
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.HandleOffenderTaskResult
import com.supercom.puretrack.data.cycle.temp.Json

class HandleOffenderRequestTask(var request_Id : Int,cycle : Cycle,type : E_TaskType?) : TaskBase(cycle) {
    var Status = ""
    var _type: E_TaskType?

    init {
        _type = type
        Status = if (type == E_TaskType.HandleOffenderRequestsStart) "1" else "0"
    }

    override val type: E_TaskType
        get() = _type!!

    override fun post(retrofitBuilder: APIInterface) {
        retrofitBuilder
            .handleOffenderRequest(
                HandleOffenderBody(
                    cycle.repository.deviceId,
                    cycle.repository.token,
                    request_Id,
                    Status
                )
            )
            .enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var res = Json.toObject(resultData, HandleOffenderTaskResult::class.java)
        if (!res!!.HandleOffenderRequestResult.isSuccess()) {
            return res!!.HandleOffenderRequestResult
        }



        return res!!.HandleOffenderRequestResult
    }

    override
    fun handledError(){

    }

    override
    fun getMainPropertyName(): String{
        return "HandleOffenderRequestResult"
    }
}