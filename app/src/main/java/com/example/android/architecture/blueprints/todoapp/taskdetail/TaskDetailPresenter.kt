/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.architecture.blueprints.todoapp.taskdetail

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.Result
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.launchSilent
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Listens to user actions from the UI ([TaskDetailFragment]), retrieves the data and updates
 * the UI as required.
 */
class TaskDetailPresenter(
        private val taskId: String,
        private val tasksRepository: TasksRepository,
        private val taskDetailView: TaskDetailContract.View,
        private val uiContext:CoroutineContext = UI
) : TaskDetailContract.Presenter {

    init {
        taskDetailView.presenter = this
    }

    override fun start() {
        openTask()
    }

    private fun openTask() = launch(uiContext) {
        if (taskId.isEmpty()) {
            taskDetailView.showMissingTask()
        } else {
            taskDetailView.setLoadingIndicator(true)
            val result = tasksRepository.getTask(taskId)
            if (isActive) {
                if (result is Result.Success) {
                    taskDetailView.setLoadingIndicator(false)
                    showTask(result.data)
                } else {
                    taskDetailView.showMissingTask()
                }
            }

        }
    }

    override fun editTask() {
        if (taskId.isEmpty()) {
            taskDetailView.showMissingTask()
            return
        }
        taskDetailView.showEditTask(taskId)
    }

    override fun deleteTask() = launchSilent(uiContext) {
        if (taskId.isEmpty()) {
            taskDetailView.showMissingTask()
        } else {
            tasksRepository.deleteTask(taskId)
            taskDetailView.showTaskDeleted()
        }

    }

    override fun completeTask() = launchSilent(uiContext) {
        if (taskId.isEmpty()) {
            taskDetailView.showMissingTask()
        } else {
            tasksRepository.completeTask(taskId)
            taskDetailView.showTaskMarkedComplete()
        }

    }

    override fun activateTask() = launchSilent(uiContext) {
        if (taskId.isEmpty()) {
            taskDetailView.showMissingTask()
        } else {
            tasksRepository.activateTask(taskId)
            taskDetailView.showTaskMarkedActive()
        }
    }

    private fun showTask(task: Task) {
        with(taskDetailView) {
            if (taskId.isEmpty()) {
                hideTitle()
                hideDescription()
            } else {
                showTitle(task.title)
                showDescription(task.description)
            }
            showCompletionStatus(task.isCompleted)
        }
    }
}
