package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class NoMotionX(
    val enabled: Int,
    val motion_percentage: Int,
    val no_motion_percentage: Int,
    val signals_to_motion: Int,
    val signals_to_no_motion: Int
)