package io.github.andrewk2112.detekt.processors

import io.github.andrewk2112.detekt.api.MustBeUsed
import io.github.andrewk2112.detekt.visitors.MustBeUsedDeclarationsVisitor
import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.resolve.BindingContext

/**
 * Walks through the entire codebase
 * and checks whether everything marked with [MustBeUsed] is actually used in the code.
 */
internal class MissingExplicitlyRequiredUsageProcessor : FileProcessListener {

    // Overrides.

    override fun onProcess(file: KtFile, bindingContext: BindingContext) {
        MustBeUsedDeclarationsVisitor(bindingContext, file).apply(file::accept).run {
            allAnnotatedDeclarations.addAll(annotatedDeclarations)
            allReferencesFullNames.addAll(referencesFullNames)
        }
    }

    override fun onFinish(files: List<KtFile>, result: Detektion, bindingContext: BindingContext) = result.add(
        MissingExplicitlyRequiredUsageReport(
            allAnnotatedDeclarations.filter { it.fqName?.asString() !in allReferencesFullNames }
        )
    )



    // Private.

    private val allAnnotatedDeclarations = mutableSetOf<KtNamedDeclaration>()
    private val allReferencesFullNames   = mutableSetOf<String>()

}

/**
 * A little hack based on the [ProjectMetric] to prepare detailed reports for missing explicitly required usages.
 */
private class MissingExplicitlyRequiredUsageReport private constructor(
    fullReport: String,
    numberOfUnused: Int,
) : ProjectMetric(fullReport, numberOfUnused) {

    constructor(unusedDeclarations: Collection<KtNamedDeclaration>) : this(
        "The following declarations are unused, although they must be:\n" +
                unusedDeclarations.joinToString(separator = "\n", postfix = "\n") {
                    "\t\t${it.javaClass.simpleName} ${Entity.atName(it).compact()}"
                },
        unusedDeclarations.size
    )

}
