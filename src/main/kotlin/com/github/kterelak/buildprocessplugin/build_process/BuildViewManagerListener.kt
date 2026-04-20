package com.github.kterelak.buildprocessplugin.build_process

import com.github.kterelak.buildprocessplugin.build_process.TaskStatus.EXECUTED
import com.github.kterelak.buildprocessplugin.build_process.TaskStatus.FAILED
import com.github.kterelak.buildprocessplugin.build_process.TaskStatus.FROM_CACHE
import com.github.kterelak.buildprocessplugin.build_process.TaskStatus.SKIPPED
import com.github.kterelak.buildprocessplugin.build_process.TaskStatus.UP_TO_DATE
import com.intellij.build.BuildProgressListener
import com.intellij.build.events.BuildEvent
import com.intellij.build.events.FinishBuildEvent
import com.intellij.build.events.FinishEvent
import com.intellij.build.events.MessageEvent
import com.intellij.build.events.OutputBuildEvent
import com.intellij.build.events.StartBuildEvent
import com.intellij.build.events.StartEvent
import com.intellij.build.events.impl.FailureResultImpl
import com.intellij.build.events.impl.SkippedResultImpl
import com.intellij.build.events.impl.SuccessResultImpl
import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

internal class BuildViewManagerListener : BuildProgressListener {

    // Define logger
    private val log = Logger.getInstance(BuildViewManagerListener::class.java)
    private val reportPrinter = BuildReportPrinter(log)

    private val tasks = CopyOnWriteArrayList<TaskInfo>()
    private val taskStartTimes = ConcurrentHashMap<String, Long>()

    // Fallback: statuses parsed from Gradle output ("> Task :foo UP-TO-DATE")
    private val outputStatuses = ConcurrentHashMap<String, TaskStatus>()

    private var buildStartMs = 0L
    private var diagnosticLogged = false

    override fun onEvent(buildId: Any, event: BuildEvent) {
        when (event) {
            is StartBuildEvent -> onBuildStarted()
            is FinishBuildEvent -> onBuildFinished()
            is StartEvent -> taskStartTimes[event.message] = System.currentTimeMillis()
            is FinishEvent -> onTaskFinished(event)
            is OutputBuildEvent -> parseGradleOutput(event.message)
            is MessageEvent -> { /* ignore */ }
        }
    }

    private fun onBuildStarted() {
        tasks.clear()
        taskStartTimes.clear()
        outputStatuses.clear()
        diagnosticLogged = false
        buildStartMs = System.currentTimeMillis()
        log.warn("[BuildProcess] ━━━ BUILD STARTED ━━━")
    }

    private fun onTaskFinished(event: FinishEvent) {
        val name = event.message
        // Skip non-task events (e.g. "Run build", project-level events)
        if (!name.startsWith(":")) return

        val startTime = taskStartTimes.remove(name)
        val duration = if (startTime != null) System.currentTimeMillis() - startTime else 0L

        val status = when (val result = event.result) {
            is SkippedResultImpl -> SKIPPED
            is FailureResultImpl -> FAILED
            is SuccessResultImpl -> {
                if (result.isUpToDate) UP_TO_DATE
                else {
                    // Check fallback from output parsing
                    outputStatuses[name] ?: EXECUTED
                }
            }

            else -> outputStatuses[name] ?: EXECUTED
        }

        tasks.add(TaskInfo(name, status, duration))
    }

    private fun onBuildFinished() {
        val totalMs = System.currentTimeMillis() - buildStartMs
        reportPrinter.printReport(tasks, totalMs)
    }

    // Parse Gradle output as fallback
    private fun parseGradleOutput(text: String) {
        for (line in text.lines()) {
            val trimmed = line.trim()
            if (!trimmed.startsWith("> Task :")) continue
            val taskLine = trimmed.removePrefix("> Task ").trim()
            when {
                taskLine.endsWith(" UP-TO-DATE") ->
                    outputStatuses[taskLine.removeSuffix(" UP-TO-DATE")] = UP_TO_DATE

                taskLine.endsWith(" FROM-CACHE") ->
                    outputStatuses[taskLine.removeSuffix(" FROM-CACHE")] = FROM_CACHE

                taskLine.endsWith(" SKIPPED") ->
                    outputStatuses[taskLine.removeSuffix(" SKIPPED")] = SKIPPED

                taskLine.endsWith(" NO-SOURCE") ->
                    outputStatuses[taskLine.removeSuffix(" NO-SOURCE")] = SKIPPED

                taskLine.endsWith(" FAILED") ->
                    outputStatuses[taskLine.removeSuffix(" FAILED")] = FAILED
            }
            // No suffix = executed, but we don't store that (it's the default)
        }
    }
}

