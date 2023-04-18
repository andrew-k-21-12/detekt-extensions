package io.github.andrewk2112.detekt.rules.requirements

import io.github.andrewk2112.detekt.api.MustBeUsed
import io.github.andrewk2112.detekt.visitors.MustBeUsedDeclarationsVisitor
import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.*

/**
 * Checks whether everything marked with [MustBeUsed] is actually used in the code.
 *
 * With [MustBeUsed] it's easier to mark parts prone to be cleaned up time to time:
 * it can help for example to locate and remove old unused resources from the codebase.
 */
@Deprecated("Doesn't work correctly because there is no clean way to accumulate results of visiting separate files " +
            "and process all them in the end")
internal class MissingExplicitlyRequiredUsage(config: Config) : Rule(config) {

    // Overrides.

    override fun visit(root: KtFile) {
        super.visit(root)
        // Using a separate visitor for each file
        // as there can be too many entities to be processed within a single scope.
        MustBeUsedDeclarationsVisitor(bindingContext, root).run {
            root.accept(this)
            // This is a mistake to take into account only references met in a single file,
            // as a declaration made in one file can be used outside it.
            // At the same time detekt rules do not provide APIs
            // to perform processing after all files have been visited.
            annotatedDeclarations
                .filter { it.fqName?.asString() !in referencesFullNames }
                .forEach(::reportMissingRequiredUsage)
        }
    }

    /** How to activate the rule and report about its violations. */
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Maintainability,
        "Missing any usage for something explicitly declared as required to be used: consider a clean-up.",
        Debt.FIVE_MINS,
    )



    // Private.

    /**
     * The format to report about each particular [declaration] missing required usage.
     */
    private fun reportMissingRequiredUsage(declaration: KtNamedDeclaration) = report(
        CodeSmell(
            issue,
            Entity.atName(declaration),
            "${declaration.javaClass.simpleName} `${declaration.fqName?.asString()}` is unused, although it must be.",
        )
    )

}
