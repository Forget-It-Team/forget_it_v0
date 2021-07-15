package com.example.android.forget_it_v0.models

import android.view.View
import com.example.android.forget_it_v0.models.Contact

interface RecyclerViewOnClickContact {
    fun onClick(text : String, contact : Contact)
}