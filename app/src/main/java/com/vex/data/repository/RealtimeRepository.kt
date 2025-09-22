package com.vex.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.vex.model.Assignment
import com.vex.model.Topic
import com.vex.model.UnitItem

class RealtimeRepository {

    private val db: DatabaseReference = Firebase.database.reference
    private val userId: String get() = Firebase.auth.currentUser?.uid ?: ""


    /**
     * Add a Unit under: users/{userId}/units/{unitId}
     */
    fun addUnit(unit: UnitItem) {
        val key = db.child("users")
            .child(userId)
            .child("units")
            .push()
            .key ?: return

        val unitWithId = unit.copy(id = key)

        db.child("users")
            .child(userId)
            .child("units")
            .child(key)
            .setValue(unitWithId)
    }


    /**
     * Add a Topic under: users/{userId}/units/{unitId}/topics/{topicId}
     */
    fun addTopic(unitId: String, topic: Topic) {
        val key = db.child("users")
            .child(userId)
            .child("units")
            .child(unitId)
            .child("topics")
            .push()
            .key ?: return

        val topicWithId = topic.copy(id = key)

        db.child("users")
            .child(userId)
            .child("units")
            .child(unitId)
            .child("topics")
            .child(key)
            .setValue(topicWithId)
    }


    /**
     * Add an Assignment under: users/{userId}/units/{unitId}/assignments/{assignmentId}
     */
    fun addAssignment(unitId: String, a: Assignment) {
        val key = db.child("users")
            .child(userId)
            .child("units")
            .child(unitId)
            .child("assignments")
            .push()
            .key ?: return

        val assignmentWithId = a.copy(id = key)

        db.child("users")
            .child(userId)
            .child("units")
            .child(unitId)
            .child("assignments")
            .child(key)
            .setValue(assignmentWithId)
    }


    /**
     * Query upcoming assignments for this user:
     * There is no Firestore-style collectionGroup in Realtime DB,
     * so we must read all units' assignments and filter in code.
     */
    fun getUpcomingAssignments(listener: (List<Assignment>) -> Unit) {
        val now = System.currentTimeMillis()

        db.child("users")
            .child(userId)
            .child("units")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val now = System.currentTimeMillis()
                    val upcoming = mutableListOf<Assignment>()
                    for (unitSnap in snapshot.children) {
                        for (aSnap in unitSnap.child("assignments").children) {
                            val a = aSnap.getValue(Assignment::class.java)
                            if (a?.status == "pending" && a.dueDate > now) {
                                upcoming.add(a)
                            }
                        }
                    }
                    listener(upcoming.sortedBy { it.dueDate })
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirestoreRepository", "Failed to fetch assignments", error.toException())
                    listener(emptyList())
                }
            })

    }
    fun getUnits(listener: (List<UnitItem>) -> Unit) {
        db.child("users").child(userId).child("units")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(UnitItem::class.java)
                    }
                    listener(list)
                }
                override fun onCancelled(error: DatabaseError) {
                    // You could log or show an error message here
                }
            })
    }
}