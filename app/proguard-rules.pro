# Proguard rules for BasketballGame 

-keepattributes *Annotation*,InnerClasses,EnclosingMethod

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keepclassmembers class * {
    @androidx.room.* <fields>;
}

# JSONObject-based parsing in OnlineMatchClient
-keep class org.json.** { *; }

# OkHttp (usually safe, but keep to reduce obfuscation-related issues when debugging)
-dontwarn okhttp3.**
-dontwarn okio.**