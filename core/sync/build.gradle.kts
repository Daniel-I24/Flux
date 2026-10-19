plugins {
    alias(libs.plugins.flux.android.library)
    alias(libs.plugins.flux.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.flux.core.sync"
}

dependencies {
    api(projects.core.domain)
    api(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.play.services)
}
