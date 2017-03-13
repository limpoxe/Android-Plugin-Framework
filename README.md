# Android-Plugin-Framework

此项目是Android插件开发框架完整源码及示例。用来通过动态加载的方式在宿主程序中运行插件APK。

开发插件apk和开发普通apk无区别。对插件宿主来说，框架完全透明，无约定约束

# 如何使用

## 宿主侧
    1、新建一个工程, 作为宿主工程
    
    2、在宿主工程的build.gradle文件下添加如下3个配置
         
        android {
            defaultConfig {
                //这个配置不可省略
                applicationId 宿主app包名        
            }
        }
        
        dependencies {
            compile('com.limpoxe.fairy:FairyPlugin:0.0.49-snapshot@aar')
            //optional， 用于支持函数式服务，不使用函数服务不需要添加此依赖
            compile('com.limpoxe.support:android-servicemanager:1.0.5@aar')
        }
        
        apply from: "https://raw.githubusercontent.com/limpoxe/Android-Plugin-Framework/master/FairyPlugin/host.gradle"
        
        
        默认情况下，插件运行在插件进程中，插件进程名为:plugin
        若需要修改插件进程名称，或者需要使插件和宿主运行在同一进程
        可增加如下配置：
        ext {
            //可选配置，用于指定插件进程名。
            //不设置即使用默认的独立进程(:plugin)
            //设置为空串或者null即是和宿主同进程
            //pluginProcess = ""
            //pluginProcess = null
            pluginProcess = ":xxx"
        }
        
    3、在宿主工程中新建一个类继承自Application类, 并配置到AndroidManifest.xml中
       重写这个类的2个方法
       
        @Override
        protected void attachBaseContext(Context base) {
            super.attachBaseContext(base);
            PluginLoader.initLoader(this);
            //这个方法是设置首次加载插件时, 定制loading页面的UI, 不传即默认没有loading页
            //在宿主中创建任意一个layout传进去即可
            PluginLoader.setLoadingResId(R.layout.loading);
        }

	    @Override
	    public Context getBaseContext() {
	        return PluginLoader.fixBaseContextForReceiver(super.getBaseContext());
	    }
	    
    4、在宿主工程中通过下面3个方法进行插件操作
	    安装: PluginManagerHelper.installPlugin( SDcard上插件apk的路径 );
	    卸载: PluginManagerHelper.remove( 插件packageName );
	    列表: PluginManagerHelper.getPlugins();
	    
    5、在宿主中打开插件
	
       //打开插件的Launcher界面
       Intent launchIntent = getPackageManager().getLaunchIntentForPackage( 插件packageName );			
       startActivity(launchIntent);
    	
     以上所有内容及更多详情可以参考Demo
	
