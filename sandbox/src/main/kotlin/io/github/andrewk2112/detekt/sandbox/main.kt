package io.github.andrewk2112.detekt.sandbox

import FromDependentModule
import io.github.andrewk2112.detekt.api.MustBeUsed

@MustBeUsed
fun truePositive() {}

@MustBeUsed
fun someUsedFunction() {}

fun main() {
    someUsedFunction()
    println(SomeClass().falsePositive)
    println(FromDependentModule.falsePositive)
}
