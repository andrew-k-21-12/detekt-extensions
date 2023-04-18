package io.github.andrewk2112.detekt.api

/**
 * Marks classes, functions and properties as required to be used in the code.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class MustBeUsed
