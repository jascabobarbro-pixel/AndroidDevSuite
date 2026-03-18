# Android Development Suite - ProGuard Rules
# منصة تطوير أندرويد الشاملة

# =====================================================
# GENERAL RULES
# =====================================================

# Keep all native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Parcelables
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# =====================================================
# KOTLIN RULES
# =====================================================

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.androiddevsuite.**$$serializer { *; }
-keepclassmembers class com.androiddevsuite.** {
    *** Companion;
}
-keepclasseswithmembers class com.androiddevsuite.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# =====================================================
# JETPACK COMPOSE RULES
# =====================================================

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.ui.** { *; }

# Keep Composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# =====================================================
# HILT DEPENDENCY INJECTION
# =====================================================

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep,allowobfuscation,allowshrinking class com.androiddevsuite.common.HiltWrapper { *; }
-keep,allowobfuscation,allowshrinking class dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Hilt generated classes
-keep class com.androiddevsuite.**_HiltComponents { *; }
-keep class com.androiddevsuite.**_HiltModules { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# =====================================================
# ROOM DATABASE
# =====================================================

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# =====================================================
# RETROFIT & OKHTTP
# =====================================================

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Moshi
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class *
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
}

# =====================================================
# GITHUB API
# =====================================================

-keep class com.androiddevsuite.data.remote.github.** { *; }
-keepclassmembers class com.androiddevsuite.data.remote.github.** { *; }

# =====================================================
# TENSORFLOW LITE
# =====================================================

-keep class org.tensorflow.** { *; }
-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.**

# =====================================================
# CODE EDITOR (ROSEMOE)
# =====================================================

-keep class io.github.rosemoe.** { *; }
-dontwarn io.github.rosemoe.**

# =====================================================
# APK PARSER & ANALYSIS
# =====================================================

-keep class net.dongliu.apk.parser.** { *; }
-keep class com.android.tools.smali.** { *; }
-dontwarn com.android.tools.smali.**

# =====================================================
# JGIT (GIT VERSION CONTROL)
# =====================================================

-keep class org.eclipse.jgit.** { *; }
-dontwarn org.eclipse.jgit.**

# =====================================================
# BOUNCY CASTLE (CRYPTOGRAPHY)
# =====================================================

-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-dontwarn javax.naming.**

# =====================================================
# TERMINAL EMULATOR
# =====================================================

-keep class com.termux.** { *; }
-dontwarn com.termux.**

# =====================================================
# APPLICATION SPECIFIC
# =====================================================

# Keep all AI models
-keep class com.androiddevsuite.ai.** { *; }

# Keep sandbox classes
-keep class com.androiddevsuite.sandbox.** { *; }

# Keep tool classes
-keep class com.androiddevsuite.tools.** { *; }

# Keep data models
-keep class com.androiddevsuite.data.model.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep Build Config
-keep class com.androiddevsuite.BuildConfig { *; }
