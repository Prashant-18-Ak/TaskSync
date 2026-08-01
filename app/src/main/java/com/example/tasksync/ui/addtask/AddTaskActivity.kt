package com.example.tasksync.ui.addtask

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tasksync.R
import com.example.tasksync.data.local.TaskEntity
import com.example.tasksync.viewmodel.TaskViewModel
import com.google.android.material.textfield.TextInputEditText
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import com.example.tasksync.notification.TaskReminderReceiver
import android.app.TimePickerDialog
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Build
import android.provider.Settings
import android.net.Uri

class AddTaskActivity : AppCompatActivity() {

    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_task)

        val rootView = findViewById<View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(rootView)

        val etTitle = findViewById<TextInputEditText>(R.id.etTitle)
        val etDescription = findViewById<TextInputEditText>(R.id.etDescription)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val actStatus = findViewById<AutoCompleteTextView>(R.id.actStatus)
        val etDueDate = findViewById<TextInputEditText>(R.id.etDueDate)
        val etReminderTime =
            findViewById<TextInputEditText>(R.id.etReminderTime)
        val actPriority = findViewById<AutoCompleteTextView>(R.id.actPriority)
        val actCategory = findViewById<AutoCompleteTextView>(R.id.actCategory)

        // Get data passed from MainActivity
        val taskId = intent.getIntExtra("id", 0)
        val taskTitle = intent.getStringExtra("title")
        val taskDescription = intent.getStringExtra("description")
        val taskStatus = intent.getStringExtra("status") ?: "Pending"
        val taskDueDate = intent.getStringExtra("dueDate") ?: ""
        val taskPriority = intent.getStringExtra("priority") ?: "Medium"
        val taskCategory = intent.getStringExtra("category") ?: "Personal"
        val reminderTime = intent.getStringExtra("reminderTime") ?: ""
        val remoteId = intent.getStringExtra("remoteId")

        val priorityList = listOf(
            "High",
            "Medium",
            "Low"
        )

        val priorityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            priorityList
        )

        actPriority.setAdapter(priorityAdapter)

        val categoryList = listOf(
            "Study",
            "Work",
            "Personal",
            "Shopping",
            "Health"
        )

        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            categoryList
        )

        actCategory.setAdapter(categoryAdapter)

        val statusList = listOf(
            "Pending",
            "In Progress",
            "Completed"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            statusList
        )

        actStatus.setAdapter(adapter)

        if (taskId != 0) {

            etTitle.setText(taskTitle)
            etDescription.setText(taskDescription)
            actStatus.setText(taskStatus, false)
            etDueDate.setText(taskDueDate)
            etReminderTime.setText(reminderTime)
            actPriority.setText(taskPriority, false)
            actCategory.setText(taskCategory, false)

            btnSave.text = "Update Task"


        } else {

            actStatus.setText("Pending", false)
            actPriority.setText("Medium", false)
            actCategory.setText("Personal", false)

        }

        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->

                calendar.set(year, month, day)

                val format = SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                )

                etDueDate.setText(format.format(calendar.time))

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val timePicker = TimePickerDialog(
            this,
            { _, hour, minute ->

                val cal = Calendar.getInstance()

                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)

                val format = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                )

                etReminderTime.setText(
                    format.format(cal.time)
                )

            },
            9,
            0,
            false
        )

        etReminderTime.setOnClickListener {
            timePicker.show()
        }

        etDueDate.setOnClickListener {
            datePicker.show()
        }
        btnSave.setOnClickListener {

            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                return@setOnClickListener
            }

            val task = TaskEntity(
                id = taskId,
                title = title,
                description = description,
                status = actStatus.text.toString(),
                dueDate = etDueDate.text.toString(),
                priority = actPriority.text.toString(),
                category = actCategory.text.toString(),
                reminderTime = etReminderTime.text.toString(),
                remoteId = remoteId
            )

            if (taskId == 0) {

                viewModel.insertTask(task) { generatedId ->

                    scheduleReminder(
                        generatedId,
                        title,
                        etDueDate.text.toString(),
                        etReminderTime.text.toString()
                    )

                    Toast.makeText(
                        this,
                        "Task Added Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }

            } else {

                viewModel.updateTask(task)

                scheduleReminder(
                    taskId,
                    title,
                    etDueDate.text.toString(),
                    etReminderTime.text.toString()
                )

                Toast.makeText(
                    this,
                    "Task Updated Successfully",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }
    }

    private fun scheduleReminder(
        taskId: Int,
        title: String,
        dueDate: String,
        reminderTime: String
    ) {

        if (dueDate.isEmpty() || reminderTime.isEmpty()) {

            cancelReminder(taskId)

            return
        }

        val format = SimpleDateFormat(
            "dd MMM yyyy hh:mm a",
            Locale.getDefault()
        )

        val reminderDate = format.parse(
            "$dueDate $reminderTime"
        ) ?: return

        val triggerTime = reminderDate.time

        if (triggerTime <= System.currentTimeMillis()) {

            cancelReminder(taskId)

            Toast.makeText(
                this,
                "Reminder time has already passed",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val intent = Intent(
            this,
            TaskReminderReceiver::class.java
        ).apply {
            putExtra("title", title)
            putExtra("taskId", taskId)
        }


        val pendingIntent = PendingIntent.getBroadcast(
            this,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            getSystemService(ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (alarmManager.canScheduleExactAlarms()) {

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )

            } else {

                Toast.makeText(
                    this,
                    "Please allow exact alarms for task reminders",
                    Toast.LENGTH_LONG
                ).show()

                val settingsIntent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                ).apply {
                    data = Uri.parse("package:$packageName")
                }

                startActivity(settingsIntent)

                return
            }

        } else {

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        Toast.makeText(
            this,
            "Reminder Scheduled",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun cancelReminder(taskId: Int) {

        val intent = Intent(
            this,
            TaskReminderReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {

            val alarmManager =
                getSystemService(ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}