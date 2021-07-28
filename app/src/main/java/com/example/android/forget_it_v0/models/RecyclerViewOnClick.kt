package com.example.android.forget_it_v0.models

import android.content.Intent
import android.view.View
import com.example.android.forget_it_v0.LoginActivity
import com.example.android.forget_it_v0.UpcomingRemindersFragment

interface RecyclerViewOnClick {
    fun onClick(view : View, pending: Pending, position: Int)
}