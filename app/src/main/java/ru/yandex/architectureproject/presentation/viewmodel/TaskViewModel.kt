package ru.yandex.architectureproject.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.yandex.architectureproject.domain.AddTaskUseCase
import ru.yandex.architectureproject.domain.CompleteTaskUseCase
import ru.yandex.architectureproject.domain.DeleteTaskUseCase
import ru.yandex.architectureproject.domain.GetAllTasksUseCase
import ru.yandex.architectureproject.domain.IncompleteTaskUseCase
import ru.yandex.architectureproject.presentation.state.TaskAction
import ru.yandex.architectureproject.presentation.state.TaskState
import java.util.concurrent.ConcurrentHashMap

class TaskViewModel(
    private val addTaskUseCase: AddTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val incompleteTaskUseCase: IncompleteTaskUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _state = MutableStateFlow<TaskState>(TaskState.Loading)
    val state: StateFlow<TaskState> = _state.asStateFlow()

    private val taskComplete: ConcurrentHashMap<Int, Job> = ConcurrentHashMap()

    init {
        reduce(TaskAction.LoadTask)
    }

    fun reduce(action: TaskAction) {
        viewModelScope.launch {
            when (action) {
                TaskAction.LoadTask -> loadTasks()

                is TaskAction.AddTask -> addTaskUseCase(action.task)

                is TaskAction.DeleteTask -> deleteTaskUseCase(action.taskId)

                is TaskAction.UpdateTaskStatus ->
                    if (action.isComplete) {
                        completedTask(action.taskId)
                    } else {
                        incompleteTask(action.taskId)
                    }
            }
        }
    }

    private suspend fun loadTasks() {
        withContext(ioDispatcher) {
            getAllTasksUseCase()
                .distinctUntilChanged()
                .onStart { _state.value = TaskState.Loading }
                .catch { e -> _state.value = TaskState.Error(e.message ?: "Ошибка загрузки") }
                .collect { tasks -> _state.value = TaskState.Loaded(tasks) }
        }
    }

    private suspend fun incompleteTask(taskId: Int) {
        taskComplete[taskId]?.cancel()
        taskComplete.remove(taskId)
        withContext(ioDispatcher) {
            incompleteTaskUseCase(taskId)
        }
    }

    private suspend fun completedTask(taskId: Int) {
        withContext(ioDispatcher) {
            val job = taskComplete.getOrPut(
                taskId
            ) {
                launch {
                    completeTaskUseCase(taskId)
                }
            }

            job.join()
            taskComplete[taskId]?.let {
                if (it.isCompleted) taskComplete.remove(taskId)
            }
        }
    }
}
