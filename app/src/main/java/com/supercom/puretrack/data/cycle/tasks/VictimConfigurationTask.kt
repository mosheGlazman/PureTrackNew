package com.supercom.puretrack.data.cycle.tasks

import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.DeviceConfigurationTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.VictimConfigurationTaskResult
import com.supercom.puretrack.data.cycle.JsonKotlin

class VictimConfigurationTask(cycle : Cycle) : TaskBase(cycle) {
    override val type: E_TaskType?
        get() = E_TaskType.VictimConfiguration

    override fun post(retrofitBuilder: APIInterface) {
        retrofitBuilder
            .getVictimConfiguration(
                cycle.repository.deviceId,
                cycle.repository.token
            )
            .enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var res = JsonKotlin.toObject(resultData, VictimConfigurationTaskResult::class.java)

        return res!!.GetVictimConfigurationResult
    }

    override
    fun handledError(){

    }

    override
    fun getMainPropertyName(): String{
        return "GetVictimConfigurationResult"
    }
}