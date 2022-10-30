package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class RapidEventsFilterOut(
    val Enabled: Int,
    val filters: List<Filter>
)