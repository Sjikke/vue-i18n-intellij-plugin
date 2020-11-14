package com.github.sjikke.vuei18nintellijplugin.services

import com.github.sjikke.vuei18nintellijplugin.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
