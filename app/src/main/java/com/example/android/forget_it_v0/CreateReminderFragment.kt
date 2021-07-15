package com.example.android.forget_it_v0

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.android.forget_it_v0.databinding.FragmentCreateReminderBinding
import com.example.android.forget_it_v0.models.MyAlarm
import com.example.android.forget_it_v0.models.Pending
import com.example.android.forget_it_v0.models.SentReminder
import com.example.android.forget_it_v0.models.toast
import com.example.android.forget_it_v0.repository.FirestoreRepo
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class CreateReminderFragment : Fragment() {

    private lateinit var name: String
    private lateinit var number: String
    private lateinit var myNumber: String
    private val auth = Firebase.auth
    private var alarmCount: Int = 0
    private lateinit var sharedPref: SharedPreferences
    private val phone = auth.currentUser!!.phoneNumber
    private val sub_phone = phone!!.subSequence(3, 13)


    private lateinit var binding : FragmentCreateReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_create_reminder,container,false)
        // Inflate the layout for requireActivity() fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = CreateReminderFragmentArgs.fromBundle(requireArguments())
        name = args.name
        number = args.number
        myNumber = args.myNumber

        initSharedPref()

        binding.activityCreateNameEt.setText(name)
        binding.activityCreateDateButton.setOnClickListener{
            selectDate()
        }

        binding.activityCreateTimeButton.setOnClickListener {
            selectTime()
        }
        binding.activityCreateSendButton.setOnClickListener {
            sendReminder()
        }


    }


    private fun initSharedPref() {
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE) ?: return
        alarmCount = sharedPref.getInt("alarmCount", 0)
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendReminder() {
        if (binding.activityCreateTitleEt.text.toString()
                .isEmpty() || binding.activityCreateDateEt.text.toString()
                .isEmpty() || binding.activityCreateTimeEt.text.toString().isEmpty()
        ) {
            Toast.makeText(requireActivity(), "Don't leave required fields empty!", Toast.LENGTH_SHORT).show()
            return
        }
        var photo: Bitmap = BitmapFactory.decodeResource(
            requireActivity().resources,
            R.drawable.person
        )
        if (binding.activityCreateDescriptionEt.text.toString().isEmpty())
            binding.activityCreateDescriptionEt.setText("")

        val time: String = binding.activityCreateTimeEt.text.toString()
        val date: String = binding.activityCreateDateEt.text.toString()
        val dateTime = "$date $time"

        val reminder = SentReminder(
            name,
            number,
            binding.activityCreateTitleEt.text.toString(),
            binding.activityCreateDescriptionEt.text.toString(),
            dateTime,
            photo
        )

        GlobalScope.launch(Dispatchers.Main) {
            binding.progressCircular.visibility = View.VISIBLE
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            val title = reminder.title
            val to = reminder.to

            val firestorePendingSender = Firebase.firestore.collection(myNumber)
                .document("Sent")
                .collection("Pending")

            val firestorePending = Firebase.firestore.collection(number)
                .document("Upcoming")
                .collection("Pending")

            val reminderFinal = reminder.title + "\n" + reminder.reminder

            val dataSender = hashMapOf(
                "date" to reminder.date,
                "name" to reminder.name,
                "reminder" to reminderFinal,
                "to" to number
            )

            val dataReceiver = hashMapOf(
                "date" to reminder.date,
                "name" to "<Unknown>",
                "reminder" to reminderFinal,
                "from" to myNumber,
                "pastDeadline" to false
            )

            if(to != sub_phone) {
                firestorePendingSender
                    .add(dataSender)
                    .addOnSuccessListener { reminder ->
                        firestorePending
                            .document(reminder.id)
                            .set(dataReceiver)
                            .addOnSuccessListener { reminder ->
                                Log.i("Added reminder", "To receiver")
                                binding.progressCircular.visibility = View.GONE
                                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                Toast.makeText(
                                    requireActivity(),
                                    "Reminder sent",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                getContactName(title, to, sub_phone as String)
                                view?.findNavController()?.navigate(R.id.action_createReminderFragment_to_upcomingRemindersFragment)
                            }
                    }
            }else{

                progressShow()

                val dataSender = hashMapOf(
                    "date" to reminder.date,
                    "name" to reminder.name,
                    "reminder" to reminderFinal,
                    "to" to number
                )

                val dataReceiver = hashMapOf(
                    "date" to reminder.date,
                    "name" to "MySelf!",
                    "reminder" to reminderFinal,
                    "from" to myNumber,
                    "pastDeadline" to false
                )

                var id : String = ""
                val formatter: DateTimeFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

                val time: LocalDateTime =
                    LocalDateTime.parse(reminder.date, formatter)

                var photo: Bitmap = BitmapFactory.decodeResource(
                    requireActivity().resources,
                    R.drawable.person
                )

                firestorePendingSender
                    .add(dataSender)
                    .addOnSuccessListener { reminder ->
                        id = reminder.id
                        firestorePending
                            .document(reminder.id)
                            .set(dataReceiver)
                            .addOnSuccessListener { reminder ->
                                val pending = Pending(
                                    myNumber,
                                    "MySelf!",
                                    reminderFinal,
                                    time,
                                    photo,
                                    id,
                                    false
                                )
                                FirestoreRepo.swapData("Pending", "Accepted", pending.from, number, pending)
                                val calendar: Calendar = Calendar.getInstance()
                                calendar.set(
                                    time.year,
                                    time.monthValue - 1,
                                    time.dayOfMonth,
                                    time.hour,
                                    time.minute,
                                    0
                                )

                                Log.i("HERE", "onaccept")

                                setAlarmCode(calendar.timeInMillis)
                                progressHide()
//                                val intent = Intent(requireActivity(), HomeActivity::class.java)
//                                startActivity(intent)
//                                finish()
                                view?.findNavController()?.navigate(R.id.action_createReminderFragment_to_upcomingRemindersFragment)
                            }
                    }


//                val dataReceiver = hashMapOf(
//                    "date" to reminder.date,
//                    "name" to "Myself!",
//                    "reminder" to reminderFinal,
//                    "from" to myNumber,
//                    "pastDeadline" to false
//                )
//                val firestore_mySelf = Firebase.firestore.collection(number)
//                    .document("Upcoming")
//                    .collection("Accepted")
//                    .add(dataReceiver)
//                    .addOnSuccessListener {
//                        Toast.makeText(applicationContext, "Reminder Set", Toast.LENGTH_SHORT).show()
//                    }
//
//                val calendar: Calendar = Calendar.getInstance()
//                calendar.set(
//                    time.year,
//                    time.monthValue - 1,
//                    time.dayOfMonth,
//                    time.hour,
//                    time.minute,
//                    0
//                )
//
//                Log.i("HERE", "onaccept")
//
//                setAlarmCode(calendar.timeInMillis)
//                progressHide()
//                finish()
            }

        }
    }

    private var alarmManager = arrayOfNulls<AlarmManager>(1000)

    private fun setAlarmCode(timeInMillis: Long) {

        Log.i("HERE", "setalarm")

        alarmManager[alarmCount] = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.i("Test emulator", alarmManager[alarmCount].toString())
        val intent = Intent(requireActivity(), MyAlarm::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(requireActivity(), alarmCount, intent, 0)

        alarmManager[alarmCount]?.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        Log.i("Test emulator time", alarmManager[alarmCount]?.nextAlarmClock.toString())

        requireActivity().toast("Alarm is set")

        alarmCount++
        if (alarmCount == 999)
            alarmCount = 0

        with(sharedPref.edit()) {
            putInt("alarmCount", alarmCount)
            commit()
        }

    }


    private fun getContactName(title: String, to: String, from: String){
        var name :String = ""
        Firebase.firestore
            .collection("Contacts")
            .document(to)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val document = it.getResult()
                    if(document != null && document.exists()) {
                        val list: HashMap<*, *> = document.get("list") as HashMap<*, *>
                        if (list[from].toString() != "null") {
                            name = list[from].toString()
                            sendRemainderNotification(title, to, from, name)
                        } else {
                            name = from
                            sendRemainderNotification(title, to, from, name)
                        }
                    }
                }
            }
    }
    fun sendRemainderNotification(title : String , to: String, from : String, name: String){

        val queue = Volley.newRequestQueue(requireActivity())

        val url = "https://fcm.googleapis.com/fcm/send"

        val jsonObject = JSONObject()

        jsonObject.put("to", "/topics/$to")

        val notification = JSONObject()

        notification.put("title", "Reminder request from $name")

        notification.put("body", title)

        jsonObject.put("notification", notification)

        // Post Request a json data to  provided URL.
        val jsonObjectRequest = object : JsonObjectRequest(

            Request.Method.POST, url, jsonObject,

            { response ->

                // Get your json response and convert it to whatever you want.
            },
            { error ->
                requireActivity().toast(error.message.toString())
            }
        ) {
            //            passing headers
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"
//                Firebase cloud messaging server key
                headers["Authorization"] = "key=AAAAA4L5_Yk:APA91bEQlDf0Y4WU50Q2MZwhnTRQK1HhtFBxYiJJVcVlhpIE3DMfkhYqAAQIpZjWQI6Ve6nx8aEAtrFWQDlrp0GIpJfRa8X1BtwjquwmDNnHSL0jwAo4Ifal4LM16aEM8phit5POK2hW"
                return headers
            }
        }

// Add the request to the RequestQueue.
        queue.add(jsonObjectRequest)
        return
    }



    private fun selectTime() {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            binding.activityCreateTimeEt.setText(SimpleDateFormat("HH:mm").format(cal.time))
        }
        TimePickerDialog(
            requireActivity(),
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun selectDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireActivity(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                var monthString: String = (monthOfYear + 1).toString()
                var dayString: String = dayOfMonth.toString()

                if (monthOfYear < 9)
                    monthString = "0$monthString"

                if (dayOfMonth < 10)
                    dayString = "0$dayOfMonth"

                binding.activityCreateDateEt.setText("$year-${monthString}-$dayString")
            },
            year,
            month,
            day
        ).show()
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