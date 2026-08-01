package com.example.tasksync.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): LiveData<List<TaskEntity>>

    @Query("UPDATE tasks SET remoteId = :remoteId WHERE id = :id")
    suspend fun updateRemoteId(
        id: Int,
        remoteId: String
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}