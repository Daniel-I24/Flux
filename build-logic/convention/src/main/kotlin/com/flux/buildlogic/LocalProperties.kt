package com.flux.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project
import java.util.Properties

/** Reads local.properties (git-ignored). Secrets and environment values never live in the repo. */
fun Project.localProperty(key: String, default: String = ""): String {
    val properties = rootProject.extensions.extraProperties.let { extra ->
        if (extra.has(LOCAL_PROPERTIES_KEY)) {
            extra.get(LOCAL_PROPERTIES_KEY) as Properties
        } else {
            Properties().also { loaded ->
                val file = rootProject.file("local.properties")
                if (file.exists()) file.inputStream().use(loaded::load)
                extra.set(LOCAL_PROPERTIES_KEY, loaded)
            }
        }
    }
    return properties.getProperty(key, default)
}

/** Release signing only when a keystore is configured; otherwise release builds stay unsigned. */
internal fun Project.configureReleaseSigning(extension: ApplicationExtension) {
    val storePath = localProperty("flux.keystore.path")
    if (storePath.isBlank()) return
    extension.signingConfigs.create("release") {
        storeFile = rootProject.file(storePath)
        storePassword = localProperty("flux.keystore.password")
        keyAlias = localProperty("flux.keystore.alias")
        keyPassword = localProperty("flux.keystore.keyPassword")
    }
    extension.buildTypes.getByName("release").signingConfig = extension.signingConfigs.getByName("release")
}

private const val LOCAL_PROPERTIES_KEY = "flux.localProperties"
