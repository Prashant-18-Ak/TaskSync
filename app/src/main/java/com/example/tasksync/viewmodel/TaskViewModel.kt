package com.example.tasksync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.tasksync.data.local.TaskDatabase
import com.example.tasksync.data.local.TaskEntity
import com.example.tasksync.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val allTasks: LiveData<List<TaskEntity>>

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        allTasks = repository.allTasks
    }

    fun insertTask(
        task: TaskEntity,
        onInserted: (Int) -> Unit
    ) {

        viewModelScope.launch {

            val localId = repository.insertTask(task)

            onInserted(localId)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun syncTasks() {

        viewModelScope.launch {

            repository.syncFromServer()

        }
    }
}