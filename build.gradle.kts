plugins {
    kotlin("jvm")
}

group   = "io.github.andrew-k-21-12"
version = "1.0.0"

dependencies {
    compileOnly(libs.detekt.api)
}
