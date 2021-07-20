package com.example.android.forget_it_v0

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.forget_it_v0.adapter.ContactAdapter
import com.example.android.forget_it_v0.databinding.FragmentSendRemindersBinding
import com.example.android.forget_it_v0.models.Contact
import com.example.android.forget_it_v0.models.RecyclerViewOnClickContact
import com.example.android.forget_it_v0.models.toast
import com.example.android.forget_it_v0.models.UploadContactList
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class SendRemindersFragment : Fragment() ,RecyclerViewOnClickContact{

    private var number: String = Firebase.auth.currentUser!!.phoneNumber!!.subSequence(3, 13).toString()
    //list of people using the app
    private val PERMISSIONS_REQUEST_READ_CONTACTS = 1
    private var myList: ArrayList<String> = arrayListOf()
    private var contacts : HashMap<String, String> = HashMap()
    private lateinit var binding : FragmentSendRemindersBinding
    private lateinit var contactAdapter: ContactAdapter

    private var contactList: ArrayList<Contact> = arrayListOf()
    private var listHaving: ArrayList<Contact> = arrayListOf()
    private var listNotHaving: ArrayList<Contact> = arrayListOf()
    private lateinit var contact: Contact
    private lateinit var id: String
    private lateinit var photo: Bitmap

    private var hashMap: HashMap<String, String> = hashMapOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_send_reminders,container,false)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            Dexter
                .withContext(requireContext())
                .withPermission(Manifest.permission.READ_CONTACTS)
                .withListener(object : PermissionListener{
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        initRV()
                        getNumbersUsingApp()
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        requireActivity().toast("Please do not deny, otherwise you won't be able to use the app and connect with your friends")
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {
                        p1?.continuePermissionRequest()
                    }
                })
                .check()

        }else{
            initRV()
            getNumbersUsingApp()
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.setPersonalReminder.setOnClickListener {
            val intent = Intent(requireActivity(), CreateReminderActivity::class.java)
            intent.putExtra("name", "MySelf!")
            intent.putExtra("number", number)
            intent.putExtra("myNumber", number)
            requireActivity().startActivity(intent)
        }



        binding.contactFilter.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                contactAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun info(){
        val info =  Dialog(requireActivity())
        info.setContentView(R.layout.info)
        info.show()
        val backBtn = info.findViewById<Button>(R.id.backButton)
        backBtn.setOnClickListener{
            info.dismiss()
        }

    }

    private fun getNumbersUsingApp() {
        val firestoreContacts = Firebase.firestore.collection("Existing users")

        firestoreContacts.get()
            .addOnSuccessListener { numbers ->
                for (number in numbers)
                    myList.add(number.getString("number")!!)
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                    getContacts()
                }
            }
    }




    private fun initRV() {
        binding.progressCircular.visibility = View.VISIBLE
        contactAdapter = ContactAdapter(contactList, this)
        binding.contactsRv.layoutManager = LinearLayoutManager(requireActivity())
        binding.contactsRv.adapter = contactAdapter
    }


    private fun getContacts(){
        GlobalScope.launch(Dispatchers.Main) {
            photo = BitmapFactory.decodeResource(
                requireActivity().resources,
                R.drawable.person
            )
            val cursor = requireActivity().contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            while (cursor?.moveToNext() == true) {
                val name: String =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                var phoneNo: String =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                Log.d("contact", "$name  $phoneNo")
                phoneNo = phoneNo.replace(" ", "")
                phoneNo = phoneNo.replace("+91", "")
                if (!hashMap.containsKey(phoneNo)) {
                    if (myList.contains(phoneNo)) {
                        contact = Contact(name, phoneNo, "Send Reminder", photo)
                        if(!(phoneNo.isBlank() || name.isBlank())){
                            contacts[phoneNo] = name
                        }
                        listHaving.add(contact)
                    } else {
                        contact = Contact(name, phoneNo, "Invite", photo)
                        if(!(phoneNo.isBlank() || name.isBlank())){
                            contacts[phoneNo] = name
                        }
                        listNotHaving.add(contact)
                    }
                    hashMap[phoneNo] = name
                }
            }
            contactList.clear()
            contactList.addAll(listHaving)
            contactList.addAll(listNotHaving)
            contactAdapter.notifyDataSetChanged()
            Firebase.firestore.collection("Contacts").document(number).set(UploadContactList(contacts))

            binding.progressCircular.visibility = View.GONE
        }
    }

    override fun onClick(text: String, contact: Contact) {
        if (text.equals("Send Reminder")) {
            val intent = Intent(requireActivity(), CreateReminderActivity::class.java)
            intent.putExtra("name", contact.name)
            intent.putExtra("number", contact.number)
            intent.putExtra("myNumber", number)
            requireActivity().startActivity(intent)



        }else{
            val uri = Uri.parse("smsto:${contact.number}")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra("sms_body", "Check out forget'it app, to make sure you don't forget anything." +
                    "Get it free at.....")
            requireActivity().startActivity(intent)
        }
    }

}