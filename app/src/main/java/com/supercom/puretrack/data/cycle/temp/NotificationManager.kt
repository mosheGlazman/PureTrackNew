package com.supercom.puretrack.data.cycle.temp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.supercom.puretrack.ui.activity.MainActivity

import com.supercom.puretrack.data.R

object NotificationManager {
    open class Service(var id : Int,var chanel : String,var title : Int,var message : Int,var icon : Int)
    var Cycle = Service(12346,"Cycle", R.string.service_cycle_title, R.string.service_cycle_message, R.drawable.service_cycle_icon)

    fun getNotification(context: Context, service: Service): Notification? {
        var chan: NotificationChannel? = null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return getOldNotification(context,service)
        }

        chan = NotificationChannel(service.chanel, "Test", NotificationManager.IMPORTANCE_MIN)
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        chan.setSound(null, null)
        chan.enableLights(true)
        chan.setShowBadge(false)
        val manager =
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)

        val targetIntent = Intent(context, MainActivity::class.java)
        targetIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntent: PendingIntent = targetIntent.let { notificationIntent ->
            PendingIntent.getActivity(
                context,
                999,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        return Notification.Builder(context, service.chanel)
            .setContentTitle(context.getText(service.title))
            .setContentText(context.getText(service.message))
            .setSmallIcon(service.icon)
            .setContentIntent(pendingIntent)
            .setTicker(context.getText(R.string.ticker_text))
            .build()

    }

    fun getOldNotification(context: Context,service : Service): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return null
        }

        val targetIntent = Intent(context, MainActivity::class.java)
        targetIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntent: PendingIntent = targetIntent.let { notificationIntent ->
            PendingIntent.getActivity(
                context,
                999,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        return Notification.Builder(context)
            .setContentTitle(context.getText(service.title))
            .setContentText(context.getText(service.message))
            .setSmallIcon(service.icon)
            .setContentIntent(pendingIntent)
            .setTicker(context.getText(R.string.ticker_text))
            .build()
    }

    fun updateNotification(context: Context){

    }
}