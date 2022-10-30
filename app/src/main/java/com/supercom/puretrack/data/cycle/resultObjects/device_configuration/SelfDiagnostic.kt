package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class SelfDiagnostic(
    val enabled: Int,
    val gyroscope_sensitivity: Int,
    val magnetic_sensitivity: Int
)