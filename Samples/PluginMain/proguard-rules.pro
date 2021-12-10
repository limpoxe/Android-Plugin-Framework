-optimizationpasses 5  #指定代码的压缩级别
-dontusemixedcaseclassnames #包名不混合大小写
-dontskipnonpubliclibraryclasses #不忽略非公共库类
-dontoptimize #不优化输入的类文件
-dontpreverify  #不预校验
-verbose #混淆时是否记录日志
-dontshrink #禁用压缩--重要
#混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#未混淆的类和成员
-printseeds build/outputs/seeds.txt
#列出被删除的代码
-printusage build/outputs/unused.txt
#混淆前后的映射
-printmapping build/outputs/mapping.txt

#Android组件
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

#保持控件构造器不被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

#保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保持枚举 enum 类不被混淆
-keepclassmembers enum * {
     public static **[] values();
     public static ** valueOf(java.lang.String);
}

#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#保持 Serializable 不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#避免混淆泛型
-keepattributes Signature
-keepattributes *JavascriptInterface*
-keepattributes *Annotation*

#保护第三方jar包
#-libraryjars libs/xx.jar

#如果引用了v4或者v7包
-dontwarn android.support.**
-keep class android.support.v4.** {
 *;
}
-keep class android.support.v7.** {
 *;
}

# 保护FairyPlugin不受混淆影响 Begin
-keep public class * extends android.app.Instrumentation {public *;}
-keep public class * extends android.content.ContextWrapper {public *;}
-keep class com.limpoxe.fairy.content.** {*;}
-keep class android.support.v4.content.LocalBroadcastManager {*;}
-keep class android.support.v4.app.Fragment {*;}
-keep class android.support.v7.app.AppCompatViewInflater {*;}
-keep class android.support.v7.app.AppCompatActivity {*;}
-keep class android.support.v7.widget.TintResources {*;}
-keep class android.support.v7.widget.TintContextWrapper {*;}
-keep class androidx.appcompat.app.AppCompatViewInflater {*;}
-keep class androidx.fragment.app.FragmentFactory {*;}
-keep class androidx.fragment.app.Fragment {*;}
-keep class androidx.localbroadcastmanager.content.LocalBroadcastManager {*;}
# 保护FairyPlugin不受混淆影响 End

# bugly sdk
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

# butterknife 混淆保护  Begin
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}
# butterknife 混淆保护  End

# 保护AIDL Begin
-keep class * implements android.os.IInterface {
    *;
}
-keep class * extends android.os.IInterface {
    *;
}
# 保护AIDL End