plugins { id("com.android.application") }

android {
    namespace = "com.airi.ios266stable"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.airi.ios266stable3"
        minSdk = 23
        targetSdk = 29
        versionCode = 300
        versionName = "26.6.7-stable3"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
