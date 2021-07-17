package com.example.android.forget_it_v0

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.forget_it_v0.adapter.PendingAdapter
import com.example.android.forget_it_v0.databinding.FragmentPendingRemindersBinding
import com.example.android.forget_it_v0.models.Pending
import com.example.android.forget_it_v0.models.RecyclerViewOnClickPending
import com.example.android.forget_it_v0.models.toast
import com.example.android.forget_it_v0.repository.FirestoreRepo
import com.example.android.forget_it_v0.models.MyAlarm
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class PendingRemindersFragment : Fragment() , RecyclerViewOnClickPending {

    private var alarmCount: Int = 0
    private lateinit var pendingAdapter: PendingAdapter
    private var pendingList: ArrayList<Pending> = arrayListOf()
    private lateinit var rejectDialog: Dialog
    private lateinit var dialogYes: Button
    private lateinit var dialogNo: Button
    private lateinit var number: String
    private lateinit var id: String
    private var hashMap: HashMap<String, String> = HashMap()
    private lateinit var sharedPref: SharedPreferences
    private var auth : FirebaseAuth = Firebase.auth
    private lateinit var photo: Bitmap
    private lateinit var binding : FragmentPendingRemindersBinding


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_pending_reminders,container,false)
        initSharedPref()
        initLayout()
        initRV()
        contacts()

        auth = Firebase.auth
        return binding.root
    }
    private fun info(pending: Pending){
        val remin = pending.task.split(";").toTypedArray()
        val title = remin[0]
        val desc = remin[1]
        val info =  Dialog(requireActivity())
        info.setContentView(R.layout.info)
        info.findViewById<TextView>(R.id.infoTitle).text = title
        info.findViewById<TextView>(R.id.infoDesc).text = desc
        info.show()
        val backBtn = info.findViewById<CardView>(R.id.backButton)
        backBtn.setOnClickListener{
            info.dismiss()
        }
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        initSharedPref()
//        initLayout()
//        initRV()
//        contacts()
//    }

    private fun initSharedPref() {
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        alarmCount = sharedPref.getInt("alarmCount", 0)
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
        addData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadSharedPref() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("pending contact", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = sharedPreferences.getString("pending contact list", null)
        val type = object : TypeToken<HashMap<String, String>>() {}.type

        if (json == null)
            contacts()
        else {
            Log.i("pending contacct", json)
            Log.i("pending contact", "Deep inside load Shared")
            hashMap = gson.fromJson(json, type)
            Log.i("pending contact", hashMap.toString())
            addData()
            binding.progressCircular.visibility = View.GONE
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
        hashMap[number] = "Myself!"
    }

    private fun initLayout() {
        auth = Firebase.auth
        number = auth.currentUser!!.phoneNumber!!.subSequence(3, 13).toString()

        rejectDialog = Dialog(requireActivity())
        rejectDialog.setContentView(R.layout.dialog_pending_reject)

        dialogYes = rejectDialog.findViewById(R.id.reject_dialog_yes)
        dialogNo = rejectDialog.findViewById(R.id.reject_dialog_no)

        rejectDialog.dismiss()
    }

    private fun initRV() {
        pendingAdapter = PendingAdapter(pendingList, this)
        binding.pendingRv.layoutManager = LinearLayoutManager(requireActivity())
        binding.pendingRv.adapter = pendingAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addData() {
        Log.i("pending contact", "inside add data")
        var photo: Bitmap = BitmapFactory.decodeResource(
            requireActivity().resources,
            R.drawable.person
        )

        GlobalScope.launch(Dispatchers.Main) {
            progressShow()
            val firestoreRV = Firebase.firestore.collection(number)
                .document("Upcoming")
                .collection("Pending")

            firestoreRV
                .orderBy("date")
                .get()
                .addOnSuccessListener { reminders ->

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
                        pendingList.add(pending)
                    }
                    pendingAdapter.notifyDataSetChanged()
                    progressHide()
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View, pending: Pending) {

        when (view.id) {
            R.id.infoButtonPending -> info(pending)
            R.id.pending_rv_reject -> onReject(pending)
            R.id.pending_rv_accept -> onAccept(pending)
        }

    }

    private fun onReject(pending: Pending) {
        rejectDialog.show()
        dialogYes.setOnClickListener {
            progressShow()
            FirestoreRepo.swapData("Pending", "Rejected", pending.from, number, pending)
            progressHide()

            pendingList.remove(pending)
            pendingAdapter.notifyDataSetChanged()
            rejectDialog.dismiss()
        }

        dialogNo.setOnClickListener {
            rejectDialog.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onAccept(pending: Pending) {
        progressShow()
        FirestoreRepo.swapData("Pending", "Accepted", pending.from, number, pending)

        pendingList.remove(pending)
        pendingAdapter.notifyDataSetChanged()

        //adds an alarm

        progressShow()

        val calendar: Calendar = Calendar.getInstance()
        calendar.set(
            pending.date.year,
            pending.date.monthValue - 1,
            pending.date.dayOfMonth,
            pending.date.hour,
            pending.date.minute,
            0
        )

        Log.i("HERE", "onaccept")

        setAlarmCode(calendar.timeInMillis)
        progressHide()
    }

    private var alarmManager = arrayOfNulls<AlarmManager>(1000)

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun setAlarmCode(timeInMillis: Long) {

        Log.i("HERE", "setalarm")

        alarmManager[alarmCount] = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.i("Test emulator", alarmManager[alarmCount].toString())
        val intent = Intent(requireContext(), MyAlarm::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(requireActivity(), alarmCount, intent, 0)

        alarmManager[alarmCount]?.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.i("Test emulator time", alarmManager[alarmCount]?.nextAlarmClock.toString())
        }

        requireActivity().toast("Alarm is set")

        alarmCount++
        if (alarmCount == 999)
            alarmCount = 0

        with(sharedPref.edit()) {
            putInt("alarmCount", alarmCount)
            commit()
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