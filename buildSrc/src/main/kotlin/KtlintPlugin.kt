import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

private const val KTLINT_VERSION = "0.42.1"
private const val KTLINT = "com.pinterest:ktlint:$KTLINT_VERSION"

class KtlintPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val ktlint = target.configurations.create("ktlint") {
            dependencies.add(target.dependencies.create(KTLINT))
        }

        target.tasks.register("ktlint", JavaExec::class.java) {
            group = "verification"
            description = "Check Kotlin code style."
            classpath = ktlint
            mainClass.set("com.pinterest.ktlint.Main")
            args("--android", "src/**/*.kt")
        }

        target.tasks.getByName("check").dependsOn.add("ktlint")

        target.tasks.register("ktlintFormat", JavaExec::class.java) {
            group = "formatting"
            description = "Fix Kotlin code style deviations."
            classpath = ktlint
            mainClass.set("com.pinterest.ktlint.Main")
            args("--android", "-F", "src/**/*.kt")
        }
    }
}
