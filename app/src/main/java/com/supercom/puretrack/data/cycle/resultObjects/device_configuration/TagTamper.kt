package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class TagTamper(
    val Debounce: Int,
    val Grace: Int,
    val Repeat: Int,
    val Type: String
)