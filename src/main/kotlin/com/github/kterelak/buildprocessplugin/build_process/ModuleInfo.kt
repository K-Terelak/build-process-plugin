package com.github.kterelak.buildprocessplugin.build_process

import com.github.kterelak.buildprocessplugin.build_process.TaskStatus.*

internal data class ModuleInfo(
    val name: String,
    val executedTasks: List<TaskInfo>,
    val upToDateTasks: List<TaskInfo>,
    val fromCacheTasks: List<TaskInfo>,
    val skippedTasks: List<TaskInfo>,
    val failedTasks: List<TaskInfo>,
    val totalDurationMs: Long
) {
    val status: TaskStatus
        get() = when {
            failedTasks.isNotEmpty() -> FAILED
            executedTasks.isNotEmpty() -> EXECUTED
            fromCacheTasks.isNotEmpty() -> FROM_CACHE
            upToDateTasks.isNotEmpty() -> UP_TO_DATE
            else -> SKIPPED
        }
}
