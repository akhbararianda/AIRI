plugins { id("com.android.application") }

android {
    namespace = "com.airi.ios266stable"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.airi.ios266stable4"
        minSdk = 23
        targetSdk = 29
        versionCode = 400
        versionName = "26.6.8-stable4"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
