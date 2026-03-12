package com.github.kterelak.buildprocessplugin.startup

import com.github.kterelak.buildprocessplugin.build_process.BuildViewManagerListener
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class MyProjectActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        thisLogger().debug("[BuildProcess] Registering build listener for: ${project.name}")

        try {
            val bvm = project.getService(com.intellij.build.BuildViewManager::class.java)
            @Suppress("UnstableApiUsage")
            bvm.addListener(BuildViewManagerListener(), project)
            thisLogger().debug("[BuildProcess] BuildViewManagerListener registered successfully")
        } catch (e: Exception) {
            thisLogger().warn("[BuildProcess] Registration FAILED: ${e.message}", e)
        }
    }
}
