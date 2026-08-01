# TaskSync

TaskSync is an Android task management application developed in Kotlin. It allows users to create, manage, organize, and synchronize to-do tasks while supporting both local persistence and remote database synchronization.

## Features

- Create new tasks
- View all tasks using RecyclerView
- Update task status:
    - Pending
    - In Progress
    - Completed
- Delete tasks
- Set task priority
- Organize tasks by category
- Set due dates and reminder times
- Search tasks
- Filter tasks by status and category
- Calendar-based task viewing
- Task dashboard with status statistics
- Light, Dark, and System Default themes
- Local task persistence
- Manual synchronization with remote database
- Task reminder notifications

## Technology Stack

### Android Application

- Kotlin
- XML Layouts
- RecyclerView
- Room Database
- Retrofit
- MVVM Architecture

### Backend

- Node.js
- Express.js
- MongoDB Atlas
- Mongoose
- REST API

## Architecture

The Android application follows the MVVM (Model-View-ViewModel) architecture.

```text
UI / Activity
      |
      v
   ViewModel
      |
      v
  Repository
   /       \
  v         v
Room DB   Retrofit
(Local)     |
            v
       REST API
            |
            v
       MongoDB Atlas
```

Room Database provides local task persistence, while Retrofit communicates with the TaskSync backend REST API for remote synchronization.

## Main Components

### RecyclerView

Tasks are displayed on the home screen using RecyclerView with a custom `TaskAdapter`.

### Room Database

Room is used to store and manage tasks locally on the Android device, providing persistent local storage.

### Retrofit

Retrofit is used for REST API communication between the Android application and the TaskSync backend.

### Repository Layer

The repository coordinates task data between the local Room database and the remote REST API.

### ViewModel

The ViewModel separates UI logic from data operations and provides task data to the application's UI layer.

## Remote Synchronization

TaskSync supports synchronization between the local Room database and a remote MongoDB Atlas database.

The synchronization flow is:

```text
TaskSync Android App
        |
        | Retrofit
        v
Node.js + Express REST API
        |
        | Mongoose
        v
MongoDB Atlas
```

The application also provides a manual synchronization option to retrieve remote task changes and synchronize task data.

## Backend Repository

The backend REST API is maintained in a separate repository:

https://github.com/Prashant-18-Ak/TaskSyncBackend

The backend contains the Express server, REST API routes, controllers, Mongoose models, and MongoDB configuration required for remote synchronization.

## Project Structure

```text
app/src/main/java/com/example/tasksync/

├── adapter/
│   └── TaskAdapter.kt
├── data/
│   ├── local/
│   │   ├── TaskDao.kt
│   │   ├── TaskDatabase.kt
│   │   └── TaskEntity.kt
│   ├── remote/
│   │   └── TaskDto.kt
│   └── repository/
│       └── TaskRepository.kt
├── network/
│   ├── ApiService.kt
│   └── RetrofitClient.kt
├── notification/
│   └── TaskReminderReceiver.kt
├── ui/
│   ├── CalendarActivity.kt
│   ├── SettingsActivity.kt
│   └── addtask/
│       └── AddTaskActivity.kt
├── viewmodel/
│   └── TaskViewModel.kt
└── MainActivity.kt
```

## Task Data

A task can contain information such as:

- Title
- Description
- Status
- Priority
- Category
- Due date
- Reminder time

## Running the Android Application

1. Clone the repository.
2. Open the project in Android Studio.
3. Allow Gradle to synchronize the required dependencies.
4. Start the TaskSync backend server if remote synchronization is required.
5. Run the application using an Android emulator or physical Android device.

## Backend Setup

Clone the TaskSyncBackend repository and install the required dependencies:

```bash
npm install
```

Configure the MongoDB connection using a local `.env` file.

Start the backend:

```bash
npm start
```

The backend runs on port `5000` by default.

> MongoDB credentials and other sensitive environment variables are intentionally excluded from GitHub.

## Project Requirements Implemented

TaskSync implements the major project requirements:

- RecyclerView for displaying tasks
- Create, update and delete task functionality
- Room Database for local persistence
- Retrofit for REST API communication
- Remote MongoDB database synchronization
- Kotlin for application development
- XML for Android layouts
- MVVM architecture
- Separation of UI, data, network and repository responsibilities

## Author

**Prashant Kumar**

Major Project — Task Management Android Application