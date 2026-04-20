package com.github.kterelak.buildprocessplugin.build_process

import  com.github.kterelak.buildprocessplugin.build_process.TaskStatus.EXECUTED
import  com.github.kterelak.buildprocessplugin.build_process.TaskStatus.FAILED
import  com.github.kterelak.buildprocessplugin.build_process.TaskStatus.FROM_CACHE
import  com.github.kterelak.buildprocessplugin.build_process.TaskStatus.SKIPPED
import  com.github.kterelak.buildprocessplugin.build_process.TaskStatus.UP_TO_DATE
import com.intellij.openapi.diagnostic.Logger

internal class BuildReportPrinter(private val logger: Logger) {

    fun printReport(tasks: List<TaskInfo>, totalMs: Long) {
        val report = buildReport(tasks, totalMs)
        logger.warn(report)
    }

    private fun buildReport(tasks: List<TaskInfo>, totalMs: Long): String {
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

        return buildString {
            appendLine()
            appendBorder()
            appendHeaderLine("INCREMENTAL BUILD REPORT")
            appendBorderDivider()
            appendSummarySection(totalMs, totalTasks, modules.size, executed, upToDate, fromCache, skipped, failed, reusePct, rebuiltModules)
            if (failedModules.isNotEmpty()) appendFailedModulesSection(failedModules)
            if (rebuiltModules.isNotEmpty()) appendRebuiltModulesSection(rebuiltModules)
            if (unchangedModules.isNotEmpty()) appendUnchangedModulesSection(unchangedModules)
            if (executed.isNotEmpty()) appendSlowestTasksSection(executed)
            appendLine("┃")
            appendBorder()
        }
    }

    private fun StringBuilder.appendBorder() {
        appendLine("┏${"━".repeat(71)}┓")
    }

    private fun StringBuilder.appendBorderDivider() {
        appendLine("┣${"━".repeat(71)}┫")
    }

    private fun StringBuilder.appendHeaderLine(text: String) {
        val padding = (71 - text.length) / 2
        val rightPadding = 71 - padding - text.length
        appendLine("┃${"".padStart(padding)}$text${"".padStart(rightPadding)}┃")
    }

    private fun StringBuilder.appendSummarySection(
        totalMs: Long,
        totalTasks: Int,
        totalModules: Int,
        executed: List<TaskInfo>,
        upToDate: List<TaskInfo>,
        fromCache: List<TaskInfo>,
        skipped: List<TaskInfo>,
        failed: List<TaskInfo>,
        reusePct: Int,
        rebuiltModules: List<ModuleInfo>
    ) {
        appendLine("┃  Total time    : ${formatDuration(totalMs)}")
        appendLine("┃  Total tasks   : $totalTasks")
        appendLine("┃  Total modules : $totalModules")
        appendLine("┃")
        appendLine("┃  🔨 Executed    : ${executed.size} tasks in ${rebuiltModules.size} modules")
        appendLine("┃  ✅ Up-to-date  : ${upToDate.size}")
        appendLine("┃  📦 From cache  : ${fromCache.size}")
        appendLine("┃  ⏭  Skipped     : ${skipped.size}")
        appendLine("┃  ❌ Failed      : ${failed.size}")
        appendLine("┃  💡 Reuse rate  : $reusePct%")
    }

    private fun StringBuilder.appendFailedModulesSection(failedModules: List<ModuleInfo>) {
        appendLine("┃")
        appendLine("┃  ❌ FAILED MODULES:")
        for (m in failedModules) {
            appendLine("┃    ${m.name}")
            m.failedTasks.forEach { t ->
                appendLine("┃      ${shortTask(t.path, m.name)}")
            }
        }
    }

    private fun StringBuilder.appendRebuiltModulesSection(rebuiltModules: List<ModuleInfo>) {
        appendLine("┃")
        appendLine("┃  🔨 REBUILT MODULES (code changed):")
        for (m in rebuiltModules.sortedByDescending { it.totalDurationMs }) {
            appendLine("┃")
            appendLine("┃    🟢 ${m.name}  [${formatDuration(m.totalDurationMs)}]")
            m.executedTasks.sortedByDescending { it.durationMs }.forEach { t ->
                appendLine("┃       🔨 ${shortTask(t.path, m.name)} [${formatDuration(t.durationMs)}]")
            }
            val otherCount = m.upToDateTasks.size + m.fromCacheTasks.size + m.skippedTasks.size
            if (otherCount > 0) {
                appendLine("┃       ⚪ ... $otherCount tasks unchanged")
            }
        }
    }

    private fun StringBuilder.appendUnchangedModulesSection(unchangedModules: List<ModuleInfo>) {
        appendLine("┃")
        appendLine("┃  ⚪ UNCHANGED MODULES (${unchangedModules.size}):")
        unchangedModules.forEach { m ->
            appendLine("┃    ${m.status.toIcon()} ${m.name} (${m.upToDateTasks.size + m.fromCacheTasks.size + m.skippedTasks.size} tasks)")
        }
    }

    private fun StringBuilder.appendSlowestTasksSection(executed: List<TaskInfo>) {
        appendLine("┃")
        appendLine("┃  ⏱  SLOWEST REBUILT TASKS:")
        executed.sortedByDescending { it.durationMs }.take(10).forEach { t ->
            appendLine("┃    ${formatDuration(t.durationMs).padEnd(8)} ${t.path}")
        }
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

private fun TaskStatus.toIcon(): String = when (this) {
    UP_TO_DATE -> "✅"
    FROM_CACHE -> "📦"
    SKIPPED -> "⏭ "
    else -> "⚪"
}
