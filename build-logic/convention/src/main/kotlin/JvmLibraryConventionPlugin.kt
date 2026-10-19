import com.flux.buildlogic.configureKotlinJvm
import com.flux.buildlogic.configureQuality
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Pure Kotlin module (no Android): model, domain, ai. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            configureKotlinJvm()
            configureQuality()
        }
    }
}
