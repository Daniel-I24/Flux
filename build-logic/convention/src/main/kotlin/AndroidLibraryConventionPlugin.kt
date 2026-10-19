import com.android.build.api.dsl.LibraryExtension
import com.flux.buildlogic.configureKotlinAndroid
import com.flux.buildlogic.configureQuality
import com.flux.buildlogic.libs
import com.flux.buildlogic.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")
            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                testOptions.targetSdk = libs.version("targetSdk").toInt()
                buildTypes {
                    release {
                        isMinifyEnabled = false
                        consumerProguardFiles("consumer-rules.pro")
                    }
                }
            }
            configureQuality()
        }
    }
}
