package com.supercom.puretrack.data.cycle.resultObjects.base

import com.supercom.puretrack.data.cycle.resultObjects.location.InsertOffenderLocationsData

class BaseErrorListObject : BaseErrorObject() {
    val data: List<BaseErrorObject>?=null
    fun copyErrorFromChildren() {
        error=""

        if(data!=null && data.isNotEmpty()){
            for(o in data){
                if(!o.isSuccess()){
                    error += o.error+"\n"
                    status  =o.status
                }
            }
        }
    }
}