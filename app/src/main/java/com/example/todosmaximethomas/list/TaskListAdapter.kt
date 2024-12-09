package com.example.todosmaximethomas.list

import android.app.ActivityManager.TaskDescription
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todosmaximethomas.R
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter



object MyTasksDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldTask: Task, newTask: Task) : Boolean {
        return oldTask.id == newTask.id
    }

    override fun areContentsTheSame(oldTask: Task, newTask: Task) : Boolean {
        return (oldTask.title == newTask.title) && (oldTask.description == newTask.description)
    }
}

// l'IDE va râler ici car on a pas encore implémenté les méthodes nécessaires
class TaskListAdapter : ListAdapter<Task, TaskListAdapter.TaskViewHolder>(MyTasksDiffCallback) {

    var onClickDelete: (Task) -> Unit = {}

    var onClickEdit: (Task) -> Unit = {}

    // on utilise `inner` ici afin d'avoir accès aux propriétés de l'adapter directement
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var textViewTitle = itemView.findViewById<TextView>(R.id.task_title)
        private var textViewDescription = itemView.findViewById<TextView>(R.id.task_description)
        private var textViewEdit = itemView.findViewById<ImageButton>(R.id.editButton)
        private var textViewDel = itemView.findViewById<ImageButton>(R.id.deleteButton)

        fun bind(task : Task) {
            textViewTitle.text = task.title
            textViewDescription.text = task.description

            textViewEdit.setOnClickListener {
                onClickEdit(task)
            }
            textViewDel.setOnClickListener {
                onClickDelete(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)

        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    fun refreshAdapter(newList: List<Task>) {
        submitList(newList)
        notifyDataSetChanged()
    }


}
