package io.github.andrewk2112.detekt.visitors

import io.github.andrewk2112.detekt.api.MustBeUsed
import io.github.detekt.psi.internal.FullQualifiedNameGuesser
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.rules.fqNameOrNull
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

/**
 * Looks for [MustBeUsed]-annotated declarations and all references used.
 *
 * It may seem more rational to split this class into two visitors
 * to find target declarations and all references separately,
 * but it's going to affect performance as it will introduce two passes instead of single one.
 */
@Deprecated("This class is pointless as there is no way to find the declaration for a visited reference correctly, " +
            "it's more like a token-based reader of source files rather than a full-fledged dependency graph analyzer")
internal class MustBeUsedDeclarationsVisitor private constructor(
    private val bindingContext: BindingContext,
    private val fullQualifiedNameGuesser: FullQualifiedNameGuesser,
) : DetektVisitor() {

    // Overrides.

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        super.visitClassOrObject(classOrObject)
        classOrObject.addAsAnnotatedIfNeeded()
    }

    override fun visitNamedFunction(function: KtNamedFunction) {
        super.visitNamedFunction(function)
        function.addAsAnnotatedIfNeeded()
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        property.addAsAnnotatedIfNeeded()
    }

    override fun visitReferenceExpression(expression: KtReferenceExpression) {
        super.visitReferenceExpression(expression)
        if (expression.run { isPackageDirective() || isImportDirective() }) return // no interest in packages, imports
        expression.getFullQualifiedName()?.let { _referencesFullNames.add(it) }
    }



    // Internal.

    /**
     * A convenience constructor to accept simple arguments instead of actual dependencies.
     */
    internal constructor(
        bindingContext: BindingContext,
        root: KtFile,
    ) : this(bindingContext, FullQualifiedNameGuesser(root))

    /** Exposes all [KtNamedDeclaration]s found annotated with [MustBeUsed]. */
    internal inline val annotatedDeclarations: Set<KtNamedDeclaration> get() = _annotatedDeclarations

    /** Exposes all full names of references used in the visited file. */
    internal inline val referencesFullNames: Set<String> get() = _referencesFullNames



    // Private.

    /**
     * Checks whether a [KtNamedDeclaration] is annotated with [MustBeUsed] and adds it to the corresponding set.
     */
    private fun KtNamedDeclaration.addAsAnnotatedIfNeeded() {
        annotationEntries
            .mapNotNull {
                it.typeReference?.run { getFullQualifiedNameFromContext() ?: getFullQualifiedNameFromGuesser() }
            }
            .any { it == MustBeUsed::class.java.name }
            .ifTrue {
                _annotatedDeclarations.add(this)
            }
    }

    /**
     * Attempts to get a full qualified name of a [KtTypeReference] by using a [BindingContext].
     */
    private fun KtTypeReference.getFullQualifiedNameFromContext(): String? =
        bindingContext[BindingContext.TYPE, this]?.fqNameOrNull()?.asString()

    /**
     * Attempts to get a full qualified name of a [KtTypeReference] by using a [FullQualifiedNameGuesser].
     */
    private fun KtTypeReference.getFullQualifiedNameFromGuesser(): String? =
        fullQualifiedNameGuesser
            .getFullQualifiedName(text)
            .minByOrNull { it.length } // selecting the shortest guess allows to pick the right package

    /**
     * If this [PsiElement] or some of its parents is represented by [KtPackageDirective].
     */
    private fun PsiElement.isPackageDirective(): Boolean = this isRecursively KtPackageDirective::class.java

    /**
     * If this [PsiElement] or some of its parents is represented by [KtImportDirective].
     */
    private fun PsiElement.isImportDirective(): Boolean = this isRecursively KtImportDirective::class.java

    /**
     * If this [PsiElement] or some of its parents is instance of the [targetClass].
     */
    private infix fun PsiElement.isRecursively(targetClass: Class<out PsiElement>): Boolean =
        targetClass.isAssignableFrom(javaClass) || parent?.isRecursively(targetClass) == true

    /**
     * Attempts to get a full qualified name of a [KtReferenceExpression].
     */
    private fun KtReferenceExpression.getFullQualifiedName(): String? =
        fullQualifiedNameGuesser
            .getFullQualifiedName(text.removeSurrounding("`"))
            .maxByOrNull { it.length } // the longest guess represents a better identifiable reference name

    private val _annotatedDeclarations = mutableSetOf<KtNamedDeclaration>()
    private val _referencesFullNames   = mutableSetOf<String>()

}
