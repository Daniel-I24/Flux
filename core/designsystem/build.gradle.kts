plugins {
    alias(libs.plugins.flux.android.library)
    alias(libs.plugins.flux.android.compose)
}

android {
    namespace = "com.flux.core.designsystem"
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons)
}
