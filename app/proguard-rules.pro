# Proguard rules for BasketMars

-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keepclassmembers class * {
    @androidx.room.* <fields>;
}

# LeaderboardEntry — POJO используется Room и Firestore (рефлексия)
-keep class com.example.basketballgame.data.LeaderboardEntry { *; }
-keep class com.example.basketballgame.data.PlayerStats { *; }

# Firebase / Firestore
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Google Sign-In
-keep class com.google.android.gms.auth.** { *; }

# Firebase Analytics
-keep class com.google.android.datatransport.** { *; }
-dontwarn com.google.android.datatransport.**

# Kotlin coroutines (Firebase SDK использует их внутри)
-dontwarn kotlinx.coroutines.**

# JSON parsing (org.json используется в нескольких местах)
-keep class org.json.** { *; }

# OkHttp (оставлено для совместимости, хотя зависимость удалена)
-dontwarn okhttp3.**
-dontwarn okio.**