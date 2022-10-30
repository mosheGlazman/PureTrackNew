package com.supercom.puretrack.data.cycle.interfaces

import com.supercom.puretrack.data.cycle.tasks.TaskBase

interface TaskInterface {
    fun onTaskStatusUpdated(task : TaskBase)
}