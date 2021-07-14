package com.example.forgetit.model

//data class UploadContacts(
//    val name : String = "",
//    val number: String = ""
//)



data class UploadContactList(
    val list : HashMap<String, String> = HashMap()
)