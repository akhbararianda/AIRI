plugins { id("com.android.application") }

android {
    namespace = "com.airi.ios266stable"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.airi.ios266stable6"
        minSdk = 23
        targetSdk = 29
        versionCode = 600
        versionName = "26.6.10-stable6-native-core"
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }
}
