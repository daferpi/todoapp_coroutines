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

package com.example.android.architecture.blueprints.todoapp.addedittask

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.Result
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.util.launchSilent

/**
 * Listens to user actions from the UI ([AddEditTaskFragment]), retrieves the data and updates
 * the UI as required.
 * @param taskId ID of the task to edit or null for a new task
 *
 * @param tasksRepository a repository of data for tasks
 *
 * @param addTaskView the add/edit view
 *
 * @param isDataMissing whether data needs to be loaded or not (for config changes)
 */
class AddEditTaskPresenter(
        private val taskId: String?,
        val tasksRepository: TasksDataSource,
        val addTaskView: AddEditTaskContract.View,
        override var isDataMissing: Boolean
) : AddEditTaskContract.Presenter {

    init {
        addTaskView.presenter = this
    }

    override fun start() {
        if (taskId != null && isDataMissing) {
            populateTask()
        }
    }

    override fun saveTask(title: String, description: String) {
        if (taskId == null) {
            createTask(title, description)
        } else {
            updateTask(title, description)
        }
    }

    override fun populateTask() = launchSilent {
        if (taskId == null) {
            throw RuntimeException("populateTask() was called but task is new.")
        }
        val result = tasksRepository.getTask(taskId)
        when(result) {
            is Result.Success -> onTaskLoaded(result.data)
            is Result.Error -> onDataNotAvailable()
        }
    }

    fun onTaskLoaded(task: Task) = launchSilent {
        // The view may not be able to handle UI updates anymore
        if (addTaskView.isActive) {
            addTaskView.setTitle(task.title)
            addTaskView.setDescription(task.description)
        }
        isDataMissing = false
    }

    fun onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (addTaskView.isActive) {
            addTaskView.showEmptyTaskError()
        }
    }

    private fun createTask(title: String, description: String) = launchSilent {
        val newTask = Task(title, description)
        if (newTask.isEmpty) {
            addTaskView.showEmptyTaskError()
        } else {
            tasksRepository.saveTask(newTask)
            addTaskView.showTasksList()
        }
    }

    private fun updateTask(title: String, description: String) = launchSilent {
        if (taskId == null) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        tasksRepository.saveTask(Task(title, description, taskId))
        addTaskView.showTasksList() // After an edit, go back to the list.
    }
}
