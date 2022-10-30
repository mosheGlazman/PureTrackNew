package com.supercom.puretrack.data.cycle

import com.supercom.puretrack.data.cycle.CycleLogLine
import com.supercom.puretrack.data.cycle.tasks.TaskBase
import java.util.*

class CycleLog : ArrayList<CycleLogLine?>() {
    fun addLine(task: TaskBase) {
        val line = CycleLogLine()
        line.type = task.type
        line.status = task.status
        line.time = Date()
        line.error = task.error
        add(line)
    }
}
