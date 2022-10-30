package com.supercom.puretrack.data.cycle.temp

import java.util.*

class TimeSpan(early: Date, later: Date) {
    var totalYears:         Float
    var totalDays:         Double
    var totalHours:         Double
    var totalMilliseconds: Double
    var totalMinutes:      Double
    var totalSeconds:      Double

    companion object {
        fun fromNow(early: Date): TimeSpan {
            return TimeSpan(early, Date())
        }

        fun getDiff(d: Date): TimeSpan {
            val res = TimeSpan(d, Date())
            if (res.totalMilliseconds < 0) {
                res.totalMilliseconds = Math.abs(res.totalMilliseconds)
                res.totalSeconds = Math.abs(res.totalSeconds)
                res.totalMinutes = Math.abs(res.totalMinutes)
                res.totalHours = Math.abs(res.totalHours)
                res.totalDays = Math.abs(res.totalDays)
                res.totalYears = Math.abs(res.totalYears)
            }
            return res
        }

        fun toNow(later: Date): TimeSpan {
            return TimeSpan(Date(), later)
        }



        fun isFuture(date: Date): Boolean {
            return if (date == null) {
                false
            } else fromNow(date).totalSeconds < 0
        }

        fun equals(o1: Date?, o2: Date?): Int {
            if (o2 == null && o1 == null) {
                return 0
            }
            if (o1 == null) {
                return -1
            }
            if (o2 == null) {
                return 1
            }
            return if (o1.time == o2.time) {
                0
            } else (o1.time - o2.time).toInt()
        }
    }

    init {
        totalMilliseconds = (later!!.time - early!!.time).toDouble()
        totalSeconds = (later.time.toDouble() - early.time.toDouble()) / 1000
        totalMinutes = (later.time.toDouble() - early.time.toDouble()) / 60000
        totalHours = (later.time.toDouble() - early.time.toDouble()) / 3600000
        totalDays = (later.time.toDouble() - early.time.toDouble()) / 86400000
        totalYears = totalDays.toFloat() / 365
    }
}
