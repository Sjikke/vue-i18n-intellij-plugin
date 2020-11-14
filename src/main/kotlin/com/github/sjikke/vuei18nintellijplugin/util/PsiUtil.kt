package com.github.sjikke.vuei18nintellijplugin.util

import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.anyDescendantOfType

class PsiUtil private constructor() {

    companion object {
        fun isEligibleJSLiteralExpression(element: PsiElement): Boolean {
            val argumentList = findArgumentList(element)
            var result = false
            if (argumentList != null) {
                val isFirst = isFirstArgument(element, argumentList)

                if (isFirst) {
                    val callExpr = findCallExpression(element)
                    val expression = callExpr?.methodExpression
                    if (expression is JSReferenceExpression) {
                        result = "\$t" == expression.referenceName
                    }
                }
            }
            return result
        }

        fun hasAncestorNamed(virtualFile: VirtualFile, ancestorName: String): Boolean {
            var current = virtualFile as VirtualFile?
            while (current != null) {
                if (current.name == ancestorName) {
                    return true
                }
                current = current.parent
            }
            return false
        }

        private fun isFirstArgument(psiElement: PsiElement, argumentList: JSArgumentList): Boolean {
            return argumentList.arguments[0].anyDescendantOfType<PsiElement>() {
                it == psiElement
            }
        }

        private fun findCallExpression(psiElement: PsiElement): JSCallExpression? {
            var el = psiElement as PsiElement?
            while (el != null) {
                if (el is JSCallExpression) {
                    return el
                }

                el = el.parent
            }
            return null
        }

        private fun findArgumentList(psiElement: PsiElement): JSArgumentList? {
            var el = psiElement as PsiElement?
            while (el != null) {
                if (el is JSArgumentList) {
                    return el
                }

                el = el.parent
            }
            return null
        }
    }
}
