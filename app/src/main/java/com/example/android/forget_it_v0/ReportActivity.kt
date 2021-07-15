package com.example.android.forget_it_v0

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.android.forget_it_v0.databinding.ActivityReportBinding
import com.example.android.forget_it_v0.models.toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ReportActivity : AppCompatActivity() {

    private lateinit var binding : ActivityReportBinding
    private lateinit var number: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_report)
        number = intent.getStringExtra("number").toString()

        binding.activityReportSubmitBtn.setOnClickListener {
            onSubmit()
        }

    }

    private fun onSubmit(){
        if(binding.activityReportEmailEt.text.isNullOrBlank() || binding.activityReportIssueEt.text.isNullOrBlank()){
            this.toast("Don't leave any field empty")
            return
        }

        val firestoreIssues = Firebase.firestore

        val issue = hashMapOf(
            "number" to number,
            "email" to binding.activityReportEmailEt.text.toString(),
            "issue" to binding.activityReportIssueEt.text.toString()
        )

        firestoreIssues
            .collection("Issues")
            .add(issue)
            .addOnSuccessListener { documentReference ->
                Log.i("Issue", documentReference.id)
                this.toast("Issue has been reported. Thank you for your time.")
                clearFields()
                finish()
            }
            .addOnFailureListener { e->
                Log.w("Issue", "Error", e)
                this.toast("There was some error. Please try again later.\nError:$e" )
                clearFields()
            }


    }

    private fun clearFields(){
        binding.activityReportEmailEt.setText("")
        binding.activityReportIssueEt.setText("")
        binding.activityReportEmailEt.isFocusable
    }
}