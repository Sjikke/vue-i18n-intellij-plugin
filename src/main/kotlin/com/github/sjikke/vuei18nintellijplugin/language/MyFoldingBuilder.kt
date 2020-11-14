package com.github.sjikke.vuei18nintellijplugin.language

import com.github.sjikke.vuei18nintellijplugin.util.JsonUtil
import com.github.sjikke.vuei18nintellijplugin.util.PsiUtil
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import java.util.Collections
import kotlin.collections.ArrayList

class MyFoldingBuilder : FoldingBuilderEx(), DumbAware {
    companion object {
        const val MAX_LEN = 60
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val group = FoldingGroup.newGroup("vue-i18n")
        // Initialize the list of folding regions
        val descriptors: MutableList<FoldingDescriptor> = ArrayList()
        // Get a collection of the literal expressions in the document below root
        val literalExpressions: Collection<JSLiteralExpression> = PsiTreeUtil.findChildrenOfType(
            root,
            JSLiteralExpression::class.java
        )

        // Evaluate the collection
        for (literalExpression in literalExpressions) {
            if (!PsiUtil.isEligibleJSLiteralExpression(literalExpression)) {
                continue
            }

            val textRange = TextRange(1, literalExpression.text.length - 1)
            if (textRange.length > 0) {
                val props = JsonUtil.findJsonProperties(
                    literalExpression.project,
                    literalExpression.text.substring(textRange.startOffset, textRange.endOffset),
                    true
                )
                if (props.isNotEmpty()) {
                    // Add a folding descriptor for the literal expression at this node.
                    descriptors.add(
                        FoldingDescriptor(
                            literalExpression.node,
                            TextRange(
                                literalExpression.textRange.startOffset + 1,
                                literalExpression.textRange.endOffset - 1
                            ),
                            group,
                            Collections.EMPTY_SET,
                            false,
                            null,
                            null
                        )
                    )
                }
            }
        }
        return descriptors.toTypedArray()
    }

    /**
     * Gets the Simple Language 'value' string corresponding to the 'key'
     *
     * @param node Node corresponding to PsiLiteralExpression containing a string in the format
     * SIMPLE_PREFIX_STR + SIMPLE_SEPARATOR_STR + Key, where Key is
     * defined by the Simple language file.
     */
    override fun getPlaceholderText(node: ASTNode): String? {
        val retTxt = "..."
        if (node.psi is JSLiteralExpression) {
            val nodeElement: JSLiteralExpression = node.psi as JSLiteralExpression
            val key: String = nodeElement.stringValue!!
            val properties = JsonUtil.findJsonProperties(nodeElement.project, key, true)
            var place: String? = null
            val jsonValue = properties.firstOrNull()?.value
            place = if (jsonValue is JsonStringLiteral) {
                jsonValue.value
            } else {
                jsonValue?.text
            }
            // IMPORTANT: keys can come with no values, so a test for null is needed
            // IMPORTANT: Convert embedded \n to backslash n, so that the string will look
            // like it has LF embedded in it and embedded " to escaped "
            place = place?.replace("\n".toRegex(), "\\\\n")?.replace("\"".toRegex(), "\\\\\"")
            return if (place == null || place.length < MAX_LEN) place ?: retTxt else place.substring(0, MAX_LEN) + "..."
        }
        return retTxt
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }
}
