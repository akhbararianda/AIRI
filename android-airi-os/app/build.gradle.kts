plugins { id("com.android.application") }

android {
    namespace = "com.airi.ios266stable"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.airi.ios266stable2"
        minSdk = 23
        targetSdk = 29
        versionCode = 200
        versionName = "26.6.6-stable2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
