package com.example.android.forget_it_v0.models

import android.graphics.Bitmap

data class Contact(
    var name : String,
    var number : String,
    var buttonText : String,
    var image : Bitmap
)