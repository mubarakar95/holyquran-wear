# Keep QVP native JNI classes
-keep class ws.quran.qvp.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Kotlinx Serialization models
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
