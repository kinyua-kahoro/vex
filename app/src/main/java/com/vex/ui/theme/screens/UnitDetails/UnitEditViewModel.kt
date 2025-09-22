package com.vex.ui.theme.screens.UnitDetails

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vex.model.UnitItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UnitEditViewModel : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    private val db = FirebaseDatabase.getInstance()
        .getReference("users")
        .child(userId)
        .child("units")

    fun getUnit(id: String): Flow<UnitItem?> = flow {
        val snapshot = db.child(id).get().await()
        emit(snapshot.getValue(UnitItem::class.java))
    }

    fun generateId(): String = db.push().key ?: UUID.randomUUID().toString()

    fun saveUnit(unit: UnitItem, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.child(unit.id).setValue(unit)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Unknown error") }
    }
}


