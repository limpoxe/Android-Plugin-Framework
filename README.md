# Android-Plugin-Framework

README: [中文](https://github.com/limpoxe/Android-Plugin-Framework/blob/master/README.md)

Android-Plugin-Framework是一个Android插件化框架，用于通过动态加载的方式免安装运行插件apk

### 最新版本: 'com.github.limpoxe:Android-Plugin-Framework:0.0.67@aar'
               
### 此项目主要目标是为了运行非独立插件，而不是任意第三方app。

尽管此框架支持独立插件，但目标并不是为了支持任意三方app，不同于平行空间或应用分身之类的产品。
非独立插件相比任意三方app来说，可以预见到其使用了哪些系统api和特性，而且所有行为都是可以预测的。而任意三方app是不可预测的。
框架的做法是按需hook，即需要用到哪些系统特性和api，就对哪些特性和api提供支持。这种做法对开发非独立插件和二方独立插件而言完全足够。
目前已经添加了对常用特性和api的支持，如需使用的api还未支持请联系作者。

### FEATURE
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
- 支持FileProvider
- 支持2.3-9.0

### LIMIT
- 不支持插件Activity转场动画使用插件中的动画资源
- 不支持插件Manifest中申请权限，所有权限必须预埋到宿主Manifest中
- 不支持第三方app试图唤起插件中的组件时直接使用插件组件的Intent。
  第三方app要唤起插件中的静态组件，例如Activity/service/Provider，必须由宿主程序进行桥接，即此组件需同时预埋到宿主和插件的Manifest中
- 不支持android.app.NativeActivity
- 不支持当一个插件依赖另一个插件时，被插件依赖的包含资源
- 不支持插件中的webview弹出```原生Chrome组件```
  例如通过html的<input type="date"/>标签设置时间选择器。
  说明：是否能支持原生组件取决于系统中使用WebView的实现。
       如果是使用的Android System Webview，则可以支持。因为它packageId是以0x3f开头；
       如果是使用的Chrome Webview，则不支持。因为它packageId是以0x7f开头，会和插件冲突。
       这是采用Public.xml进行资源分组的缺陷。
- 可能不支持对插件或者宿主进行加壳加固处理，未尝试

# HOW TO USE
### 重要：android.enableAapt2=true，com.android.tools.build:gradle:3.2.1，gradle-4.6
```
    allprojects {
    		repositories {
    			...
    			maven { url 'https://jitpack.io' }
    		}
    }
```
### 宿主侧
1、 新建一个工程，作为宿主工程

2、 在宿主工程的build.gradle文件下添加如下3个配置
```
    //脚本版本跟插件框架版本保持同步
    apply from: "https://raw.githubusercontent.com/limpoxe/Android-Plugin-Framework/0.0.67/FairyPlugin/host.gradle"        

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
        implementation('com.github.limpoxe:Android-Plugin-Framework:0.0.67@aar')
        //可选，用于支持插件全局函数式服务，不使用全局函数式服务不需要添加此依赖
        //implementation('com.limpoxe.support:android-servicemanager:1.0.5@aar')
    }
```

```
    fairy {
        //可选配置，用于指定插件进程名。默认插件进程为单独的进程，进程名为":plugin"
        //若设置为空串或者null即是使用宿主进程作为插件进程
        //pluginProcess = ""
        //pluginProcess = null
        //pluginProcess = ":xxx"
    }
```

3、 在宿主工程中新建一个类继承自Application类, 并配置到AndroidManifest.xml中并重写这个类的下面2个方法
```
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        
        //框架日志开关, 默认false
        FairyGlobal.setLogEnable(true);
        
        //首次加载插件会创建插件对象，比较耗时，通过弹出loading页来过渡。
        //这个方法是设置首次加载插件时, 定制loading页面的UI, 不传即默认没有loading页
        //在宿主中创建任意一个layout传进去即可
        //注意：首次唤起插件组件时，如果是通过startActivityForResult唤起的，如果配置了loading页，
        //则实际是先打开了loading页，再转到目标页面，此时会忽略ForResult的结果。这种情况下应该禁用loading页配置
        FairyGlobal.setLoadingResId(R.layout.loading);
        
        //是否支持插件中使用本地html, 默认false
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
   宿主编译完成后，会在outputs/distrubites目录下生成一个名为host.bar的基线包，作为编译插件的基线。
   以上所有内容及更多详情可以参考Demo
	
### 插件侧  
独立插件：

    新建一个工程, 作为插件工程，无需任何其他配置，编译出来即可当插件apk安装到宿主中。

非独立插件：

1、新建一个工程, 作为插件工程。
            
2、在build.gradle中添加如下2个配置
```
    //脚本版本跟插件框架版本保持同步
    apply from: "https://raw.githubusercontent.com/limpoxe/Android-Plugin-Framework/0.0.67/FairyPlugin/plugin.gradle"

    android {
        defaultConfig {
            //这个配置不可省略
            applicationId 插件app包名        
        }
    }
    
    dependencies {
        //***这是demo中的示例，请根据自己的实际情况修改，作用是指向插件依赖的宿主基线包***
        //支持文件、maven坐标等写法
        //baselinePatch 'xxx:xxx:xxx@bar'
        baselinePatch files(project(':Samples:PluginMain').getBuildDir().absolutePath + '/distributions/host.bar')
    }

 ```       
  
  完成以上2步后即可编译出非独立插件，以上所有内容及更多详情可以参考Demo
  
### Demo编译方法
    
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

        3、由于宿主和插件在同一个工程中，点击assembleDebug时编译顺序不可控，会导致每次clean后，首次assembleDebug会失败，此时重新编译即可
           可能需要执行3次assembleDebug，
               第一次是编译宿主，产生bar文件，
               第二次是依赖bar编译插件，产生插件文件
               第三次是重新编译宿主，将插件文件内置到宿主assets中
            所以如果使用其他编译方法，请务必仔细阅读build.gradle，了解编译过程和依赖关系后可以自行调整编译脚本，否则可能会失败。

	    4、Demo中使用了arm平台的so，若在x86平台上测试Demo可能会有so异常，请自行适配so。
	
   待插件编译完成后，即可通过宿主在运行时下载插件apk或者将插件apk复制到sdcard调用PluginManagerHelper.installPlugin("插件apk绝对路径")进行插件安装。

   通常插件会内置一个版本到宿主中随宿主一起发布，则需要将插件配置到宿主的assets目录下，再编译一次宿主（即上述3中的第三次编译）。
   配置方法如下：

        dependencies {
            //支持坐标依赖
            //innerPlugin 'xxx:xxx:xxx@apk'
            innerPlugin '/xx/xx/xx/xx.apk'
        }


   增加这个配置以后，宿主在打包时会将这个依赖的插件apk打包到宿主的assets目录中

## 其他
1. [使用指南](https://github.com/limpoxe/Android-Plugin-Framework/wiki/%E5%85%B6%E4%BB%96%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97)
2. [原理简介](https://github.com/limpoxe/Android-Plugin-Framework/wiki/%E5%8E%9F%E7%90%86%E7%AE%80%E4%BB%8B)
3. [使用Public.xml的坑和填坑](https://github.com/limpoxe/Android-Plugin-Framework/wiki/%E4%BD%BF%E7%94%A8Public.xml%E7%9A%84%E5%9D%91%E5%92%8C%E5%A1%AB%E5%9D%91).
4. [更新记录](https://github.com/limpoxe/Android-Plugin-Framework/wiki/%E6%9B%B4%E6%96%B0%E8%AE%B0%E5%BD%95)

## 联系作者：
  Q：15871365851，添加时请注明插件开发
  Q群：116993004，重要：添加前请务必仔细阅读此ReadMe！请务必仔细阅读Demo！
