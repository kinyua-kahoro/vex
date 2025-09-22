package com.vex.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vex.model.DueAssignment
import kotlinx.coroutines.tasks.await

object AssignmentsRepository {

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

    private val db = FirebaseDatabase.getInstance()
        .getReference("users")
        .child(uid)

    suspend fun getDueAssignmentsWithin7Days(): List<DueAssignment> {
        val snapshot = db.child("units").get().await()

        val dueSoon = mutableListOf<DueAssignment>()
        val now = System.currentTimeMillis()
        val sevenDays = 7 * 24 * 60 * 60 * 1000L

        for (unitSnap in snapshot.children) {
            for (topicSnap in unitSnap.child("topics").children) {
                for (assignmentSnap in topicSnap.child("assignments").children) {
                    val status = assignmentSnap.child("status")
                        .getValue(String::class.java) ?: "pending"

                    if (status != "done") {
                        val unitId = unitSnap.key ?: continue
                        val topicId = topicSnap.key ?: continue
                        val assignmentId = assignmentSnap.key ?: continue

                        val title = assignmentSnap.child("title")
                            .getValue(String::class.java) ?: "Untitled"
                        val dueDate = assignmentSnap.child("dueDate")
                            .getValue(Long::class.java) ?: continue
                        val lecturer = unitSnap.child("lecturer")
                            .getValue(String::class.java) ?: "Unknown"

                        if (dueDate > now && dueDate - now <= sevenDays) {
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
        }
        return dueSoon
    }
}
