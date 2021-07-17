package com.example.android.forget_it_v0

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.databinding.DataBindingUtil
import com.example.android.forget_it_v0.databinding.ActivityCreateReminderBinding
import com.example.android.forget_it_v0.models.MyAlarm
import com.example.android.forget_it_v0.models.Pending
import com.example.android.forget_it_v0.models.SentReminder
import com.example.android.forget_it_v0.models.toast
import com.example.android.forget_it_v0.repository.FirestoreRepo

class CreateReminderActivity : AppCompatActivity() , View.OnClickListener{

    private lateinit var name: String
    private lateinit var number: String
    private lateinit var myNumber: String
    private val auth = Firebase.auth
    private var alarmCount: Int = 0
    private lateinit var sharedPref: SharedPreferences
    private val phone = auth.currentUser!!.phoneNumber
    val sub_phone = phone!!.subSequence(3, 13)

    private lateinit var binding : ActivityCreateReminderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_reminder)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_create_reminder)
        initView()
        initSharedPref()
        attachOnClickListeners()
    }

    private fun initSharedPref() {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        alarmCount = sharedPref.getInt("alarmCount", 0)
    }

    private fun initView() {
        name = intent.getStringExtra("name").toString()
        number = intent.getStringExtra("number").toString()
        myNumber = intent.getStringExtra("myNumber").toString()
        binding.activityCreateNameEt.setText(name)
    }

    private fun attachOnClickListeners() {
        binding.activityCreateDateButton.setOnClickListener(this)
        binding.activityCreateTimeButton.setOnClickListener(this)
        binding.activityCreateSendButton.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.activity_create_date_button -> selectDate()
            R.id.activity_create_time_button -> selectTime()
            R.id.activity_create_send_button -> sendReminder()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendReminder() {
        if (binding.activityCreateTitleEt.text.toString()
                .isEmpty() || binding.activityCreateDateEt.text.toString()
                .isEmpty() || binding.activityCreateTimeEt.text.toString().isEmpty()
        ) {
            Toast.makeText(this, "Don't leave required fields empty!", Toast.LENGTH_SHORT).show()
            return
        }
        var photo: Bitmap = BitmapFactory.decodeResource(
            applicationContext.resources,
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
            window.setFlags(
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
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                Toast.makeText(
                                    applicationContext,
                                    "Reminder sent",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                getContactName(title, to, sub_phone as String)
                                finish()
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
                    applicationContext.resources,
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
                                finish()
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

        alarmManager[alarmCount] = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        Log.i("Test emulator", alarmManager[alarmCount].toString())
        val intent = Intent(this, MyAlarm::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, alarmCount, intent, 0)

        alarmManager[alarmCount]?.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)

        Log.i("Test emulator time", alarmManager[alarmCount]?.nextAlarmClock.toString())

        this.toast("Alarm is set")

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

        val queue = Volley.newRequestQueue(this)

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
                this.toast(error.message.toString())
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
            this,
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
            this,
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
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun progressHide() {
        binding.progressCircular.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}