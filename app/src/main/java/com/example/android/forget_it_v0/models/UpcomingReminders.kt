package com.example.forgetit.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.Month

data class UpcomingReminders @RequiresApi(Build.VERSION_CODES.O) constructor(
    var name : String = "",
    var from : String = "",
    var reminder : String = "",
    var dateTime : LocalDateTime = LocalDateTime.of(2001, Month.JULY, 29, 10, 10),
    var pastDeadline : Boolean = false
)