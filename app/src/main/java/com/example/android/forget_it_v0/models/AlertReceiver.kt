package com.example.android.forget_it_v0.models

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var notificationHelper = NotificationHelper(context)
        var nb : NotificationCompat.Builder = notificationHelper.channelNotification
        notificationHelper.manager?.notify(1, nb.build())
    }
}