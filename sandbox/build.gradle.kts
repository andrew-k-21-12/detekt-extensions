import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
}

// By using this way of detekt configuration no sources from dependent modules will be included for checking.
detekt {
    config = files("$projectDir/config/detekt.yml") // overriding the path to the detekt configuration
}

/**
 * An example of task to run detekt checks on this project and its dependent modules.
 */
val detektAll by tasks.registering(Detekt::class) {
    description = "Runs over the whole codebase including dependent modules without the starting overhead for each."
    parallel    = true
    config.setFrom("$projectDir/config/detekt.yml")
    setSource(projectDir) // including only the current project initially
    include("**/*.kt")
    exclude("**/resources/**")
    exclude("**/build/**")
    reports {
        arrayOf(html, sarif, txt, xml).forEach { it.required.set(false) }
    }
}

/**
 * Retrieves a project by its [path] and adds it to be checked by detekt in the scope of [detektAll].
 */
fun DependencyHandler.projectWithDetekt(path: String): ProjectDependency = project(path).apply {
    detektAll.get().apply {
        setSource(source.plus(dependencyProject.projectDir))
    }
}

dependencies {
    detektPlugins(rootProject)  // including custom rules and processors from a local project,
    implementation(rootProject) // some sources from custom detekt extensions are needed to be used in the code as well
    implementation(projectWithDetekt(":sandbox-dependency"))
}
