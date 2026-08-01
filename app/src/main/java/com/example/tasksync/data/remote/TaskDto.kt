package com.example.tasksync.data.remote

data class TaskDto(
    val _id: String? = null,
    val title: String,
    val description: String,
    val status: String,
    val dueDate: String,
    val priority: String,
    val category: String,
    val reminderTime: String
)