package com.example.android.forget_it_v0.repository

import android.util.Log
import com.example.android.forget_it_v0.models.Pending
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object FirestoreRepo {
    fun swapData(from: String, to: String, sender: String, receiver: String, pending: Pending) {
        GlobalScope.launch(Dispatchers.Main) {
            val firestoreAccepted = Firebase.firestore
                .collection(receiver)
                .document("Upcoming")
                .collection(from)

            val firestoreCompleted = Firebase.firestore
                .collection(receiver)
                .document("Upcoming")
                .collection(to)

            firestoreAccepted.document(pending.id)
                .get()
                .addOnSuccessListener { reminder ->
                    val dummy = reminder

                    firestoreCompleted
                        .document(pending.id)
                        .set(dummy.data!!)
                        .addOnSuccessListener {
                            firestoreAccepted.document(pending.id).delete()
                                .addOnSuccessListener {
                                    Log.i("Moved successfully", "And deleted")
                                }
                        }
                }
        }
        GlobalScope.launch(Dispatchers.Main) {
            val firestoreAcceptedSender = Firebase.firestore
                .collection(sender)
                .document("Sent")
                .collection(from)

            val firestoreCompletedSender = Firebase.firestore
                .collection(sender)
                .document("Sent")
                .collection(to)

            firestoreAcceptedSender.document(pending.id)
                .get()
                .addOnSuccessListener { reminder ->
                    val dummy = reminder

                    firestoreCompletedSender
                        .document(pending.id)
                        .set(dummy.data!!)
                        .addOnSuccessListener {
                            firestoreAcceptedSender.document(pending.id).delete()
                                .addOnSuccessListener {
                                    Log.i("Moved successfully", "And deleted")
                                }
                        }
                }
        }
    }
}