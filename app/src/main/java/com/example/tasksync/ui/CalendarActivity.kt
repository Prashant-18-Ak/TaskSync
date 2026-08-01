package com.example.tasksync.ui

import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksync.R
import com.example.tasksync.adapter.TaskAdapter
import com.example.tasksync.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CalendarActivity : AppCompatActivity() {

    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_calendar)

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

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val tvSelectedDate = findViewById<TextView>(R.id.tvSelectedDate)
        val recyclerView = findViewById<RecyclerView>(R.id.rvDateTasks)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = TaskAdapter(
            emptyList(),
            {},
            {}
        )

        recyclerView.adapter = adapter

        calendarView.setOnDateChangeListener { _, year, month, day ->

            val calendar = Calendar.getInstance()

            calendar.set(year, month, day)

            val sdf = SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            )

            val selectedDate = sdf.format(calendar.time)

            tvSelectedDate.text = "Tasks for $selectedDate"

            viewModel.allTasks.observe(this) { tasks ->

                val filteredTasks = tasks.filter {
                    it.dueDate == selectedDate
                }

                adapter.updateTasks(filteredTasks)

                if (filteredTasks.isEmpty()) {
                    tvSelectedDate.text = "No tasks on $selectedDate"
                } else {
                    tvSelectedDate.text =
                        "Tasks on $selectedDate (${filteredTasks.size})"
                }
            }
        }
    }
}