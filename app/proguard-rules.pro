# Room: keep entities and DAOs, generated code relies on reflection for schema validation
-keep class com.wickwirez.myride.model.** { *; }
-keep class com.wickwirez.myride.data.**Dao { *; }
-keep class com.wickwirez.myride.data.**Dao_Impl { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# MLKit text recognition and barcode scanning: keep their public API classes
-keep class com.google.mlkit.vision.text.** { *; }
-keep class com.google.mlkit.vision.barcode.** { *; }
-keep class com.google.mlkit.vision.common.** { *; }
-dontwarn com.google.mlkit.**

# CameraX: keep core classes, uses reflection for extensions
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-dontwarn androidx.camera.**

# org.json usage in GeminiApiClient: keep field names so JSON key mapping isn't broken
-keepclassmembers class org.json.** { *; }
-dontwarn org.json.**

# androidx.security (EncryptedSharedPreferences / Tink): keep to avoid crypto init crashes
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# Kotlin coroutines: standard keep rules to avoid stripping suspend function metadata
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Keep BuildConfig fields accessible (used directly in AboutScreen.kt)
-keep class com.wickwirez.myride.BuildConfig { *; }
