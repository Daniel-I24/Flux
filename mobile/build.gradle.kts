import com.flux.buildlogic.localProperty

plugins {
    alias(libs.plugins.flux.android.application)
    alias(libs.plugins.flux.android.compose)
    alias(libs.plugins.flux.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.flux.mobile"

    defaultConfig {
        buildConfigField("String", "API_BASE_URL", "\"${localProperty("flux.apiBaseUrl")}\"")
        buildConfigField("long", "CLASSIFY_TIMEOUT_MS", localProperty("flux.classifyTimeoutMs", "6000") + "L")
        buildConfigField("String", "FIREBASE_API_KEY", "\"${localProperty("firebase.apiKey")}\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"${localProperty("firebase.appId")}\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${localProperty("firebase.projectId")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${localProperty("firebase.webClientId")}\"")
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
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.wear.remote.interactions)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.okhttp)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    testImplementation(libs.androidx.test.core)
}
