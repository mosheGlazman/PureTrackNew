package com.supercom.puretrack.data.cycle

import android.util.Log
import com.supercom.puretrack.data.cycle.interfaces.CycleInterface
import com.supercom.puretrack.data.cycle.tasks.TaskBase
import com.supercom.puretrack.data.cycle.temp.TimeSpan
import java.util.*
import kotlin.collections.ArrayList


object CycleManager : CycleInterface {
    private const val TAG = "CycleManager"
    var cycle: Cycle? = null
    var historyLog: ArrayList<CycleLog>
    var cycleRequired = false
    var lastEndRunning: Date? = null
    lateinit var lastStartRunning: Date
    var listener: CycleInterface? = null
    var lastRunningFailed=false
    var hasCycleSuccess=false

    val isRunning: Boolean
        get() = cycle != null && cycle!!.isRunning

    fun runIfRequired() {
        if (cycleRequired  || lastEndRunning == null) {
            run()
            return
        }

        val ts = TimeSpan.getDiff(lastEndRunning!!)
        if (ts.totalSeconds >= 30) {
            run()
        }
    }

    fun run() : Boolean{
        if (isRunning) {
            cycleRequired = true
            return false
        }

        if(lastRunningFailed){
            val ts = TimeSpan.getDiff(lastStartRunning!!)
            if (ts.totalSeconds < 50) {
               return false
            }
        }

        cycleRequired =false
        lastRunningFailed =false

        cycle = Cycle()
        cycle.let{
            lastStartRunning = Date()
            it!!.repository.deviceId = "pt212"
            it!!.repository.password = "100000"
            it!!.repository.setLocationsToSend(ArrayList())
            it!!.run(this)
        }

        return true
    }

    override fun onCycleStart() {
        Log.i(TAG,"onCycleStart")
        if (listener != null) {
            listener!!.onCycleStart()
        }
    }

    override fun onCycleSuccess() {
        Log.i(TAG,"onCycleSuccess")
        hasCycleSuccess = true
        lastEndRunning = Date()
        if (listener != null) {
            listener!!.onCycleSuccess()
        }
    }

    override fun onCycleError(task: TaskBase) {
        Log.e(TAG,"onCycleError ${task.error}")
        lastEndRunning = Date()
        if (listener != null) {
            listener!!.onCycleError(task)
        }
        lastRunningFailed = true
    }

    override fun onCycleTaskStatusUpdated(task: TaskBase) {
        Log.i(TAG,"onCycleTaskStatusUpdated  ${task.type} ${task.status}")
        if (listener != null) {
            listener!!.onCycleTaskStatusUpdated(task)
        }
    }

    init {
        historyLog = ArrayList()
    }
}
