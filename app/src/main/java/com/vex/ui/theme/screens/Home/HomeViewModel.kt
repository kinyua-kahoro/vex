package com.vex.ui.theme.screens.Home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vex.model.DueAssignment
import com.vex.model.Topic
import com.vex.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeViewModel : ViewModel() {


    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    private val db = FirebaseDatabase.getInstance().getReference("users").child(userId)

    var firstName by mutableStateOf<String?>(null)
        private set

    // Units count
    private val _unitsCount = MutableStateFlow(0)
    val unitsCount: StateFlow<Int> = _unitsCount

    // Assignments count
    private val _assignmentsCount = MutableStateFlow(0)
    val assignmentsCount: StateFlow<Int> = _assignmentsCount

    // Assignments due soon
    private val _dueAssignments = MutableStateFlow<List<DueAssignment>>(emptyList())
    val dueAssignments: StateFlow<List<DueAssignment>> = _dueAssignments

    init {
        // Listen for all units
        db.child("units").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _unitsCount.value = snapshot.childrenCount.toInt()

                // Count assignments and due assignments
                var totalAssignments = 0
                val dueSoon = mutableListOf<DueAssignment>()
                val now = System.currentTimeMillis()
                val sevenDays = 7 * 24 * 60 * 60 * 1000L

                for (unitSnap in snapshot.children) {
                    for (topicSnap in unitSnap.child("topics").children) {
                        for (assignmentSnap in topicSnap.child("assignments").children) {
                            totalAssignments++

                            val unitId = unitSnap.key ?: continue
                            val topicId = topicSnap.key ?: continue
                            val assignmentId = assignmentSnap.key ?: continue

                            val title = assignmentSnap.child("title").getValue(String::class.java) ?: "Untitled"
                            val dueDate = assignmentSnap.child("dueDate").getValue(Long::class.java) ?: continue
                            val lecturer = unitSnap.child("lecturer").getValue(String::class.java) ?: "Unknown"

                            // 👇 new line – read the status
                            val status = assignmentSnap.child("status").getValue(String::class.java) ?: "pending"

                            // only add if due in 7 days AND not done
                            if (status != "done" && dueDate - now <= sevenDays) {
                                dueSoon.add(
                                    DueAssignment(
                                        unitId = unitId,
                                        topicId = topicId,
                                        assignmentId = assignmentId,
                                        title = title,
                                        lecturer = lecturer,
                                        dueDate = dueDate
                                    )
                                )
                            }
                        }
                    }
                }

                _assignmentsCount.value = totalAssignments
                _dueAssignments.value = dueSoon
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors if necessary
            }
        })
    }
    init {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            db.child("profile").child("firstName")
                .get()
                .addOnSuccessListener { snapshot ->
                    firstName = snapshot.getValue(String::class.java) ?: "Student"
                }
                .addOnFailureListener {
                    firstName = "Student"
                }
        } else {
            firstName = "Student"
        }
    }

    private val _allTopics = MutableStateFlow<List<Topic>>(emptyList())
    val allTopics: StateFlow<List<Topic>> = _allTopics

    private val _dailyTopics = MutableStateFlow<List<Pair<Topic, String>>>(emptyList())
    val dailyTopics: StateFlow<List<Pair<Topic, String>>> = _dailyTopics

    private var lastGeneratedDay = -1

    init {
        generateDailyTopics()
        viewModelScope.launch {
            while (true) {
                val now = Calendar.getInstance()
                val dayOfYear = now.get(Calendar.DAY_OF_YEAR)

                if (dayOfYear != lastGeneratedDay) {
                    generateDailyTopics()
                    lastGeneratedDay = dayOfYear
                }

                // Check every 15 minutes if day has changed
                delay(15 * 60 * 1000L)
            }
        }
    }


    private var dailySelection: List<Pair<Topic, String>> = emptyList()

    private fun generateDailyTopics() {
        db.child("units").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val unitsMap = mutableMapOf<String, String>()
                val allTopics = mutableListOf<Pair<Topic, String>>()

                snapshot.children.forEach { unitSnap ->
                    val unitId = unitSnap.key ?: return@forEach
                    val unitName = unitSnap.child("name").getValue(String::class.java) ?: "Unknown"
                    unitsMap[unitId] = unitName

                    unitSnap.child("topics").children.forEach { topicSnap ->
                        val topic = topicSnap.getValue(Topic::class.java) ?: return@forEach
                        allTopics.add(topic to unitId)
                    }
                }

                _unitNamesMap.value = unitsMap

                // Only shuffle if we haven't chosen today's list yet
                if (dailySelection.isEmpty()) {
                    dailySelection = allTopics.shuffled().take(10)
                }

                // Always filter today's selection against current DB so deleted topics disappear
                _dailyTopics.value = dailySelection.filter { (topic, unitId) ->
                    snapshot.child(unitId).child("topics").hasChild(topic.id)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }



    private val _unitNamesMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val unitNamesMap: StateFlow<Map<String, String>> = _unitNamesMap
    init {
        generateDailyTopics()
    }

    private fun fetchUnits() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("units")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.children.associate { unitSnap ->
                    val unitId = unitSnap.key ?: ""
                    val unitName = unitSnap.child("name").getValue(String::class.java) ?: "Unknown Unit"
                    unitId to unitName
                }
                _unitNamesMap.value = map
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
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

}

