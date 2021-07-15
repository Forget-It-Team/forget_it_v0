package com.example.android.forget_it_v0.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.Month


@RequiresApi(Build.VERSION_CODES.O)
data class UpcomingReminders(
    var name : String = "",
    var from : String = "",
    var reminder : String = "",
    var dateTime : LocalDateTime = LocalDateTime.of(2001, Month.JULY, 29, 10, 10),
    var pastDeadline : Boolean = false
)