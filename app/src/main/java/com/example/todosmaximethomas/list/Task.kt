package com.example.todosmaximethomas.list

data class Task (val id : String,val title : String,val description : String = "Description") : java.io.Serializable {
    companion object {
        const val TASK_KEY = "task"
    }
}