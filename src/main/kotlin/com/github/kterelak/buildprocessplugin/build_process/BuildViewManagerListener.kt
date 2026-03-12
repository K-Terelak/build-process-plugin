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

internal class BuildViewManagerListener : BuildProgressListener {

    // Define logger
    private val log = Logger.getInstance(BuildViewManagerListener::class.java)

    private val tasks = mutableListOf<TaskInfo>()
    private val taskStartTimes = mutableMapOf<String, Long>()

    // Fallback: statuses parsed from Gradle output ("> Task :foo UP-TO-DATE")
    private val outputStatuses = mutableMapOf<String, TaskStatus>()

    private var buildStartMs = 0L
    private var diagnosticLogged = false

    override fun onEvent(buildId: Any, event: BuildEvent) {
        when (event) {
            is StartBuildEvent -> onBuildStarted()
            is FinishBuildEvent -> onBuildFinished()
            is StartEvent -> taskStartTimes[event.message] = System.currentTimeMillis()
            is FinishEvent -> onTaskFinished(event)
            is OutputBuildEvent -> parseGradleOutput(event.message)
            is MessageEvent -> { /* ignore */
            }
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

        // Log first few events for diagnostics
        if (!diagnosticLogged && tasks.size < 3) {
            val r = event.result
            log.warn(
                "[BuildProcess] DIAG: task=$name result=${r.javaClass.simpleName} " +
                        "isUpToDate=${(r as? SuccessResultImpl)?.isUpToDate} " +
                        "fields=${r.javaClass.declaredFields.map { it.name }}"
            )
            if (tasks.size == 2) diagnosticLogged = true
        }

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
        printReport(totalMs)
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

    //  Report
    private fun printReport(totalMs: Long) {
        val executed = tasks.filter { it.status == EXECUTED }
        val upToDate = tasks.filter { it.status == UP_TO_DATE }
        val fromCache = tasks.filter { it.status == FROM_CACHE }
        val skipped = tasks.filter { it.status == SKIPPED }
        val failed = tasks.filter { it.status == FAILED }

        val totalTasks = tasks.size
        val reused = upToDate.size + fromCache.size + skipped.size
        val reusePct = if (totalTasks > 0) reused * 100 / totalTasks else 0

        val modules = tasks
            .groupBy { extractModule(it.path) }
            .map { (module, moduleTasks) ->
                ModuleInfo(
                    name = module,
                    executedTasks = moduleTasks.filter { it.status == EXECUTED },
                    upToDateTasks = moduleTasks.filter { it.status == UP_TO_DATE },
                    fromCacheTasks = moduleTasks.filter { it.status == FROM_CACHE },
                    skippedTasks = moduleTasks.filter { it.status == SKIPPED },
                    failedTasks = moduleTasks.filter { it.status == FAILED },
                    totalDurationMs = moduleTasks.sumOf { it.durationMs }
                )
            }
            .sortedBy { it.name }

        val rebuiltModules = modules.filter { it.status == EXECUTED }
        val failedModules = modules.filter { it.status == FAILED }
        val unchangedModules = modules.filter { it.status != EXECUTED && it.status != FAILED }

        // Build report
        val sb = StringBuilder()
        sb.appendLine()
        sb.appendLine("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓")
        sb.appendLine("┃                      INCREMENTAL BUILD REPORT                       ┃")
        sb.appendLine("┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫")
        sb.appendLine("┃  Total time    : ${formatDuration(totalMs)}")
        sb.appendLine("┃  Total tasks   : $totalTasks")
        sb.appendLine("┃  Total modules : ${modules.size}")
        sb.appendLine("┃")
        sb.appendLine("┃  🔨 Executed    : ${executed.size} tasks in ${rebuiltModules.size} modules")
        sb.appendLine("┃  ✅ Up-to-date  : ${upToDate.size}")
        sb.appendLine("┃  📦 From cache  : ${fromCache.size}")
        sb.appendLine("┃  ⏭  Skipped     : ${skipped.size}")
        sb.appendLine("┃  ❌ Failed      : ${failed.size}")
        sb.appendLine("┃  💡 Reuse rate  : $reusePct%")

        // Failed modules
        if (failedModules.isNotEmpty()) {
            sb.appendLine("┃")
            sb.appendLine("┃  ❌ FAILED MODULES:")
            for (m in failedModules) {
                sb.appendLine("┃    ${m.name}")
                m.failedTasks.forEach { t ->
                    sb.appendLine("┃      ${shortTask(t.path, m.name)}")
                }
            }
        }

        // rebuilt modules
        if (rebuiltModules.isNotEmpty()) {
            sb.appendLine("┃")
            sb.appendLine("┃  🔨 REBUILT MODULES (code changed):")
            for (m in rebuiltModules.sortedByDescending { it.totalDurationMs }) {
                sb.appendLine("┃")
                sb.appendLine("┃    🟢 ${m.name}  [${formatDuration(m.totalDurationMs)}]")
                m.executedTasks.sortedByDescending { it.durationMs }.forEach { t ->
                    sb.appendLine(
                        "┃       🔨 ${shortTask(t.path, m.name)} [${formatDuration(t.durationMs)}]"
                    )
                }
                // Also show up-to-date tasks in the module (greyed out)
                val otherCount = m.upToDateTasks.size + m.fromCacheTasks.size + m.skippedTasks.size
                if (otherCount > 0) {
                    sb.appendLine("┃       ⚪ ... $otherCount tasks unchanged")
                }
            }
        }

        // Unchanged modules
        if (unchangedModules.isNotEmpty()) {
            sb.appendLine("┃")
            sb.appendLine("┃  ⚪ UNCHANGED MODULES (${unchangedModules.size}):")
            unchangedModules.forEach { m ->
                val icon = when (m.status) {
                    UP_TO_DATE -> "✅"
                    FROM_CACHE -> "📦"
                    SKIPPED -> "⏭ "
                    else -> "⚪"
                }
                sb.appendLine("┃    $icon ${m.name} (${m.upToDateTasks.size + m.fromCacheTasks.size + m.skippedTasks.size} tasks)")
            }
        }

        // Slowest executed tasks (10)
        if (executed.isNotEmpty()) {
            sb.appendLine("┃")
            sb.appendLine("┃  ⏱  SLOWEST REBUILT TASKS:")
            executed.sortedByDescending { it.durationMs }.take(10).forEach { t ->
                sb.appendLine("┃    ${formatDuration(t.durationMs).padEnd(8)} ${t.path}")
            }
        }

        sb.appendLine("┃")
        sb.appendLine("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛")
        log.warn(sb.toString())
    }
}

private fun extractModule(taskPath: String): String {
    val lastColon = taskPath.lastIndexOf(':')
    return if (lastColon > 0) taskPath.substring(0, lastColon) else ":"
}

private fun shortTask(taskPath: String, module: String): String {
    return taskPath.removePrefix(module).removePrefix(":")
}

private fun formatDuration(ms: Long): String = when {
    ms < 1000 -> "${ms}ms"
    ms < 60000 -> "%.1fs".format(ms / 1000.0)
    else -> "%dm %ds".format(ms / 60000, (ms % 60000) / 1000)
}
