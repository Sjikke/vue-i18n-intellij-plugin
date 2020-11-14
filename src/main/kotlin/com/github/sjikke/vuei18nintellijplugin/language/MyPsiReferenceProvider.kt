package com.github.sjikke.vuei18nintellijplugin.language

import com.github.sjikke.vuei18nintellijplugin.util.PsiUtil
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext

class MyPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (element.text.length <= 2) {
            return emptyArray()
        }

        val result = ArrayList<PsiReference>()
        if (PsiUtil.isEligibleJSLiteralExpression(element)) {
            var modifier = 0
            if (element.text.contains("IntellijIdeaRulezzz ")) {
                modifier = "IntellijIdeaRulezzz ".length
            }
            val textRange = TextRange(1, element.text.length - 1 - modifier)
            if (textRange.length > 0) {
                result.addAll(buildReferences(element, textRange))
            }
        }
        return result.toTypedArray()
    }

    private fun buildReferences(element: PsiElement, textRange: TextRange): List<JSJsonReference> {
        val text = element.text.substring(textRange.startOffset, textRange.endOffset)
        var accumulatedStart = textRange.startOffset
        return text.split(".").map { t ->
            val res = JSJsonReference(element, textRange, TextRange(accumulatedStart, accumulatedStart + t.length))
            accumulatedStart += t.length + 1
            res
        }
    }
}
