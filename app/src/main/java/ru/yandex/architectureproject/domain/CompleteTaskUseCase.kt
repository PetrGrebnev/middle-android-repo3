package ru.yandex.architectureproject.domain

import kotlinx.coroutines.delay
import ru.yandex.architectureproject.data.repository.TaskRepository

class CompleteTaskUseCase(
    private val repository: TaskRepository,
) {
    private val DEFAULT_TIME_FOR_TASK_DELETE = 10000L

    suspend operator fun invoke(taskId: Int) {
        repository.completeTask(taskId)
        delay(DEFAULT_TIME_FOR_TASK_DELETE)
        repository.deleteTask(taskId)
    }
}
