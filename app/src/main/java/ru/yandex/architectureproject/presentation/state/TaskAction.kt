package ru.yandex.architectureproject.presentation.state

sealed class TaskAction {
    // TODO: Здесь должны быть действия (загрузка заданий, добавление задания, обновление статуса задания (сделано/не сделано) и удаление задания)
    data object LoadTask : TaskAction()

    data class AddTask(val task: String) : TaskAction()

    data class UpdateTaskStatus(val taskId: Int, val isComplete: Boolean): TaskAction()

    data class DeleteTask(val taskId: Int) : TaskAction()
}
