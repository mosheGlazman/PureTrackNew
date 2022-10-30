package com.supercom.puretrack.data.cycle

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.supercom.puretrack.data.cycle.interfaces.CycleInterface
import com.supercom.puretrack.data.cycle.tasks.TaskBase
import com.supercom.puretrack.data.cycle.temp.NotificationManager
import com.supercom.puretrack.data.cycle.temp.Utils

class CycleService : Service(), CycleInterface {
    private val TAG = "CycleService"

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        startForeground(
            NotificationManager.Cycle.id,
            NotificationManager.getNotification(applicationContext, NotificationManager.Cycle)
        )

        runCycleThread()
    }

    private fun runCycleThread() {
        val t = Thread {
            while (isRunning) {
                Utils.sleep(6000)
                CycleManager.runIfRequired()
            }
        }
        t.priority = Thread.MAX_PRIORITY
        t.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CycleManager.listener = this
        return START_STICKY
    }

    companion object {
        var isRunning = false

        fun start(context: Context) {
            if (isRunning) {
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, CycleService::class.java))
            } else {
                context.startService(Intent(context, CycleService::class.java))
            }
        }
    }

    override fun onCycleStart() {
        Log.i(TAG, "onCycleStart")
    }

    override fun onCycleSuccess() {
        Log.i(TAG, "onCycleSuccess")
    }

    override fun onCycleError(task: TaskBase) {
        Log.e(TAG, "onCycleError " + task.error)
    }

    override fun onCycleTaskStatusUpdated(task: TaskBase) {
            sendCycleBroadcast(task)
    }

    private fun sendCycleBroadcast(task: TaskBase) {
        Log.i(TAG,"onCycleTaskStatusUpdated  ${task.type} ${task.status}")
        CycleEventsManager.sendEvent(CycleEventsManager.e_Event.CycleUpdate)
    }
}