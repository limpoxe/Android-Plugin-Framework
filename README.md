# Android-Plugin-Framework

README: [中文](https://github.com/limpoxe/Android-Plugin-Framework/blob/master/README.md)

Android-Plugin-Framework是一个Android插件化框架，用于通过动态加载的方式免安装运行插件apk

#### 最新版本: 0.0.53-snapshot

#### 项目结构

| 文件夹        |     说明     |
| :----------- | :-----------|
| FairyPlugin | 插件框架包 |
| Samples | 示例代码，包含宿主APP、各种Demo插件、各种场景测试 |

#### 名词解释

| 名词         |     说明     |
| :----------- | :-----------|
| 宿主 | 正常安装在设备上的apk |
| 容器 | 宿主中由框架创建的插件运行环境 |
| 插件 | 被宿主加载运行的apk |
| 独立插件 | 运行时不依赖宿主的插件，自身可以是完整app，也可以不是一个完整app |
| 非独立插件 | 运行时依赖宿主（类、资源）的插件，自身不是一个完整app |
| 插件进程 | 插件运行时所在进程，也即容器所在进程 |

独立插件和非独立插件, 不以是否可以单独安装运行来区分，仅以是否依赖宿主的类和资源来区分。

#### 此项目主要目标是为了运行非独立插件，而不是任意第三方app。

尽管此框架支持独立插件，但目标并不是为了支持任意三方app，不同于平行空间或应用分身之类的产品。
非独立插件相比任意三方app来说，可以预见到其使用了哪些系统api和特性，而且所有行为都是可以预测的。而任意三方app是不可预测的。
框架的做法是按需hook，即需要用到哪些系统特性和api，就对哪些特性和api提供支持。这种做法对开发非独立插件和二方独立插件而言完全足够。
目前已经添加了对常用特性和api的支持，如需使用的api还未支持请联系作者。

#### FEATURE
- 框架透明, 插件开发与普通apk开发无异，无约定约束
- 支持非独立插件和独立插件(非任意三方)
- 支持四大组件/Application/Fragment/Accessibility/LaunchMode/so
- 支持插件Theme/Style,宿主Theme/Style,轻松支持基于主题属性的皮肤切换
- 支持插件发送Notification/时在RemoteViews中携带插件中的资源（只支持5.x及以上, 且不支持miui8）
- 支持插件热更新：即在插件模块已经被唤起的情况先安装新版本插件，无需重启插件进程（前提是插件高度内敛，宿主```不主动```持有插件中的任何对象）
- 支持全局服务：即插件向容器注册一个服务，其他所有插件已经宿主都获取并调用此服务
- 支持DataBinding（仅限独立插件）
- 支持插件WebView加载插件本地HTML文件
- 支持插件Fragment/View内嵌宿主Activity中

#### LIMIT
- 不支持插件Activity转场动画使用插件中的动画资源
- 不支持插件Manifest中申请权限，所有权限必须预埋到宿主Manifest中
- 不支持第三方app试图唤起插件中的组件时直接使用插件组件的Intent。
  第三方app要唤起插件中的静态组件必须由宿主程序进行桥接，即此组件需同时预埋到宿主和插件的Manifest中
- 不支持android.app.NativeActivity
- 不支持当一个插件依赖另一个插件时，被插件依赖的包含资源
- 不支持插件中的webview弹出```原生Chrome组件```，例如通过html的<input type="date"/>标签设置时间选择器,（可将Chrome路径添加到插件Assets解决，但似乎存在ROM兼容问题）
- 可能不支持对插件或者宿主进行加壳加固处理，未尝试
    
# HOW TO USE
#### 宿主侧
1、 新建一个工程，作为宿主工程

2、 在宿主工程的build.gradle文件下添加如下3个配置
```
    android {
        defaultConfig {
            //这个配置不可省略
            applicationId 宿主app包名        
        }
    }
```

```
    dependencies {
        //请务必使用@aar结尾，以中断依赖传递
        compile('com.limpoxe.fairy:FairyPlugin:0.0.53-snapshot@aar')
        //可选，用于支持插件全局函数式服务，不使用全局函数式服务不需要添加此依赖
        //compile('com.limpoxe.support:android-servicemanager:1.0.5@aar')
    }
```

```
    ext {
        //可选配置，用于指定插件进程名。默认插件进程为单独的进程，进程名为":plugin"
        //若设置为空串或者null即是使用宿主进程作为插件进程
        //pluginProcess = ""
        //pluginProcess = null
        //pluginProcess = ":xxx"
    }
    apply from: "https://raw.githubusercontent.com/limpoxe/Android-Plugin-Framework/master/FairyPlugin/host.gradle"        
```

3、 在宿主工程中新建一个类继承自Application类, 并配置到AndroidManifest.xml中并重写这个类的下面2个方法
```
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //框架日志开关
        FairyGlobal.setLogEnable(true);
        //这个方法是设置首次加载插件时, 定制loading页面的UI, 不传即默认没有loading页
        //在宿主中创建任意一个layout传进去即可
        FairyGlobal.setLoadingResId(R.layout.loading);
        //是否支持插件中使用本地html
        FairyGlobal.setLocalHtmlenable(true);
        //初始化框架
        PluginLoader.initLoader(this);
    }
```
        
```
    @Override
    public Context getBaseContext() {
        return PluginLoader.fixBaseContextForReceiver(super.getBaseContext());
    }
```

4、在宿主工程中通过下面3个方法进行最基本的插件操作
```
    安装: PluginManagerHelper.installPlugin( SDcard上插件apk的路径 );
    卸载: PluginManagerHelper.remove( 插件packageName );
    列表: PluginManagerHelper.getPlugins();
```
    
5、通过构造一个插件组件Intent打开插件

   例如打开插件的Launcher界面
```       
   Intent launchIntent = getPackageManager().getLaunchIntentForPackage( 插件packageName );			
   startActivity(launchIntent);
```
   以上所有内容及更多详情可以参考Demo
	
#### 插件侧  
独立插件：新建一个工程, 作为插件工程，无需任何其他配置，编译出来即可当插件apk安装到宿主中。

非独立插件：

1、新建一个工程, 作为插件工程。

2、在插件AndroidManifest.xml中manifest节点中增加如下配置:
```       
    <manifest android:sharedUserId="这里填写宿主工程包名"/>
```       
此配置```与其原始含义无关```。插件框架识别一个插件是否为独立插件，是根据插件的manifest文件中的android:sharedUserId配置来判断，
将android:sharedUserId设置为宿主的packageName，则表示为非独立插件，不设置或者设置为其他值，则表示为独立插件。
                 
3、在build.gradle中添加如下2个配置
```
    dependencies {
        //这个配置的意思是使是插件运行时依赖宿主的class或者依赖宿主依赖的class, 这些jar不会被打包到插件中
        //***这是demo中的示例，请根据自己的实际情况修改***
        provided files(project(':Samples:PluginMain').getBuildDir().absolutePath + '/outputs/PluginMain-Debug.jar')
    }
```
       
```       
    ext {
        //表示指定宿主工程的编译输出目录
        //***这是demo中的示例，请根据自己的实际情况修改***
        host_output_dir = project(':Samples:PluginMain').getBuildDir().absolutePath + "/outputs"
        
        //表示指定宿主工程编译后的中间产物 .ap_ 文件的路径
        //***这是demo中的示例，请根据自己的实际情况修改***
        host_ap_path = host_output_dir+ '/PluginMain-resources-debug.ap_'
        
        //用于混淆配置，如果需要混淆宿主和插件，需要此配置，具体看后文说明
        //***这是demo中的示例，请根据自己的实际情况修改***
        //host_obfuscated_jar = host_output_dir + '/host_[buildType]_obfuscated.jar'
    }
    
    apply from: "https://raw.githubusercontent.com/limpoxe/Android-Plugin-Framework/master/FairyPlugin/plugin.gradle"
 ```       
  
  完成以上3步后即可编译出非独立插件，以上所有内容及更多详情可以参考Demo
  
#### Demo编译方法
    
   a）如果是命令行中：
   
   cd  Android-Plugin-Framework
   
   ./gradlew clean
   
   ./gradlew assembleDebug

   b）如果是studio中：
   
   打开studio右侧gradle面板区，点clean、点assembleDebug。不要使用菜单栏的菜单编译。

   重要：
        
        1、由于编译脚本依赖Task.doLast, 使用其他编译方法（如菜单编译）可能不会触发Task.doLast导致编译失败
        
        2、必须先编译宿主，再编译非独立插件。（这也是使用菜单栏编译会失败的原因之一）
           原因很简单，既然是非独立插件，肯定是需要引用宿主的类和资源的。所以编译非独立插件时会用到编译宿主时的输出物
   
        所以如果使用其他编译方法，请务必仔细阅读build.gradle，了解编译过程和依赖关系后可以自行调整编译脚本，否则可能会失败。

   待插件编译完成后，插件的编译脚本会自动将插件demo的apk复制到PlugiMain/assets目录下（复制脚本参看插件工程的build.gradle）,然后重新
   打包安装PluginMain。
   或者也可将插件apk复制到sdcard，然后在宿主程序中调用PluginLoader.installPlugin("插件apk绝对路径")进行安装。

        
# 其他指南
1. 如何使非独立插件依赖其他插件

   例：插件A依赖插件B，则需在插件A的manifest文件中的application节点下增加如下配置：
       
        <uses-library android:name="这里填写被依赖的插件B的包名" android:required="true" />
       
        并在插件A的build.gradle文件中使用provided添加对插件B的jar的依赖。
        
       此处uses-library与其原始含义无关，仅作一个配置项取巧使用。
       限制：被插件依赖的插件只可以包含class和Manifest和assets等文件，不可以携带资源文件。可参考demo中的pluginBase工程
        
2. 如何使独立插件依赖其他插件
   
   同上。
       
3. 如何将插件中的Fragment嵌入宿主中的Activity中

   首先，需要将此宿主Activity的android:process属性配置为插件进程（插件的代码需在插件进程运行，因此fragment的所在的Activity也需要在插件进程中）
   
   然后，在此插件的AndroidManifest.xml配置<exported-fragment>节点：
   
       <exported-fragment android:name="这里为此Fragment设置一个唯一标识符Id" android:value="这里为此Fragment类全名"/>
       此配置的目的是为了框架能够快速查找到目标Fragment的类名，而不必进行遍历。
            
        在宿主Activity中创建此插件Fragment的实例：
        Class clazz = PluginLoader.loadPluginFragmentClassById(这里填写Fragment的唯一标识符Id);
        if (clazz != null) {
            Fragment fragment = (Fragment) clazz.newInstance();
        }
        可参考demo中的plugintest工程。
        另：根据对宿主Activity的配置，此Fragment的写法又分为两种，见下文。
  
4. 如何将插件的Fragment嵌入其他插件的Activity中

   同上。区别在于此插件Activity无需再配置进程属性，因为插件就是运行在插件进程中。

5. 插件UI可通过Fragment、pluginView或者直接使用Activity来实现        
    
        如果是Fragment，又分为3种：
          1、Fragment运行在宿主中的任意的Activity中
          2、Fragment运行在宿主中的指定的Activity中
          3、Fragment运行在插件中的任意Activity中

        首先需要明确的是，无论是哪种情况，插件Fragment中的Context都必须使用插件自身的Context。
        之所以有上述3种情况，即是根据在插件Fragment中获取Context的方式不同来区分。
        
        第1种，在插件Fragment中通过常规方法获取到的Context都是宿主Context，不可用于插件。
        所以，需要约束Fragment中凡是要使用context的地方，都需要使用通过
              PluginLoader.getDefaultPluginContext(FragmentClass)或者context.createPackageContext("插件包名")
        来获取的插件context。
        注意，对这种Fragment中调用其中View.getContext()返回的类型不是其所在的Activity，强转Activity会出错。其真实类型是PluginContextTheme。
        若需要获取所在的Activity，需要通过View.getParent.getContext() 返回其所在的Activity对象, getParent的目的是拿到宿主控件

        第2种，由于是"运行在宿主中的指定的Activity中"，因此，仅需在指定的Activity上添加@PluginContainer注解，框架会根据此注解自动修正Activity的Context。
        使得在插件Fragment中通过常规方法获取到的Context即是插件Context，可直接使用。也即对Fragment无约束。
        
        第3种，对Activity和Fragment都无特别要求。
          
        总结：
           如果插件Fragment不是运行在自己的插件提供的Activity中，则要么约束Fragment的context的获取方法，要么约束其运行时的容器Activity的Context(通
           过@PluginContainer注解指明使用哪个插件的Context)
           
           如果插件Fragment是运行在自己的插件提供的Activity中，则对Fragment和Activity都无特别要求。
               
        demo中都有例子。
		
		
6. 如何将插件中的View是嵌入宿主中的Activity中使用

   鉴于实际情况中一些特殊场景可能不适合使用Fragment，框架也支持直接将插件View嵌入宿主Activity中使用。
   
   首先，需要将此宿主Activity的android:process属性配置为插件进程，在宿主的布局文件中嵌入pluginView 节点，通过class="View的类全名"来指定使用的控件，
   
   其他用法和普通控件无异，具体参考demo
        
        这些可以被嵌入插件view的宿主Activity，需要在Activity的class上添加@PluginContainer注解（无需指插件包名，插件包名通过pluginView节点指定），
        框架借此来识别pluginVie标签并解析出插件包名。
        
        注意，在嵌入式插件View中调用getContext()返回的类型不是其所在的Activity，而是插件PluginContextTheme类型。
        若想获得所在宿主的Activity对象，需要使用((PluginContextTheme)View.getContext()).getOuter()获取，
        或者通过View.getParent.getContext()来获取， getParent的目的是拿到宿主控件。
                                    
7. 如何在插件使用宿主中定义的主题

        插件中可以直接使用宿主中定义的主题。
        例如，宿主中定义了一个主题为AppHostTheme，那么插件的Manifest文件中可直接使用android:theme="@style/AppHostTheme"来使用宿主主题
        如果插件要扩展宿主主题，也可以直接使用。例如，在插件的style.xml中定义一个主题PluginTheme：<style name="PluginTheme" parent="AppHostTheme" />
        以上写法，IDE中可能会标红，但是不影响编译和运行。
            
8. 如何在插件使用宿主中定义的其他资源

   分为在代码中和在资源文件中两种。代码中直接通过R即可使用。资源中，插件中可以直接使用宿主中定义的资源，但是写法和直接使用插件自己的资源略有不同,
   通常应写为：
        
        android:label="@string/string_in_plugin"
        
        但是上面只适用资源在插件中的情况，如果资源是定义在宿主中，应使用下面的写法
        
        android:label="@*xx.xx.xx:string/string_in_host"
        
        其中，xx.xx.xx是宿主包名
    
        以上写法，IDE中可能会标红，但是不影响编译和运行。
   
    注意本条与上一条区别：插件中使用宿主主题，可以直接使用，但是使用宿主资源，需要带上*packageName:前缀
                                                
9. 如何在插件中对外提供函数式服务（非Service组件，支持同进程和跨进程）

        插件和外界交互，除了可以使用标准api交互之外，还提供了函数式服务
        插件发布一个函数服务，只需要在AndroidManifest.xml配置<exported-service>节点
        其他插件或者宿主即可调用此服务，具体参考demo
      
        
10. 如何在插件中获取宿主包名

    插件getPackageName返回的是插件包名。如果要在插件中获取宿主包名，使用框架提供的FakeUtil.getHostPackageName()函数，参数为插件的context，例如插件activity或者插件Application
   
   
11. 如何在插件中使用需要appkey的三方sdk
        
         插件中使用需要appkey的sdk，例如将微信分享、百度地图sdk、友盟分享sdk集成到插件，请参考demo：baidumapsdk，wxsdklibrary，仔细阅读。
         
         需要使用appKey的sdk，通常需要使得sdk能同时正确的取到下面3个值，
             1、packageName
             2、meta-data
             3、signatures
         SDK取到这3个值以后，会去它自身的服务器上验证appkey。
         
         然而，在插件中调用getPackageName等等相应的系统api，得到的是插件的packageName，插件的meta-data，以及插件的signatures。
         
         所以，在sdk平台上注册appkey时直接使用插件的包名，签名，然后将appkey的配置埋入插件的meta-data, 此种情况无需特别配置。插件集成此sdk即可正常使用。
         
         但是，实际中仍然会存在下面2种情况：
               1、可能sdk需要通过其自身的app来进行校验或者交互。例如微信分享sdk，它需要唤醒微信App，再由微信App和宿主App进行验证和交互（第三方app要唤起插件中的静态组件必须由宿
                  主程序进行桥接，方法请参看wxsdklibrary工程的用法），绕过了插件。
                  此种sdk在平台上注册appkey时必须使用宿主的包名
               
               2、可能由于特殊原因，在sdk平台上注册appkey时已经使用了宿主的包名，不能在更换使用插件的包名进行注册。
               
               以上两种情况，sdk在拿到插件的Context以后（通常是在sdk的init方法里面传入的插件Application），
               sdk借助插件Context取不到正确的packageName、meta-data、signatures。正确的值全部在宿主中，插件拿到的全部是插件自己的。
               
         sdk在获取packageName、meta-data、signatures这3个信息，都是通过传入的Context调用其getXXXX或者context.getPackageManager().getXXX来获取。
         因此，针对这两种case，需要在初始化插件sdk是，传入fakeContext而不是插件的Context来欺骗sdk，使其能拿到正确信息。
        
         在demo中，微信sdk插件的FakeContext，和百度地图sdk的FakeContext，即是用来解决上面两种情况下的问题。
         demo中的fakeContext重写了需要的相关方法。
         
         如要使用此类插件，请务必先完全理解上述解释，以及为何使用FakeContext可以达成目的。然后遇到各种相关问题都可迎刃而解。

12. 如何混淆宿主和插件

    若需要混淆宿主，请参考PluginMain工程下的混淆配置，以宿主防止混淆后框架异常。
    
    若需要混淆非独立插件，步骤如下：
             
         1、在宿主中开启混淆编译，outputs目录下会生成一个混淆后的jar：host_[buildType]_obfuscated.jar，以及mapping目录下会生成一个mapping文件
         2、在非独立插件工程中开启混淆，同时将provided宿主jar的配置修改为compile宿主jar
         3、在非独立插件工程的build.gradle下增加proguardRule相关配置，在rule文件中使用添加：-applymapping mapping文件路径。 此mappiing文件为第1步中编译宿主生成的文件
         4、在非独立插件工程的build.gradle下增加如下配置
              ext {
                  //用于混淆配置， 此配置路径指向第1步中编译宿主产生的混淆后的jar：host_[buildType]_obfuscated.jar文件
                  host_obfuscated_jar = host_output_dir + '/host_[buildType]_obfuscated.jar'
              }
         执行这4个步骤之后，编译出来的非独立插件即为混淆后的插件
         
         若混淆后出现运行时异常，请检查上述第7条补充说明第3步产生的临时文件，是否存在不该存在的类或者少了需要的类。
         文件位于build/tmp/jarUnzip/host,build/tmp/jarUnzip/plugin;
         host目录为宿主编译出来混淆后的jar包解压后目录，等同于对宿主反编译后得到class目录
         plugin为插件编译出来混淆后的jar包解压后目录。正常情况下host目录的内容应该为plugin目录内容的子集。
         且host目录存在的每一个文件，必定在plugin相同路径下存在，否则很可能是依赖配置错误或者mapping文件配置错误，会导致diff是出现遗漏而引起class异常
         插件最终的混淆后jar包，即是通过这两个目录diff后从plugin中剔除了所有在host中存在的文件后压缩而成。
         
         插件混淆后的jar包和diff后的jar包，在插件outputs目录下都有备份。
             
         这里需要注意的是插件开启混淆以后，需要在插件的proguard里面增加对插件Fragment的keep，否则如果此fragment没有在插件自身
         使用，仅作为嵌入宿主使用，则progurad可能误以为这个类在插件中没有被使用过而被精简掉
             
13. 如何使外部应用或者系统可以直接通过插件组件的Intent打开插件

    由于插件并没有正常安装到系统中，插件组件的Intent不能被系统识别，因此外部应用或者系统需要直接唤起插件组件时，需要将插件Intent在宿主的Manifest中        
    也预置一份，并在IntentFilter增加STUB_EXACT配置，如：

        <receiver android:name="com.example.plugintest.receiver.BootCompletedReceiver"
              android:process=":plugin">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"/>
                <action android:name="android.intent.action.ACTION_SHUTDOWN"/>
            </intent-filter>
            <!--下面是额外添加的配置项，作用是使得框架将此组件配置识别为插件组件 -->
            <intent-filter>
                <action
                    android:name="${applicationId}.STUB_EXACT" />
                <category
                    android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </receiver>
        
       可以参考demo        


# 注意事项

    1、非独立插件中的class不能同时存在于宿主和插件程序中
      
       如果插件和宿主共享依赖库，常见的如supportv4，那么编译插件的时候不可将共享库编译到插件当中，包括共享库的代码以及R文件。
       只需在编译时以provided方式添加到classpath中，公共库仅参与编译，不参与打包。参看demo。
    
    2、若插件中包含so，则需要在宿主的相应目录下添加至少一个so文件，以确保插件和宿主支持的so种类完全相同
    
       例如：插件包含armv7a、arm64两种so文件，则宿主必须至少包含一个armv7a的so以及一个arm64的so。
       若宿主本身不需要so文件，可随意创建一个假so文件放在宿主的相应目录下。例如pluginMain工程中的libstub.so其实只是一个txt文件。

       需要占位so的原因是，宿主在安装时，系统会扫描宿主中的so的（根据文件夹判断）类型，决定宿主在哪种cpu模式下运行、并保持到系统设置里面。
       (系统源码可查看com.android.server.pm.PackageManagerService.setBundledAppAbisAndRoots()方法)
       例如32、64、还是x86等等。如果宿主中不包含so，系统默认会选择一个最适合当前设备的模式。
       那么问题来了，如果系统默认选择的模式，和将来下载安装的插件中支持的so模式不匹配，则会出现so异常。

       因此需要提前通知系统，宿主需要在哪些cpu模式下运行。提前通知的方式即内置占位so。

    3、框架中对非独立插件做了签名校验。如果宿主是release模式，要求插件的签名和宿主的签名一致才允许安装。
       这是为了验证插件来源合法性

    4、需要在android studio中关闭instantRun选项。因为instantRun会替换apk的application配置导致框架异常
 
    5、本项目除master分支外，其他分支不会更新维护。
    
    6、如果是非独立插件, 需要先编译宿主, 再编译插件, 
       如果是非独立插件, 需要先编译宿主, 再编译插件
       如果是非独立插件, 需要先编译宿主, 再编译插件
       
       重要的事情讲3遍！遇到编译问题请先编译宿主, 再编译插件。因为从配置可以看出非独立插件编译时需要依赖宿主编译时的输出物
       
    以上所有内容及更多详情可以参考Demo
  
## 其他
1. [原理简介](https://github.com/limpoxe/Android-Plugin-Framework/wiki/%E5%8E%9F%E7%90%86%E7%AE%80%E4%BB%8B)
2. [使用Public.xml的坑和填坑](https://github.com/limpoxe/Android-Plugin-Framework/wiki/%E4%BD%BF%E7%94%A8Public.xml%E7%9A%84%E5%9D%91%E5%92%8C%E5%A1%AB%E5%9D%91).
3. [更新记录](https://github.com/limpoxe/Android-Plugin-Framework/wiki/%E6%9B%B4%E6%96%B0%E8%AE%B0%E5%BD%95)

## 联系作者：
  Q：15871365851，添加时请注明插件开发

  Q群：116993004、207397154(已满)，重要：添加前请务必仔细阅读此ReadMe！请务必仔细阅读Demo！
