package com.vex.model

data class DueAssignment(
    val unitId: String,
    val topicId: String,
    val assignmentId: String,
    val title: String,
    val lecturer: String,
    val dueDate: Long
)