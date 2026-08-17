plugins { id("com.android.application") }

android {
    namespace = "com.airi.ios266stable"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.airi.ios266stable5"
        minSdk = 23
        targetSdk = 29
        versionCode = 500
        versionName = "26.6.9-stable5-ai"
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }
}
