package com.example.android.forget_it_v0.adapter

import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.example.android.forget_it_v0.AuthenticationActivity
import com.example.android.forget_it_v0.R
import com.example.android.forget_it_v0.models.Pending
import com.example.android.forget_it_v0.models.RecyclerViewOnClick
import pl.droidsonroids.gif.GifImageButton

import kotlin.random.Random

@Suppress("Deprecation")
class UpcomingAdapter (var list: ArrayList<Pending>, var listener : RecyclerViewOnClick):
    RecyclerView.Adapter<UpcomingAdapter.UpcomingViewHolder>() {
    inner class UpcomingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.rv_home_contact_name)
        var reminder: TextView = itemView.findViewById(R.id.rv_reminder)
        var date: TextView = itemView.findViewById(R.id.rv_date)
        var parent_layout: LinearLayout = itemView.findViewById(R.id.upcoming_rv_parent_layout)
        var button_markDone : CardView = itemView.findViewById(R.id.rv_markDone)
        var buttonDelete : CardView = itemView.findViewById(R.id.rv_delete)
        var info: GifImageButton = itemView.findViewById(R.id.infoUpcoming_rv)

        var image: ImageView = itemView.findViewById(R.id.rv_home_image)
        val personGif : IntArray = intArrayOf(
            R.drawable.pokone,
            R.drawable.poktwo,
            R.drawable.pokthree,
            R.drawable.pokfour,
            R.drawable.pokfive,
            R.drawable.poksix,
            R.drawable.pokseven,
            R.drawable.pokeight,
            R.drawable.poknine,
            R.drawable.pokten,
            R.drawable.pokeleven,
            R.drawable.poktwelve,
            R.drawable.pokthirteen,
        )
        val random = Random

        fun populate(pending: Pending) {
            name.text = pending.name
            val remin = pending.task.split(";").toTypedArray()
            val title = remin[0]
            val desc = remin[1]
            Log.d("title", remin[0]+"+"+remin[1])
            reminder.text = title
            image.setImageResource(personGif[random.nextInt(personGif.size)])

            var dateTimeText = pending.date.toString()
            dateTimeText = dateTimeText.replace("-", "/")
            dateTimeText = dateTimeText.replace("T", ", ")

            date.text = dateTimeText

            if (pending.pastDeadline) {
                parent_layout.setBackgroundResource(R.drawable.upcoming_rv_border_red)
            }else{
                parent_layout.setBackgroundResource(R.color.colorBackground)
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
            listener.onClick(holder.button_markDone, item, position)
        }

        holder.info.setOnClickListener{
            listener.onClick(holder.info,item, position)
        }



        holder.buttonDelete.setOnClickListener {
            listener.onClick(holder.buttonDelete, item, position)
        }
//
    }
}