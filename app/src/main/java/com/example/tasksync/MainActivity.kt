package com.example.tasksync

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksync.adapter.TaskAdapter
import com.example.tasksync.ui.addtask.AddTaskActivity
import com.example.tasksync.viewmodel.TaskViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.widget.SearchView
import com.example.tasksync.data.local.TaskEntity
import android.widget.TextView
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import android.graphics.Color
import com.github.mikephil.charting.formatter.PercentFormatter
import android.widget.ImageButton
import com.example.tasksync.ui.CalendarActivity
import androidx.appcompat.app.AlertDialog
import com.example.tasksync.utils.ThemeManager
import androidx.appcompat.app.AppCompatDelegate
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.Button
import android.widget.RadioGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.AlarmManager
import android.app.PendingIntent
import com.example.tasksync.notification.TaskReminderReceiver

class MainActivity : AppCompatActivity() {

    private val viewModel: TaskViewModel by viewModels()

    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: TaskAdapter

    private lateinit var tvEmpty: TextView

    private var allTasks = listOf<TaskEntity>()

    private var selectedStatus = "All"
    private var selectedCategory = "All"
    private var searchQuery = ""

    private var selectedSort = "None"

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

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

        requestNotificationPermission()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTasks)

        val searchView = findViewById<SearchView>(R.id.searchView)
        val btnFilter = findViewById<ImageButton>(R.id.btnFilter)

        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val tvPending = findViewById<TextView>(R.id.tvPending)
        val tvProgress = findViewById<TextView>(R.id.tvProgress)
        val tvCompleted = findViewById<TextView>(R.id.tvCompleted)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val btnCalendar = findViewById<ImageButton>(R.id.btnCalendar)
        val btnSort = findViewById<ImageButton>(R.id.btnSort)
        val btnSync = findViewById<ImageButton>(R.id.btnSync)
        val tvLastSync = findViewById<TextView>(R.id.tvLastSync)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)


        btnCalendar.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    CalendarActivity::class.java
                )
            )

        }

        btnSort.setOnClickListener {

            val options = arrayOf(
                "None",
                "Priority",
                "Due Date",
                "Title (A-Z)"
            )

            AlertDialog.Builder(this)
                .setTitle("Sort Tasks")
                .setItems(options) { _, which ->

                    selectedSort = options[which]

                    applyFilters()

                }
                .show()
        }

        btnSettings.setOnClickListener {

            val themes = arrayOf(
                "🌞 Light",
                "🌙 Dark",
                "📱 System Default"
            )

            AlertDialog.Builder(this)
                .setTitle("Choose Theme")
                .setItems(themes) { _, which ->

                    when (which) {

                        0 -> {
                            ThemeManager.saveTheme(
                                this,
                                AppCompatDelegate.MODE_NIGHT_NO
                            )

                            AppCompatDelegate.setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_NO
                            )
                        }

                        1 -> {
                            ThemeManager.saveTheme(
                                this,
                                AppCompatDelegate.MODE_NIGHT_YES
                            )

                            AppCompatDelegate.setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_YES
                            )
                        }

                        2 -> {
                            ThemeManager.saveTheme(
                                this,
                                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                            )

                            AppCompatDelegate.setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                            )
                        }
                    }
                }
                .show()
        }

        btnSync.setOnClickListener {

            progressBar.visibility = View.VISIBLE

            viewModel.syncTasks()

            val time = java.text.SimpleDateFormat(
                "dd MMM, hh:mm a",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            tvLastSync.text = "Last Sync: $time"

            Toast.makeText(
                this,
                "Sync completed",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnFilter.setOnClickListener {
            showFilterBottomSheet()
        }


        searchView.setIconifiedByDefault(false)
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.queryHint = "Search tasks..."

        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.isNestedScrollingEnabled = false

        adapter = TaskAdapter(
            emptyList(),

            onDeleteClick = { task ->

                AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete '${task.title}'?")
                    .setPositiveButton("Delete") { _, _ ->

                        cancelReminder(task.id)

                        viewModel.deleteTask(task)

                        Toast.makeText(
                            this,
                            "Task deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },

            onItemClick = { task ->

                val intent = Intent(this, AddTaskActivity::class.java)

                intent.putExtra("id", task.id)
                intent.putExtra("title", task.title)
                intent.putExtra("description", task.description)
                intent.putExtra("status", task.status)
                intent.putExtra("dueDate", task.dueDate)
                intent.putExtra("priority", task.priority)
                intent.putExtra("category", task.category)
                intent.putExtra("reminderTime", task.reminderTime)
                intent.putExtra("remoteId", task.remoteId)

                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter
        progressBar.visibility = View.VISIBLE

        viewModel.syncTasks()

        val time = java.text.SimpleDateFormat(
            "dd MMM, hh:mm a",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        tvLastSync.text = "Last Sync: $time"

        viewModel.allTasks.observe(this) { tasks ->

            progressBar.visibility = View.GONE
            allTasks = tasks
            applyFilters()

            val total = tasks.size

            val pending = tasks.count {
                it.status == "Pending"
            }

            val progress = tasks.count {
                it.status == "In Progress"
            }

            val completed = tasks.count {
                it.status == "Completed"
            }

            tvTotal.text = total.toString()
            tvPending.text = pending.toString()
            tvProgress.text = progress.toString()
            tvCompleted.text = completed.toString()

            setupPieChart(
                pieChart,
                pending,
                progress,
                completed
            )

            if (total == 0) {

                pieChart.visibility = View.GONE

            } else {

                pieChart.visibility = View.VISIBLE

            }

            android.util.Log.d("TASK_DEBUG", "Tasks from Room = ${tasks.size}")
        }



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                searchQuery = newText ?: ""
                applyFilters()

                return true
            }
        })

        val fab = findViewById<FloatingActionButton>(R.id.fabAddTask)

        fab.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    private fun applyFilters() {

        var filteredList = allTasks.filter { task ->

            val statusMatch =
                selectedStatus == "All" ||
                        task.status == selectedStatus

            val categoryMatch =
                selectedCategory == "All" ||
                        task.category == selectedCategory

            val searchMatch =
                task.title.contains(searchQuery, true) ||
                        task.description.contains(searchQuery, true) ||
                        task.category.contains(searchQuery, true)

            statusMatch && categoryMatch && searchMatch
        }

        filteredList = when (selectedSort) {

            "Priority" -> filteredList.sortedBy {
                when (it.priority) {
                    "High" -> 1
                    "Medium" -> 2
                    else -> 3
                }
            }

            "Due Date" -> filteredList.sortedBy {
                it.dueDate
            }

            "Title (A-Z)" -> filteredList.sortedBy {
                it.title.lowercase()
            }

            else -> filteredList
        }

        android.util.Log.d("TASK_DEBUG", "Filtered = ${filteredList.size}")

        adapter.filterList(filteredList)

        if (filteredList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
        }
    }

    private fun showFilterBottomSheet() {

        val view = layoutInflater.inflate(
            R.layout.bottom_sheet_filter,
            null
        )

        val dialog = BottomSheetDialog(this)

        dialog.setContentView(view)

        val rgStatus =
            view.findViewById<RadioGroup>(R.id.rgStatus)

        val rgCategory =
            view.findViewById<RadioGroup>(R.id.rgCategory)

        val btnApply =
            view.findViewById<Button>(R.id.btnApplyFilter)

        val btnReset =
            view.findViewById<Button>(R.id.btnResetFilter)

        // Restore current Status selection
        when (selectedStatus) {

            "Pending" -> rgStatus.check(R.id.rbPending)

            "In Progress" -> rgStatus.check(R.id.rbProgress)

            "Completed" -> rgStatus.check(R.id.rbCompleted)

            else -> rgStatus.check(R.id.rbAllStatus)
        }

        // Restore current Category selection
        when (selectedCategory) {

            "Study" -> rgCategory.check(R.id.rbStudy)

            "Work" -> rgCategory.check(R.id.rbWork)

            "Personal" -> rgCategory.check(R.id.rbPersonal)

            "Shopping" -> rgCategory.check(R.id.rbShopping)

            "Health" -> rgCategory.check(R.id.rbHealth)

            else -> rgCategory.check(R.id.rbAllCategory)
        }

        // Apply filter
        btnApply.setOnClickListener {

            selectedStatus = when (rgStatus.checkedRadioButtonId) {

                R.id.rbPending -> "Pending"

                R.id.rbProgress -> "In Progress"

                R.id.rbCompleted -> "Completed"

                else -> "All"
            }

            selectedCategory = when (rgCategory.checkedRadioButtonId) {

                R.id.rbStudy -> "Study"

                R.id.rbWork -> "Work"

                R.id.rbPersonal -> "Personal"

                R.id.rbShopping -> "Shopping"

                R.id.rbHealth -> "Health"

                else -> "All"
            }

            applyFilters()

            dialog.dismiss()
        }

        // Reset filter
        btnReset.setOnClickListener {

            selectedStatus = "All"
            selectedCategory = "All"

            applyFilters()

            dialog.dismiss()
        }

        // Show BottomSheet first
        dialog.show()

        // Force BottomSheet to fully expand
        val bottomSheet =
            dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )

        bottomSheet?.let {

            val behavior =
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)

            behavior.state =
                com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED

            behavior.skipCollapsed = true
        }
    }

    private fun requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    private fun setupPieChart(
        pieChart: PieChart,
        pending: Int,
        progress: Int,
        completed: Int
    ) {

        val entries = ArrayList<PieEntry>()

        if (pending > 0)
            entries.add(PieEntry(pending.toFloat(), "Pending"))

        if (progress > 0)
            entries.add(PieEntry(progress.toFloat(), "In Progress"))

        if (completed > 0)
            entries.add(PieEntry(completed.toFloat(), "Completed"))

        val dataSet = PieDataSet(entries, "Tasks")

        dataSet.colors = listOf(
            Color.parseColor("#FFA000"),   // Orange
            Color.parseColor("#2196F3"),   // Blue
            Color.parseColor("#4CAF50")    // Green
        )

        dataSet.valueTextSize = 16f
        dataSet.valueTextColor = Color.WHITE

        val data = PieData(dataSet)

        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.setUsePercentValues(true)

        pieChart.data = data

        pieChart.description.isEnabled = false

        pieChart.centerText = "Tasks\n${pending + progress + completed}"
        pieChart.setCenterTextSize(18f)

        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 60f
        pieChart.transparentCircleRadius = 65f

        pieChart.setDrawEntryLabels(false)

        pieChart.legend.isEnabled = true
        pieChart.legend.textColor = Color.BLACK

        pieChart.legend.textSize = 13f

        pieChart.animateY(1200)

        pieChart.invalidate()
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