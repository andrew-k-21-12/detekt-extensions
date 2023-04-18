package io.github.andrewk2112.detekt.sandbox

import io.github.andrewk2112.detekt.api.MustBeUsed

@MustBeUsed
class SomeClass {

    @MustBeUsed
    val falsePositive = 123

}
