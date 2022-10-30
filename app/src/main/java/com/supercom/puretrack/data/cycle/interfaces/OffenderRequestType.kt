package com.supercom.puretrack.data.cycle.interfaces

interface OffenderRequestType {
    companion object {
        const val MESSAGE = 1
        const val SYNC = 2
        const val BIOMETRIC = 3
        const val SYNC_NOW = 4
        const val TERMINATE = 5
        const val SUSPEND = 6
        const val ACTIVATE = 7
        const val SW_UPGRADE = 8
        const val MANUAL_HANDLE = 10
        const val REMOTE_COMMAND = 14
        const val SUSPEND_RESUME = 15
        const val PHOTO_ON_DEMAND_MESSAGE = 18
    }
}