# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.fieldcam.**$$serializer { *; }
-keepclassmembers class com.fieldcam.** { *** Companion; }
-keepclasseswithmembers class com.fieldcam.** { kotlinx.serialization.KSerializer serializer(...); }
