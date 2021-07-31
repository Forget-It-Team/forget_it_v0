package com.example.android.forget_it_v0.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.forget_it_v0.R
import com.example.android.forget_it_v0.models.Pending
import com.example.android.forget_it_v0.repository.EndFragment
import pl.droidsonroids.gif.GifImageButton

class EndAdapter(var list: ArrayList<Pending>, var listener: EndFragment):
    RecyclerView.Adapter<EndAdapter.EndViewHolder>() {

    inner class EndViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var task: TextView = itemView.findViewById(R.id.end_rvTV)
        var btnRevive: ImageButton = itemView.findViewById(R.id.add_again)
        var btnInfo: GifImageButton = itemView.findViewById(R.id.infoEndRV)

        fun populate(pending: Pending) {
            val reminder = pending.task.split(";").toTypedArray()
            val title = reminder[0]
            val desc = reminder[1]
            Log.d("title", reminder.toString())
            task.text = title
            Log.d("adapter", "populate called")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EndViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.end_rv, parent, false)
        return EndViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: EndViewHolder, position: Int) {
        val item = list[position]
        holder.populate(item)

    }

    override fun getItemCount(): Int = list.size
}

