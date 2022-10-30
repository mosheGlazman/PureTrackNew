package com.supercom.puretrack.data.cycle.tasks

import com.supercom.puretrack.data.cycle.Cycle
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.APIInterface
import com.supercom.puretrack.data.cycle.resultObjects.base.BaseTaskResult
import com.supercom.puretrack.data.cycle.resultObjects.DeviceConfigurationTaskResult
import com.supercom.puretrack.data.cycle.JsonKotlin

class DeviceConfigurationTask(cycle : Cycle) : TaskBase(cycle) {
    override val type: E_TaskType?
        get() = E_TaskType.Configuration

    override fun post(retrofitBuilder: APIInterface) {
        retrofitBuilder
            .getDeviceConfiguration(
                cycle.repository.deviceId,
                cycle.repository.token,
                cycle.repository.deviceConfigVersion
            )
            .enqueue(getCallBack())
    }

    override fun handledData(resultData: String): BaseTaskResult {
        var data = resultData.replace("Wi-Fi", "WiFi")
        data = data.replace("WiFi", "Details  WifiDetails")
        data = data.replace("Network Test", "NetworkTest")
        data = data.replace("Offender Details", "OffenderDetails")
        data = data.replace("Offender Finger Enrollment", "OffenderFingerEnrollment")
        data = data.replace("Officer Finger Enrollment", "OfficerFingerEnrollment")
        data = data.replace("Tag Setup", "TagSetup")

        var res = JsonKotlin.toObject(data, DeviceConfigurationTaskResult::class.java)

        if (!res!!.GetDeviceConfigurationResult.isSuccess()) {
            return res!!.GetDeviceConfigurationResult
        }

        if (res!!.GetDeviceConfigurationResult!!.data!!.isEmpty()) {
            return BaseTaskResult.toError("no configuration data")
        }

        cycle.repository.setDeviceConfigurationResult(res!!.GetDeviceConfigurationResult!!.data!!)


        return res!!.GetDeviceConfigurationResult
    }

    override
    fun handledError(){

    }

    override
    fun getMainPropertyName(): String{
        return "GetDeviceConfigurationResult"
    }
}