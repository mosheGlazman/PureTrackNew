package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class PureTrackCaseTamperV2(
    val Enabled: Int,
    val MagnetCalibrationOnRestart: Int,
    val caseClosedThreshold: Int
)