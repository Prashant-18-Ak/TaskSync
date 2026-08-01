package com.example.tasksync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksync.R
import com.example.tasksync.data.local.TaskEntity

class TaskAdapter(
    private var taskList: List<TaskEntity>,
    private val onDeleteClick: (TaskEntity) -> Unit,
    private val onItemClick: (TaskEntity) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvTaskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        val btnDelete: ImageButton =
            itemView.findViewById(R.id.btnDelete)

        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)

        val tvPriority: TextView =
            itemView.findViewById(R.id.tvPriority)

        val tvCategory: TextView =
            itemView.findViewById(R.id.tvCategory)
    }

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)

        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {

        val task = taskList[position]

        holder.tvTaskTitle.text = task.title
        holder.tvDescription.text = task.description
        holder.tvCategory.text = when (task.category) {

            "Study" -> "📚 Study"

            "Work" -> "💼 Work"

            "Personal" -> "🏠 Personal"

            "Shopping" -> "🛒 Shopping"

            "Health" -> "❤️ Health"

            else -> task.category
        }
        when (task.category) {

            "Study" ->
                holder.tvCategory.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_blue_dark)
                )

            "Work" ->
                holder.tvCategory.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_orange_dark)
                )

            "Personal" ->
                holder.tvCategory.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_green_dark)
                )

            "Shopping" ->
                holder.tvCategory.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_purple)
                )

            "Health" ->
                holder.tvCategory.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_red_dark)
                )
        }
        holder.tvStatus.text = task.status
        when (task.status) {

            "Pending" -> {
                holder.tvStatus.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_orange_dark)
                )
            }

            "In Progress" -> {
                holder.tvStatus.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_blue_dark)
                )
            }

            "Completed" -> {
                holder.tvStatus.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_green_dark)
                )
            }

            else -> {
                holder.tvStatus.setTextColor(
                    holder.itemView.context.getColor(android.R.color.black)
                )
            }
        }
        holder.btnDelete.setOnClickListener {
            onDeleteClick(task)
        }
        holder.itemView.setOnClickListener {
            onItemClick(task)
        }
        holder.tvDueDate.text = "Due: ${task.dueDate}"
        holder.tvPriority.text = "Priority: ${task.priority}"

        when (task.priority) {

            "High" -> {
                holder.tvPriority.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_red_dark)
                )
            }

            "Medium" -> {
                holder.tvPriority.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_orange_dark)
                )
            }

            "Low" -> {
                holder.tvPriority.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_green_dark)
                )
            }
        }
    }

    override fun getItemCount(): Int {
        android.util.Log.d("TASK_DEBUG", "Adapter Count = ${taskList.size}")
        return taskList.size
    }

    fun updateTasks(newTasks: List<TaskEntity>) {
        taskList = newTasks
        notifyDataSetChanged()
    }

    fun filterList(filteredList: List<TaskEntity>) {
        taskList = filteredList
        notifyDataSetChanged()
    }
}