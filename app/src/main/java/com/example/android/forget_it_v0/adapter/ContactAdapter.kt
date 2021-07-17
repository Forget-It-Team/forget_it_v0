package com.example.android.forget_it_v0.adapter

import android.graphics.Color
import android.media.Image
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.forget_it_v0.PhoneNumberFragmentDirections
import com.example.android.forget_it_v0.R
import com.example.android.forget_it_v0.models.Contact
import com.example.android.forget_it_v0.models.RecyclerViewOnClickContact
import pl.droidsonroids.gif.GifImageView

import kotlin.collections.ArrayList
import kotlin.random.Random



class ContactAdapter(var list : ArrayList<Contact>,var listener : RecyclerViewOnClickContact) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>(),
    Filterable {

    var listFull : ArrayList<Contact> = arrayListOf()
    init {
        listFull = list
    }


    inner class ContactViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var name : TextView = itemView.findViewById(R.id.rv_contact_name)
        var number : TextView = itemView.findViewById(R.id.rv_contact_number)
        var button : CardView = itemView.findViewById(R.id.rv_contact_button)
        var buttonText: TextView = itemView.findViewById(R.id.buttonText)

        var image: GifImageView = itemView.findViewById(R.id.gifPerson)
        val personGif : IntArray = intArrayOf(
            R.drawable.persongifone,
            R.drawable.persongiftwo,
            R.drawable.persongifthree,
            R.drawable.persongiffour,
            R.drawable.persongiffive,
            R.drawable.persongifsix,
        )
        val random = Random





        fun populate(contact : Contact){
            name.text = contact.name
            number.text = contact.number
            buttonText.text = contact.buttonText
            image.setImageResource(personGif[random.nextInt(personGif.size)])



            Log.i("button", buttonText.text.toString())

            if(buttonText.text.toString() == "Send Reminder"){
                button.visibility = View.VISIBLE
            }

            else {
                button.visibility = View.VISIBLE
                buttonText.text = "Invite\nContact"
            }
        }
    }
    fun update(newList : ArrayList<Contact>){
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context).inflate(R.layout.contact_rv, parent, false)
        return ContactViewHolder(inflater)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val item = list[position]
        holder.populate(item)

        holder.button.setOnClickListener{
            listener.onClick(holder.buttonText.text.toString(), list[position])
//            if(holder.button.text.toString() == "Send Reminder"){
////                view?.findNavController()?.navigate(SendRemindersFragmentDirections.actionSendRemindersFragmentToCreateReminderFragment(item.name,item.number,item.buttonText))
//            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString().toLowerCase()
                list = if (charSearch.isEmpty()) {
                    listFull
                } else {
                    val resultList = ArrayList<Contact>()
                    for (row in listFull) {
                        if (row.name.toLowerCase().contains(charSearch) || row.number.contains(charSearch)) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = list
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                list = results?.values as ArrayList<Contact>
                notifyDataSetChanged()
            }

        }
    }


}