package com.vex.ui.theme.screens.Assignment

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vex.model.Assignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AssignmentListViewModel(private val unitId: String, private val topicId: String) : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    private val db = FirebaseDatabase.getInstance().getReference("users")
        .child(userId)
        .child("units")
        .child(unitId)
        .child("topics")
        .child(topicId)
        .child("assignments")

    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments: StateFlow<List<Assignment>> = _assignments

    init {
        // Listen for real-time updates
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Assignment::class.java) }
                _assignments.value = list.sortedBy { it.dueDate } // sorted by due date
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addOrUpdateAssignment(assignment: Assignment, onDone: () -> Unit, onError: (String) -> Unit) {
        val id = if (assignment.id.isEmpty()) db.push().key ?: UUID.randomUUID().toString() else assignment.id
        db.child(id).setValue(assignment)
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { onError(it.message ?: "Unknown error") }
    }

    fun getAssignment(assignmentId: String): Flow<Assignment?> = flow {
        try {
            val snapshot = db.child(assignmentId).get().await()
            val assignment = snapshot.getValue(Assignment::class.java)
            emit(assignment)
        } catch (e: Exception) {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun deleteAssignment(unitId: String, topicId: String, assignment: Assignment) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val assignmentRef = FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("units")
            .child(unitId)
            .child("topics")
            .child(topicId)
            .child("assignments")
            .child(assignment.id)

        assignmentRef.removeValue()
            .addOnSuccessListener {
                // Remove from local StateFlow
                _assignments.value = _assignments.value.filter { it.id != assignment.id }
                Log.d("AssignmentListViewModel", "Deleted assignment ${assignment.title}")
            }
            .addOnFailureListener { e ->
                Log.e("AssignmentListViewModel", "Failed to delete assignment ${assignment.title}", e)
            }
    }



    fun generateId(): String = db.push().key ?: UUID.randomUUID().toString()
}
