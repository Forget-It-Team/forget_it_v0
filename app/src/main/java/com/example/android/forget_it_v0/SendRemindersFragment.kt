package com.example.android.forget_it_v0

import android.Manifest
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class SendRemindersFragment : Fragment() , RecyclerViewOnClickContact{

    private var number: String = Firebase.auth.currentUser!!.phoneNumber!!.subSequence(3, 13).toString()
    //list of people using the app
    private val PERMISSIONS_REQUEST_READ_CONTACTS = 1
    private var myList: ArrayList<String> = arrayListOf()
    private var contacts : HashMap<String, String> = HashMap()
    private lateinit var binding : FragmentSendRemindersBinding
    //to prevent repeated numbers from being added. Holds unique numbers
//    private var numbersStored: ArrayList<String> = arrayListOf()
    private lateinit var contactAdapter: ContactAdapter

    //the final list, which is sent to the RV adapter
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
        requestContactPermission()
        initRV()
        getNumbersUsingApp()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.setPersonalReminder.setOnClickListener {
            view?.findNavController()?.navigate(SendRemindersFragmentDirections.actionSendRemindersFragmentToCreateReminderFragment("MySelf!",number,number))
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

    private fun getNumbersUsingApp() {
        val firestoreContacts = Firebase.firestore.collection("Existing users")

        firestoreContacts.get()
            .addOnSuccessListener { numbers ->
                for (number in numbers)
                    myList.add(number.getString("number")!!)
                getContacts()
//                loadSharedPref()
            }
    }

    private fun writeSharedPref() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("contacts", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val gson: Gson = Gson()
        val json: String = gson.toJson(contactList)
        Log.i("Contact", "Inside write")

        Log.i("COntact", json)
        editor.putString("contact list", json)
        editor.apply()
//        upload_contacts()
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


    private fun upload_contacts(){
//        Firebase.firestore.collection(number).document("Contacts").add(contactList)
    }

    private fun loadSharedPref() {
        val sharedPreferences: SharedPreferences =
            requireActivity().getSharedPreferences("contacts", Context.MODE_PRIVATE)
        val gson = Gson()
        val json: String? = sharedPreferences.getString("contact list", null)
        val type = object : TypeToken<List<Contact?>?>() {}.type

        if (json == null)
            getContacts()
        else {
            Log.i("Contact", json)
            contactList = gson.fromJson(json, type)
            Log.i("Contact", "Deep inside load Shared")

            Log.i("Contact", contactList.toString())
            contactAdapter.update(contactList)
            binding.progressCircular.visibility = View.GONE
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
//                        val con = UploadContacts(name, phoneNo)

                        contacts[phoneNo] = name
                        listHaving.add(contact)
                    } else {
                        contact = Contact(name, phoneNo, "Invite", photo)
//                        val con = UploadContacts(name, phoneNo)
                        contacts[phoneNo] = name
                        listNotHaving.add(contact)
                    }
                    hashMap[phoneNo] = name
                }
            }
            contactList.clear()
//            val myselfContact = Contact(
//                "Myself!", number, "Set a Reminder", photo
//            )
//            contactList.add(myselfContact)
            contactList.addAll(listHaving)
            contactList.addAll(listNotHaving)
            contactAdapter.notifyDataSetChanged()
            Firebase.firestore.collection("Contacts").document(number).set(UploadContactList(contacts))
            writeSharedPref()

            binding.progressCircular.visibility = View.GONE
        }
    }

    override fun onClick(text: String, contact: Contact) {
        if (text.equals("Send Reminder")) {
            view?.findNavController()?.navigate(SendRemindersFragmentDirections.actionSendRemindersFragmentToCreateReminderFragment(contact.name,contact.number,number))
//            val intent: Intent = Intent(this, CreateReminderActivity::class.java)
//            intent.putExtra("name", contact.name)
//            intent.putExtra("number", contact.number)
//            intent.putExtra("myNumber", number)
//            startActivity(intent)
//            finish()
        }else{
            val uri = Uri.parse("smsto:${contact.number}")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra("sms_body", "Check out forget'it app, to make sure you don't forget anything." +
                    "Get it free at.....")
            requireActivity().startActivity(intent)
        }
    }

}