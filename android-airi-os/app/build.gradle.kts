plugins { id("com.android.application") }

android {
    namespace = "com.airi.ios266stable"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.airi.ios266stable7"
        minSdk = 23
        targetSdk = 35
        versionCode = 700
        versionName = "26.6.11-stable7-owner-rmx1851"
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }
}
