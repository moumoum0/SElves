plugins {
    alias(libs.plugins.android.application)
    // AGP 9 内置 Kotlin，不再需要 org.jetbrains.kotlin.android
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt.android)
    // KSP 必须放在最后，确保能看到 Parcelize 等插件生成的代码
    alias(libs.plugins.ksp)
}

import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// 读取 local.properties 中的签名配置
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.selves.xnn"
    compileSdk = 37

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
        targetSdk = 37
        versionCode = 17
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

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
        buildConfigField("String", "KOTLIN_BOM_VERSION", "\"${libs.versions.kotlin.get()}\"")
        buildConfigField("String", "CORE_KTX_VERSION", "\"${libs.versions.coreKtx.get()}\"")
        buildConfigField("String", "LIFECYCLE_VERSION", "\"${libs.versions.lifecycleRuntimeKtx.get()}\"")
        buildConfigField("String", "ACTIVITY_COMPOSE_VERSION", "\"${libs.versions.activityCompose.get()}\"")
        buildConfigField("String", "APPCOMPAT_VERSION", "\"${libs.versions.appcompat.get()}\"")
        buildConfigField("String", "SPLASH_SCREEN_VERSION", "\"${libs.versions.splashScreen.get()}\"")
        buildConfigField("String", "HILT_NAVIGATION_COMPOSE_VERSION", "\"${libs.versions.hiltNavigationCompose.get()}\"")
        buildConfigField("String", "TINYPINYIN_VERSION", "\"${libs.versions.tinyPinyin.get()}\"")
    }

    androidResources {
        localeFilters += listOf("zh", "en")
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        // 禁用 Room 数据库验证以避免 SQLite 临时目录问题
        arg("room.verifyDatabase", "false")
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

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Web 构建任务配置（Configuration Cache 兼容）
val webProjectDir = rootProject.projectDir.resolve("web")
val webDistDir = webProjectDir.resolve("dist")
val hasWebProject = webProjectDir.isDirectory
val isWindows = System.getProperty("os.name").lowercase().contains("windows")

val buildWebDist = tasks.register<Exec>("buildWebDist") {
    workingDir = webProjectDir
    
    // 增量构建：只有源码变化时才重新构建
    inputs.dir(webProjectDir.resolve("src"))
    inputs.file(webProjectDir.resolve("package.json"))
    inputs.file(webProjectDir.resolve("package-lock.json"))
    inputs.file(webProjectDir.resolve("tsconfig.json"))
    inputs.file(webProjectDir.resolve("vite.config.ts"))
    outputs.dir(webDistDir)
    
    if (isWindows) {
        commandLine("npm.cmd", "run", "build")
        val appData = System.getenv("APPDATA") ?: System.getProperty("user.home") + "\\AppData\\Roaming"
        environment("npm_config_prefix", "$appData\\npm")
        environment("npm_config_cache", "$appData\\npm-cache")
    } else {
        commandLine("npm", "run", "build")
    }
    isEnabled = hasWebProject
    
    // 跳过条件：如果 dist 目录已存在且源码未变化
    onlyIf {
        !webDistDir.exists() || inputs.sourceFiles.any { 
            it.lastModified() > webDistDir.lastModified() 
        }
    }
}

val syncWebAssets = tasks.register<Sync>("syncWebAssets") {
    dependsOn(buildWebDist)
    from(webDistDir)
    into(layout.projectDirectory.dir("src/main/assets/web"))
    isEnabled = hasWebProject
    
    // 增量同步：只有 dist 变化时才同步
    inputs.dir(webDistDir)
    outputs.dir(layout.projectDirectory.dir("src/main/assets/web"))
    onlyIf { webDistDir.exists() }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(syncWebAssets)
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
    
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    
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
    implementation(libs.ktor.server.auth)
    
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

