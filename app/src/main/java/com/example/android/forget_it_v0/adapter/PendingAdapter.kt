package com.example.android.forget_it_v0.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.android.forget_it_v0.R
import com.example.android.forget_it_v0.models.Pending

class PendingAdapter(var list: ArrayList<Pending>) :
    RecyclerView.Adapter<PendingAdapter.PendingViewHolder>() {
    inner class PendingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.pending_rv_name)
        var task: TextView = itemView.findViewById(R.id.pending_rv_task)
        var date: TextView = itemView.findViewById(R.id.pending_rv_date)
        var image: ImageView = itemView.findViewById(R.id.pending_rv_image)
        var btnA: Button = itemView.findViewById(R.id.pending_rv_accept)
        var btnR: Button = itemView.findViewById(R.id.pending_rv_reject)

        var progress_circular : ProgressBar = itemView.findViewById(R.id.progress_circular)

        fun populate(pending: Pending) {
            name.text = pending.name
            task.text = pending.task
            image.setImageBitmap(pending.image)
            date.text = pending.date.toString().replace("T", "\n").replace("-", "/")
        }

    }

    fun update(newList: ArrayList<Pending>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.pending_rv, parent, false)
        return PendingViewHolder(inflater)
    }

    override fun getItemCount(): Int = list.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        val item = list[position]
        holder.populate(item)

        holder.btnA.setOnClickListener {
///Problemmmm
        }


        holder.btnR.setOnClickListener {

        }

    }
}