## 插件侧    
    1、新建一个插件工程（对独立插件和非独立插件含义后文有解释）
    
    2、如果是独立插件, 无需任何配置, 一个普通的apk编译出来即可当插件apk安装到宿主中
       如果独立插件也需要依赖其他公共独立插件，同下述3.3做法
    
    3、如果是非独立插件：
     
        3.1、在插件AndroidManifest.xml中manifest节点中增加如下配置:
             
             <manifest android:sharedUserId="这里填写宿主工程包名"/>
             
	     这个配置只是作为一个标记符使用，框架通过检查这个配置来判断是否为独立插件，和其原始含义无关
	     
	     插件框架识别一个插件是否为独立插件，是根据插件的Manifest文件中的android:sharedUserId属性。
	     将android:sharedUserId属性设置为宿主的packageName，则表示为非独立插件。
	      
	     不配置或配置为其他值表示为独立插件
                            
        3.2、在build.gradle中加入如下3个配置
    
            dependencies {
                //这个配置的意思是使插件运行时依赖宿主中的jar、依赖宿主依赖的aar中的jar
                //根据自己的实际情况修改为想要依赖的jar的路径即可
                //这些jar不会被打包到插件中，
                provided files(project(':Samples:PluginMain').getBuildDir().absolutePath + '/xxx/xxx/xxx.jar')
            }
            
            ext {
                //这2个常量在下面的apply的脚本中要用到
                //host_output_dir表示指定宿主工程的编译输出目录
                //host_ap_path表示指定宿主工程编译后的中间产物 .ap_ 文件的路径
                host_output_dir = project(':Samples:PluginMain').getBuildDir().absolutePath + "/outputs"
                
                //注意这里的.ap_文件，是通过编译宿主产生，不同的buildType请更换成不同的名字
                host_ap_path = host_output_dir+ '/PluginMain-resources-debug.ap_'
                
                //用于混淆配置，如果需要混淆宿主和插件，需要此配置，具体看后文说明
                //host_obfuscated_jar = host_output_dir + '/host_[buildType]_obfuscated.jar'
            }
        
            apply from: "https://raw.githubusercontent.com/limpoxe/Android-Plugin-Framework/master/FairyPlugin/plugin.gradle"
              
        3.3、如果此插件还依赖其他运行时公共插件，比如将网络请求库从宿主中剔除出来作为单独的公共插件使用，参考pluginbase用法
                在插件AndroidManifest.xml中application节点中增加如下配置:
               <uses-library android:name="xx.xx.xx" android:required="true" />
                xx.xx.xx 是其依赖的插件的包名，被依赖的插件不支持携带资源
       
        3.4、如果此插件需要对外提供函数式服务（支持同进程和跨进程）
             插件和外界交互，除了可以使用标准api交互之外，还提供了函数式服务
             插件发布一个函数服务，只需要在AndroidManifest.xml配置<exported-service>节点
             其他插件或者宿主即可调用此服务，具体参考demo
             
        3.5  如果此插件需要对外提供嵌入式Fragment
             如果需要在其他插件，或者宿主的Activity中嵌入插件提供的Fragment
             需要在AndroidManifest.xml配置<exported-fragment>节点，具体参考demo
             
        3.5.1 插件Fragment
              插件UI可通过fragment、<pluginView/>或者activity来实现        
    
              如果是fragment实现的插件，又分为3种：
                  1、fragment运行在宿主中的普通Activity中
                  2、fragment运行在宿主中的特定Activity中
                  3、fragment运行在插件中的Activity中

                  对第1种，对Frament的开发有约束，要求Fragment中凡是要使用context的地方，都需要使用通过
                        PluginLoader.getDefaultPluginContext(FragmentClass)
                  或者 
                        context.createPackageContext("插件包名")来获取的插件context，
                        
                  但对fragment的运行容器，即对宿主中的普通Activity没有特殊要求和约束。
                  
                  注意，，对这种Fragment中调用其View.getContext()返回的类型不是其所在的Activity，强转会出错。得到的真实类型是PluginContextTheme类型。
                  若需要获取Activity，需要通过View.getRootView().getContext() 返回其所在的Activity对象
                                    
                  对第2种和第3种，对Fragmet的开发无约束，和正常写法没有任何区别，但是对其运行容器，即Activity有不同要求
                 
                  对第2种，要求Activity上添加@PluginContainer注解，作用是通知框架这个fragment来自哪个插件，以便框架替换相应的Context
                  若需要获取其所在的Activity、除了可以通过上述View.getRootView().getContext()方法之外，还可以通过
                  ((PluginContextTheme)View.getContext()).getOuter()来获取其所在的Activity
                        
                  对第3种，由于本身就是运行在自己的插件索提供的Activity中，则对Fragment和Activity都无特别要求和约束
                  view.getContext()即为Activity对象
    
                  总结：
                       如果插件Fragment不是运行在自己的插件提供的Activity中，则要么约束Fragment的context的获取方法，要么约束其运行时的容器
                       Activity的Context(通过@PluginContainer注解指明使用哪个插件的Context)
                       
                       如果插件Fragment是运行在自己的插件提供的Activity中，则对Fragment和Activity都无特别要求。
                       
                  demo中都有例子。
		
        3.6 如果此插件需要对外提供嵌入式View     
            如果需要在其他插件，或者宿主的Activity中嵌入插件提供的View
            需要在其他插件，或者宿主的的布局文件中嵌入<pluginView /> 节点，用法同普通控件，具体参考demo
            
            此类嵌入插件view的宿主Activity，需要添加@PluginContainer注解（无需指插件包名，插件包名通过pluginView节点指定），
            用来识别pluginVie标签并解析出插件包名。
            
            注意，在嵌入式插件View中调用getContext()返回的类型不是其所在的Activity，而是插件PluginContextTheme类型。
            若想获得所在宿主的Activity对象，需要使用((PluginContextTheme)View.getContext()).getOuter()获取，
            或者通过View.getRootView().getContext()来获取
                
        3.7 如果插件需要使用宿主中定义的主题
            插件中可以直接使用宿主中定义的主题。例如，宿主中定义了一个主题为AppHostTheme，
            那么插件的Manifest文件中可直接使用android:theme="@style/AppHostTheme"来使用宿主主题
             
            如果插件要扩展宿主主题，也可以直接使用。例如，在插件的style.xml中定义一个主题PluginTheme
            <style name="PluginTheme" parent="AppHostTheme" />
             
            以上写法，IDE中可能会标红，但是不影响编译和运行。
            
        3.8 如果插件需要使用宿主中定义的其他资源
            插件中可以直接使用宿主中定义的资源，但是写法和直接使用插件自己的资源略有不同,
            通常应写为：
                android:label="@string/string_in_plugin"
            但是上面只适用资源在插件中的情况，如果资源是定义在宿主中，应使用下面的写法
                android:label="@*xx.xx.xx:string/string_in_host"
            其中，xx.xx.xx是宿主包名
            
            以上写法，IDE中可能会标红，但是不影响编译和运行。
            
            注意本条与上一条3.7的区别：插件中使用宿主主题，可以直接使用，但是使用宿主资源，需要带上*packageName:前缀
            
        3.9 如果要在插件中获取宿主包名
            插件返回的是插件包名，如果要在插件中获取宿主包名，写法如下：
            (见FakeUtil.getHostPackageName())
            //参数为插件的context，例如插件activity或者插件Application
            public String getHostPackageName(ContextWrapper pluginContext) {
                  Context context = pluginContext;
                  while (context instanceof ContextWrapper) {
                    context = ((ContextWrapper) context).getBaseContext();
                  }
                  //到这里context的实际类型应当是ContextImpl类，可以返回宿主packageName
                  return context.getPackageName();
            }
            
        3.10 如果插件中要使用需要appkey的sdk
            插件中使用需要appkey的sdk，例如微信分享和百度地图sdk，参考demo：baidumapsdk，wxsdklibrary
                  
    4、如果是非独立插件, 需要先编译宿主, 再编译插件, 因为从如上的配置可以看出非独立插件编译时需要依赖宿主编译时的输出物
       如果是非独立插件, 需要先编译宿主, 再编译插件
       如果是非独立插件, 需要先编译宿主, 再编译插件
       如果是非独立插件, 需要先编译宿主, 再编译插件
       
       重要的事情讲3遍！遇到编译问题请先编译宿主, 再编译插件
       
    以上所有内容及更多详情可以参考Demo
 

