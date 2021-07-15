package com.example.android.forget_it_v0

import android.R
import android.app.*
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class FirebaseCloudMessaging : FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel(notificationManager)
        }
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, "channel")
            .setContentTitle(p0.notification!!.title)
            .setContentText(p0.notification!!.body)
            .setSmallIcon(R.drawable.arrow_up_float)
            .setSound(sound)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationID, builder)

//        showNotification(p0.notification?.title!!, p0.notification!!.body!!)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(notificationManager: NotificationManager){
        val channel_name = "channel_name"
        val channel = NotificationChannel("channel", channel_name, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "channel....."
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)
    }
    fun showNotification(title : String , body : String){
        val builder : NotificationCompat.Builder = NotificationCompat.Builder(this, "reminderSent")
            .setContentTitle(title)
            .setSmallIcon(R.drawable.arrow_down_float)
            .setAutoCancel(true)
            .setContentText(body)

        val manager : NotificationManagerCompat = NotificationManagerCompat.from(this)
        manager.notify(999, builder.build())
    }
}