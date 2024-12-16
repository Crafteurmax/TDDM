package detail.ui.theme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todosmaximethomas.list.Task
import detail.ui.theme.ui.theme.TodosMaximeThomasTheme
import java.util.UUID

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var initialTask = intent.getSerializableExtra(Task.TASK_KEY) as Task?

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        if (sharedText.isNotEmpty()) {
            initialTask = initialTask?.copy(title = sharedText) ?: Task(
                id = UUID.randomUUID().toString(),
                title = sharedText,
                description = ""
            )
        }

        enableEdgeToEdge()
        setContent {
            TodosMaximeThomasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Detail(
                        initialTask,
                        modifier = Modifier.padding(innerPadding),
                        onValidate = { newTask ->
                            intent.putExtra(Task.TASK_KEY, newTask)
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Detail(initialTask : Task?, modifier: Modifier = Modifier, onValidate: (Task) -> Unit = {}) {

    val newTask = Task(id = UUID.randomUUID().toString(), title = "", description = "")
    var task by remember { mutableStateOf(initialTask ?: newTask) }

    Column {
        Modifier.padding(16.dp)
        Arrangement.spacedBy(16.dp)
        Text(
            text = "Task Detail",
            modifier = modifier,
            style = MaterialTheme.typography.headlineLarge,
        )
        OutlinedTextField(
            task.title,
            onValueChange = { newTitle ->
                task = task.copy(title = newTitle)
            },
            label = { Text("Title") },
            modifier = modifier,
        )
        OutlinedTextField(
            task.description,
            onValueChange = { newDescription ->
                task = task.copy(description = newDescription)
            },
            label = { Text("Description") },
            modifier = modifier,
        )
        Button(
            onClick = {
                onValidate(task)
            }
        ) {
            Text("Validate")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailPreview() {
    TodosMaximeThomasTheme {
        Detail(Task(UUID.randomUUID().toString(), "", ""))
    }
}