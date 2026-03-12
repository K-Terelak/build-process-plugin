package com.github.kterelak.buildprocessplugin.build_process

internal data class TaskInfo(
    val path: String,
    val status: TaskStatus,
    val durationMs: Long
)
