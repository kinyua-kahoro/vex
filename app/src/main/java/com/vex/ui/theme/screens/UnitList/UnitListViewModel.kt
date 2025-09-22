package com.vex.ui.theme.screens.UnitList

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vex.data.repository.RealtimeRepository
import com.vex.model.UnitItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UnitListViewModel : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    private val db = FirebaseDatabase.getInstance()
        .getReference("users")
        .child(userId)
        .child("units")

    private val _units = MutableStateFlow<List<UnitItem>>(emptyList())
    val units: StateFlow<List<UnitItem>> = _units

    init {
        // Listen for changes in Firebase automatically
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(UnitItem::class.java) }
                _units.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UnitListViewModel", "Failed to fetch units: ${error.message}")
            }
        })
    }

    fun deleteUnit(unit: UnitItem) {
        db.child(unit.id).removeValue()
            .addOnSuccessListener {
                Log.d("UnitListViewModel", "Unit ${unit.name} deleted")
                // No need to manually update _units; ValueEventListener will handle it
            }
            .addOnFailureListener { e ->
                Log.e("UnitListViewModel", "Failed to delete unit ${unit.name}", e)
            }
    }
}

