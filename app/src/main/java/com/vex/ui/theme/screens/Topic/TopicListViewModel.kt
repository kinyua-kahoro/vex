package com.vex.ui.theme.screens.Topic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vex.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TopicListViewModel(private val unitId: String) : ViewModel() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    private val db = FirebaseDatabase.getInstance().getReference("users")
        .child(userId)
        .child("units")
        .child(unitId)
        .child("topics")

    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics

    init {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Topic::class.java) }
                _topics.value = list.sortedByDescending { it.priority }
            }

            override fun onCancelled(error: DatabaseError) {
                // Optional: handle error
            }
        })
    }

    private fun fetchTopics() {
        viewModelScope.launch {
            val snapshot = db.get().await()
            val list = snapshot.children.mapNotNull { it.getValue(Topic::class.java) }
            _topics.value = list.sortedByDescending { it.priority }
        }
    }

    fun addOrUpdateTopic(topic: Topic, onDone: () -> Unit, onError: (String) -> Unit) {
        val topicId = if (topic.id.isEmpty()) db.push().key ?: UUID.randomUUID().toString() else topic.id
        db.child(topicId).setValue(topic)
            .addOnSuccessListener {
                fetchTopics() // ✅ refresh the list after saving
                onDone()
            }
            .addOnFailureListener { onError(it.message ?: "Unknown error") }
    }

    fun getTopic(topicId: String): Flow<Topic?> = flow {
        val snapshot = db.child(topicId).get().await()
        emit(snapshot.getValue(Topic::class.java))
    }

    fun generateId(): String = db.push().key ?: UUID.randomUUID().toString()

    fun deleteTopic(unitId: String, topic: Topic) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val topicRef = FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("units")
            .child(unitId)
            .child("topics")
            .child(topic.id)

        topicRef.removeValue()
            .addOnSuccessListener {
                _topics.value = _topics.value.filter { it.id != topic.id }
                Log.d("UnitDetailViewModel", "Deleted topic ${topic.title}")
            }
            .addOnFailureListener { e ->
                Log.e("UnitDetailViewModel", "Failed to delete topic ${topic.title}", e)
            }
    }
}

