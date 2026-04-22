package com.example.alertify.data.repository

import com.example.alertify.data.model.EmergencyContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await




class ContactRepository {

    private val db = FirebaseFirestore.getInstance()
    // Helper to get the current user ID
    private fun getUserId(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    suspend fun addContact(name: String, phone: String): Boolean {
        return try {
            val userId = getUserId()
            if (userId.isEmpty()) return false

            val doc = db.collection("users")
                .document(userId)
                .collection("contacts")
                .document()

            val contact = EmergencyContact(
                id = doc.id,
                name = name,
                phoneNumber =phone // Fixed parameter name here
            )

            doc.set(contact).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getContacts(): List<EmergencyContact> {
        return try {
            val userId = getUserId()
            if (userId.isEmpty()) return emptyList()

            db.collection("users")
                .document(userId)
                .collection("contacts")

                .get()
                .await()
                .toObjects(EmergencyContact::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // NEW: Remove feature
     suspend fun deleteContact(contactId: String): Boolean {
        return try {
            val userId = getUserId()
            db.collection("users")
                .document(userId)
                .collection("contacts")
                .document(contactId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
    fun listenToContacts(onUpdate: (List<EmergencyContact>) -> Unit) {

        val userId = getUserId()
        if (userId.isEmpty()) return

        db.collection("users")
            .document(userId)
            .collection("contacts")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {
                    val contacts =
                        snapshot.toObjects(EmergencyContact::class.java)
                    onUpdate(contacts)
                }
            }
    }
}