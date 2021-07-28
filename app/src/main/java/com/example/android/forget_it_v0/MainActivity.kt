package com.example.android.forget_it_v0

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import com.example.android.forget_it_v0.adapter.ViewPagerAdapter
import com.example.android.forget_it_v0.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    lateinit var number :String
    lateinit var auth : FirebaseAuth
    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        setUpTabs()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        auth = Firebase.auth
        number = auth.currentUser!!.phoneNumber.toString()
        binding.imageButton.setOnClickListener {
            menuButton()
        }

    }
    private fun menuButton() {
        val menu_dialog = Dialog(this)
        menu_dialog.setContentView(R.layout.menu_dialog)
        menu_dialog.show()
        menu_dialog.setCanceledOnTouchOutside(false)
        menu_dialog.setCancelable(false)
        val sign_out_button = menu_dialog.findViewById<CardView>(R.id.signOutCV)
        val report_issue_button = menu_dialog.findViewById<CardView>(R.id.reportIssueCV)
        sign_out_button.setOnClickListener {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener { // user is now signed out
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            report_issue_button.setOnClickListener{
                val intent = Intent(this , ReportActivity::class.java)
                intent.putExtra("number", number)
                startActivity(intent)
            }
        }
    }
    private fun setUpTabs() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(SendRemindersFragment(),"")
        adapter.addFragment(UpcomingRemindersFragment(), "")
        adapter.addFragment(PendingRemindersFragment(), "")


        binding.viewPager.adapter = adapter
        binding.tabs.setupWithViewPager(binding.viewPager)
        binding.tabs.getTabAt(0)!!.setIcon(R.drawable.sendicon)
        binding.tabs.getTabAt(1)!!.setIcon(R.drawable.listicon)
        binding.tabs.getTabAt(2)!!.setIcon(R.drawable.chooseicon)

    }


}