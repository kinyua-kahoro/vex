package com.vex.ui.theme.screens.UnitDetails

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vex.model.UnitItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UnitDetailViewModel(
    private val unitId: String
) : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val _unit = MutableStateFlow<UnitItem?>(null)
    val unit: StateFlow<UnitItem?> = _unit

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadUnit()
    }

    private fun loadUnit() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _error.value = "User not logged in"
            _loading.value = false
            return
        }

        val unitRef = db.child("users").child(uid).child("units").child(unitId)

        unitRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val unitItem = snapshot.getValue(UnitItem::class.java)
                if (unitItem != null) {
                    _unit.value = unitItem.copy(id = unitId)
                } else {
                    _error.value = "Unit not found"
                }
                _loading.value = false
            }

            override fun onCancelled(error: DatabaseError) {
                _error.value = error.message
                _loading.value = false
            }
        })
    }
}

