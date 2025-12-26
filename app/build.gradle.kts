plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt) //hilt
    alias(libs.plugins.hilt.plugin) //hilt
    alias(libs.plugins.google.gms.google.services)//firebase

}

android {
    namespace = "ru.wert.quickloupe"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "ru.wert.quickloupe"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    kapt {
        correctErrorTypes = true
        javacOptions {
            option("-Adagger.fastInit=ENABLED")
        }
    }

    buildFeatures {
        compose = true
    }

    tasks.withType<Test> {
        useJUnitPlatform() // Включаем JUnit 5 Platform
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

//    testOptions {
//        unitTests.all {
//            useJUnitPlatform()
//        }
//    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.test.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)



    //Permissions
    implementation(libs.permissions)

    //Firebase
    implementation(libs.firebase.analytics)

    //CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    //Hilt
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    implementation("androidx.compose.material:material-icons-extended")

    //ТЕСТИРОВАНИЕ
    // JUnit 5 (Jupiter) для unit тестов
    testImplementation(libs.junit) // Это теперь junit-jupiter-api
    testImplementation(libs.junit.jupiter.engine) // JUnit 5 engine
    testImplementation(libs.junit.jupiter.params) // JUnit 5 engine
//    testImplementation(libs.junit.vintage.engine) // Для поддержки JUnit 4 тестов, если есть

    // MockK для unit тестов
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.android) // Для Android-специфичных тестов

    // Интеграционные тесты
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.kotlinx.coroutines.test)
}

