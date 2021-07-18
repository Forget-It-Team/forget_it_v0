package com.example.android.forget_it_v0

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.databinding.DataBindingUtil
import com.example.android.forget_it_v0.databinding.ActivitySplashBinding
@Suppress("Deprecation")
class splashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash)

        //Animation
        val leftToRight = AnimationUtils.loadAnimation(this,R.anim.left_to_right)
        val RightToLeft = AnimationUtils.loadAnimation(this,R.anim.right_to_left)
        val TopToBottom = AnimationUtils.loadAnimation(this,R.anim.top_to_bottom)
        val BottomToTop = AnimationUtils.loadAnimation(this,R.anim.bottom_to_top)
        binding.logo.setAnimation(TopToBottom)
        binding.logoText.setAnimation(BottomToTop)


        // This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // we used the postDelayed(Runnable, time) method
        // to send a message with a delayed time.
        Handler().postDelayed({
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
            finish()
        }, 2500) // 2500 is the delayed time in milliseconds.
    }


}
