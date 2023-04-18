import io.github.andrewk2112.detekt.api.MustBeUsed

@MustBeUsed
object FromDependentModule {

    @MustBeUsed
    val falsePositive = "456"

}
