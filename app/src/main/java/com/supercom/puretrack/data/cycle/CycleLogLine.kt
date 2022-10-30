package com.supercom.puretrack.data.cycle

import com.supercom.puretrack.data.cycle.enums.E_TaskStatus
import com.supercom.puretrack.data.cycle.enums.E_TaskType
import java.util.*

class CycleLogLine {
    var status: E_TaskStatus? = null
    var type: E_TaskType? = null
    var time: Date? = null
    var error: String? = null
}