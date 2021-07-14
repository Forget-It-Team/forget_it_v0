package com.example.android.forget_it_v0.models

import android.graphics.Bitmap
import java.time.LocalDateTime

data class SentReminder(
    var name : String,
    var to : String,
    var title : String,
    var reminder : String = "",
    var date : String,
    var image : Bitmap,
    var id : String = ""
)