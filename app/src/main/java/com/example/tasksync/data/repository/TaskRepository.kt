package com.example.tasksync.data.repository

import androidx.lifecycle.LiveData
import com.example.tasksync.data.local.TaskDao
import com.example.tasksync.data.local.TaskEntity
import com.example.tasksync.data.remote.TaskDto
import com.example.tasksync.network.RetrofitClient

class TaskRepository(
    private val taskDao: TaskDao
) {

    val allTasks: LiveData<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskEntity): Int {

        val localId = taskDao.insertTask(task).toInt()

        try {

            val response = RetrofitClient.api.createTask(
                TaskDto(
                    title = task.title,
                    description = task.description,
                    status = task.status,
                    dueDate = task.dueDate,
                    priority = task.priority,
                    category = task.category,
                    reminderTime = task.reminderTime
                )
            )

            if (response.isSuccessful) {

                response.body()?._id?.let { mongoId ->

                    taskDao.updateRemoteId(
                        localId,
                        mongoId
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return localId
    }

    suspend fun updateTask(task: TaskEntity) {

        taskDao.updateTask(task)

        if (task.remoteId == null) return

        try {

            RetrofitClient.api.updateTask(
                task.remoteId,
                TaskDto(
                    _id = task.remoteId,
                    title = task.title,
                    description = task.description,
                    status = task.status,
                    dueDate = task.dueDate,
                    priority = task.priority,
                    category = task.category,
                    reminderTime = task.reminderTime
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteTask(task: TaskEntity) {

        taskDao.deleteTask(task)

        if (task.remoteId == null) return

        try {
            RetrofitClient.api.deleteTask(task.remoteId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun syncFromServer() {

        try {

            val response = RetrofitClient.api.getTasks()

            if (response.isSuccessful) {

                val remoteTasks = response.body() ?: emptyList()

                val roomTasks = remoteTasks.map {

                    TaskEntity(
                        remoteId = it._id,
                        title = it.title,
                        description = it.description,
                        status = it.status,
                        dueDate = it.dueDate,
                        priority = it.priority,
                        category = it.category,
                        reminderTime = it.reminderTime
                    )
                }

                taskDao.deleteAll()
                taskDao.insertAll(roomTasks)

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}