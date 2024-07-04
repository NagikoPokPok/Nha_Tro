plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "edu.poly.nhtr"
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.poly.nhtr"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
    }

    buildFeatures{
        viewBinding = true
    }
}

dependencies {


    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    // FCM
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("com.google.firebase:firebase-analytics")
    //Cloud Firestore
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation ("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.android.material:material:1.12.0")

    // Firebase gmail authentication
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-auth")

    //Rounded ImageView
    implementation("com.makeramen:roundedimageview:2.3.0")

    //Material Design
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")


    implementation("com.google.firebase:firebase-appcheck-playintegrity")

    //Send email otp
    implementation ("com.github.1902shubh:SendMail:1.0.0")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    // Full number support
    implementation("com.hbb20:ccp:2.7.1")

    // FCM
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation ("com.android.volley:volley:1.2.1")

    //Timber
    implementation("com.jakewharton.timber:timber:4.7.1")

}