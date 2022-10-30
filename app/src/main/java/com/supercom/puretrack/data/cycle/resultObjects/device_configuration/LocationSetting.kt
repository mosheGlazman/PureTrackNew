package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class LocationSetting(
    val AllowedSpeed: Int,
    val BadGpsAccuracyCounter: Int,
    val BadPointThreshold: Int,
    val GoodPointThreshold: Int,
    val GpsServiceAverageTimeSpan: Int,
    val GpsServiceCalculationType: Int,
    val GpsServiceSampleIntervalX: Int,
    val LocationSmoothing: Int,
    val LocationTypes: String,
    val LocationValidity: Int,
    val SatelliteNum: Int,
    val SmoothingActivation: Int,
    val WeightedAverage: Int
)