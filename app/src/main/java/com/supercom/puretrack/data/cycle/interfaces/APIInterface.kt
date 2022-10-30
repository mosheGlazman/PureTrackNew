package com.supercom.puretrack.data.cycle.interfaces

import com.supercom.puretrack.data.cycle.body.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface APIInterface {
    @POST("GetAuthenticationToken")
     fun getAuthenticationToken(
        @Body authenticationBody : AuthenticationBody
    ) : Call<Any>

    @POST("TerminateSession")
     fun terminateAuthenticationToken(
        @Body authBody: TerminateAuthenticationBody
    ): Call<Any>

    @POST("HandleOffenderRequest")
    fun handleOffenderRequest(
        @Body handleOffenderBody : HandleOffenderBody
    ) : Call<Any>

    @POST("InsertDeviceEvents")
    fun insertDeviceEvents(
        @Body body : DeviceEventsBody
    ) : Call<Any>

    @POST("InsertVictimLocations")
    fun insertVictimLocations(
        @Body body : LocationBody
    ) : Call<Any>

    @POST("InsertOffenderLocations")
    fun insertOffenderLocations(
        @Body body : LocationBody
    ) : Call<Any>

    @GET("GetDeviceConfiguration")
    fun getDeviceConfiguration(
        @Query("deviceId") deviceId : String,
        @Query("token") token : String,
        @Query("deviceConfigVersion") deviceConfigVersion : String,
    ): Call<Any>

    @GET("GetVictimConfiguration")
    fun getVictimConfiguration(
        @Query("deviceId") deviceId : String,
        @Query("token") token : String
    ): Call<Any>

    @GET("GetOffenderZones")
    fun getOffenderZones(
        @Query("deviceId") deviceId : String,
        @Query("token") token : String,
        @Query("offenderId") offenderId : Int,
        @Query("version") version : String,
    ): Call<Any>

    @GET("GetOffenderScheduleOfZone")
    fun getOffenderScheduleOfZone(
        @Query("zoneId") zoneId : Int,
        @Query("deviceId") deviceId : String,
        @Query("token") token : String,
        @Query("offenderId") offenderId : Int,
        @Query("version") version : String,
    ): Call<Any>

    @GET("GetOffenderRequests")
     fun getOffenderRequests(
        @Query("deviceId") deviceId : String,
        @Query("token") token : String,
    ): Call<Any>
}