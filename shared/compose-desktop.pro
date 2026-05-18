# Keep your app + CEF internals
-keep class org.cef.** { *; }
-keep class org.jetbrains.cef.** { *; }

# Keep kotlinx.serialization
-keep class kotlinx.serialization.** { *; }

# Keep coroutines Swing dispatcher
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory { *; }


# --- OkHttp optional platforms ---

-keep class org.conscrypt.** { *; }
-keep class org.bouncycastle.** { *; }
-keep class org.openjsse.** { *; }
-keep class android.** { *; }
-keep class javafx.** { *; }

# --- KEEP ALL JCEF ---
-keep class org.cef.** { *; }
# --- KEEP JetBrains remote layer ---
# -keep class com.jetbrains.cef.** { *; }

# Keep all generated JCEF Thrift classes
# -keep class com.jetbrains.cef.remote.thrift_codegen.** { *; }


# Keep Compose test internals
# -keep class androidx.compose.ui.test.** { *; }

-keep class androidx.compose.ui.scene.CanvasLayersComposeScene_skikoKt { *; }
-keep class androidx.compose.ui.platform.PlatformContext** { *; }

# Keep JogAmp/JOGL
# -keep class com.jogamp.** { *; }
# -keep class jogamp.** { *; }


# Keep Eclipse SWT
-keep class org.eclipse.swt.** { *; }

# Keep JOGL SWT bridge
-keep class jogamp.newt.swt.** { *; }

# Keep Apache PDFBox
-keep class org.apache.pdfbox.** { *; }

# Keep Apache commons
#-keep class org.apache.commons.** { *; }

# -keep class org.apache.** { *; }

# Keep all Thrift runtime classes
#-keep class org.apache.thrift.** { *; }


# Keep Thrift async classes
-keep class org.apache.thrift.async.** { *; }
-keep class org.cef.** { *; }
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory

-dontwarn com.jetbrains.cef.**
-dontwarn androidx.compose.ui.test.**
-dontwarn com.jogamp.**
-dontwarn jogamp.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.**
-dontwarn org.apache.thrift.**
-dontwarn okhttp3.internal.platform.**

-keep class io.ktor.serialization.kotlinx.** { *; }
-keep class com.arkivanov.decompose.extensions.compose.mainthread.** { *; }
-keep class kotlinx.coroutines.test.internal.** { *; }
-keep class com.sun.jna.** { *; }
# Keep SQLite JDBC driver
-keep class org.sqlite.** { *; }
# Keep JDBC driver registration
-keep class * implements java.sql.Driver { *; }

-keepclassmembers class * {
    java.util.concurrent.atomic.AtomicReferenceFieldUpdater *;
}

# Keep Ktor client classes
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }

# Keep all fields that might be accessed via reflection
-keepclassmembers class io.ktor.** {
    volatile <fields>;
}

# Specifically keep fields used by AtomicReferenceFieldUpdater
-keepclassmembers class * {
    volatile ** readHandlerReference;
    volatile ** writeHandlerReference;
    volatile ** _state;
    volatile ** _channel;
}

# Keep Ktor's HTTP client engine
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.plugins.** { *; }

# Keep serialization classes used by Ktor
-keep class io.ktor.serialization.** { *; }
-keep class io.ktor.utils.io.** { *; }

# Don't obfuscate Ktor classes (prevents reflection issues)
-keep,allowobfuscation class io.ktor.** { *; }

# Keep coroutines integration
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep atomic field updaters working
-keepclassmembers class * {
    ** get*();
    void set*(***);
}

-dontwarn io.ktor.**

# ========================================
# BouncyCastle - CRITICAL: Don't process signed JARs
# ========================================

# Don't obfuscate, optimize, or shrink BouncyCastle (it's a signed JAR)
-keep class org.bouncycastle.** { *; }
-keepclassmembers class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Don't modify BouncyCastle at all - it's cryptographically signed
-keepnames class org.bouncycastle.** { *; }

# Keep security providers
-keep class * extends java.security.Provider { *; }

# Keep JCE provider classes
-keep class javax.crypto.** { *; }
-keep class javax.security.** { *; }