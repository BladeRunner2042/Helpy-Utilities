# ProGuard/R8 rules for HelpyUtilities
# -------------------------------------------------
# Jetpack Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keep @androidx.compose.runtime.Composable class *
-keep @androidx.compose.runtime.Composable interface *

# Kotlin & Coroutines
-keepclassmembers class kotlin.Metadata {
    *;
}
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }

# AndroidX (general)
-keep class androidx.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }

# OkHttp & Retrofit
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Gson & Kotlinx Serialization
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class kotlinx.serialization.** { *; }

# Miscellaneous
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn javax.annotation.**