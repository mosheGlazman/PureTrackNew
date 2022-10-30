package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class PureTrackCaseTamper(
    val Calibration: Int,
    val Enabled: Int,
    val Validity: Int,
    val xMagnetThreshold: Int,
    val yMagnetThreshold: Int
)