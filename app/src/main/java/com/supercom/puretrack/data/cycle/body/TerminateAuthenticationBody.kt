package com.supercom.puretrack.data.cycle.body

data class TerminateAuthenticationBody(
    val deviceId: String,
    val token: String
)