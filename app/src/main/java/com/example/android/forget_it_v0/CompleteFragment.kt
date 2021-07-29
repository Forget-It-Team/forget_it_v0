package com.example.android.forget_it_v0

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
import com.example.android.forget_it_v0.adapter.CompleteAdapter
import com.example.android.forget_it_v0.adapter.PendingAdapter
import com.example.android.forget_it_v0.databinding.FragmentCompleteBinding
import com.example.android.forget_it_v0.models.Pending
import com.example.android.forget_it_v0.models.RecyclerViewOnClickPending
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

class CompleteFragment : Fragment() , RecyclerViewOnClickPending {

    private var completedList: ArrayList<Pending> = arrayListOf()
    private var uncompletedList: ArrayList<Pending> = arrayListOf()
    private lateinit var binding : FragmentCompleteBinding
    private var hashMap: HashMap<String, String> = HashMap()
    private lateinit var photo: Bitmap

    private val auth = Firebase.auth
    private var uncompletedAdapter: CompleteAdapter = CompleteAdapter(uncompletedList, this)
    private var completedAdapter: CompleteAdapter = CompleteAdapter(completedList, this)
    private val phone : String = auth.currentUser!!.phoneNumber!!.subSequence(3,13).toString()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_complete,container,false)
        binding.completedRV.layoutManager = LinearLayoutManager(requireActivity())
        binding.completedRV.adapter = completedAdapter

        binding.unCompletedRV.layoutManager = LinearLayoutManager(requireActivity())
        binding.unCompletedRV.adapter = uncompletedAdapter
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        initRV()
        contacts()
    }


    private fun initRV() {
        completedAdapter = CompleteAdapter(completedList, this)
        binding.completedRV.layoutManager = LinearLayoutManager(requireActivity())
        binding.completedRV.adapter = completedAdapter
        uncompletedAdapter = CompleteAdapter(uncompletedList, this)
        binding.unCompletedRV.layoutManager = LinearLayoutManager(requireContext())
        binding.completedRV.adapter = uncompletedAdapter
    }


    private fun completed(){
        Log.i("pending contact", "inside add data")
        var photo: Bitmap = BitmapFactory.decodeResource(
            requireActivity().resources,
            R.drawable.person
        )

        GlobalScope.launch(Dispatchers.Main) {
            progressShow()
            val firestoreRV = Firebase.firestore.collection(phone)
                .document("Upcoming")
                .collection("Completed")

            firestoreRV
                .orderBy("date")
                .get()
                .addOnSuccessListener { reminders ->
                    completedList.clear()
                    val formatter: DateTimeFormatter =
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                    for (reminder in reminders) {
                        Log.d("uncompleted", reminder.data.toString())
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
                    completedAdapter.update(completedList)
                    Log.d("uncompleted", completedList.toString())
                    progressHide()
                }
        }
    }

    private fun uncompleted(){
        Log.i("pending contact", "inside add data")
        var photo: Bitmap = BitmapFactory.decodeResource(
            requireActivity().resources,
            R.drawable.person
        )

        GlobalScope.launch(Dispatchers.Main) {
            progressShow()
            val firestoreRV = Firebase.firestore.collection(phone)
                .document("Upcoming")
                .collection("Incompleted")

            firestoreRV
                .orderBy("date")
                .get()
                .addOnSuccessListener { reminders ->
                    uncompletedList.clear()
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
                        uncompletedList.add(pending)
                    }
                    uncompletedAdapter.notifyDataSetChanged()
                    progressHide()
                    Log.d("uncompleted", uncompletedList.toString() + phone)
                }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun contacts(){
        photo = BitmapFactory.decodeResource(
            requireActivity().resources,
            R.drawable.person
        )
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
    private fun writeSharedPref() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("pending contact", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val gson = Gson()
        val json: String = gson.toJson(hashMap)
        editor.putString("pending contact list", json)
        editor.apply()
        completed()
        uncompleted()
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




    override fun onClick(v: View, pending: Pending) {
        TODO("Not yet implemented")
    }
}