package com.vex.ui.theme.screens.Profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vex.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    fun loadProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users")
            .child(uid).child("profile")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(UserProfile::class.java)?.let {
                        _profile.value = it
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun saveProfile(profile: UserProfile, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        // ✅ write only to /users/<uid>/profile
        db.child(uid).child("profile")
            .setValue(profile)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun editProfile(
        firstName: String? = null,
        secondName: String? = null,
        university: String? = null,
        regNumber: String? = null,
        levelOfStudy: String? = null,
        courseOfStudy: String? = null,
        onComplete: (Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return

        // Build a map only with the non-null arguments
        val updates = mutableMapOf<String, Any>()
        firstName?.let    { updates["firstName"]    = it }
        secondName?.let   { updates["secondName"]   = it }
        university?.let   { updates["university"]   = it }
        regNumber?.let    { updates["regNumber"]    = it }
        levelOfStudy?.let { updates["levelOfStudy"] = it }
        courseOfStudy?.let{ updates["courseOfStudy"]= it }

        if (updates.isEmpty()) {
            onComplete(true) // nothing to update
            return
        }

        db.child(uid).child("profile")
            .updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update local StateFlow so UI shows the changes immediately
                    _profile.value = _profile.value.copy(
                        firstName    = firstName    ?: _profile.value.firstName,
                        secondName   = secondName   ?: _profile.value.secondName,
                        university   = university   ?: _profile.value.university,
                        regNumber    = regNumber    ?: _profile.value.regNumber,
                        levelOfStudy = levelOfStudy ?: _profile.value.levelOfStudy,
                        courseOfStudy= courseOfStudy?: _profile.value.courseOfStudy
                    )
                }
                onComplete(task.isSuccessful)
            }
    }
}
