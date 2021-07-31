package com.example.android.forget_it_v0.repository

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.forget_it_v0.R
import com.example.android.forget_it_v0.adapter.EndAdapter
import com.example.android.forget_it_v0.databinding.FragmentCompleteBinding
import com.example.android.forget_it_v0.models.Pending
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter

class EndFragment : Fragment() {
    private var completedList: ArrayList<Pending> = arrayListOf()
    private var uncompletedList: ArrayList<Pending> = arrayListOf()
    private lateinit var binding : FragmentCompleteBinding
    private var hashMap: HashMap<String, String> = HashMap()
    private lateinit var photo: Bitmap

    private val auth = Firebase.auth
    private lateinit var endAdapter: EndAdapter
    private val phone : String = auth.currentUser!!.phoneNumber!!.subSequence(3,13).toString()





    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_complete,container,false)
        initRV()
        contacts()
        return binding.root
    }

    private fun initRV() {
        Log.d("EndFragment","initRV called")
        endAdapter = EndAdapter(completedList,this)
        binding.completedRV.layoutManager = LinearLayoutManager(requireContext())
        binding.completedRV.adapter = endAdapter
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun writeSharedPref() {
        Log.d("CompleteActivity","writeSharedPref called")
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("pending contact", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val gson = Gson()
        val json: String = gson.toJson(hashMap)
        editor.putString("pending contact list", json)
        editor.apply()
        addData()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun contacts(){
        val cursor = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (cursor?.moveToNext() == true) {
            val name: String =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phoneNo: String =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            Log.d("contact", "$name  $phoneNo")
            phoneNo = phoneNo.replace(" ", "")
            phoneNo = phoneNo.replace("+91", "")
            if (!hashMap.containsKey(phoneNo)) {
                hashMap[phoneNo] = name
            }
        }
        writeSharedPref()
        progressHide()
        hashMap[phone] = "Myself!"
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private fun addData() {
        GlobalScope.launch(Dispatchers.Main) {
            progressShow()
            val firestoreRV = Firebase.firestore.collection(phone)
                .document("Upcoming")
                .collection("Pending")

            firestoreRV
                .orderBy("date")
                .get()
                .addOnSuccessListener { reminders ->
                    completedList.clear()
                    val formatter: DateTimeFormatter =
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                    for (reminder in reminders) {
                        var pending =
                            Pending(
                                "",
                                "",
                                "",
                                LocalDateTime.of(2001, Month.JULY, 29, 10, 10),
                                photo,
                                reminder.id
                            )

                        pending.from = reminder.getString("from").toString()
                        pending.task = reminder.getString("reminder").toString()
                        if (hashMap.containsKey(pending.from)) {
                            pending.name = hashMap[pending.from].toString()

                            firestoreRV
                                .document(reminder.id)
                                .update("name", pending.name)
                                .addOnSuccessListener {
                                    Log.i("Done", "Updating")
                                }
                        } else
                            pending.name = reminder.getString("name").toString()

                        val time: LocalDateTime =
                            LocalDateTime.parse(reminder.getString("date").toString(), formatter)

                        pending.date = time
                        pending.image = photo
                        completedList.add(pending)
                    }
                    endAdapter.notifyDataSetChanged()
                    progressHide()
                }
        }
    }
    private fun progressShow() {
        binding.progressCircular.visibility = View.VISIBLE
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun progressHide() {
        binding.progressCircular.visibility = View.GONE
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

}