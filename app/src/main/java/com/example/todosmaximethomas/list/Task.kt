package com.example.todosmaximethomas.list

import kotlinx.serialization.SerialName

data class Task (
    @SerialName("id")
    val id : String,
    @SerialName("content")
    var title : String,
    @SerialName("description")
    val description : String = "Description"
) : java.io.Serializable {
    companion object {
        const val TASK_KEY = "task"
    }
}