# 注意事项

    1、非独立插件中的class不能同时存在于宿主和插件程序中
      
       如果插件和宿主共享依赖库，常见的如supportv4，那么编译插件的时候不可将共享库编译到插件当中，
       包括共享库的代码以及R文件，只需在编译时以provided方式添加到classpath中，公共库仅参与编译，不参与打包，
       且插件中如果要使用共享依赖库中的资源，需要使用共享库的R文件来进行引用。参看demo。
    
    2、若插件中包含so，则需要在宿主的相应目录下添加至少一个so文件，以确保插件和宿主支持的so种类完全相同
    
       例如：插件包含armv7a、arm64两种so文件，则宿主必须至少包含一个armv7a的so以及一个arm64的so。
       若宿主本身不需要so文件，可随意创建一个假so文件放在宿主的相应目录下。例如pluginMain工程中的libstub.so其实只是一个txt文件。

       需要占位so的原因是，宿主在安装时，系统会扫描宿主中的so的（根据文件夹判断）类型，决定宿主在哪种cpu模式下运行、并保持到系统设置里面。
       (系统源码可查看com.android.server.pm.PackageManagerService.setBundledAppAbisAndRoots()方法)
       例如32、64、还是x86等等。如果宿主中不包含so，系统默认会选择一个最适合当前设备的模式。
       那么问题来了，如果系统默认选择的模式，和将来下载安装的插件中支持的so模式不匹配，则会出现so异常。

       因此需要提前通知系统，宿主需要在哪些cpu模式下运行。提前通知的方式即内置占位so。

    3、插件默认是在插件进程中运行，如需切到宿主进程，仅需将Fairy包的Manifest中配置的所有组件去都去掉掉process属性即可。
       
       demo中PluginMain工程下有几个插件进程的demo也需要去掉process属性

    4、编译方法

       a）如果是命令行中：
       cd  Android-Plugin-Framework
       ./gradlew clean
       ./gradlew build

       b）如果是studio中：
       打开studio右侧gradle面板区，点clean、点build。不要使用菜单栏的菜单编译。

       重要：
            1、由于编译脚本依赖Task.doLast, 使用其他编译方法（如菜单编译）可能不会触发Task.doLast导致编译失败
            2、必须先编译宿主，再编译非独立插件。（这也是使用菜单栏编译会失败的原因之一）
               原因很简单，既然是非独立插件，肯定是需要引用宿主的类和资源的。所以编译非独立插件时会用到编译宿主时的输出物
       
            所以如果使用其他编译方法，请务必仔细阅读build.gradle，了解编译过程和依赖关系后可以自行调整编译脚本，否则可能会失败。

       待插件编译完成后，插件的编译脚本会自动将插件demo的apk复制到PlugiMain/assets目录下（复制脚本参看插件工程的build.gradle）,然后重新
       打包安装PluginMain。
       或者也可将插件apk复制到sdcard，然后在宿主程序中调用PluginLoader.installPlugin("插件apk绝对路径")进行安装。
    
    5、框架中对非独立插件做了签名校验。如果宿主是release模式，要求插件的签名和宿主的签名一致才允许安装。
       这是为了验证插件来源合法性

    6、需要在android studio中关闭instantRun选项。因为instantRun会替换apk的application配置导致框架异常
 
    7、项目插件化后，特别需要注意的是宿主程序混淆问题。
      若公共库混淆后，可能会导致非独立插件程序运行时出现classnotfound，原因很好理解。
      所以公共库一定要排除混淆或者使用稳定的mapping混淆。
      
      具体方法：
            1、开启混淆编译宿主，保留mapping文件
            2、将插件的build.gradle文件中的provided配置换成compile， 因为provided方式提供的包不会被混淆
            3、在插件的混淆配置中apply编译宿主时产生的mapping文件。
            4、接着在插件编译脚本中开启multdex编译。并配置multdex的mainlist，使得原先所有provided的包的class被打入到副dex中。
               这样插件编译完成后，会有2个dex，1个是插件自己需要的代码，1个是原先provided后来改成了compile的那些包。
            5、再将这个原provided的包形成的dex，也就是副dex从apk中删除，再对插件apk重新签名。
            
            上述方法作者也未试过，理论上可以解决公共库混淆问题。
            gradle插件在1.5版本以后去除了指定mainlist的功能，因此在高于这个版本时指定multidex分包需要使用其他分包插件。
            可使用这个项目：https://github.com/ceabie/DexKnifePlugin
      
      若需要混淆FairyPlugin工程的代码，请参考PluginMain工程下的混淆配置
      
      补充：
          鉴于上述方案实现起来有一定难度，这里再提供另外一个较容易实现的思路。
          
          1、正常编译宿主，保留mapping文件和build/intermediates/transforms/proguard/下的jar包
          2、编译插件时，不使用provided，仍使用compile引用宿主的混淆前jar编译插件，并应用第一步的mapping文件，完成后保留build/intermediates/transforms/proguard/下的jar包。
          3、将第一步和第二步的两个jar包按class粒度进行diff，即，将第一步中存在jar中的class文件，从第二步中的jar包中删除。得到新的jar包
          4、将新的jar包编译成dex，即得到了我们需要的插件dex，再将其写入插件apk，签名即可
      
    7.1、最新脚本已支持对非独立插件进行混淆(2017-03-12)
           
          基于上述第7条的补充说明中的原理实现。
          使用方法步骤如下：
          1、在宿主中开启混淆编译，outputs目录下会生成一个混淆后的jar：host_[buildType]_obfuscated.jar，以及mapping目录
          2、在非独立插件工程中开启混淆，同时将provided宿主jar的配置修改为compile宿主jar
          3、在非独立插件工程的build.gradle下增加proguardRule相关配置，在rule文件中使用添加：-applymapping mapping文件路径。 此mappiing文件为第1步中编译宿主生成的文件
          4、在非独立插件工程的build.gradle下增加如下配置
               ext {
                   //用于混淆配置， 此配置路径指向第1步中编译宿主产生的host_[buildType]_obfuscated.jar文件
                   host_obfuscated_jar = host_output_dir + '/host_[buildType]_obfuscated.jar'
               }
          执行这4个步骤之后，编译出来的非独立插件即为混淆后的插件
          
          若混淆后出现运行时异常，请检查上述第7条补充说明第3步产生的临时文件，是否存在不该存在的类或者少了需要的类。
          文件位于build/tmp/jarUnzip/host,build/tmp/jarUnzip/plugin;
          host目录为宿主编译出来混淆后的jar包解压后目录，plugin为插件编译出来混淆后的jar包解压后目录
          插件最终的混淆后jar包，即是通过这两个目录diff后从plugin中剔除了所有在host中存在的文件后压缩而成。
          
          插件混淆后的jar包和diff后的jar包，在插件outputs目录下都有备份。
          
          这里需要注意的是插件开启混淆以后，需要在插件的proguard里面增加对插件Fragment的keep，否则如果此fragment没有在插件自身
          使用，仅作为嵌入宿主使用，则progurad可能误以为这个类在插件中没有被使用过而被精简掉
          
    8、android sdk中的build tools版本较低时也无法编译public.xml文件，因此如果采用public.xml的方式，应使用较新版本的buildtools。
    
    9、本项目除master分支外，其他分支不会更新维护。

