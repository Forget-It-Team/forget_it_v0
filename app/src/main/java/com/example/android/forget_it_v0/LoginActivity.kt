package com.example.android.forget_it_v0

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.airbnb.lottie.LottieAnimationView
import com.example.android.forget_it_v0.R
import com.example.android.forget_it_v0.databinding.ActivityLoginBinding
import com.example.android.forget_it_v0.databinding.ActivityReportBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay


@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    companion object {
        private const val RC_Sign = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        createLoginUI()
    }


    private fun createLoginUI() {
        val providers = arrayListOf<AuthUI.IdpConfig>(
            AuthUI.IdpConfig.PhoneBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).setTheme(R.style.loginUI)
                .build(), RC_Sign
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_Sign) {
            var response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                Handler().postDelayed({
                    binding.SuccessanimationView.visibility = View.VISIBLE
                },4000)
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                if (response == null) {
                    finish()
                }
                if (response?.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    return
                }
                if (response?.error?.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, response?.error?.errorCode.toString(), Toast.LENGTH_LONG)
                }
                return
            }
        }
    }
}



