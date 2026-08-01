package com.example.tasksync.network

import com.example.tasksync.data.remote.TaskDto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("tasks")
    suspend fun getTasks(): Response<List<TaskDto>>

    @GET("tasks/{id}")
    suspend fun getTask(
        @Path("id") id: String
    ): Response<TaskDto>

    @POST("tasks")
    suspend fun createTask(
        @Body task: TaskDto
    ): Response<TaskDto>

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body task: TaskDto
    ): Response<TaskDto>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Path("id") id: String
    ): Response<Unit>
}