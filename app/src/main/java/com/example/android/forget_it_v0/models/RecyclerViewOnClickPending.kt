package com.example.android.forget_it_v0.models

import android.view.View
import com.example.android.forget_it_v0.models.Pending

interface RecyclerViewOnClickPending {
    fun onClick(v : View, pending: Pending)
}