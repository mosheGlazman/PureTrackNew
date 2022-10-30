package com.supercom.puretrack.data.cycle.tasks

import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.body.AuthenticationBody
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.AuthenticationTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.JsonKotlin

class AuthenticationTask(cycle : Cycle) : TaskBase(cycle) {
    override val type: E_TaskType?
        get() = E_TaskType.Authentication

    override fun post(retrofitBuilder: APIInterface) {
        retrofitBuilder
            .getAuthenticationToken(
                AuthenticationBody(
                    cycle.repository.deviceId,
                    cycle.repository.password
                )
            ).enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var res = JsonKotlin.toObject(resultData, AuthenticationTaskResult::class.java)
        if (!res!!.GetAuthenticationTokenResult.isSuccess()) {
            return res!!.GetAuthenticationTokenResult
        }
        cycle.repository.setAuthenticationTokenResult(res!!.GetAuthenticationTokenResult!!.data!!)
        return res!!.GetAuthenticationTokenResult
    }

    override
    fun getMainPropertyName(): String{
        return "GetAuthenticationTokenResult"
    }

    override
    fun handledError(){

    }
}