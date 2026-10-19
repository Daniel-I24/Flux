import com.flux.buildlogic.localProperty

plugins {
    alias(libs.plugins.flux.android.application)
    alias(libs.plugins.flux.android.compose)
    alias(libs.plugins.flux.hilt)
}

android {
    namespace = "com.flux.wear"

    defaultConfig {
        minSdk = libs.versions.minSdkWear.get().toInt()
        buildConfigField("long", "PHONE_REPLY_TIMEOUT_MS", localProperty("flux.phoneReplyTimeoutMs", "4000") + "L")
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.navigation)
    implementation(libs.androidx.wear.remote.interactions)
    implementation(libs.androidx.wear.input)
    implementation(libs.androidx.wear.tiles)
    implementation(libs.androidx.wear.protolayout)
    implementation(libs.androidx.wear.protolayout.material3)
    implementation(libs.androidx.wear.protolayout.expression)
    implementation(libs.androidx.wear.complications.data.source)
    implementation(libs.kotlinx.coroutines.play.services)
    debugImplementation(libs.androidx.wear.compose.ui.tooling)

    testImplementation(libs.androidx.test.core)
}
