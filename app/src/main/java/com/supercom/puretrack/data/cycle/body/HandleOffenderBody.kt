package com.supercom.puretrack.data.cycle.body

class HandleOffenderBody(
    var deviceId: String,
    var token: String,
    Request_Id: Int?,
    Status: String?
) {
    var Requests_Array: Array<HandleOffenderItem>

    init {
        Requests_Array = arrayOf(
            HandleOffenderItem(Request_Id, Status)
        )
    }
}

