plugins { id("com.android.application") }

android {
    namespace = "com.airi.ios266stable"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.airi.ios266stable"
        minSdk = 23
        targetSdk = 29
        versionCode = 5
        versionName = "26.6.5-premium"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
