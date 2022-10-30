package com.supercom.puretrack.data.cycle

import com.supercom.puretrack.data.cycle.enums.E_TaskStatus
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import com.supercom.puretrack.data.cycle.interfaces.CycleInterface
import com.supercom.puretrack.data.cycle.interfaces.TaskInterface
import com.supercom.puretrack.data.cycle.tasks.*
import com.supercom.puretrack.data.cycle.temp.TimeSpan
import java.util.*

class Cycle : TaskInterface {

    var log: CycleLog? = null
    var startTime: Date? = null
    var endTime: Date? = null
    var success: Boolean? = null
    var error: String? = null
    var repository : CycleRepository = CycleRepository()

    var listener: CycleInterface? = null
    fun run(listener: CycleInterface) {
        this.listener = listener
        startTime = Date()
        log = CycleLog()
        AuthenticationTask(this).run(this)
        listener.onCycleStart()
    }

    val isRunning: Boolean
        get() {
            if (endTime != null){
                return false
            }

            if (startTime == null){
                return false
            }

            return TimeSpan.getDiff(startTime!!).totalSeconds < 60
        }

    override fun onTaskStatusUpdated(task: TaskBase) {
        log!!.addLine(task)
        listener!!.onCycleTaskStatusUpdated(task)

        if (task.status == E_TaskStatus.error) {
            error = task.error
            success = false
            endTime = Date()
            listener!!.onCycleError(task)
            return
        }

        if (task.status == E_TaskStatus.finish) {
            val nextTask = getNextTask(task)

            if(task.type==E_TaskType.VictimConfiguration){
                CycleEventsManager.sendEvent(CycleEventsManager.e_Event.VictimConfigurationUpdate)
            }

            if (nextTask != null) {
                nextTask!!.run(this)
                return
            }

            success = true
            endTime = Date()
            listener!!.onCycleSuccess()
        }
    }

    private fun getNextTask(task: TaskBase) :TaskBase? {
        if(task.hasNextTask()){
            return task.nextTask
        }

        when (task.type) {
            E_TaskType.Terminated -> {
                return null
            }
            E_TaskType.Authentication -> {
                return VictimConfigurationTask(this)
                //return OffenderRequestsTask(this)
            }
            E_TaskType.OffenderRequests -> {
                var deviceTask : TaskBase?=null
                var syncTask : TaskBase?=null

                if (repository.deviceRequest != null) {
                    deviceTask = HandleOffenderRequestTask(repository.deviceRequest!!.RequestId, this,E_TaskType.HandleOffenderRequestsStart)
                    deviceTask.addTask(DeviceConfigurationTask(this))
                    deviceTask.addTask(HandleOffenderRequestTask(repository.deviceRequest!!.RequestId,this, E_TaskType.HandleOffenderRequestsFinish))
                }

                if (repository.syncRequest != null) {
                    syncTask = HandleOffenderRequestTask(repository.syncRequest!!.RequestId,this, E_TaskType.HandleOffenderRequestsStart)
                    syncTask.addTask(OffenderZonesTask(this))
                }

                if(deviceTask != null){

                    if (syncTask != null) {
                        deviceTask.addTask(syncTask!!)
                    }

                    return deviceTask
                }

                if(syncTask != null){
                    return syncTask
                }
            }
            E_TaskType.OffenderZones ->{
               // return null
               //var task : TaskBase?=null

               //for (i in 1..4){
               //    var t= if (i<4) OffenderScheduleTask(i)else HandleOffenderRequestTask(this,E_TaskType.HandleOffenderRequestsFinish)
               //    if(task==null){
               //        task=t
               //    }else{
               //        TaskBase.put(task,t)
               //    }
               //}

               //return task
            }
            else -> {}
        }

        if(repository.hasLocationsToSend()){
            return VictimLocationTask(this)
        }

        return TerminatedTask(this)
    }
}
