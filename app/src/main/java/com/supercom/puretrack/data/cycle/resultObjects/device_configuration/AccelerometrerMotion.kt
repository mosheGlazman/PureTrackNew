package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class AccelerometrerMotion(
    val Enabled: Int,
    val MotionThreshold: Double,
    val MotionWinPercentage: Int,
    val MotionWinSamples: Int,
    val StaticWinPercentage: Int,
    val StaticWinSamples: Int,
    val motion_sample_time: Int,
    val motion_window_level: Int,
    val motion_window_time: Int,
    val staticThreshold: Int
)