# 已支持：
   
    1、支持非独立插件和独立插件
        非独立插件指自己编译的需要依赖宿主中的公共类和资源的插件，不可独立安装运行。
       
        独立插件不依赖宿主，可以独立安装运行，其中又分为两种：
            一种是自己编译的不需要依赖宿主中的类和资源的插件，可独立安装运行；
	    一种是第三方发布的apk，如从应用市场下载的apk，可独立安装
	   
        之所以独立插件区分这两种情况，是因为插件框架中没有全量hook系统api。如果要支持第三方apk，在无约束规范的情况下，全量hook系统api是必须的。
	而实际情况是，如果插件是自己开发的，可以预见其中使用到的系统api是都是常用的和极少量的。插件框架的做法是按需hook。
	
    2、支持fragment、activity、service、receiver、contentprovider、so、application、notification、LaunchMode。
    
    3、支持插件无缝使用宿主资源、宿主主题、系统主题、插件自身主题以及style、轻松支持皮肤切换
    
    4、支持插件发送notification时在RemoteViews携带插件自定义的布局资源（只支持5.x及以上, 且不支持miui8）
    
    5、支持插件热更新：即在插件模块已经被唤起的情况先安装新版本插件，无需重启进程（有前提）
    
    6、支持在插件中注册全局服务：即在某个插件中注册一个服务，在其他插件中通过LocalServiceManager获取这个服务

