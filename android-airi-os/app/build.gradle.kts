plugins { id("com.android.application") }

android {
    namespace = "com.airi.ios266stable"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.airi.ios266stable"
        minSdk = 23
        targetSdk = 29
        versionCode = 4
        versionName = "26.6.4-stable"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
