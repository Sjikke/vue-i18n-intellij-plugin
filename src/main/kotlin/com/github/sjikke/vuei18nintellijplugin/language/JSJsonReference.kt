package com.github.sjikke.vuei18nintellijplugin.language

import com.github.sjikke.vuei18nintellijplugin.util.JsonUtil
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.ResolveResult

class JSJsonReference(element: PsiElement, pathRange: TextRange, textRange: TextRange) :
    PsiReferenceBase<PsiElement?>(element, textRange), PsiPolyVariantReference {
    companion object {
        const val MAX_LEN = 20
    }

    private val key: String = element.text.substring(textRange.startOffset, textRange.endOffset)
    private val path: String = element.text.substring(pathRange.startOffset, textRange.endOffset)

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val project = myElement!!.project
        val properties: List<JsonProperty> = JsonUtil.findJsonProperties(project, path, false)
        val results: MutableList<ResolveResult> = ArrayList()
        for (property in properties) {
            results.add(PsiElementResolveResult(property))
        }
        return results.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.size == 1) resolveResults[0].element else null
    }

    override fun getVariants(): Array<Any> {
        val project = myElement!!.project
        val properties: List<JsonProperty> = JsonUtil.findJsonProperties(project, path, false)
        val variants: MutableList<LookupElement> = ArrayList()
        for (property in properties) {
            val path = JsonUtil.pathForJsonProperty(property)
            if (path != null && path.isNotEmpty()) {
                variants.add(
                    LookupElementBuilder
                        .create(property)
                        .withTailText(tailTextForJsonProperty(property))
                        .withTypeText(property.containingFile.name)
                )
            }
        }
        return variants.toTypedArray()
    }

    private fun tailTextForJsonProperty(jsonProperty: JsonProperty): String? {
        var resultText: String? = null
        val value = jsonProperty.value
        if (value is JsonObject) {
            resultText = " {...}"
        } else if (value is JsonArray) {
            resultText = " [...]"
        } else if (value is JsonLiteral) {
            val unwrapped = StringUtil.stripQuotesAroundValue(value.text)
            resultText = " " + if (unwrapped.length > MAX_LEN) unwrapped.substring(0, MAX_LEN) + "..." else unwrapped
        }

        return resultText
    }
}
