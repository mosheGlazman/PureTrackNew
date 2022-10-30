package com.supercom.puretrack.data.cycle.interfaces

import com.supercom.puretrack.data.cycle.tasks.TaskBase

interface CycleInterface {

    fun onCycleStart()
    fun onCycleSuccess()
    fun onCycleError(task : TaskBase)
    fun onCycleTaskStatusUpdated(task : TaskBase)
}