# ProGuard rules for SupaVault
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }
-dontwarn io.ktor.**
-dontwarn io.github.jan.supabase.**
-dontwarn okio.**
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
