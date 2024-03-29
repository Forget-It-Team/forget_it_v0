package com.example.android.forget_it_v0.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.forget_it_v0.R
import com.example.android.forget_it_v0.models.Pending
import com.example.android.forget_it_v0.models.RecyclerViewOnClickPending
import pl.droidsonroids.gif.GifImageButton
import kotlin.random.Random

class CompleteAdapter(var list: ArrayList<Pending>, var listener : RecyclerViewOnClickPending):
    RecyclerView.Adapter<CompleteAdapter.CompleteViewHolder>() {

    inner class CompleteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.pending_rv_name)
        var task: TextView = itemView.findViewById(R.id.pending_rv_task)
        var date: TextView = itemView.findViewById(R.id.pending_rv_date)
        var btnA: CardView = itemView.findViewById(R.id.pending_rv_accept)
        var btnR: CardView = itemView.findViewById(R.id.pending_rv_reject)
        var image: ImageView = itemView.findViewById(R.id.pending_rv_image)
        var info: GifImageButton = itemView.findViewById(R.id.infoButtonPending)
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
            val reminder = pending.task.split(";").toTypedArray()
            val title = reminder[0]
            val desc = reminder[1]
            Log.d("title", reminder.toString())
            task.text = title
            image.setImageResource(personGif[random.nextInt(personGif.size)])
            date.text = pending.date.toString().replace("T", ",").replace("-", "/")
            Log.d("adapter","populate called")
        }

    }

    fun update(newList: ArrayList<Pending>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):CompleteViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.pending_rv, parent, false)
        Log.d("adapter","on create view Holder called")
        return CompleteViewHolder(inflater)
    }


    override fun getItemCount(): Int = list.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CompleteAdapter.CompleteViewHolder, position: Int) {
        val item = list[position]
        holder.populate(item)
        Log.d("adapter", item.toString())

        holder.info.visibility = View.GONE
        holder.btnA.visibility = View.GONE
        holder.btnR.visibility = View.GONE
    }
}