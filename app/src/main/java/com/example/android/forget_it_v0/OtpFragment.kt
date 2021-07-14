package com.example.android.forget_it_v0

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.example.android.forget_it_v0.databinding.FragmentOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
class OtpFragment : Fragment() {
    private lateinit var number: String
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    private var existingUsers: ArrayList<String> = arrayListOf()
    private lateinit var storedVerificationId : String
    private lateinit var binding: FragmentOtpBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_otp,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = OtpFragmentArgs.fromBundle(requireArguments())
        number = args.number
        auth = Firebase.auth

        initCallback()
        startVerification()

        binding.verifyOtpBtn.setOnClickListener {
            val otp = binding.otpET.text.toString()
            if(otp.isEmpty()){
                binding.otpET.error = "Don't leave Empty"
            }else{
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
                signup(credential)
            }

        }

    }
    private fun initCallback() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                signup(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {

                if (p0 is FirebaseAuthInvalidCredentialsException)
                    Log.i("ERROR INVALID", p0.message.toString())
                else if (p0 is FirebaseTooManyRequestsException)
                    Log.i("ERROR TOO MANY REQUESTS", p0.message.toString())

                fail(p0)
            }
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAGA", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
//            resendToken = token
            }
        }
    }
    private fun fail(error: FirebaseException) {
        Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
        Navigation.createNavigateOnClickListener(R.id.action_otpFragment_to_phoneNumberFragment)

    }



    private fun signup(credential: PhoneAuthCredential) {
        Log.i("AAUUTTHH", "here1")
        number = number.replace("+91", "").replace(" ", "")
        val user = hashMapOf(
            "number" to number
        )
        db.collection("Existing users")
            .get()
            .addOnSuccessListener { numbers ->
                for (number in numbers)
                    existingUsers.add(number.getString("number").toString())
                Log.d("otp", number)
                if (!existingUsers.contains(number)) {
                    db.collection("Existing users")
                        .add(user)
                        .addOnSuccessListener { documentReference ->
                            Log.i("AUTH", documentReference.id)
                            Log.i("AAUUTTHH", "here2")
                        }
                        .addOnFailureListener { e ->
                            Log.w("AUTH", "Error", e)
                            Log.i("AAUUTTHH", "here2 fail")
                        }
                }

                Log.i("AAUUTTHH", "here3")
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Log.i("AAUUTTHH", "here4")

                            Log.i("AUTH", "SUCCESS")

                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.putExtra(
                                "number",
                                number.replace("+91", "").replace(" ", "")
                            )
                            requireActivity().startActivity(intent)
                            requireActivity().finish()
                        } else {
                            Log.i("AAUUTTHH", "here4 fal")
                            Log.i("AUTH", "FAIL")
                        }
                    }
            }
    }

    private fun startVerification() {
        val option = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$number")
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()

        PhoneAuthProvider.verifyPhoneNumber(option)
    }




}