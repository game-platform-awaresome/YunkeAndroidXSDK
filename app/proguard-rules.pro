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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-------------------------------------------基本指令区-----------------------------------------------#
-optimizationpasses 5
-dontskipnonpubliclibraryclassmembers
-verbose
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes InnerClasses
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes JavascriptInterface
-keepattributes LineNumberTable
-keepattributes Signature
-keepattributes SourceFile

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-dontoptimize
-dontshrink
-allowaccessmodification


#---------------------------------默认保留区---------------------------------
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
-dontwarn android.**
-keep class android.** { *; }
-keep class android.support.** {*;}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers enum * {
    public static <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.google.**{*;}
-keep class sun.misc.Unsafe { *; }
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    void set*(***);
    *** get*();
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keep class **.R$* {
    public static <fields>;
}

-keep @android.support.annotation.Keep class *
-keep @android.support.annotation.Keep interface *

-keepclassmembers class * {
    @android.support.annotation.Keep <methods>;
}

-keepclassmembers class * {
    @android.support.annotation.Keep <fields>;
}

-keepclassmembers interface * {
    @android.support.annotation.Keep <methods>;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String,int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

#----------------------------------------------腾讯广点通 start-------------------------------------#
-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.**{
    public *;
}
-keep class android.support.v7.**{
    public *;
}
#----------------------------------------------腾讯广点通 end---------------------------------------#

#----------------------------------------------头条穿山甲 start-------------------------------------#
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep class com.androidquery.callback.** {*;}
-keep class com.bytedance.sdk.openadsdk.service.TTDownloadProvider
#----------------------------------------------头条穿山甲 end---------------------------------------#

#-------------------------------------------云客 start---------------------------------------------#

-dontwarn com.androidquery.**
-keep class com.androidquery.** { *; }

-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.** { *; }

-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }

-dontwarn com.shykad.yunke.sdk.ui.widget.**
-keep class com.shykad.yunke.sdk.ui.widget.** { *; }

-dontwarn com.shykad.yunke.sdk.ui.activity.**
-keep class com.shykad.yunke.sdk.ui.activity.** { *; }

#-dontwarn com.shykad.yunke.sdk.**
#-keep class com.shykad.yunke.sdk.** { *; }

-dontwarn com.shykad.yunke.sdk.config.**
-keep class com.shykad.yunke.sdk.config.** { *; }

-dontwarn com.shykad.yunke.sdk.engine.**
-keep class com.shykad.yunke.sdk.engine.** { *; }

-dontwarn com.shykad.yunke.sdk.engine.BannerEngine**
-keep class com.shykad.yunke.sdk.engine.BannerEngine.** { *; }

-dontwarn com.shykad.yunke.sdk.engine.YunKeEngine**
-keep class com.shykad.yunke.sdk.engine.YunKeEngine.** { *; }

-dontwarn com.shykad.yunke.sdk.engine.InterstitialEngine**
-keep class com.shykad.yunke.sdk.engine.InterstitialEngine.** { *; }

-dontwarn com.shykad.yunke.sdk.engine.SplashEngine**
-keep class com.shykad.yunke.sdk.engine.SplashEngine.** { *; }

-dontwarn com.shykad.yunke.sdk.utils.**
-keep class com.shykad.yunke.sdk.utils.** { *; }

-dontwarn com.shykad.yunke.sdk.utils.LogUtils.**
-keep class com.shykad.yunke.sdk.utils.LogUtils.** { *; }

-dontwarn com.shykad.yunke.sdk.utils.ShykadUtils.**
-keep class com.shykad.yunke.sdk.utils.ShykadUtils.** { *; }

-dontwarn com.shykad.yunke.sdk.utils.WeakHandler.**
-keep class com.shykad.yunke.sdk.utils.WeakHandler.** { *; }

-dontwarn com.shykad.yunke.sdk.manager.**
-keep class com.shykad.yunke.sdk.manager.** { *; }

-dontwarn com.shykad.yunke.sdk.manager.ShykadManager.**
-keep class com.shykad.yunke.sdk.manager.ShykadManager.** { *; }

-dontwarn com.shykad.yunke.sdk.ShykadApplication.**
-keep class com.shykad.yunke.sdk.ShykadApplication.** { *; }



#easyPermission封装
-dontwarn com.shykad.yunke.sdk.engine.permission.**
-keep class com.shykad.yunke.sdk.engine.permission.** { *; }

-dontwarn android.support.v4.**
-dontwarn **CompatHoneycomb
-dontwarn **CompatHoneycombMR2
-dontwarn **CompatCreatorHoneycombMR2
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

#指定一个混淆关键字的字典
-obfuscationdictionary proguard-dictionary-keywords.txt
#指定一个混淆类名的字典
-classobfuscationdictionary proguard-dictionary-classname.txt
#指定一个混淆包名的字典
-packageobfuscationdictionary proguard-dictionary-packagename.txt
#-------------------------------------------云客 end-----------------------------------------------#



#-------------------------------------------webview------------------------------------------------#
-keepclassmembers class com.shykad.yunke.sdk.ui.widget.YunKeWebview {
   public *;
}
-keepclassmembers class fqcn.of.javascript.interface.for.Webview {
   public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, jav.lang.String);
}
#------------------------------------------webview-------------------------------------------------#
#-------------------------------------------定制化区域----------------------------------------------#
#-------------------------------------------定制化区域----------------------------------------------#

#-------------------------------------------实体类--------------------------------------------------#
-keep class com.shykad.yunke.sdk.okhttp.bean.** { *; }

#-------------------------------------------实体类--------------------------------------------------#
#-------------------------------------------第三方包------------------------------------------------#
#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#okhttputils
-dontwarn com.shykad.yunke.sdk.okhttp.**
-keep class com.shykad.yunke.sdk.okhttp.**{*;}


#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}


#okio
-dontwarn okio.**
-keep class okio.**{*;}

-keep class com.didi.virtualapk.internal.VAInstrumentation { *; }
-keep class com.didi.virtualapk.internal.PluginContentResolver { *; }

-dontwarn com.didi.virtualapk.**

#gson
-keep public class com.google.gson.**
-keep public class com.google.gson.** {
    public private protected *;
}
#-keepattributes Signature-keepattributes *Annotation*
-keep public class com.project.mocha_patient.login.SignResponseData {
    private *;
}
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep public class * implements java.io.Serializable {*;}

#-------------------------------------------第三方包------------------------------------------------#
#-------------------------------------------与js互相调用的类-----------------------------------------#
#-------------------------------------------与js互相调用的类-----------------------------------------#
#-------------------------------------------反射相关的类和方法----------------------------------------#
#-------------------------------------------反射相关的类和方法----------------------------------------#