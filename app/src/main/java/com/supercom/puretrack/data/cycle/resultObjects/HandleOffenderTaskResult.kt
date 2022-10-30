package com.supercom.puretrack.data.cycle.resultObjects

import com.supercom.puretrack.data.cycle.resultObjects.authentication_token.GetAuthenticationTokenResult
import com.supercom.puretrack.data.cycle.resultObjects.handle_offender_request.HandleOffenderRequestResult

data class HandleOffenderTaskResult(
    val HandleOffenderRequestResult: HandleOffenderRequestResult
)