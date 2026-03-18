/**
 * Android Development Suite - App Module Build Configuration
 * منصة تطوير أندرويد الشاملة
 * 
 * Production-Grade Build Configuration
 * Target SDK: 35 (Android 15)
 * 
 * Features:
 * - Jetpack Compose UI
 * - Material Design 3
 * - Hilt Dependency Injection
 * - Room Database
 * - TensorFlow Lite for AI
 * - OkHttp + Retrofit for Networking
 * - Coroutines & Flow
 * - Navigation Component
 * - DataStore Preferences
 * - WorkManager for Background Tasks
 * 
 * @author Lead Systems Architect
 * @version 1.0.0
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.androiddevsuite"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.androiddevsuite"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Enable multidex for large number of dependencies
        multiDexEnabled = true
        
        // NDK configuration for native code support (Termux-like terminal)
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
        }
        
        // Build config fields
        buildConfigField("String", "GITHUB_API_URL", "\"https://api.github.com\"")
        buildConfigField("String", "AI_MODEL_VERSION", "\"1.0.0\"")
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            buildConfigField("boolean", "ENABLE_SANDBOX_STRICT_MODE", "false")
        }
        
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            buildConfigField("boolean", "ENABLE_SANDBOX_STRICT_MODE", "true")
        }
    }
    
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
        compose = true
        aidl = true
        renderScript = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
        }
        
        jniLibs {
            useLegacyPackaging = true
        }
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        warningsAsErrors = false
    }
    
    testOptions {
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
    }
}

// KSP configuration for Room
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

dependencies {
    // =====================================================
    // CORE ANDROID DEPENDENCIES
    // =====================================================
    
    // Android Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.preference:preference-ktx:1.2.1")
    
    // Core Library Desugaring for Java 8+ APIs on older Android
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")
    
    // Multidex support
    implementation("androidx.multidex:multidex:2.0.1")
    
    // =====================================================
    // UI COMPONENTS - JETPACK COMPOSE & MATERIAL 3
    // =====================================================
    
    // Jetpack Compose BOM (Bill of Materials)
    implementation(platform("androidx.compose:compose-bom:2024.11.00"))
    
    // Compose Core
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    
    // Material Design 3
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Material Components for Views (XML-based UI)
    implementation("com.google.android.material:material:1.13.0-alpha09")
    
    // Compose Animation
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-core")
    implementation("androidx.compose.animation:animation-graphics")
    
    // Compose Runtime
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.runtime:runtime-rxjava3")
    
    // ConstraintLayout for Compose
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0")
    
    // Activity & Fragment Compose Integration
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.fragment:fragment-compose:1.8.5")
    
    // =====================================================
    // NAVIGATION COMPONENT
    // =====================================================
    
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.4")
    
    // =====================================================
    // LIFECYCLE & VIEWMODEL
    // =====================================================
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    
    // =====================================================
    // DEPENDENCY INJECTION - HILT
    // =====================================================
    
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    
    // Hilt Navigation
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")
    
    // =====================================================
    // COROUTINES & REACTIVE STREAMS
    // =====================================================
    
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    
    // RxJava3 for reactive programming
    implementation("io.reactivex.rxjava3:rxjava:3.1.10")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    
    // =====================================================
    // NETWORKING - OKHTTP & RETROFIT
    // =====================================================
    
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.12.0")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.11.0")
    
    // Moshi for JSON parsing
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
    
    // Gson
    implementation("com.google.code.gson:gson:2.11.0")
    
    // =====================================================
    // DATABASE - ROOM
    // =====================================================
    
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-rxjava3:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // SQLite with support for large databases
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    implementation("androidx.sqlite:sqlite-framework:2.4.0")
    
    // SQLCipher for encrypted databases
    implementation("net.zetetic:android-database-sqlcipher:4.5.7")
    
    // =====================================================
    // DATA STORAGE - DATASTORE
    // =====================================================
    
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore-core:1.1.1")
    
    // =====================================================
    // WORK MANAGER - BACKGROUND TASKS
    // =====================================================
    
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.work:work-rxjava3:2.9.1")
    
    // =====================================================
    // SECURITY & CRYPTOGRAPHY
    // =====================================================
    
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")
    
    // Bouncy Castle for advanced cryptography
    implementation("org.bouncycastle:bcprov-jdk18on:1.79")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.79")
    
    // =====================================================
    // ARTIFICIAL INTELLIGENCE - TENSORFLOW LITE
    // =====================================================
    
    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.5.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.5.0")
    
    // ML Kit for on-device ML
    implementation("com.google.mlkit:common:22.2.0")
    implementation("com.google.mlkit:translate:17.0.3")
    implementation("com.google.mlkit:language-id:17.0.6")
    
    // =====================================================
    // CODE EDITOR & SYNTAX HIGHLIGHTING
    // =====================================================
    
    // CodeView for code editing with syntax highlighting
    implementation("io.github.amrdeveloper:codeview:1.3.9")
    
    // Rosemoe Code Editor (like AndroidIDE)
    implementation("io.github.rosemoe:code-editor:0.11.1")
    implementation("io.github.rosemoe:language-java:0.0.6")
    implementation("io.github.rosemoe:language-kotlin:0.0.6")
    implementation("io.github.rosemoe:language-base:0.0.6")
    
    // Tree-sitter for parsing
    implementation("io.github.rosemoe:sora-editor:0.23.4")
    
    // =====================================================
    // BLOCK PROGRAMMING (SKETCHWARE-LIKE)
    // =====================================================
    
    // Blockly for visual programming
    implementation("com.google.blockly:blockly-android:2024.02.05")
    
    // =====================================================
    // APK ANALYSIS & MANIPULATION
    // =====================================================
    
    // APK Parser
    implementation("net.dongliu:apk-parser:2.6.10")
    
    // Dex2Jar for DEX analysis
    implementation("com.github.lanchon.dexpatcher:multidexlib2:2.3.4.r2")
    
    // Smali/Baksmali
    implementation("com.android.tools.smali:smali:3.0.8")
    implementation("com.android.tools.smali:baksmali:3.0.8")
    
    // AXMLEditor for Android XML manipulation
    implementation("com.github.lanchon:axmlparser:1.0.0")
    
    // =====================================================
    // TERMINAL EMULATOR (TERMUX-LIKE)
    // =====================================================
    
    // Terminal Emulator
    implementation("com.github.termux:termux-terminal-emulator:v0.118.0")
    implementation("com.github.termux:termux-view:v0.118.0")
    
    // JSch for SSH
    implementation("com.github.mwiede:jsch:0.2.19")
    
    // =====================================================
    // FILE MANAGEMENT & ARCHIVE
    // =====================================================
    
    // Apache Commons IO
    implementation("commons-io:commons-io:2.17.0")
    
    // ZIP handling
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    
    // Apache Commons Compress for various archive formats
    implementation("org.apache.commons:commons-compress:1.27.1")
    
    // XZ compression
    implementation("org.tukaani:xz:1.10")
    
    // =====================================================
    // IMAGE LOADING & PROCESSING
    // =====================================================
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-svg:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")
    
    // Glide alternative
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")
    
    // =====================================================
    // DEBUGGING & LOGGING
    // =====================================================
    
    // Timber for logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // Chucker for network debugging
    debugImplementation("com.github.chuckerteam.chucker:library:4.0.0")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:4.0.0")
    
    // LeakCanary for memory leak detection
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    
    // =====================================================
    // TESTING
    // =====================================================
    
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("app.cash.turbine:turbine:1.2.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.11.00"))
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // =====================================================
    // GIT VERSION CONTROL
    // =====================================================
    
    // JGit for Git operations
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.0.0.202409031743-r")
    
    // =====================================================
    // MARKDOWN RENDERING
    // =====================================================
    
    implementation("com.halilibo.compose-richtext:richtext-commonmark:0.20.0")
    implementation("com.halilibo.compose-richtext:richtext-ui-material3:0.20.0")
    
    // =====================================================
    // CHARTS & VISUALIZATION
    // =====================================================
    
    implementation("com.patrykandpatrick.vico:compose:2.0.0-beta.2")
    implementation("com.patrykandpatrick.vico:compose-m3:2.0.0-beta.2")
    
    // =====================================================
    // PERMISSIONS
    // =====================================================
    
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")
    
    // =====================================================
    // WINDOW SIZE & FOLDABLE SUPPORT
    // =====================================================
    
    implementation("androidx.window:window:1.3.0")
    implementation("androidx.window:window-core:1.3.0")
    
    // =====================================================
    // STARTUP
    // =====================================================
    
    implementation("androidx.startup:startup-runtime:1.2.0")
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21")
    }
}
