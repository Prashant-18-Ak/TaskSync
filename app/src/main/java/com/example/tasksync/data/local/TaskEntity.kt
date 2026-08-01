package com.example.tasksync.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val remoteId: String? = null,

    val title: String,

    val description: String,

    val status: String,

    val dueDate: String,

    val priority: String,

    val category: String,

    val reminderTime: String = ""
)