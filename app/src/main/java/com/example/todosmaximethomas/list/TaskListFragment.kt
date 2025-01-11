package com.example.todosmaximethomas.list

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.example.todosmaximethomas.R
import user.UserActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import data.Api
import detail.ui.theme.DetailActivity
import kotlinx.coroutines.launch
import coil3.request.error

class TaskListFragment : Fragment() {
    private val adapterListener : TaskListListener = object : TaskListListener {
        override fun onClickDelete(task: Task) {
            viewModel.remove(task)
        }

        override fun onClickEdit(task: Task) {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(Task.TASK_KEY, task)
            editTask.launch(intent)
        }
    }
    private val adapter = TaskListAdapter(adapterListener)

    private val viewModel : TaskListViewModel by viewModels();

    private val createTask = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->

        val newTask = result.data?.getSerializableExtra(Task.TASK_KEY) as Task?

        if (newTask != null) {
            viewModel.add(newTask)
        }
    }

    private val editTask = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        val task = result.data?.getSerializableExtra(Task.TASK_KEY) as Task?

        if (task != null) {
            viewModel.edit(task)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = this.adapter

        val fab = view.findViewById<FloatingActionButton>(R.id.floatingActionButton7)
        fab.setOnClickListener {
            addTask()
        }

        lifecycleScope.launch { // on lance une coroutine car `collect` est `suspend`
            viewModel.tasksStateFlow.collect { newList ->
                // cette lambda est exécutée à chaque fois que la liste est mise à jour dans le VM
                // -> ici, on met à jour la liste dans l'adapter
                adapter.refreshAdapter(newList)
            }
        }
    }
    
    private fun addTask() {
        val intent = Intent(context, DetailActivity::class.java)

        createTask.launch(intent)
    }

    override fun onResume() {
        super.onResume()

        val userTextView = view?.findViewById<TextView>(R.id.user_name)
        val userImageView = view?.findViewById<ImageView>(R.id.user_picture)

        lifecycleScope.launch {
            // Ici on ne va pas gérer les cas d'erreur donc on force le crash avec "!!"
            val user = Api.userWebService.fetchUser().body()!!

            userTextView?.text = user.name

            userImageView?.load(user.avatar) {
                error(R.drawable.ic_launcher_background) // image par défaut en cas d'erreur
            }
            userImageView?.setOnClickListener {
                val intent = Intent(context, UserActivity::class.java)
                startActivity(intent)
            }
        }

        viewModel.refresh() // on demande de rafraîchir les données sans attendre le retour directement
    }
}