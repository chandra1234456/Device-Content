# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line number information for Google Play Console de-obfuscation & crash stack traces.
-keepattributes SourceFile,LineNumberTable

# Preserve Data Models from R8 optimization
-keep class com.chandra.practice.deviceinfo.data.model.** { *; }
-keepclassmembers class com.chandra.practice.deviceinfo.data.model.** { *; }

# Keep AndroidX DataStore preferences
-keep class androidx.datastore.preferences.protobuf.** { *; }