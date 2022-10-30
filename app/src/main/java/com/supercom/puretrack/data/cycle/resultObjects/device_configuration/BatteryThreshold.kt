package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class BatteryThreshold(
    val Charger_High: Int,
    val Charger_Low: Int,
    val Charger_Medium: Int,
    val No_Charger_Critical: Int,
    val No_Charger_Low: Int,
    val No_Charger_Medium: Int
)