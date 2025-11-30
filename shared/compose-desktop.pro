# Keep your app + CEF internals
#-keep class org.cef.** { *; }
-keep class org.jetbrains.cef.** { *; }

# Ignore Apache Thrift internals
-dontwarn org.apache.thrift.**

# Ignore JOGL/OpenGL internals
-dontwarn com.jogamp.**
-dontwarn jogamp.newt.swt.**
-dontwarn org.apache.pdfbox.**
-dontwarn com.jogamp.opengl.**
-dontwarn org.apache.commons.**

# Ignore kotlinx.serialization descriptors
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Keep coroutines Swing dispatcher
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory { *; }