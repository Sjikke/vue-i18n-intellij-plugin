package com.github.sjikke.vuei18nintellijplugin.util

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.search.FilenameIndex
import org.apache.commons.lang3.ArrayUtils
import java.util.stream.Collectors

class JsonUtil private constructor() {
    companion object {
        fun findJsonProperties(project: Project, key: String, strict: Boolean): List<JsonProperty> {
            val result = ArrayList<JsonProperty>()
            val files = findLocalFiles(project)
            for (file in files) {
                for (jsonProperty in getJsonProperties(project, file)) {
                    val path = pathForJsonProperty(jsonProperty)
                    if (path != null && keyMatchesPath(strict, path, key)) {
                        result.add(jsonProperty)
                    }
                }
            }

            return result.sortedBy { it.containingFile.name.toLowerCase().indexOf("en") * -1 }
        }

        fun pathForJsonProperty(jsonProperty: JsonProperty): String? {
            var result = ""
            var el = jsonProperty as PsiElement?
            while (el != null) {
                if (el is JsonProperty) {
                    result = el.name + result
                } else if (el is JsonObject) {
                    result = ".$result"
                }
                if (el.parent is JsonArray) {
                    val index = ArrayUtils.indexOf(el.parent.children, el)
                    result = ".$index$result"
                }
                el = el.parent
            }

            if (result.startsWith(".")) {
                result = result.substring(1)
            }
            return result
        }

        private fun keyMatchesPath(strict: Boolean, path: String, key: String) =
            (!strict && pathIsParent(path, key)) || (strict && path == key)

        private fun pathIsParent(path: String, parent: String): Boolean {
            return path.startsWith(parent) && path.split(".").size == parent.split(".").size
        }

        private fun findLocalFiles(project: Project): MutableSet<VirtualFile> {
            val jsonFiles = FilenameIndex.getAllFilesByExt(project, "json")
            return jsonFiles.stream().filter {
                val correctStart = it.name.startsWith("locale-") || it.name.startsWith("messages.")
                correctStart && PsiUtil.hasAncestorNamed(it, "public")
            }.collect(Collectors.toSet())
        }

        private fun getJsonProperties(project: Project, file: VirtualFile): ArrayList<JsonProperty> {
            val result = ArrayList<JsonProperty>()
            val psiFile = PsiManager.getInstance(project).findFile(file)
            if (psiFile is JsonFile) {
                val tlv = psiFile.topLevelValue
                if (tlv is JsonObject) {
                    tlv.accept(
                        object : PsiRecursiveElementWalkingVisitor() {
                            override fun visitElement(element: PsiElement) {
                                super.visitElement(element)
                                if (element is JsonObject) {
                                    result.addAll(element.propertyList)
                                }
                            }
                        }
                    )
                }
            }
            return result
        }
    }
}
