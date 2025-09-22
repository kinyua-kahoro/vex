package com.vex.model

data class Assignment (
    val id: String = "",
    val title: String = "",
    val dueDate: Long = 0L,
    val status: String = "pending",
    val notes: String = ""
)
