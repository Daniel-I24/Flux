import com.android.build.api.dsl.ApplicationExtension
import com.flux.buildlogic.configureKotlinAndroid
import com.flux.buildlogic.configureQuality
import com.flux.buildlogic.configureReleaseSigning
import com.flux.buildlogic.libs
import com.flux.buildlogic.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Phone and watch share the same applicationId and signing key: the Wearable
 * Data Layer only connects apps with identical package name and signature.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.android")
            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig {
                    applicationId = "com.flux.notes"
                    targetSdk = libs.version("targetSdk").toInt()
                    versionCode = providers.gradleProperty("flux.versionCode").get().toInt()
                    versionName = providers.gradleProperty("flux.versionName").get()
                }
                buildFeatures.buildConfig = true
                packaging.resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "/META-INF/*.kotlin_module")
                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                    }
                }
                defaultConfig.buildConfigField(
                    "String",
                    "PLAY_STORE_URL",
                    "\"https://play.google.com/store/apps/details?id=com.flux.notes\"",
                )
                configureReleaseSigning(this)
            }
            configureQuality()
        }
    }
}
