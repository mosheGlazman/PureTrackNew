package com.supercom.puretrack.data.cycle.body

import com.google.gson.annotations.SerializedName

data class AuthenticationBody(
    val deviceId: String?,
    val password: String?,
    val version: String? = "1",
    @SerializedName("comm_type") val commType: String? = "1"
)

