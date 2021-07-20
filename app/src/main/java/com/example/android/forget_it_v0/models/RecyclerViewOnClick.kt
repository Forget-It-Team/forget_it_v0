package com.example.android.forget_it_v0.models

import android.view.View
import com.example.android.forget_it_v0.models.Pending
import java.text.FieldPosition

interface RecyclerViewOnClick {
    fun onClick(view : View, pending: Pending, position: Int)
}