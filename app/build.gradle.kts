plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

import java.util.Properties

val roomVersion = "2.6.1"
val hiltVersion = "2.52"
val datastoreVersion = "1.1.1"
val coilVersion = "2.7.0"
val navigationComposeVersion = "2.8.5"
val accompanistVersion = "0.36.0"
val imageCropperVersion = "4.3.2"
val composeBomVersion = "2024.12.01"
val gsonVersion = "2.11.0"
val kotlinCoroutinesVersion = "2.1.0"
val coreKtxVersion = "1.15.0"
val lifecycleVersion = "2.8.7"
val activityComposeVersion = "1.9.3"
val appcompatVersion = "1.7.0"
val splashScreenVersion = "1.0.1"
val hiltNavigationComposeVersion = "1.2.0"
val tinyPinyinVersion = "2.0.3"

// 读取 local.properties 中的签名配置
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.selves.xnn"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("KEYSTORE_FILE", ""))
            storePassword = localProperties.getProperty("KEYSTORE_PASSWORD", "")
            keyAlias = localProperties.getProperty("KEY_ALIAS", "")
            keyPassword = localProperties.getProperty("KEY_PASSWORD", "")
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    defaultConfig {
        applicationId = "com.selves.xnn"
        minSdk = 26
        targetSdk = 36
        versionCode = 11
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // 为Room和SQLite设置系统属性
        System.setProperty("org.sqlite.tmpdir", "${project.buildDir}/tmp/sqlite")
        System.setProperty("room.schemaLocation", "${project.buildDir}/schemas")
        
        // 只保留必要的资源密度
        resourceConfigurations += listOf("zh", "en")
        
        // 16KB页面大小支持配置
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        buildConfigField("String", "COMPOSE_BOM_VERSION", "\"$composeBomVersion\"")
        buildConfigField("String", "ROOM_VERSION", "\"$roomVersion\"")
        buildConfigField("String", "HILT_VERSION", "\"$hiltVersion\"")
        buildConfigField("String", "NAVIGATION_COMPOSE_VERSION", "\"$navigationComposeVersion\"")
        buildConfigField("String", "COIL_VERSION", "\"$coilVersion\"")
        buildConfigField("String", "DATASTORE_VERSION", "\"$datastoreVersion\"")
        buildConfigField("String", "IMAGE_CROPPER_VERSION", "\"$imageCropperVersion\"")
        buildConfigField("String", "ACCOMPANIST_VERSION", "\"$accompanistVersion\"")
        buildConfigField("String", "GSON_VERSION", "\"$gsonVersion\"")
        buildConfigField("String", "KOTLIN_BOM_VERSION", "\"$kotlinCoroutinesVersion\"")
        buildConfigField("String", "CORE_KTX_VERSION", "\"$coreKtxVersion\"")
        buildConfigField("String", "LIFECYCLE_VERSION", "\"$lifecycleVersion\"")
        buildConfigField("String", "ACTIVITY_COMPOSE_VERSION", "\"$activityComposeVersion\"")
        buildConfigField("String", "APPCOMPAT_VERSION", "\"$appcompatVersion\"")
        buildConfigField("String", "SPLASH_SCREEN_VERSION", "\"$splashScreenVersion\"")
        buildConfigField("String", "HILT_NAVIGATION_COMPOSE_VERSION", "\"$hiltNavigationComposeVersion\"")
        buildConfigField("String", "TINYPINYIN_VERSION", "\"$tinyPinyinVersion\"")
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        disable += setOf(
            "MutableCollectionMutableState",
            "AutoboxingStateCreation"
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
            excludes += "DebugProbesKt.bin"
            excludes += "/kotlin/**"
            excludes += "/*.properties"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:$coreKtxVersion")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinCoroutinesVersion"))
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:$activityComposeVersion")
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:$navigationComposeVersion")
    
    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended")
    
    // AppCompat (for CropImageActivity)
    implementation("androidx.appcompat:appcompat:$appcompatVersion")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:$datastoreVersion")
    implementation("androidx.datastore:datastore-preferences-core:$datastoreVersion")
    
    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltNavigationComposeVersion")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:$coilVersion")
    
    // Image cropper
    implementation("com.github.CanHub:Android-Image-Cropper:$imageCropperVersion")
    
    // SplashScreen
    implementation("androidx.core:core-splashscreen:$splashScreenVersion")
    
    // Accompanist for System UI Controller
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:$gsonVersion")
    
    // TinyPinyin for Chinese pinyin conversion
    implementation("com.github.promeg:tinypinyin:$tinyPinyinVersion")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

