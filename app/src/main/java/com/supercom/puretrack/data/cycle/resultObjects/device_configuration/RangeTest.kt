package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class RangeTest(
    val Level_Threshold: Int,
    val Max_Misses: Int,
    val Min_Reception: Int,
    val Miss_Timeout: Int,
    val Tag_Interval: Int,
    val Timeout: Int,
    val TxBuffer: Int
)