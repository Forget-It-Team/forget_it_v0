package com.example.android.forget_it_v0

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.android.forget_it_v0.databinding.FragmentPhoneNumberBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class PhoneNumberFragment : Fragment() {
    private lateinit var auth : FirebaseAuth
    private lateinit var binding: FragmentPhoneNumberBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_phone_number,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        checkAuth()
    }
    //       bundle
    private fun checkAuth() {
        if (auth.currentUser != null) { //old user
            var number: String = auth.currentUser!!.phoneNumber!!
            number = number.replace("+91", "").replace(" ", "")
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("number", number)
            requireActivity().startActivity(intent)
            requireActivity().finish()
        } else   /// new user
            initActivity()
    }

    private fun initActivity() {
        binding.sendOtpBtn.setOnClickListener{
            startRegistration(
                binding.phoneNumberET.text.toString()
            )
        }
    }



    private fun startRegistration(phoneNumber: String) {
        if (phoneNumber.isEmpty())
            binding.phoneNumberET.error = "Don't leave this empty!"
        else {
            Log.i("OTP BUTTON","send otp button is working")
            view?.findNavController()?.navigate(PhoneNumberFragmentDirections.actionPhoneNumberFragmentToOtpFragment(phoneNumber))
        }
    }


}