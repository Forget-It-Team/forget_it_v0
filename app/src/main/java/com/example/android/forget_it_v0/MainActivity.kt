package com.example.android.forget_it_v0

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.example.android.forget_it_v0.databinding.ActivityMainBinding
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
            val intent = Intent(this , ReportActivity::class.java)
            intent.putExtra("number", number)
            startActivity(intent)
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