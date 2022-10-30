package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

 class ConfigurationItem {
     lateinit var AccelerometrerMotion: List<AccelerometrerMotion>
     var AllowedSpeed: Int=0
     lateinit var Apps: List<App>
     lateinit var BLE_Scan_Type: List<BLEScanType>
      var Bad_Gps_Accuracy_Counter: Int=0
      var Bad_Point_Threshold: Int=0
     lateinit var Battery_Threshold: List<BatteryThreshold>
      var BeaconTimeSensitivity: Int=0
     lateinit var Biometric: List<Biometric>
      var CellCountersEnabled: Int=0
     lateinit var Cellular_APN: List<CellularAPN>
      var CommNetworkTest: Int=0
      var Comm_Interval: Int=0
      var Comm_Interval_Low: Int=0
     lateinit var DST: List<DST>
      var Debug_Info_Config: Int=0
      var DevStatusConfig: Int=0
      var DeveloperModeEnable: Int=0
      var DeviceCaseTamper: Int=0
      var DstOffset: Int=0
     lateinit var Emergency: String
      var Emergency_ON: Int=0
     lateinit var Enable_Message_Response: List<EnableMessageResponse>
     lateinit var Encryption: List<Encryption>
      var GPS_Interval_In_Beacon: Int=0
      var GPS_Polling_Interval: Int=0
      var GPS_interval_no_motion: Int=0
      var Good_Point_Threshold: Int=0
     lateinit var GpsSettings: List<GpsSetting>
     lateinit var HomeInstall: List<HomeInstall>
     lateinit var Landline_Params: List<LandlineParam>
     lateinit var LocationSettings: List<LocationSetting>
      var LocationValidity: Int=0
     lateinit var Monitoring_Suspend_Settings: List<MonitoringSuspendSetting>
      var NetworkTest: Int=0
      var Network_Location_for_Events: Int=0
      var Noise_Threshold: Int=0
      var Number_Of_Missing_Calls: Int=0
     lateinit var Offender_Configuration_Array: List<OffenderConfigurationArray>
      var OfficerModeTimeout: Int=0
     lateinit var Phones: List<Phone>
      var PhonesActive: Int=0
      var PureCom_As_Home_Unit: Int=0
     lateinit var PureTrackCaseTamper: List<PureTrackCaseTamper>
     lateinit var PureTrackCaseTamperV2: List<PureTrackCaseTamperV2>
     lateinit var Range_Test: List<RangeTest>
      var Sched_Expire: Int=0
      var ScheduleDisplayDays: Int=0
     lateinit var Servers_Array: List<ServersArray>
      var Site_Code: Int=0
     lateinit var Smart_Time_Sensitivity: List<SmartTimeSensitivity>
      var SoundInterval: Int=0
      var TagSetup: Int=0
      var Tag_Proximity_Grace_Time: Int=0
     lateinit var Text_Message: List<TextMessage>
      var TimeZone: Int=0
      var Time_Format: Int=0
      var Time_sensitivity_inside_beacon: Int=0
      var Time_sensitivity_outside_beacon: Int=0
     lateinit var UnitTampers: List<UnitTamper>
     lateinit var UsePincode: List<UsePincode>
      var VictimID: Int=0
      var VoiceMicAmp: Int=0
      var VoiceMicLevel: Int=0
      var VolumeEnabled: Int=0
     lateinit var WifiDetails: List<WiFiDetail>
      var WifiSetupEnable: Int=0
     lateinit var ZoneEventValidity: List<ZoneEventValidity>
     lateinit var allowed_events_while_in_suspend: String
     lateinit var no_motion: List<NoMotionX>
     lateinit var self_diagnostics: List<SelfDiagnostic>
     lateinit var zone_drifting_locations: List<ZoneDriftingLocation>
 }