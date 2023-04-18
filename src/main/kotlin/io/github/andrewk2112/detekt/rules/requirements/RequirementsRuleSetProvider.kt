package io.github.andrewk2112.detekt.rules.requirements

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/**
 * Exposes all rule sets of the requirements group.
 */
internal class RequirementsRuleSetProvider : RuleSetProvider {

    override fun instance(config: Config) = RuleSet(
        ruleSetId,
        listOf(
            MissingExplicitlyRequiredUsage(config),
        ),
    )

    /** The name (identifier) to be used in detekt config files to include and activate this group. */
    override val ruleSetId: String = "requirements"

}
