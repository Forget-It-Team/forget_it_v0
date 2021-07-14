package com.example.android.forget_it_v0.models

import android.graphics.Bitmap
import java.time.LocalDateTime

data class Pending(
    var from : String,
    var name : String,
    var task : String,
    var date : LocalDateTime,
    var image : Bitmap,
    var id : String,
    var pastDeadline : Boolean = false
)