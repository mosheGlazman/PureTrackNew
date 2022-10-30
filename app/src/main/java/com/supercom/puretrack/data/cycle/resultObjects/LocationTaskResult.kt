package com.supercom.puretrack.data.cycle.resultObjects

import com.supercom.puretrack.data.cycle.resultObjects.location.InsertOffenderLocationsResult
import com.supercom.puretrack.data.cycle.resultObjects.terminate_session_task.TerminateSessionResult

data class LocationTaskResult(
    val InsertOffenderLocationsResult: InsertOffenderLocationsResult
)