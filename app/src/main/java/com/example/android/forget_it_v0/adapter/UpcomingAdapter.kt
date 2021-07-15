package com.example.android.forget_it_v0.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.forget_it_v0.R
import com.example.android.forget_it_v0.models.Pending
import com.example.android.forget_it_v0.models.RecyclerViewOnClick

class UpcomingAdapter (var list: ArrayList<Pending>, var listener : RecyclerViewOnClick):
    RecyclerView.Adapter<UpcomingAdapter.UpcomingViewHolder>() {
    inner class UpcomingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.rv_home_contact_name)
        var reminder: TextView = itemView.findViewById(R.id.rv_reminder)
        var date: TextView = itemView.findViewById(R.id.rv_date)
        var image: ImageView = itemView.findViewById(R.id.rv_home_image)
        var parent_layout: LinearLayout = itemView.findViewById(R.id.upcoming_rv_parent_layout)
        var button_markDone : Button = itemView.findViewById(R.id.rv_markDone)
        var buttonDelete : Button = itemView.findViewById(R.id.rv_delete)

        fun populate(pending: Pending) {
            name.text = pending.name
            reminder.text = pending.task
            image.setImageBitmap(pending.image)

            var dateTimeText = pending.date.toString()
            dateTimeText = dateTimeText.replace("-", "/")
            dateTimeText = dateTimeText.replace("T", ", ")

            date.text = dateTimeText

            if (pending.pastDeadline) {
                parent_layout.setBackgroundResource(R.drawable.upcoming_rv_border_red)
            }

        }

    }

    fun updateList(newList: ArrayList<Pending>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
            .inflate(R.layout.upcoming_rv, parent, false)
        return UpcomingViewHolder(inflater)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: UpcomingViewHolder, position: Int) {
        val item = list[position]
        holder.populate(item)


        holder.button_markDone.setOnClickListener {
            listener.onClick(holder.button_markDone, item)
        }

        holder.buttonDelete.setOnClickListener {
            listener.onClick(holder.buttonDelete, item)
        }
//
    }
}