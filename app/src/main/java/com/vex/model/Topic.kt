package com.vex.model

data class Topic (
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isRevised: Boolean = false,
    val priority: Int = 0,
)