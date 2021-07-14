package com.example.android.forget_it_v0.adapter

import android.graphics.Color
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.forget_it_v0.PhoneNumberFragmentDirections
import com.example.android.forget_it_v0.R
import com.example.android.forget_it_v0.SendRemindersFragmentDirections
import com.example.android.forget_it_v0.models.Contact

class ContactAdapter(var list : ArrayList<Contact>,var view: View) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>(),
    Filterable {

    var listFull : ArrayList<Contact> = arrayListOf()
    init {
        listFull = list
    }


    inner class ContactViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var name : TextView = itemView.findViewById(R.id.rv_contact_name)
        var number : TextView = itemView.findViewById(R.id.rv_contact_number)
        var button : Button = itemView.findViewById(R.id.rv_contact_button)
//        var image : ImageView = itemView.findViewById(R.id.rv_contact_image)

        fun populate(contact : Contact){
            name.text = contact.name
            number.text = contact.number
            button.text = contact.buttonText
//            image.setImageBitmap(contact.image)

            Log.i("button", button.text.toString())

            if(button.text.toString() == "Set a Reminder"){
                button.visibility = View.VISIBLE
                button.setBackgroundColor(Color.parseColor("#020894"))
            }

            else {
                button.visibility = View.VISIBLE
                button.text = "Invite Contact"
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
            if(holder.button.text.toString() == "Set a Reminder"){
                view?.findNavController()?.navigate(SendRemindersFragmentDirections.actionSendRemindersFragmentToCreateReminderFragment(item.name,item.number,item.buttonText))
            }
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