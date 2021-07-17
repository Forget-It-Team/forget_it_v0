package com.example.android.forget_it_v0

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.forget_it_v0.adapter.UpcomingAdapter
import com.example.android.forget_it_v0.databinding.FragmentUpcomingRemindersBinding
import com.example.android.forget_it_v0.models.*
import com.example.android.forget_it_v0.repository.FirestoreRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.synnapps.carouselview.CarouselView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class UpcomingRemindersFragment : Fragment(), RecyclerViewOnClick {
    private var size: Int = 0
    private val PERMISSIONS_REQUEST_READ_CONTACTS = 1
    private var upcomingReminderAdapter = UpcomingAdapter(arrayListOf<Pending>(), this)
    private lateinit var upcomingDialog: Dialog
    private lateinit var deadlineDialog: Dialog
    private lateinit var dialogCompleted: Button
    private lateinit var dialogDelete: Button
    private lateinit var dialogYes: Button
    private lateinit var dialogNo: Button
    private lateinit var satisfy_rate_feedback : String
    private lateinit var goals_problems_feedback : String
    private lateinit var helpful_rate : String
    private lateinit var confused_feedback : String
    private lateinit var features_feedback : String
    private lateinit var recommend_friend_rate_feedback : String
    private lateinit var suggestion_feedback : String
    private lateinit var price_willing : String
    private var list: ArrayList<Pending> = arrayListOf()
    private lateinit  var number: String
    //    private var number : String = "9307829766"
    private var auth: FirebaseAuth = Firebase.auth
    private var  phone = auth.currentUser!!.phoneNumber
    var carouselView: CarouselView? = null

//    var sampleImages = intArrayOf(
//        R.drawable.one,
//        R.drawable.two,
//        R.drawable.three,
//        R.drawable.four,
//        R.drawable.five,
//    )

    private lateinit var binding: FragmentUpcomingRemindersBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        Log.i("AUTH HOME", auth.currentUser?.phoneNumber.toString())
        binding.progressCircular.visibility = View.GONE

        size = 0
        list.clear()
        GlobalScope.launch(Dispatchers.Main) {
            addToList()
        }

        GlobalScope.launch(Dispatchers.Main) {
            getPending()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_upcoming_reminders,container,false)

//        binding.carouselView.pageCount = sampleImages.size
//        binding.carouselView.setImageListener { position, imageView ->
//            imageView.setImageResource(sampleImages[position])}

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        FirebaseMessaging.getInstance().subscribeToTopic("all")
        val sub_phone = phone!!.subSequence(3, 13)
        FirebaseMessaging.getInstance().subscribeToTopic(sub_phone as String)




        requestContactPermission()
        initView()
        initRV()
        daysInstalled()



    }
    private fun info(title: String, desc: String, pending: Pending){
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



    private fun daysInstalled() {
        val installed = Date(requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).firstInstallTime)
        val localTime = Date(System.currentTimeMillis())
        val diff = localTime.time - installed.time
        val days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        if(days >= 1) {
            checkFeedbackData()
        }
        return
    }
    private fun checkFeedbackData(){
        val existingfeedback : ArrayList<String> = arrayListOf()
        GlobalScope.launch(Dispatchers.Main) {
            progressShow()
            Firebase.firestore.collection("AlreadyFeedback")
                .get()
                .addOnSuccessListener { data ->
                    for (number in data) {
                        existingfeedback.add(number.getString("number").toString())
                        Log.d("feedbacks", number.getString("number").toString())
                    }
                    if(!(existingfeedback.contains(phone.toString()))){
                        Log.d("feedbacks", "Yes")
                        ratingDialog()
                    }
                }
        }
        return
    }
    private fun ratingDialog(){

        val ask_for_feedback = Dialog(requireActivity())

        ask_for_feedback.setContentView(R.layout.dialog_rate_your_experience)

        val rateItNow = ask_for_feedback.findViewById<Button>(R.id.dialog_rate_it_now)

        val remid_me_later = ask_for_feedback.findViewById<Button>(R.id.dialog_remind_me_later)

        ask_for_feedback.show()
        ask_for_feedback.setCanceledOnTouchOutside(false)
        ask_for_feedback.setCancelable(false)

        rateItNow.setOnClickListener {
            satisfy_rate()
            ask_for_feedback.dismiss()
        }

        remid_me_later.setOnClickListener {
            ask_for_feedback.dismiss()
        }
    }
    private fun satisfy_rate() {
        val satisfy_rate_dialog = Dialog(requireActivity())
        satisfy_rate_dialog.setContentView(R.layout.dialog_satisfy_rating_bar)
        satisfy_rate_dialog.show()
        satisfy_rate_dialog.setCanceledOnTouchOutside(false)
        satisfy_rate_dialog.setCancelable(false)
        val ratingBar = satisfy_rate_dialog.findViewById<RatingBar>(R.id.dialog_satisfy_rating_bar)
        val next_button = satisfy_rate_dialog.findViewById<Button>(R.id.dialog_satify_rating_button_next)
        next_button.setOnClickListener {
            if(!(ratingBar.rating == 0.0f)){
                satisfy_rate_feedback = ratingBar.rating.toString()
                satisfy_rate_dialog.dismiss()
                goals_dialog()
            }
        }
    }

    private fun goals_dialog(){
        val goals_problems_feedback_dialog = Dialog(requireActivity())
        goals_problems_feedback_dialog.setContentView(R.layout.dialog_feedback_goals)
        goals_problems_feedback_dialog.show()
        goals_problems_feedback_dialog.setCanceledOnTouchOutside(false)
        goals_problems_feedback_dialog.setCancelable(false)
        val next_button = goals_problems_feedback_dialog.findViewById<Button>(R.id.dialog_feedback_goal_next)
        val feedBack_goals_problems_text = goals_problems_feedback_dialog.findViewById<EditText>(R.id.dialog_feedback_goal)
        next_button.setOnClickListener {
            if(feedBack_goals_problems_text.text.isEmpty()){
                feedBack_goals_problems_text.error = "Don't leave field empty"
            }else{
                goals_problems_feedback = feedBack_goals_problems_text.text.trim().toString()
                goals_problems_feedback_dialog.dismiss()
                helpful_rate()
            }
        }
    }

    private fun helpful_rate() {
        val helpful_rate_dialog = Dialog(requireActivity())
        helpful_rate_dialog.setContentView(R.layout.dialog_solve_problem_rating_bar)
        helpful_rate_dialog.show()
        helpful_rate_dialog.setCanceledOnTouchOutside(false)
        helpful_rate_dialog.setCancelable(false)
        val ratingBar = helpful_rate_dialog.findViewById<RatingBar>(R.id.dialog_help_solve_problems_rating_bar)
        val next_button = helpful_rate_dialog.findViewById<Button>(R.id.dialog_satify_rating_button_next)
        next_button.setOnClickListener {
            if(!(ratingBar.rating == 0.0f)){
                helpful_rate = ratingBar.rating.toString()
                helpful_rate_dialog.dismiss()
                confused_feedback_dialog()
            }
        }
    }

    private fun confused_feedback_dialog(){
        val confused_feedback_dialog = Dialog(requireActivity())
        confused_feedback_dialog.setContentView(R.layout.dialog_feedback_confused)
        confused_feedback_dialog.show()
        confused_feedback_dialog.setCanceledOnTouchOutside(false)
        confused_feedback_dialog.setCancelable(false)
        val next_button = confused_feedback_dialog.findViewById<Button>(R.id.dialog_feedback_goal_next)
        val feedback_confused_text = confused_feedback_dialog.findViewById<EditText>(R.id.dialog_feedback_confused_text)
        next_button.setOnClickListener {
            if(feedback_confused_text.text.isEmpty()){
                feedback_confused_text.error = "Don't leave field empty"
            }else{
                confused_feedback = feedback_confused_text.text.trim().toString()
                confused_feedback_dialog.dismiss()
                add_features_feedback_dialog()
            }
        }
    }



    private fun add_features_feedback_dialog(){
        val add_features_feedback_dialog = Dialog(requireActivity())
        add_features_feedback_dialog.setContentView(R.layout.dialog_feedback_features)
        add_features_feedback_dialog.show()
        add_features_feedback_dialog.setCanceledOnTouchOutside(false)
        add_features_feedback_dialog.setCancelable(false)
        val next_button = add_features_feedback_dialog.findViewById<Button>(R.id.dialog_feedback_goal_next)
        val feedback_confused_text = add_features_feedback_dialog.findViewById<EditText>(R.id.dialog_feedback_features)
        next_button.setOnClickListener {
            if(feedback_confused_text.text.isEmpty()){
                feedback_confused_text.error = "Don't leave field empty"
            }else{
                features_feedback = feedback_confused_text.text.trim().toString()
                add_features_feedback_dialog.dismiss()
                willing_pay_feedback_dialog()
            }
        }
    }

    private fun willing_pay_feedback_dialog(){
        val willing_pay_feedback_dialog = Dialog(requireActivity())
        willing_pay_feedback_dialog.setContentView(R.layout.dialog_feedback_willing_pay)
        willing_pay_feedback_dialog.show()
        willing_pay_feedback_dialog.setCanceledOnTouchOutside(false)
        willing_pay_feedback_dialog.setCancelable(false)
        val yes = willing_pay_feedback_dialog.findViewById<Button>(R.id.price_willing1)
        val no = willing_pay_feedback_dialog.findViewById<Button>(R.id.price_willing2)
        yes.setOnClickListener {
            price_willing = "Yes"
            willing_pay_feedback_dialog.dismiss()
            recommend_friends_rate()
        }
        no.setOnClickListener {
            price_willing = "No"
            willing_pay_feedback_dialog.dismiss()
            recommend_friends_rate()
        }
    }


    private fun recommend_friends_rate() {
        val satisfy_rate_dialog = Dialog(requireActivity())
        satisfy_rate_dialog.setContentView(R.layout.dialog_feedback_recommend_friend)
        satisfy_rate_dialog.show()
        satisfy_rate_dialog.setCanceledOnTouchOutside(false)
        satisfy_rate_dialog.setCancelable(false)
        val ratingBar = satisfy_rate_dialog.findViewById<RatingBar>(R.id.dialog_satisfy_rating_bar)
        val next_button = satisfy_rate_dialog.findViewById<Button>(R.id.dialog_recommend_rating_button_next)
        next_button.setOnClickListener {
            if(!(ratingBar.rating == 0.0f)){
                recommend_friend_rate_feedback = ratingBar.rating.toString()
                satisfy_rate_dialog.dismiss()
                additional_suggestion_feedback()
            }
        }
    }

    private fun additional_suggestion_feedback() {
        val dditional_suggestion_feedback_dialog = Dialog(requireActivity())
        dditional_suggestion_feedback_dialog.setContentView(R.layout.dialog_feedback_additional_suggestion)
        dditional_suggestion_feedback_dialog.show()
        dditional_suggestion_feedback_dialog.setCanceledOnTouchOutside(false)
        dditional_suggestion_feedback_dialog.setCancelable(false)
        val next_button = dditional_suggestion_feedback_dialog.findViewById<Button>(R.id.dialog_feedback_goal_next)
        val feedback_confused_text = dditional_suggestion_feedback_dialog.findViewById<EditText>(R.id.dialog_feedback_suggestion)
        next_button.setOnClickListener {
            if(feedback_confused_text.text.isEmpty()){
                feedback_confused_text.error = "Don't leave field empty"
            }else{
                suggestion_feedback = feedback_confused_text.text.trim().toString()
                dditional_suggestion_feedback_dialog.dismiss()
                addFeedback()
            }
        }
    }


    private fun addFeedback(){
        val feedbackModel = Feedback_Model(
            phone.toString(),
            satisfy_rate_feedback,
            goals_problems_feedback,
            helpful_rate,
            confused_feedback,
            features_feedback,
            price_willing,
            recommend_friend_rate_feedback,
            suggestion_feedback
        )
        val user = hashMapOf(
            "number" to phone
        )
        progressShow()
        Firebase.firestore.collection("AlreadyFeedback").add(user)
        Firebase.firestore.collection("Feedback").document(phone.toString()).set(feedbackModel)
        progressHide()
        requireActivity().toast("Your feedback is recorded")
    }
    private fun getPending() {
        GlobalScope.launch(Dispatchers.Main) {
            val firestoreRV = Firebase.firestore.collection(number).document("Upcoming")
                .collection("Pending")

            progressShow()

            firestoreRV
                .orderBy("date")
                .get()
                .addOnSuccessListener { reminders ->
                    for (reminder in reminders)
                        size++

                    progressHide()
                    Log.i("SIze IN", size.toString())
                    initBadge()
                }
        }
    }

    private fun initView() {
        number = requireActivity().intent.getStringExtra("number").toString()

        upcomingDialog = Dialog(requireActivity())
        deadlineDialog = Dialog(requireActivity())
        upcomingDialog.setContentView(R.layout.dialog_upcoming_reminders)
        deadlineDialog.setContentView(R.layout.dialog_upcoming_deadline)

        dialogCompleted = upcomingDialog.findViewById(R.id.upcoming_dialog_completed)
        dialogDelete = upcomingDialog.findViewById(R.id.upcoming_dialog_delete)
        dialogYes = deadlineDialog.findViewById(R.id.deadline_dialog_yes)
        dialogNo = deadlineDialog.findViewById(R.id.deadline_dialog_no)

        upcomingDialog.dismiss()
        deadlineDialog.dismiss()
    }

    private fun initBadge() {
//        Log.i("SIze out", size.toString())
//        val unread: String = size.toString()
//
//        when {
//            size == 0 -> activity_home_pending_image_badge_tv.visibility = View.GONE
//            size > 9 -> {
//                activity_home_pending_image_badge_tv.visibility = View.VISIBLE
//                activity_home_pending_image_badge_tv.text = " 9+"
//            }
//            else -> {
//                activity_home_pending_image_badge_tv.visibility = View.VISIBLE
//                activity_home_pending_image_badge_tv.text = " $unread "
//            }
//        }
    }

    private fun initRV() {
        binding.upcomingRv.layoutManager = LinearLayoutManager(requireContext())
        binding.upcomingRv.adapter = upcomingReminderAdapter
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun addToList() {

        var photo: Bitmap = BitmapFactory.decodeResource(
            requireActivity().resources,
            R.drawable.person
        )
        GlobalScope.launch(Dispatchers.Main) {
            val firestoreRV = Firebase.firestore.collection(number).document("Upcoming")
                .collection("Accepted")

            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

            progressShow()
            firestoreRV
                .orderBy("date")
                .get()
                .addOnSuccessListener { reminders ->
                    for (reminder in reminders) {
                        val pending = Pending(
                            "",
                            "",
                            "",
                            LocalDateTime.of(2001, Month.JULY, 29, 10, 10),
                            photo,
                            reminder.id
                        )


                        pending.from = reminder.getString("from").toString()
                        pending.task = reminder.getString("reminder").toString()
                        pending.name = reminder.getString("name").toString()

                        val time: LocalDateTime =
                            LocalDateTime.parse(reminder.getString("date").toString(), formatter)

                        pending.date = time
                        pending.image = photo

                        if (pending.date.isBefore(LocalDateTime.now()))
                            pending.pastDeadline = true

                        list.add(pending)

                    }
                    upcomingReminderAdapter.updateList(list)
                    progressHide()
                }
        }
    }

    private fun requestContactPermission() {
        GlobalScope.launch(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_CONTACTS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_CONTACTS
                        )
                    ) {
                        val builder: android.app.AlertDialog.Builder =
                            android.app.AlertDialog.Builder(requireContext())
                        builder.setTitle("Read Contacts permission")
                        builder.setPositiveButton(android.R.string.ok, null)
                        builder.setMessage("Please enable access to contacts.")
                        builder.setOnDismissListener {
                            requestPermissions(
                                arrayOf(Manifest.permission.READ_CONTACTS),
                                PERMISSIONS_REQUEST_READ_CONTACTS
                            )
                        }
                        builder.show()
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(), arrayOf(Manifest.permission.READ_CONTACTS),
                            PERMISSIONS_REQUEST_READ_CONTACTS
                        )
                    }
                } else {
//                    home_constraint.visibility = View.VISIBLE
//                    home_warning.visibility = View.GONE
//                    home_rv.visibility = View.VISIBLE
//                    home_upcoming_text.visibility = View.VISIBLE
                }
            } else {
                requireActivity().toast("Permission to read contacts has been granted")
//                home_constraint.visibility = View.VISIBLE
//                home_warning.visibility = View.GONE
//                home_rv.visibility = View.VISIBLE
//                home_upcoming_text.visibility = View.VISIBLE
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        GlobalScope.launch(Dispatchers.Main) {
            when (requestCode) {
                PERMISSIONS_REQUEST_READ_CONTACTS -> {
                    if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    ) {
//                        home_constraint.visibility = View.VISIBLE
//                        home_warning.visibility = View.GONE
//                        home_rv.visibility = View.VISIBLE
//                        home_upcoming_text.visibility = View.VISIBLE
                    } else {
                        requireContext().toast("Please do not deny, otherwise you won't be able to use the app and connect with your friends")
//                        home_constraint.visibility = View.GONE
//                        home_warning.visibility = View.VISIBLE
//                        home_rv.visibility = View.GONE
//                        home_upcoming_text.visibility = View.GONE
                    }
                }
            }
        }
    }


    override fun onClick(v: View, pending: Pending) {
        if (v.id == R.id.infoUpcoming_rv) {
            val remin = pending.task.split(";").toTypedArray()
            val title = remin[0]
            val desc = remin[1]
            info(title,desc,pending)
            Log.i("ifelse","if else is working")

        } else {
            if (pending.pastDeadline) {
                if (v.id == R.id.rv_markDone) {
                    onYes(pending)
                } else {
                    onNo(pending)
                }
            } else {
                if (v.id == R.id.rv_markDone) {
                    onCompleted(pending)
                } else {
                    onDelete(pending)
                }
            }
        }
    }

    private fun onYes(pending: Pending) {
        progressShow()
        FirestoreRepo.swapData("Accepted", "Completed", pending.from, number, pending)
        if(AudioPlayer.isPlaying()) {
            AudioPlayer.stopAudio()
        }
        progressHide()
        requireActivity().toast("You have completed this reminder. Congrats!")

        list.remove(pending)
        upcomingReminderAdapter.updateList(list)
        deadlineDialog.dismiss()
    }

    private fun onNo(pending: Pending) {
        progressShow()
        FirestoreRepo.swapData("Accepted", "Incomplete", pending.from, number, pending)
        progressHide()
        if(AudioPlayer.isPlaying()) {
            AudioPlayer.stopAudio()
        }
        requireActivity().toast("You have not completed this task. Better luck next time!")

        list.remove(pending)
        upcomingReminderAdapter.updateList(list)
        deadlineDialog.dismiss()
    }

    private fun onDelete(pending: Pending) {
        progressShow()
        FirestoreRepo.swapData("Accepted", "Deleted", pending.from, number, pending)
        progressHide()

        requireActivity().toast("You have deleted this task. Better luck next time!")

        list.remove(pending)
        upcomingReminderAdapter.updateList(list)
        upcomingDialog.dismiss()
    }

    private fun onCompleted(pending: Pending) {
        progressShow()
        FirestoreRepo.swapData("Accepted", "Completed", pending.from, number, pending)
        progressHide()

        requireActivity().toast("You have completed this task. Well done!")

        list.remove(pending)
        upcomingReminderAdapter.updateList(list)
        upcomingDialog.dismiss()
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