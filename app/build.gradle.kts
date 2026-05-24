plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

import java.util.Properties

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
        versionCode = 13
        versionName = "1.1.0"

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

        buildConfigField("String", "COMPOSE_BOM_VERSION", "\"${libs.versions.composeBom.get()}\"")
        buildConfigField("String", "ROOM_VERSION", "\"${libs.versions.room.get()}\"")
        buildConfigField("String", "HILT_VERSION", "\"${libs.versions.hilt.get()}\"")
        buildConfigField("String", "NAVIGATION_COMPOSE_VERSION", "\"${libs.versions.navigationCompose.get()}\"")
        buildConfigField("String", "COIL_VERSION", "\"${libs.versions.coil.get()}\"")
        buildConfigField("String", "DATASTORE_VERSION", "\"${libs.versions.datastore.get()}\"")
        buildConfigField("String", "IMAGE_CROPPER_VERSION", "\"${libs.versions.imageCropper.get()}\"")
        buildConfigField("String", "ACCOMPANIST_VERSION", "\"${libs.versions.accompanist.get()}\"")
        buildConfigField("String", "GSON_VERSION", "\"${libs.versions.gson.get()}\"")
        buildConfigField("String", "KOTLIN_BOM_VERSION", "\"${libs.versions.kotlinCoroutines.get()}\"")
        buildConfigField("String", "CORE_KTX_VERSION", "\"${libs.versions.coreKtx.get()}\"")
        buildConfigField("String", "LIFECYCLE_VERSION", "\"${libs.versions.lifecycleRuntimeKtx.get()}\"")
        buildConfigField("String", "ACTIVITY_COMPOSE_VERSION", "\"${libs.versions.activityCompose.get()}\"")
        buildConfigField("String", "APPCOMPAT_VERSION", "\"${libs.versions.appcompat.get()}\"")
        buildConfigField("String", "SPLASH_SCREEN_VERSION", "\"${libs.versions.splashScreen.get()}\"")
        buildConfigField("String", "HILT_NAVIGATION_COMPOSE_VERSION", "\"${libs.versions.hiltNavigationCompose.get()}\"")
        buildConfigField("String", "TINYPINYIN_VERSION", "\"${libs.versions.tinyPinyin.get()}\"")
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
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.kotlin.bom))
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    
    // Material Icons Extended
    implementation(libs.androidx.material.icons.extended)
    
    // AppCompat (for CropImageActivity)
    implementation(libs.androidx.appcompat)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // Coil for image loading
    implementation(libs.coil.compose)
    
    // Image cropper
    implementation(libs.android.image.cropper)
    
    // SplashScreen
    implementation(libs.androidx.core.splashscreen)
    
    // Accompanist for System UI Controller
    implementation(libs.accompanist.systemuicontroller)
    
    // Gson for JSON serialization
    implementation(libs.gson)
    
    // TinyPinyin for Chinese pinyin conversion
    implementation(libs.tinypinyin)
    
    // Ktor embedded server (Web 访问功能)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    
    // ZXing for QR code generation
    implementation(libs.zxing.core)
    
    // SLF4J no-op to suppress Ktor logging warnings on Android
    implementation(libs.slf4j.nop)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