# 不支持：

    1、插件Activity切换动画不支持使用插件自己的资源。
  
    2、不支持插件申请权限，权限必须预埋到宿主中。
  
    3、不支持第三方app试图唤起插件中的组件时直接使用插件app的Intent。即插件app不能认为自己是一个正常安装的app。
     第三方app要唤起插件中的静态组件时必须由宿主程序进行桥接，方法请参看wxsdklibrary工程的用法

    4、不支持android.app.NativeActivity
  
    5、Notification在5.x以下不支持使用插件资源, 在5.x及以上仅支持在RemoteView中使用插件资源，且不支持miui8
  
    6、插件依赖另一个插件时，被插件依赖的插件暂不支持包含资源
    
    7、非独立插件暂不支持使用databinding，独立插件可使用
    
    8、可能不支持对插件或者宿主进行加壳加固处理，未尝试。
  
##其他
1. [原理简介](https://github.com/limpoxe/Android-Plugin-Framework/wiki/%E5%8E%9F%E7%90%86%E7%AE%80%E4%BB%8B)
2. [使用Public.xml的坑和填坑](https://github.com/limpoxe/Android-Plugin-Framework/wiki/%E4%BD%BF%E7%94%A8Public.xml%E7%9A%84%E5%9D%91%E5%92%8C%E5%A1%AB%E5%9D%91).
3. [更新记录](https://github.com/limpoxe/Android-Plugin-Framework/wiki/%E6%9B%B4%E6%96%B0%E8%AE%B0%E5%BD%95)

##联系作者：
  Q：15871365851，添加时请注明插件开发

  Q群：116993004、207397154(已满)，重要：添加前请务必仔细阅读此ReadMe！请务必仔细阅读Demo！
