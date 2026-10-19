plugins {
    alias(libs.plugins.flux.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(projects.core.domain)
    implementation(libs.okhttp)
    testImplementation(libs.okhttp.mockwebserver)
}
