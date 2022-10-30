package com.supercom.puretrack.data.cycle.temp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun startForegroundService(context: Context, cls: Class<*>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, cls))
        } else {
            context.startService(Intent(context, cls))
        }
    }

    fun equals(str1: String?, str2: String?): Boolean {
        val s1 = str1 ?: ""
        val s2 = str2 ?: ""
        return s2 == s1
    }

    fun sleep(time: Long) {
        try {
            Thread.sleep(time)
        } catch (e: Exception) {
        }
    }

    fun getDate(date: String?, format: String?): Date? {
        if (date == null) {
            return null
        }
        try {
            return SimpleDateFormat(format).parse(date)
        } catch (ex: java.lang.Exception) {
        }
        return null
    }

    interface runOnUIListener {
        fun run()
    }

    fun runOnUI(listener: runOnUIListener) {
        runOnUI(0, listener)
    }

    fun runOnUI(timeout: Int, listener: runOnUIListener) {
        try {
            if (timeout > 0) {
                Thread {
                    try {
                        Thread.sleep(timeout.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    runOnUI(0, listener)
                }.start()
            } else {
                Handler(Looper.getMainLooper()).post {
                    try {
                        listener.run()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}