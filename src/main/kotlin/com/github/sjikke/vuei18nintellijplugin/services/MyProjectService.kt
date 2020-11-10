package com.github.sjikke.vuei18nintellijplugin.services

import com.intellij.openapi.project.Project
import com.github.sjikke.vuei18nintellijplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
