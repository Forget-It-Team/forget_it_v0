package com.example.android.forget_it_v0.models

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

class MyAlarm : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.i("HERE", "myalarm")

        AudioPlayer.playAudio(context!!, Settings.System.DEFAULT_RINGTONE_URI)

        val notificationHelper = NotificationHelper(context)
        val nb = notificationHelper.channelNotification
        notificationHelper.manager!!.notify(1, nb.build())
    }
}