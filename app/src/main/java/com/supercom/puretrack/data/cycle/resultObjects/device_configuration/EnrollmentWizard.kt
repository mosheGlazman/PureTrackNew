package com.supercom.puretrack.data.cycle.resultObjects.device_configuration

data class EnrollmentWizard(
    val LocationValidation: Int,
    val LoginPass: Int,
    val OffenderDetails: Int,
    val OffenderFingerEnrollment: Int,
    val OfficerFingerEnrollment: Int,
    val TagSetup: Int,
    val WiFi: Int
)