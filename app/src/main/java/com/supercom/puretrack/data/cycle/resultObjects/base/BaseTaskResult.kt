package com.supercom.puretrack.data.cycle.resultObjects.base

open class BaseTaskResult {
    var error = ""
    var status = -99

    fun isSuccess() : Boolean{
        return status == 0
    }

    companion object {
        @JvmOverloads
        fun toError(error: String, status: Int = -99): BaseTaskResult {
            val res = BaseTaskResult()
            res.error = error
            res.status = status
            return res
        }

        fun toSuccess(): BaseTaskResult {
            val res = BaseTaskResult()
            res.status = 0
            return res
        }
    }
}