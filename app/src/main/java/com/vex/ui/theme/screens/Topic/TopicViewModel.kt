package com.vex.ui.theme.screens.Topic

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vex.model.Assignment
import com.vex.model.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TopicViewModel(
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""
) : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference
        .child("users").child(userId).child("units")

    private val _topic = MutableStateFlow<Topic?>(null)
    val topic: StateFlow<Topic?> = _topic

    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments: StateFlow<List<Assignment>> = _assignments

    fun loadTopic(unitId: String, topicId: String) {
        db.child(unitId).child("topics").child(topicId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val title = snapshot.child("title").getValue(String::class.java) ?: "Untitled"
                    val description = snapshot.child("description").getValue(String::class.java) ?: ""
                    val isRevised = snapshot.child("isRevised").getValue(Boolean::class.java) ?: false
                    val priority = snapshot.child("priority").getValue(Int::class.java) ?: 0

                    _topic.value = Topic(topicId, title, description, isRevised, priority)

                    val assignmentsList = mutableListOf<Assignment>()
                    for (assignmentSnap in snapshot.child("assignments").children) {
                        val id = assignmentSnap.key ?: continue
                        val assignmentTitle = assignmentSnap.child("title").getValue(String::class.java) ?: "Untitled"
                        val dueDate = assignmentSnap.child("dueDate").getValue(Long::class.java) ?: 0L
                        val status = assignmentSnap.child("status").getValue(String::class.java) ?: "pending"
                        val notes = assignmentSnap.child("notes").getValue(String::class.java) ?: ""
                        assignmentsList.add(Assignment(id, assignmentTitle, dueDate, status, notes))
                    }
                    _assignments.value = assignmentsList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("TopicViewModel", "Failed to load topic: ${error.message}")
                }
            })
    }
}
