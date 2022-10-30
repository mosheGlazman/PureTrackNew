package com.supercom.puretrack.data.cycle.tasks

import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.body.TerminateAuthenticationBody
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.TerminatedTaskResult
import com.supercom.puretrack.data.cycle.JsonKotlin

class TerminatedTask(cycle : Cycle) : TaskBase(cycle) {
    override val type: E_TaskType?
        get() = E_TaskType.Terminated

    override fun post(retrofitBuilder: APIInterface) {
        retrofitBuilder
            .terminateAuthenticationToken(
                TerminateAuthenticationBody(
                    cycle.repository.deviceId,
                    cycle.repository.token
                )
            )
            .enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var res = JsonKotlin.toObject(resultData, TerminatedTaskResult::class.java)
        return res!!.TerminateSessionResult
    }
    override
    fun handledError(){

    }
    override
    fun getMainPropertyName(): String{
        return "TerminateSessionResult"
    }
}