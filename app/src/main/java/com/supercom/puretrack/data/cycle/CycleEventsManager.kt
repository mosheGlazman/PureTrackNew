package com.supercom.puretrack.data.cycle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.supercom.puretrack.util.application.App
import java.io.Serializable
import java.util.*

class CycleEventsManager private constructor(var context: Context) {
    interface EventInterface {
        fun onEvent(event: e_Event, data: String)
    }

    enum class e_Event : Serializable {
        // ------------ Location
        LocationNewReceived,
        LocationGeofenceOut,
        LocationGeofenceIn,

        // ------------ Bluetooth
        TagOpenCase,
        TagCloseCase,
        TagCloseStamp,
        TagOpenStamp,
        TagFrizz,
        TagRSSI,

        // -------------Cycle
        CycleStart,
        CycleFinish,
        CycleUpdate,
        CycleError,
        CycleScheduleReceived,
        CycleZoneReceived,
        CycleMessageReceived,

        // -------------Data
        VictimConfigurationUpdate
    }

    var listeners: Hashtable<String?, ListenerObject>
    fun register(tag: String, listener: EventInterface, vararg events: e_Event) {
        unRegister(tag)
        listeners[tag] = ListenerObject(tag, listener, *events)
    }

    fun unRegister(tag: String) {
        if (listeners.containsKey(tag)) {
            listeners[tag]!!.unRegisterReceiver()
            listeners.remove(tag)
        }
    }

    inner class ListenerObject(
        var tag: String,
        var listener: EventInterface,
        vararg events: e_Event
    ) :
        BroadcastReceiver() {
        var events: ArrayList<e_Event?>
         fun registerReceiver() {
            LocalBroadcastManager.getInstance(context)
                .registerReceiver(this, IntentFilter(ACTION))
        }

         fun unRegisterReceiver() {
            LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(this)
        }

        override fun onReceive(context: Context, intent: Intent) {
            val event = intent.getSerializableExtra("event") as e_Event?
            val data = intent.getStringExtra("data")
            if (events.contains(event)) {
                try {
                    listener.onEvent(event!!, data!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        init {
            this.events = ArrayList()
            for (e in events) {
                this.events.add(e)
            }
            registerReceiver()
        }
    }

    @JvmOverloads
    fun sendEvent(event: e_Event?, data: String? = "") {
        val intent = Intent(ACTION)
        intent.putExtra("event", event)
        intent.putExtra("data", data)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    companion object {
        const val ACTION = "com.supercom.pureprotect.managers.AppEventsManager.event"
        var instance: CycleEventsManager? = null
            get() {
                if (field == null) {
                    field = CycleEventsManager(App.getAppContext())
                }
                return field
            }
            private set

        fun sendEvent(event: e_Event?, data: String? = "") {
            instance!!.sendEvent(event,data)
        }
    }

    init {
        listeners = Hashtable()
    }
}
