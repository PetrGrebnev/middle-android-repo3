package ru.yandex.architectureproject.presentation.state

sealed interface TaskAction {
    data object LoadTask : TaskAction

    data class AddTask(val task: String) : TaskAction

    data class UpdateTaskStatus(val taskId: Int, val isComplete: Boolean): TaskAction

    data class DeleteTask(val taskId: Int) : TaskAction
}
