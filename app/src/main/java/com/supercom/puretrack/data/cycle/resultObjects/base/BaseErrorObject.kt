package com.supercom.puretrack.data.cycle.resultObjects.base

import com.supercom.puretrack.data.cycle.resultObjects.location.InsertOffenderLocationsData

open class BaseErrorObject {
    var error = ""
    var status = 0

    open fun isSuccess() : Boolean{
        return status == 0
    }
}