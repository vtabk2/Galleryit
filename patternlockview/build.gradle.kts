plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.andrognito.patternlockview"
    compileSdk = libs.versions.targetSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
